package com.ning.timebox;

import com.ning.timebox.clojure.CLJ;
import com.ning.timebox.ruby.Rb;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TestGather extends TestCase
{
    public void testFoo() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Gather Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bean"));
                box.provide(new Dog("Bouncer"));
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testBar() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void collectPuppies(@Gather @Rb("|d| d.age < 2") Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }

            public Boolean isBouncer(Dog dog)
            {
                return "Bouncer".equals(dog.getName());
            }

        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bean", 14));
                box.provide(new Dog("Mac", 1));
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testGuardWithRuby() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void collectPuppies(@Gather @Rb("|d| d.age < 2") Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bean", 14));
                box.provide(new Dog("Mac", 1));
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testGuardWithClojure() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void collectPuppies(@Gather @CLJ("#(< (.getAge %) 2)") Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bean", 14));
                box.provide(new Dog("Mac", 1));
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testGuardWithGuardMethod() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void collectPuppies(@Gather @GuardMethod("isPuppy") Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }

            public Boolean isPuppy(Dog dog)
            {
                return dog.getAge() < 2;
            }

        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bean", 14));
                box.provide(new Dog("Mac", 1));
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }
}
