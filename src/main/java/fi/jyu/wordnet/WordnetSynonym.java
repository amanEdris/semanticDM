/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.jyu.wordnet;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import java.io.File;
import java.util.ArrayList;

public class WordnetSynonym {


    public static ArrayList<String> findSynonyms(String wordForm) throws Exception {
        File f = new File("src/dict");
        System.setProperty("wordnet.database.dir", f.toString());
        ArrayList<String> al = new ArrayList<>();

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wordForm);

        if (synsets.length > 0) {

            for (int i = 0; i < synsets.length; i++) {
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    if (!al.contains(wordForms[j])) {
                        al.add(wordForms[j]);
                    }
                }
            }
        } else {
            throw new Exception("no synonym found for word '"+wordForm+"' please check domain ontology");
        }

        return al;
    }
}