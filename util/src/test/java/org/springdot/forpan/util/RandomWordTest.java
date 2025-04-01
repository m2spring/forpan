package org.springdot.forpan.util;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomWordTest{

    @Test
    public void testRandomWord() throws Exception{
        TestUtil.showMethod();

        final int NOF_ROWS = 20;
        final int NOF_COLS = 5;

        RandomWordGenerator rwgen = new RandomWordGenerator();
        List<String> words = IntStream.range(0,NOF_ROWS*NOF_COLS)
            .mapToObj(i -> rwgen.generate())
            .sorted()
            .collect(Collectors.toUnmodifiableList());

        String[][] table = new String[NOF_ROWS][NOF_COLS];
        int[] maxWidth = new int[NOF_COLS];
        for (int i=0, n=NOF_ROWS*NOF_COLS; i<n; i++){
            int col = i / NOF_ROWS;
            String word = words.get(i);
            table[i % NOF_ROWS][col] = word;
            if (word.length() > maxWidth[col]) maxWidth[col] = word.length();
        }

        for (int row=0; row<NOF_ROWS; row++){
            StringBuilder sb = new StringBuilder();
            for (int col=0; col<NOF_COLS-1; col++){
                sb.append(String.format("%-"+maxWidth[col]+"s ",table[row][col]));
            }
            sb.append(table[row][NOF_COLS-1]);
            System.out.println(sb.toString());
        }
    }
}
