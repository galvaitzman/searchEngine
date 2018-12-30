package sample;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Dictionary {

    // calculate in the Main View Controller
    public Map<String,Integer> numberOfUniqueTermsInDoc;  //key = docName, value = number of unique terms in doc
    public Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc; // key = doc, value = number Of Appearances Of Most Common Term In Doc
    public Map <String,Integer> numberOfTotalTermsInDoc; // key = docName, value = total words in doc, not including stop words
    public Map <String,Double> weightOfDocNormalizedByLengthOfDoc  ; // key = docName, value = weight Of Doc Normalized By Length Of Doc (based on numberOfTotalTermsInDoc)
    public Map <String,Double> weightOfDocNormalizedByMostCommonWordInDoc; // key = docName, value = weight Of Doc Normalized By Most Common Word In Doc (based on numberOfAppearancesOfMostCommonTermInDoc)
    public Map <String,String> cityOfDoc; // key = docName, value = the city of the doc which appears in <F P=104> tag
    public Map <String, Set<String>> cityInDoc; //key = doc name, value =set of all the cities from the <F P=104> tag which appears in the doc's text
    public Map <String,Integer> numberOfLinesInDoc; // key = docName, value = number Of Lines In Doc

    // indexer
    public TreeMap<String,Integer> treeMapForfrequentOfTermInCorpus; // key = term, value = number of appearances of term in the corpus
    public TreeMap<String,Integer> treeMapForDocsPerTerm; // key = term, value = number of different docs which the term appears in
    public TreeMap <String,Integer> treeMapForLineNumberInPosting; // key = term, value = the line number of the term in the posting file
    public Map<String, Double> IDF_BM25_Map; // key = docName, value = the partial result of bm25 for each doc, Will be taken into account when calculating the final result which uses the frequency of term in doc
    public Map <String,String> entities; // key = docName, value = 5 or less entities of the doc and their score


    public Dictionary( ){

    }


}
