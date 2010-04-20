package com.ning.timebox;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GuardMethodGuardHouse implements GuardHouse
{
    public Predicate<Object[]> buildMethodPredicate(Annotation a, final Object target, Method m)
    {
        String method_name = ((GuardMethod) a).value();
        final Method guard_method;
        try {
            guard_method = target.getClass().getMethod(method_name, m.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("no method with correct signature matches " + method_name, e);
        }

        if (!(Boolean.class.equals(guard_method.getReturnType())
              || boolean.class.equals(guard_method.getReturnType())))
        {
            throw new IllegalStateException("guard method, " + method_name + " must return boolean");
        }
        return new Predicate<Object[]>()
        {

            public boolean test(Object[] arg)
            {
                try {
                    return (Boolean) guard_method.invoke(target, arg);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                catch (InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    public Predicate<Object> buildArgumentPredicate(Annotation a, final Object target, Method m, int argumentIndex)
    {
        String method_name = ((GuardMethod) a).value();
        final Method guard_method;
        try {
            guard_method = target.getClass().getMethod(method_name, m.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("no method with correct signature matches " + method_name, e);
        }

        if (!(Boolean.class.equals(guard_method.getReturnType())
              || boolean.class.equals(guard_method.getReturnType())))
        {
            throw new IllegalStateException("guard method, " + method_name + " must return boolean");
        }

        return new Predicate<Object>()
        {

            public boolean test(Object arg)
            {
                try {
                    return (Boolean) guard_method.invoke(target, arg);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                catch (InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
