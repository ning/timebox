package com.ning.timebox.clojure;

import com.ning.timebox.Guard;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Guard(ClojurePredicator.class)
public @interface CLJ
{
    String value();
}
