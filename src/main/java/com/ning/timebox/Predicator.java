package com.ning.timebox;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface Predicator
{
    Predicate<Object[]> buildMethodPredicate(Annotation a, Method m);
    Predicate<Object> buildArgumentPredicate(Annotation a, Method m, int argumentIndex);
}
