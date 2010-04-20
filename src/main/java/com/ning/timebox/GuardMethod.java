package com.ning.timebox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@GuardAnnotation(GuardMethodGuardHouse.class)
public @interface GuardMethod
{
    String value();
}
