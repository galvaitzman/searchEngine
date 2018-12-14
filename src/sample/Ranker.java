package sample;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {

    private String pathOfPostingAndDictionary;
    Map<String,int []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, String = num of appearences of term 1 from query, num of appearences of term 2 from query...
    Map<String,int []> numberOfLineOfTermInDoc = new TreeMap<>();
    int sizeOfIntegerArray=0;
    int currentIndexInIntegerArray=0;
    Set<String> queryAfterParsing;
    public TreeMap<String,Integer> treeMapForLineNumberInPosting; // key = term, value = מספר שורה שבה הביטוי מופיע בקובץ פוסטינג
    public TreeMap<String,Integer> treeMapForDocsPerTerm; // key = term, value = מספר המסמכים השונים בהם מופיע הביטוי
    public TreeMap<String,Integer> treeMapForFrequentOfTermInCorpus; // key = term, value= מספר ההופעות הכללי של הביטוי בקורפוס
    public Map <String,Integer> numberOfUniqueTermsInDoc;  // key = doc, value= מספר המילים הייחודיות במסמך
    public Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc; // key = doc, value = מספר ההופעות של המילה הכי נפוצה במסמך
    public Map <String,Integer> numberOfTotalTermsInDoc; // key = doc, value = אורך המסמך-כולל כפילויות, לא כולל מילות עצירה

    //step 1
    public Ranker(String pathOfPostingAndDictionary, TreeMap <String,Integer> treeMapForLineNumberInPosting,
                  TreeMap <String,Integer> treeMapForDocsPerTerm, TreeMap <String,Integer> treeMapForFrequentOfTermInCorpus,
                  Map <String,Integer> numberOfUniqueTermsInDoc, Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc,
                  Map <String,Integer> numberOfTotalTermsInDoc){
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        this.treeMapForDocsPerTerm = treeMapForDocsPerTerm;
        this.treeMapForFrequentOfTermInCorpus = treeMapForFrequentOfTermInCorpus;
        this.treeMapForLineNumberInPosting = treeMapForLineNumberInPosting;
        this.numberOfUniqueTermsInDoc = numberOfUniqueTermsInDoc;
        this.numberOfAppearancesOfMostCommonTermInDoc = numberOfAppearancesOfMostCommonTermInDoc;
        this.numberOfTotalTermsInDoc = numberOfTotalTermsInDoc;

    }

    //step 2
    private void addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(Set<String> queryAfterParsing , boolean isOriginalQuery){
        if (isOriginalQuery) this.queryAfterParsing = queryAfterParsing;
        sizeOfIntegerArray = queryAfterParsing.size() * 2 + 1;
        sizeOfIntegerArray = 0;
        Thread currentThread = null;
        for (String s:queryAfterParsing){
            int startingChar = s.charAt(0);
            int numOfPosting=0;
            if (startingChar>=65 && startingChar<=90) numOfPosting = startingChar - 54;
            else if (startingChar>=97 && startingChar<=122) numOfPosting = startingChar - 86;
            else if (startingChar>=48 && startingChar<=57) numOfPosting = startingChar - 47;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/" + numOfPosting + ".txt"), StandardCharsets.UTF_8));
                String line = br.readLine();
                String []docs = line.split("~");
                currentThread = new Thread(()-> {addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(docs);});
                currentThread.start();
                line = br.readLine();
            }
            catch (IOException e){e.printStackTrace();}
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
            appearancesCountingOfTermsInDoc.put(docInfo[0], new int[sizeOfIntegerArray]);
            numberOfLineOfTermInDoc.put(docInfo[0], new int[sizeOfIntegerArray]);
        }
        appearancesCountingOfTermsInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[1]);
        numberOfLineOfTermInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[2]);
    }

    //step 5
    public void addTermsFromTheSameSemanticField(){

    }


}
