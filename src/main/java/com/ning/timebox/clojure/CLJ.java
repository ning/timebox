package com.ning.timebox.clojure;

import com.ning.timebox.SandPredicate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@SandPredicate(ClojurePredicator.class)
public @interface CLJ
{
    String value();
}
