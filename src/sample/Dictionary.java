package sample;

import java.util.Map;
import java.util.TreeMap;

public class Dictionary {

    // calculate in the Main View Controller
    public Map<String,Integer> numberOfUniqueTermsInDoc;  // key = doc, value= מספר המילים הייחודיות במסמך
    public Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc; // key = doc, value = מספר ההופעות של המילה הכי נפוצה במסמך
    public Map <String,Integer> numberOfTotalTermsInDoc; // key = doc, value = אורך המסמך-כולל כפילויות, לא כולל מילות עצירה
    public Map <String,Double> weightOfDocNormalizedByLengthOfDoc  ;
    public Map <String,Double> weightOfDocNormalizedByMostCommonWordInDoc;

    // indexer
    public TreeMap<String,Integer> treeMapForfrequentOfTermInCorpus;
    public TreeMap<String,Integer> treeMapForDocsPerTerm;
    public TreeMap <String,Integer> treeMapForLineNumberInPosting;
    public Map<String, Double> IDF_BM25_Map;


    public Dictionary( ){

    }


}
