/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.utils;

import java.beans.Introspector;
import java.util.Random;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author edris
 */
public class TextUtil {
    
    public static String formatName(String h) {
        //remove inital space, lowercase letters, remove hypens
        h = WordUtils.capitalizeFully(h.trim().toLowerCase().replaceAll("[-]+", " "));
        //inital letter to lowercase
        h = Introspector.decapitalize(h);
        return h.replaceAll("[^A-Za-z]", "").toLowerCase();//remove all spaces
    }
    
     public static String formatOntologyName(String h) {
        //remove inital space, lowercase letters, remove hypens
        h = WordUtils.capitalizeFully(h.trim().toLowerCase().replaceAll("[-]+", " "));
        //inital letter to lowercase
        h = Introspector.decapitalize(h);
        return h.replaceAll("[^A-Za-z]", "");
     }

    public static char getRandomCharacter() {
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        return alphabet[random(alphabet.length)];

    }

    private static int random(int length) {
        return new Random().nextInt(length);
    }
}
