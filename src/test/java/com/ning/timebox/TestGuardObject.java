package com.ning.timebox;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestGuardObject extends TestCase
{
    public void testGuardMethodOnArgument() throws Exception
    {
        final AtomicReference<String> flag = new AtomicReference<String>();
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@GuardMethod("bestGuard") Dog dog)
            {
                flag.set("first");
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set("second");
            }


            public boolean bestGuard(Dog dog) {
                return dog.getName().equals("Bean");
            }

        });

        box.provide(new Dog("Bouncer", 9), 10);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals("second", flag.get());
    }

    public void testGuardMethodOnMethod() throws Exception
    {
        final AtomicReference<String> flag = new AtomicReference<String>();
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            @GuardMethod("bestGuard")
            public void best(Dog dog, Cat cat)
            {
                flag.set("first");
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set("second");
            }


            public boolean bestGuard(Dog dog, Cat cat) {
                return dog.getName().equals("Bean");
            }

        });

        box.provide(new Dog("Bouncer", 9), 10);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals("second", flag.get());
    }

}
