package com.ning.timebox.ruby;

import com.ning.timebox.Guard;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Guard(RubyPredicator.class)
public @interface Rb
{
    String value();
}
