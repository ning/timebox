package com.ning.timebox.ruby;

import com.ning.timebox.GuardAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@GuardAnnotation(RubyPredicator.class)
public @interface Rb
{
    String value();
}
