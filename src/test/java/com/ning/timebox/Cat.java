package com.ning.timebox;

public class Cat
{
    private final int livesRemaining;

    public Cat() {
        this(9);
    }

    public Cat(int livesRemaining) {
        this.livesRemaining = livesRemaining;
    }

    public int getLivesRemaining()
    {
        return livesRemaining;
    }
}
