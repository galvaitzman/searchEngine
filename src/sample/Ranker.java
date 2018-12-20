package sample;

import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {

    int counterOfTermsInQuery = 0 ;
    private String pathOfPostingAndDictionary;
    Map<String,double []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, double[] = num of appearences of term 1 from query, num of appearences of term 2 from query...
    Map<String,int []> numberOfLineOfTermInDoc = new TreeMap<>();// String=docName, String = num of line of term 1 from query in doc, num of line of term 2 from query in doc...
    int sizeOfIntegerArray=0;
    int currentIndexInIntegerArray=0;
    Set<String> queryAfterParsing;
    Indexer indexer;
    ReadFile readFile;
    Parse parse;
    Dictionary dictionary;
    //Map<String, Double> IDF_BM25_Map = new TreeMap<>();
/*    public Map <String,Integer> numberOfUniqueTermsInDoc;  // key = doc, value= מספר המילים הייחודיות במסמך
    public Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc; // key = doc, value = מספר ההופעות של המילה הכי נפוצה במסמך
    public Map <String,Integer> numberOfTotalTermsInDoc; // key = doc, value = אורך המסמך-כולל כפילויות, לא כולל מילות עצירה
    public Map <String,Double> weightOfDocNormalizedByLengthOfDoc  ;
    public Map <String,Double> weightOfDocNormalizedByMostCommonWordInDoc;
    //step 1*/
  /*  public Ranker(String pathOfPostingAndDictionary,
                  Map <String,Integer> numberOfUniqueTermsInDoc,
                  Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc,
                  Map <String,Integer> numberOfTotalTermsInDoc,
                  Indexer indexer,
                  ReadFile readFile,
                  Parse parse,
                  Map<String,Double> weightOfDocNormalizedByLengthOfDoc,
                  Map<String,Double> weightOfDocNormalizedByMostCommonWordInDoc
                  ){*/

    public Ranker(String pathOfPostingAndDictionary, Dictionary dictionary) {
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        this.dictionary =dictionary;
      /*  this.numberOfUniqueTermsInDoc = numberOfUniqueTermsInDoc;
        this.numberOfAppearancesOfMostCommonTermInDoc = numberOfAppearancesOfMostCommonTermInDoc;
        this.numberOfTotalTermsInDoc = numberOfTotalTermsInDoc;
        this.indexer = indexer;
        this.parse = parse;
        this.readFile = readFile;
        this.weightOfDocNormalizedByLengthOfDoc = weightOfDocNormalizedByLengthOfDoc;
        this.weightOfDocNormalizedByMostCommonWordInDoc = weightOfDocNormalizedByMostCommonWordInDoc;
*/
    }

    //step 2
    public void addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(Set<String> queryAfterParsing) {
        this.queryAfterParsing = queryAfterParsing;
        counterOfTermsInQuery = 0;
        sizeOfIntegerArray = queryAfterParsing.size() * 2 + 1;
        currentIndexInIntegerArray = 0;
        for (String s : queryAfterParsing) {
            int startingChar = s.charAt(0);
            int numOfPosting = 0;
            if (startingChar >= 65 && startingChar <= 90) numOfPosting = startingChar - 54;
            else if (startingChar >= 97 && startingChar <= 122) numOfPosting = startingChar - 86;
            else if (startingChar >= 48 && startingChar <= 57) numOfPosting = startingChar - 47;
            if (dictionary.treeMapForLineNumberInPosting.get(s) == null){
                currentIndexInIntegerArray++;
                continue;
            }
            int lineInPosting = dictionary.treeMapForLineNumberInPosting.get(s) + 1;
            try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/" + numOfPosting + ".txt"), StandardCharsets.UTF_8));
            String line="";
            int currentLineIndex = 0;
            //while (line != null){
            while (currentLineIndex < lineInPosting) {
                br.readLine();
                currentLineIndex++;
            }
            line = br.readLine();
            while (!line.split("\\^")[0].equals(s)){
                line = br.readLine();
            }
            String[] docs = line.split("~");
            addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(docs);}
            catch (IOException e){
                System.out.println("goni");
                e.printStackTrace();
            }
        }

    }

    //step 3
    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(String [] docs){
        String [] docInfo;
        docInfo = new String[]{docs[0].split("\\^")[1],docs[0].split("\\^")[2],docs[0].split("\\^")[3]};
        addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo);
        for (int i=1; i<docs.length; i++){
            docInfo = docs[i].split("\\^");
            addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo);
        }
        currentIndexInIntegerArray++;
    }

    //step 4
    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(String [] docInfo) {
        if (appearancesCountingOfTermsInDoc.get(docInfo[0])== null){
            appearancesCountingOfTermsInDoc.put(docInfo[0], new double[sizeOfIntegerArray]);
            numberOfLineOfTermInDoc.put(docInfo[0], new int[sizeOfIntegerArray]);
        }
        appearancesCountingOfTermsInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[1]);
        numberOfLineOfTermInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[2]);//
    }

    public List<String> rankEveryDocument (Map<String,Integer>cities) {

        Map <String,Double> rankingForDocs = new HashMap<>();
        for (Map.Entry<String, double[]> insideEntry : appearancesCountingOfTermsInDoc.entrySet()) {
            if (cities.size() > 0) {
                if (cities.get(dictionary.cityOfDoc.get(insideEntry.getKey())) != null){
                    rankingForDocs.put(insideEntry.getKey(), getBM25ForDoc(insideEntry.getKey()) * 0.8 + getCosSim(insideEntry.getKey()) * 0.2);
                }
            }
            else{
                rankingForDocs.put(insideEntry.getKey(), getBM25ForDoc(insideEntry.getKey()) * 0.8 + getCosSim(insideEntry.getKey()) * 0.2);

            }
        }
        List<Map.Entry<String, Double>> list = new ArrayList<>(rankingForDocs.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> result = new ArrayList<>();
        int counter = 0;
        for (Map.Entry<String, Double> entry : list) {
            if (list.size() - counter <= 100) {
                result.add(entry.getKey());
                System.out.println(entry.getKey());
            }
            counter++;
        }



        return null;
    }


    /**
     * normalize by most common word in doc
     * @param doc_name
     * @return
     */
    public double getCosSim(String doc_name)
    {
        int counter = 0;
        double up = 0;
        for (String s: queryAfterParsing) {
            up +=  ( appearancesCountingOfTermsInDoc.get(doc_name)[counter]) / dictionary.numberOfAppearancesOfMostCommonTermInDoc.get(s);
        }
        double down = dictionary.weightOfDocNormalizedByMostCommonWordInDoc.get(doc_name) * Math.sqrt(queryAfterParsing.size());

        return up / down;
    }



    public double getBM25ForDoc(String doc_name) {
        // c(w,d) - Map<String,int []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, String = num of appearences of term 1 from query, num of appearences of term 2 from query...
        //M-  ReadFile.numOfDocs;
        //|d| and avdl  public Map <String,Integer> numberOfTotalTermsInDoc; // key = doc, value = אורך המסמך-כולל כפילויות, לא כולל מילות עצירה
        // df(2)- מספר מסמכים שונים שמופיע בטרם - TreeMap<String,Integer> treeMapForDocsPerTerm; // key = term, value = מספר המסמכים השונים בהם מופיע הביטוי

        double rank_BM25P = 0;
        //  double idf;
        double b = 0.75;
        double k1 = 1.2;
        double k2 = 1;
        double d_avdl = (double) dictionary.numberOfTotalTermsInDoc.get(doc_name) / Main.avdl;
        double temp = k1 * ((1 - b) + b * d_avdl);

        int counter = 0;
        for (String s : queryAfterParsing) {
            double tf = ((double) appearancesCountingOfTermsInDoc.get(doc_name)[counter]); /// numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);
            double partA = ((double) ((k1 + 1) * tf)) / (temp + tf);
            //double partB = ((double) ((k2 + 1) * entry.getValue())) / (k2 + entry.getValue());
            if (dictionary.IDF_BM25_Map.get(s) != null)
                rank_BM25P += (dictionary.IDF_BM25_Map.get(s) * partA);
            counter += 1;
        }
        return rank_BM25P;

            // maybe to calculate idf and also tf- in the indexer -  maybe to change the calculate of idf
            //idf = Math.log(((double) ReadFile.numOfDocs + 1) / (treeMapForDocsPerTerm.get(entry.getValue())));
    }

        /**
         * check if this is the values that we need to calculate
         */
        /*
        double k = 2;
        double b = 0.75;
        double wight = 1;

        for (Map.Entry<String, double[]> entry : appearancesCountingOfTermsInDoc.entrySet()) {
            String doc = entry.getKey();
            double rank = 0;
            for (int i = 0; i < queryAfterParsing.size() - 1; i = i + 1) {
                double logCal = Math.log(((double) ReadFile.numOfDocs + 1) / (treeMapForDocsPerTerm.get(queryAfterParsing.get(0))));
                double up = (entry.getValue())[i] * (k + 1) * logCal;
                double down = (k * (1 - b + b * ((double) numberOfTotalTermsInDoc.get(doc) / avdl)));

                // get less wight to semantic words - 0.5
                if(i >= queryAfterParsing.size()/2 ) wight = 0.5;
                rank += wight * (up / down);
                wight = 1;
            }
            // the last value is what i go to update - will be the final rank to the doc
            entry.getValue()[entry.getValue().length - 1] = rank;
        }
        */

}
