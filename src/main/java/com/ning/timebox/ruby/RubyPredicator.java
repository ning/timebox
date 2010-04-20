package com.ning.timebox.ruby;

import org.jruby.RubyInstanceConfig;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import com.ning.timebox.Predicate;
import com.ning.timebox.GuardHouse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class RubyPredicator implements GuardHouse
{
    public Predicate<Object[]> buildMethodPredicate(Annotation a, Object target, Method m)
    {
        Rb rb = (Rb) a;
        String block = rb.value().trim();
        if (! (block.startsWith("{") && block.endsWith("}")) ) {
            block = "{" + block + "}";
        }

        final ScriptingContainer container = new ScriptingContainer(LocalContextScope.THREADSAFE);
        container.getProvider().getRubyInstanceConfig().setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        JavaEmbedUtils.EvalUnit unit = container.parse( "lambda " + block);

        final IRubyObject ro =  unit.run();

        return new Predicate<Object[]>() {

            public boolean test(Object[] arg)
            {
                return (Boolean) container.callMethod(ro, "call", arg);
            }
        };
    }

    public Predicate<Object> buildArgumentPredicate(Annotation a, Object target, Method m, int argumentIndex)
    {
        Rb rb = (Rb) a;
        String block = rb.value().trim();
        if (! (block.startsWith("{") && block.endsWith("}")) ) {
            block = "{" + block + "}";
        }

        final ScriptingContainer container = new ScriptingContainer(LocalContextScope.THREADSAFE);
        container.getProvider().getRubyInstanceConfig().setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        JavaEmbedUtils.EvalUnit unit = container.parse( "lambda " + block);

        final IRubyObject ro =  unit.run();

        return new Predicate<Object>() {

            public boolean test(Object arg)
            {
                return (Boolean) container.callMethod(ro, "call", arg);
            }
        };
    }
}
