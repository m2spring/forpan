package org.springdot.forpan.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Lazy initializer.
 * See <a href="https://4comprehension.com/leveraging-lambda-expressions-for-lazy-evaluation-in-java"/>Leveraging Lambda Expressions for Lazy Evaluation in Java</a>
 */
public class Lazy<T> implements Supplier<T>{
    private transient Supplier<T> supplier;
    private volatile T value;

    public Lazy(Supplier<T> supplier){
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public T get(){
        if (value == null){
            synchronized (this){
                if (value == null){
                    value = Objects.requireNonNull(supplier.get());
                    supplier = null;
                }
            }
        }
        return value;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier){
        return new Lazy<>(supplier);
    }
}
