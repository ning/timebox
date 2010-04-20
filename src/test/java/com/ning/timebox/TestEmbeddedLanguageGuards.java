package com.ning.timebox;

import com.ning.timebox.clojure.CLJ;
import com.ning.timebox.ruby.Rb;
import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEmbeddedLanguageGuards extends TestCase
{
    public void testClojureParamAnnotation() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@CLJ("#(> (.getAge %) 12)") Dog dog)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer", 9), 10);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testClojureParamAnnotationOkay() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@CLJ("#(> (.getAge %) 6)") Dog dog)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer", 9), 10);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }


    public void testClojureMethodAnnotation() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            @CLJ("#(> (.getAge %1) (.getLivesRemaining %2))")
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("SamThePuppy", 1));
        box.provide(new Cat(4));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }


    public void testRubyMethodPredicateWhichFails() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            @Rb("{ |dog, cat| dog.age > cat.livesRemaining }")
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("SamThePuppy", 1));
        box.provide(new Cat(4));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testRubyMethodPredicateWhichPasses() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            @Rb("|dog, cat| dog.age < cat.livesRemaining")
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("SamThePuppy", 1));
        box.provide(new Cat(4));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testRubyParamPredicateWhichPasses() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Rb("|d| d.age == 1") Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("SamThePuppy", 1));
        box.provide(new Cat(4));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testRubyAndClojureBaby() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            @Rb("{ |dog, cat| dog.age == cat.livesRemaining }")
            @CLJ("#(= (.getAge %1) (.getLivesRemaining %2))")
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("SamThePuppy", 1));
        box.provide(new Cat(1));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testRubyAndClojureBabymethodParamsRubyOneShouldFail() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Rb("|d| d.age == 8") @CLJ("#(= (.getName %1) \"Bouncer\")") Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer", 7));
        box.provide(new Cat(1));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testRubyAndClojureBabymethodParamsClojureOneShouldFail() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Rb("|d| d.age == 7") @CLJ("#(= (.getName %1) \"Bean\")") Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer", 7));
        box.provide(new Cat(1));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testRubyAndClojureBabymethodParamsBothShouldPass() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Rb("|d| d.age == 7") @CLJ("#(= (.getName %1) \"Bouncer\")") Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog, Cat cat)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer", 7));
        box.provide(new Cat(1));

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }
}
