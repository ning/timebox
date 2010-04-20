package com.ning.timebox;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SandPredicate
{
    Class<? extends Predicator> value();
}
