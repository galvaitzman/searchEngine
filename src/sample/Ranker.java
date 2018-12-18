package sample;

import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {

    int counterOfTermsInQuery = 0 ;
    private String pathOfPostingAndDictionary;
    Map<String,double []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, String = num of appearences of term 1 from query, num of appearences of term 2 from query...
    Map<String,int []> numberOfLineOfTermInDoc = new TreeMap<>();// String=docName, String = num of line of term 1 from query in doc, num of line of term 2 from query in doc...
    int sizeOfIntegerArray=0;
    int currentIndexInIntegerArray=0;
    Set<String> queryAfterParsing;
    Indexer indexer;
    ReadFile readFile;
    Parse parse;
    //Map<String, Double> IDF_BM25_Map = new TreeMap<>();
    public Map <String,Integer> numberOfUniqueTermsInDoc;  // key = doc, value= מספר המילים הייחודיות במסמך
    public Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc; // key = doc, value = מספר ההופעות של המילה הכי נפוצה במסמך
    public Map <String,Integer> numberOfTotalTermsInDoc; // key = doc, value = אורך המסמך-כולל כפילויות, לא כולל מילות עצירה

    //step 1
    public Ranker(String pathOfPostingAndDictionary,
                  Map <String,Integer> numberOfUniqueTermsInDoc,
                  Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc,
                  Map <String,Integer> numberOfTotalTermsInDoc,
                  Indexer indexer,
                  ReadFile readFile,
                  Parse parse){
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        this.numberOfUniqueTermsInDoc = numberOfUniqueTermsInDoc;
        this.numberOfAppearancesOfMostCommonTermInDoc = numberOfAppearancesOfMostCommonTermInDoc;
        this.numberOfTotalTermsInDoc = numberOfTotalTermsInDoc;
        this.indexer = indexer;
        this.parse = parse;
        this.readFile = readFile;

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
            if (indexer.treeMapForLineNumberInPosting.get(s) == null){
                currentIndexInIntegerArray++;
                continue;
            }
            int lineInPosting = indexer.treeMapForLineNumberInPosting.get(s) + 1;
            try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/" + numOfPosting + ".txt"), StandardCharsets.UTF_8));
            String line="";
            int currentLineIndex = 0;
            //while (line != null){
            while (currentLineIndex < lineInPosting) {
                if (line.split("\\^")[0].equals("teresa")){
                    System.out.println(1);
                }
                if (currentLineIndex==5000){
                    System.out.println("asdsad");
                }
                if (currentLineIndex==10000){
                    System.out.println("asdsad");
                }
                if (currentLineIndex==15000){
                    System.out.println("asdsad");
                }
                if (currentLineIndex==20000){
                    System.out.println("asdsad");
                }
                line=br.readLine();
                currentLineIndex++;
            }
                System.out.println(4);
            //line = br.readLine();
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

    public List<String> rankEveryDocument () {
        Map <String,Double> rankingForDocs = new HashMap<>();
        for (Map.Entry<String, double[]> insideEntry : appearancesCountingOfTermsInDoc.entrySet()) {
            rankingForDocs.put(insideEntry.getKey(),getBM25ForDoc(insideEntry.getKey()));
        }
        List<Map.Entry<String, Double>> list = new ArrayList<>(rankingForDocs.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : list) {
            result.add(entry.getKey());
        }
        System.out.println(rankingForDocs.get("FBIS4-30404"));
        System.out.println(rankingForDocs.get("FBIS3-10014"));

        return null;
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
        double d_avdl = (double) numberOfTotalTermsInDoc.get(doc_name) / Main.avdl;
        double temp = k1 * ((1 - b) + b * d_avdl);

        int counter = 0;
        for (String s: queryAfterParsing) {
            // maybe to calculate idf and also tf- in the indexer -  maybe to change the calculate of idf
            //idf = Math.log(((double) ReadFile.numOfDocs + 1) / (treeMapForDocsPerTerm.get(entry.getValue())));
            double tf = ((double) appearancesCountingOfTermsInDoc.get(doc_name)[counter]); /// numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);
            double partA = ((double) ((k1 + 1) * tf)) / (temp + tf);
            //double partB = ((double) ((k2 + 1) * entry.getValue())) / (k2 + entry.getValue());
            if(indexer.IDF_BM25_Map.get(s)!= null) {
                rank_BM25P += (indexer.IDF_BM25_Map.get(s) * partA);
            }
            counter += 1;
        }
        return rank_BM25P;

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
