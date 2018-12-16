package sample;

import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {

    int counterOfTermsInQuery = 0 ;
    private String pathOfPostingAndDictionary;
    Map<String,double []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, String = num of appearences of term 1 from query, num of appearences of term 2 from query...
    Map<String,double []> numberOfLineOfTermInDoc = new TreeMap<>();// String=docName, String = num of line of term 1 from query in doc, num of line of term 2 from query in doc...
    int sizeOfIntegerArray=0;
    int currentIndexInIntegerArray=0;
    Set<String> queryAfterParsing;
    Indexer indexer;
    ReadFile readFile;
    Parse parse;
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
    private void addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(Set<String> queryAfterParsing , boolean isOriginalQuery) {

        if (isOriginalQuery){
            this.queryAfterParsing = queryAfterParsing;
            counterOfTermsInQuery = 0;
            sizeOfIntegerArray = queryAfterParsing.size() * 2 + 1;
            currentIndexInIntegerArray = 0;
        }

        for (String s : queryAfterParsing) {
            int startingChar = s.charAt(0);
            int numOfPosting = 0;
            if (startingChar >= 65 && startingChar <= 90) numOfPosting = startingChar - 54;
            else if (startingChar >= 97 && startingChar <= 122) numOfPosting = startingChar - 86;
            else if (startingChar >= 48 && startingChar <= 57) numOfPosting = startingChar - 47;
            int lineInPosting = indexer.treeMapForLineNumberInPosting.get(s) + 1;
            try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/" + numOfPosting + ".txt"), StandardCharsets.UTF_8));
            String line;
            int currentLineIndex = 0;
            while (currentLineIndex < lineInPosting) {
                br.readLine();
                currentLineIndex++;
            }
            line = br.readLine();
            String[] docs = line.split("~");
            addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(docs);}
            catch (IOException e){
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
            numberOfLineOfTermInDoc.put(docInfo[0], new double[sizeOfIntegerArray]);
        }
        appearancesCountingOfTermsInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[1]);
        numberOfLineOfTermInDoc.get(docInfo[0])[currentIndexInIntegerArray] = Integer.parseInt(docInfo[2]);//
    }






}
