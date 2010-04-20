package com.ning.timebox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GuardAnnotation
{
    Class<? extends GuardHouse> value();
}
