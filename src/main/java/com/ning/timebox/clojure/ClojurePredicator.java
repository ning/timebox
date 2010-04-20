package com.ning.timebox.clojure;

import clojure.lang.ArraySeq;
import clojure.lang.IFn;
import com.ning.timebox.GuardHouse;
import com.ning.timebox.Predicate;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static clojure.lang.Compiler.load;

public class ClojurePredicator implements GuardHouse
{

    private static final IFn FALSY_APPLYSY;

    static {
        try {
            FALSY_APPLYSY = (IFn) load(new StringReader("(fn [f a] (true? (apply f a)))"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Predicate<Object[]> buildMethodPredicate(Annotation a, Method method)
    {

        final IFn fn;
        final String clj = method.getAnnotation(CLJ.class).value();
        try {
            fn = (IFn) load(new StringReader(clj));
        }
        catch (Exception e) {
            throw new UnsupportedOperationException("Not Yet Implemented!");
        }
        return new Predicate<Object[]>()
        {
            public boolean test(Object[] arg)
            {
                try {
                    return (Boolean) FALSY_APPLYSY.invoke(fn, ArraySeq.create(arg));
                }
                catch (Exception e) {
                    throw new UnsupportedOperationException(e);
                }
            }
        };
    }

    private static final IFn FALSY;

    static {
        try {
            FALSY = (IFn) load(new StringReader("(fn [t x] (true? (t x)))"));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Predicate<Object> buildArgumentPredicate(Annotation annotation, Method m, int argumentIndex)
    {
        try {
            final IFn fn = (IFn) load(new StringReader(((CLJ) annotation).value()));
            return new Predicate<Object>()
            {
                public boolean test(Object arg)
                {
                    try {
                        return (Boolean) FALSY.invoke(fn, arg);
                    }
                    catch (Exception e) {
                        throw new UnsupportedOperationException("Not Yet Implemented!");
                    }
                }
            };
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
