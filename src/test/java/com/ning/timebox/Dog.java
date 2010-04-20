package com.ning.timebox;

public class Dog
{
    private final String name;
    private final int age;

    public Dog()
    {
        this("Happy");
    }

    public Dog(String name)
    {
        this(name, 3);
    }

    public Dog(String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public String getName()
    {
        return name;
    }

    public int getAge()
    {
        return this.age;
    }
}
