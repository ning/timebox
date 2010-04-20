package com.ning.timebox;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class TimeBox
{
    private final SortedMap<Integer, Handler> handlers = new TreeMap<Integer, Handler>(new Comparator<Integer>()
    {
        public int compare(Integer first, Integer second)
        {
            return first.compareTo(second) * -1;
        }
    });

    private final Semaphore flag = new Semaphore(0);
    private final int highestPriority;
    private final Factory factory;

    public TimeBox(Factory factory, Object handler)
    {
        this.factory = factory;
        int hp = Integer.MIN_VALUE;
        final Method[] methods = handler.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(Priority.class)) {
                int priority = method.getAnnotation(Priority.class).value();
                if (priority > hp) {
                    hp = priority;
                }
                Handler h = new Handler(handler, method);
                if (handlers.containsKey(priority)) {
                    throw new IllegalArgumentException(format("multiple reactor methods have priority %d", priority));
                }
                handlers.put(priority, h);
            }
        }
        highestPriority = hp;
    }

    public TimeBox(Object handler)
    {
        this(new DefaultFactory(), handler);
    }

    public synchronized void provide(Object value)
    {
        provide(value, 0);
    }

    public void provide(Object value, int authority)
    {
        assert authority > Long.MIN_VALUE;

        final Class type = value.getClass();
        for (Map.Entry<Integer, Handler> entry : handlers.entrySet()) {
            entry.getValue().provide(type, value, authority);
            if (entry.getKey() == highestPriority && entry.getValue().isSatisfied()) {
                flag.release();
                return; // we satisfied highest priority, short circuit
            }
        }
    }

    public boolean react(long number, TimeUnit unit) throws InterruptedException, InvocationTargetException, IllegalAccessException
    {
        if (flag.tryAcquire(number, unit)) {
            // flag will only be avail *if* highest priority handler is triggered
            for (Handler handler : handlers.values()) {
                if (handler.isSatisfied()) {
                    handler.handle();
                    return true; // satisfied highest priority so short circuit and return
                }
            }
        }

        for (Handler handler : handlers.values()) {
            if (handler.isSatisfied()) {
                handler.handle();
                return true;
            }
        }

        return false;
    }

    private class Handler
    {
        private final Object[] values;
        private final long[] authorities;
        private final Object target;
        private final Method method;
        private final Class[] types;
        private final List<Predicate<Object[]>> methodTests = new ArrayList<Predicate<Object[]>>();
        private final List<Collection<Predicate>> parameterTests;

        public Handler(Object target, Method method)
        {
            this.types = method.getParameterTypes();
            this.target = target;
            this.method = method;
            this.values = new Object[types.length];
            this.authorities = new long[types.length];
            this.parameterTests = new ArrayList<Collection<Predicate>>(method.getParameterTypes().length);


            for (Annotation annotation : method.getAnnotations()) {
                for (Class<?> iface : annotation.getClass().getInterfaces()) {
                    if (iface.isAnnotationPresent(Guard.class)) {
                        Guard sp = iface.getAnnotation(Guard.class);
                        final GuardHouse p;
                        try {
                            p = factory.instantiate(sp.value());
                        }
                        catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                        methodTests.add(p.buildMethodPredicate(annotation, method));
                    }
                }
            }

            for (int i = 0; i < method.getParameterTypes().length; i++) {
                parameterTests.add(new ArrayList<Predicate>());
            }

            // now prefill authorities to required authority - 1,
            // so that when needed authoity comes in, it is at higher
            Annotation[][] param_annos = method.getParameterAnnotations();
            for (int i = 0; i < param_annos.length; i++) {
                authorities[i] = Long.MIN_VALUE;
                for (Annotation annotation : param_annos[i]) {
                    if (annotation instanceof Authority) {
                        authorities[i] = ((Authority) annotation).value();
                    }


                    for (Class<?> iface : annotation.getClass().getInterfaces()) {
                        if (iface.isAnnotationPresent(Guard.class)) {
                            Guard sp = iface.getAnnotation(Guard.class);
                            final GuardHouse p;
                            try {
                                p = factory.instantiate(sp.value());
                            }
                            catch (Exception e) {
                                throw new IllegalStateException(e);
                            }
                            parameterTests.get(i).add(p.buildArgumentPredicate(annotation, method, i));
                        }
                    }
                }
            }
        }

        public void provide(Class type, Object value, long authority)
        {
            for (int i = 0; i < types.length; i++) {
                if (types[i].isAssignableFrom(type)
                    && authorities[i] <= authority
                    && testParameterPredicates(value, parameterTests.get(i))) {
                    values[i] = value;
                    authorities[i] = authority;
                    return;
                }
            }
        }

        private boolean testParameterPredicates(Object value, Collection<Predicate> predicates)
        {
            for (Predicate predicate : predicates) {
                if (!predicate.test(value)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isSatisfied()
        {
            for (Object value : values) {
                if (value == null) {
                    return false;
                }
            }
            for (Predicate<Object[]> test : methodTests) {
                if (!test.test(values)) {
                    return false;
                }
            }
            return true;
        }

        public void handle() throws InvocationTargetException, IllegalAccessException
        {
            method.invoke(target, values);
        }
    }
}
