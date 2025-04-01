package org.springdot.forpan.util;

import java.util.Random;
import java.util.stream.Stream;

public class RandomWordGenerator{

    private static final String VOWELS = "aeiou";
    private static final String LAST_CONSONANTS = "bcdfghjklmnprstvwxyz";

    private final static Lazy<String[]> SYLLABLES = Lazy.of(() ->
        Stream.of(
                "b","c","ch","d","f","g","h","j","k","l","m","n",
                "p","ph","qu","r","s","t","v","w","x","y","z"
            )
            .flatMap(letter -> VOWELS.chars().mapToObj(vowel -> letter+(char)vowel))
            .filter(syllable -> !"quu".equals(syllable))
            .toArray(String[]::new)
    );

    private final Random RND = new Random();

    public String generate(){
        StringBuilder sb = new StringBuilder();
        sb.append(rndChar(VOWELS));

        String[] syllables = SYLLABLES.get();
        for (int i=0, n=2+RND.nextInt(3); i<n; i++){
            sb.append(syllables[RND.nextInt(syllables.length)]);
        }

        sb.append(rndChar(LAST_CONSONANTS));
        return sb.toString();
    }

    private String rndChar(String alphabet){
        return RND.nextBoolean()? "" : String.valueOf(alphabet.charAt(RND.nextInt(alphabet.length())));
    }
}
