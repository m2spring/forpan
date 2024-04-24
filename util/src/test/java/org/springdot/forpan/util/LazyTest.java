package org.springdot.forpan.util;

import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class LazyTest{
    private static final String VALUE = "foodeebar";

    private static int callCounter = 0;

    @Test
    public void testLazyInit() throws Exception{
        final Lazy<String> lazy = Lazy.of(() -> expensiveStringMaker());

        final int nofThreads = 1000;
        ExecutorService executors = Executors.newFixedThreadPool(nofThreads);

        Random rnd = new Random();

        List<Future<String>> futures = IntStream.range(0,2*nofThreads)
            .mapToObj(i -> executors.submit(() -> {
                Thread.sleep(rnd.nextInt(20));
                return lazy.get();
            }))
            .collect(Collectors.toList());

        for (Future<String> future : futures){
            assertEquals(VALUE,future.get());
        }

        assertEquals(1,callCounter);
    }

    private static String expensiveStringMaker(){
        System.out.println("call #"+(++callCounter));
        try{
            Thread.sleep(100);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return VALUE;
    }
}
