package fi.jyu.wordnet;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordnetSimilarity {
    
    private static ILexicalDatabase db = new NictWordNet();
    private static Lin lin = new Lin(db);


    public static double getSimilarity(String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = lin.calcRelatednessOfWords(word1, word2);
        return s;
    }
    
    public static Map<String, Double> getSimilarList(String term, List<String> b) {
        Map<String, Double> mapSimilarityTable = new HashMap();
        
        for (int i = 0; i < b.size(); ++i) {
            term = term.replaceAll("^\\s+|\\s+$|[-]+", "").replaceAll("[^\\p{Alpha}]", "");
            String tempTerm = b.get(i).replaceAll("^\\s+|\\s+$", "").replaceAll("[^\\p{Alpha}]", "");
            double similarityValue = getSimilarity(term, tempTerm);
            mapSimilarityTable.put(tempTerm, similarityValue);            
        }
        return mapSimilarityTable;
    }
    
    public static String getBestMatch(Map<String, Double> mapSimilarityTable) {
        double maxSimilarityValue = Collections.max(mapSimilarityTable.values());
        String mapKey = new String();
        for (Map.Entry<String, Double> entry : mapSimilarityTable.entrySet()) {
            mapKey = entry.getKey();
            Double mapValue = entry.getValue();
            if (mapValue.equals(maxSimilarityValue)) {
                break;
            }
        }
        return mapKey;
    }

}