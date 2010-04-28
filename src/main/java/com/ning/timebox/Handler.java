package com.ning.timebox;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Handler
{
    private final Object[] values;
    private final long[] authorities;
    private final Object target;
    private final Method method;
    private final Class[] types;
    private final List<Predicate<Object[]>> methodTests = new ArrayList<Predicate<Object[]>>();
    private final List<Collection<Predicate>> parameterTests;

    // null means do not gather
    private final Class[] gatheredTypes;

    public Handler(Factory factory, Object target, Method method)
    {
        this.types = method.getParameterTypes();
        this.target = target;
        this.method = method;
        this.values = new Object[types.length];
        this.authorities = new long[types.length];
        this.gatheredTypes = new Class[types.length];
        this.parameterTests = new ArrayList<Collection<Predicate>>(method.getParameterTypes().length);

        for (Annotation annotation : method.getAnnotations()) {
            for (Class<?> iface : annotation.getClass().getInterfaces()) {
                if (iface.isAnnotationPresent(GuardAnnotation.class)) {
                    GuardAnnotation sp = iface.getAnnotation(GuardAnnotation.class);
                    final GuardHouse p;
                    try {
                        p = factory.instantiate(sp.value());
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    methodTests.add(p.buildMethodPredicate(annotation, target, method));
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
            // loop through each parameter
            authorities[i] = Long.MIN_VALUE;

            GatherData gd = isGather(param_annos[i], i);
            if (gd.isGather) {
                this.values[gd.gatherIndex] = new ArrayList();
                this.gatheredTypes[i] = gd.gatherType;
            }

            for (Annotation annotation : param_annos[i]) {

                if (annotation instanceof Authority) {
                    authorities[i] = ((Authority) annotation).value();
                }

                for (Class<?> iface : annotation.getClass().getInterfaces()) {
                    if (iface.isAnnotationPresent(GuardAnnotation.class)) {
                        GuardAnnotation sp = iface.getAnnotation(GuardAnnotation.class);
                        final GuardHouse p;
                        try {
                            p = factory.instantiate(sp.value());
                        }
                        catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                        final Predicate<Object> pred;
                        if (gd.isGather) {
                            pred = p.buildGatherPredicate(annotation, target, method, gd.gatherType, i);
                        }
                        else {
                            pred = p.buildArgumentPredicate(annotation, target, method, i);
                        }
                        parameterTests.get(i).add(pred);
                    }
                }
            }
        }
    }


    private class GatherData
    {
        boolean isGather = false;
        Class gatherType;
        int gatherIndex;
    }

    private GatherData isGather(Annotation[] annos, int parameterIndex)
    {
        GatherData gd = new GatherData();
        gd.gatherIndex = parameterIndex;
        for (Annotation annotation : annos) {
            if (annotation instanceof Gather) {
                Class param_type = method.getParameterTypes()[parameterIndex];
                if (!Collection.class.isAssignableFrom(param_type)) {
                    throw new IllegalArgumentException("Can only @Gather against Collection");
                }

                // I hate always having to do this
                ParameterizedType gen_type = (ParameterizedType) method.getGenericParameterTypes()[parameterIndex];
                gd.gatherType = (Class) gen_type.getActualTypeArguments()[0];
                gd.isGather = true;
                break;
            }
        }
        return gd;
    }

    public void provide(Class type, Object value, long authority)
    {
        for (int i = 0; i < types.length; i++) {
            if (types[i].isAssignableFrom(type)
                && authorities[i] <= authority
                && testParameterPredicates(value, parameterTests.get(i)))
            {
                values[i] = value;
                authorities[i] = authority;
                return;
            }

            if (gatheredTypes[i] != null
                && gatheredTypes[i].isAssignableFrom(type)
                && testParameterPredicates(value, parameterTests.get(i)))
            {
                ((Collection) values[i]).add(value);
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
