package com.example.brylee.zunbe.aco;

import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by brylee on 5/1/17.
 */

public class AtomicDouble extends Number implements Comparable<AtomicDouble>  {
    private static final long serialVersionUID = 1L;
    private AtomicReference<Double> atomicReference;
    public AtomicDouble(Double d) {
        atomicReference = new AtomicReference<>(d);
    }

    @Override
    public int compareTo(@NonNull AtomicDouble o) {
        return Double.compare(this.doubleValue(), o.doubleValue());
    }

    public boolean compareAndSet(double newVal) {
        return atomicReference.compareAndSet(atomicReference.get(), newVal);
    }

    @Override
    public int intValue() {
        return atomicReference.get().intValue();
    }

    @Override
    public long longValue() {
        return atomicReference.get().longValue();
    }

    @Override
    public float floatValue() {
        return atomicReference.get().floatValue();
    }

    @Override
    public double doubleValue() {
        return atomicReference.get().doubleValue();
    }
}
