package org.springdot.forpan.util;

import org.junit.Test;

import java.util.stream.IntStream;

public class RandomWordTest{

    @Test
    public void testRandomWord() throws Exception{
        TestUtil.showMethod();

        RandomWordGenerator rwgen = new RandomWordGenerator();
        IntStream.range(0,50)
            .mapToObj(i -> rwgen.generate())
            .sorted()
            .forEach(w -> System.out.println(w));
    }
}
