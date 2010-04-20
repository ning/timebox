package com.ning.timebox.ruby;

import com.ning.timebox.SandPredicate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@SandPredicate(RubyPredicator.class)
public @interface Rb
{
    String value();
}
