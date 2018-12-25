package sample;

import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {

    int counterOfTermsInQuery = 0 ;
    private String pathOfPostingAndDictionary;
    int sizeOfIntegerArray=0;
    int currentIndexInIntegerArray=0;
    Set<String> queryAfterParsing;
    Dictionary dictionary;
    Map <String, Double> rankingOfDocuments;
    Map <String, String> firstLineApperencesOfTermInDoc;
    double b = 0.75;
    double k1 = 1.2;
    double weightOfBM25 = 1.2;
    double weightOfCosSim = 0.2;
    double weightOfLIneFirstAppearence=0.4;


    public Ranker(String pathOfPostingAndDictionary, Dictionary dictionary) {
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        this.dictionary =dictionary;
    }

    //step 2
    public void addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(Set<String> queryAfterParsing) {
        rankingOfDocuments = new HashMap<>();
        firstLineApperencesOfTermInDoc = new HashMap<>();
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
                e.printStackTrace();
            }
        }
    }

    //step 3
    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(String [] docs){
        String [] docInfo;
        String currentTerm = docs[0].split("\\^")[0];
        docInfo = new String[]{docs[0].split("\\^")[1],docs[0].split("\\^")[2],docs[0].split("\\^")[3]};
        addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo,currentTerm);
        for (int i=1; i<docs.length; i++){
            docInfo = docs[i].split("\\^");
            addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo,currentTerm);
        }
        currentIndexInIntegerArray++;
    }


    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(String [] docInfo, String currentTerm) {
       getBM25ForDoc(docInfo[0],Integer.parseInt(docInfo[1]),currentTerm);
       getCosSim(docInfo[0],Integer.parseInt(docInfo[1]),currentTerm);
       getLineFirstAppearence(docInfo[0],Integer.parseInt(docInfo[2]),currentTerm);
    }



    public List<String> rankEveryDocument (Map<String,Integer>cities) {
        //addRankingBasedOnMinimalDistanceBetweenTermsInDoc();
        Map <String,Double> finalRankingForDocs = new HashMap<>();
        for (Map.Entry<String, Double> insideEntry : rankingOfDocuments.entrySet()) {
            if (cities.size() > 0) {
                if (cities.get(dictionary.cityOfDoc.get(insideEntry.getKey())) != null ){
                    finalRankingForDocs.put(insideEntry.getKey(), insideEntry.getValue());
                }
                else{
                    boolean foundCity = false;
                    for (Map.Entry<String,Integer> entry: cities.entrySet()){
                        if (dictionary.cityInDoc.get(insideEntry.getKey()) != null && dictionary.cityInDoc.get(insideEntry.getKey()).contains(entry.getKey())) {
                            foundCity = true;
                            break;
                        }
                    }
                    if (foundCity) finalRankingForDocs.put(insideEntry.getKey(), insideEntry.getValue());
                }
            }
            else{
                finalRankingForDocs.put(insideEntry.getKey(), insideEntry.getValue());
            }
        }

        List<Map.Entry<String, Double>> list = new ArrayList<>(finalRankingForDocs.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> result = new ArrayList<>();
        int counter = 0;
        for (Map.Entry<String, Double> entry : list) {
            if (list.size() - counter <= 50) {
                result.add(entry.getKey());
                System.out.println(entry.getKey());
            }
            counter++;
        }

        return result;
    }


    /**
     * normalize by most common word in doc
     * @param doc_name
     * @return
     */

    public void getCosSim(String doc_name,int numberOfAppearancesOfCurentTermInDoc, String currentTerm)
    {
        double up = 0;
        up +=  numberOfAppearancesOfCurentTermInDoc / dictionary.numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);

        double down = dictionary.weightOfDocNormalizedByMostCommonWordInDoc.get(doc_name) * Math.sqrt(queryAfterParsing.size());

        if (rankingOfDocuments.get(doc_name) == null){
            rankingOfDocuments.put(doc_name,(up/down)*weightOfCosSim);
        }
        else{
            rankingOfDocuments.put(doc_name, rankingOfDocuments.get(doc_name) + (up/down)*weightOfCosSim);
        }
    }



    public void getBM25ForDoc(String doc_name,int numberOfAppearancesOfCurentTermInDoc, String currentTerm) {
        double rank_BM25P = 0;
        double d_avdl = (double) dictionary.numberOfTotalTermsInDoc.get(doc_name) / Main.avdl;
        //double k2 = k1 + (((dictionary.numberOfTotalTermsInDoc.get(doc_name) - Main.avdl) / 12 ) * 0.01);
       // k2 = Math.min(k2,2);
        double temp = k1 * ((1 - b) + b * d_avdl);
        int counter = 0;
        //for (String s : queryAfterParsing) {
            double tf = numberOfAppearancesOfCurentTermInDoc; /// numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);
            double partA = ((double) ((k1 + 1) * tf)) / (temp + tf);
            //double partB = ((double) ((k2 + 1) * entry.getValue())) / (k2 + entry.getValue());
            //if (dictionary.IDF_BM25_Map.get(s) != null)
            rank_BM25P += (dictionary.IDF_BM25_Map.get(currentTerm) * partA);
            //counter += 1;
        //}
        if (rankingOfDocuments.get(doc_name) == null){
            rankingOfDocuments.put(doc_name,rank_BM25P*weightOfBM25);
        }
        else{
            rankingOfDocuments.put(doc_name,rankingOfDocuments.get(doc_name) + rank_BM25P*weightOfBM25);
        }
    }

    private void getLineFirstAppearence(String doc_name,int firstLineOfCurrentTermInDocName, String currentTerm){
        if (firstLineApperencesOfTermInDoc.get(doc_name) == null) firstLineApperencesOfTermInDoc.put(doc_name,firstLineOfCurrentTermInDocName + ",");
        else firstLineApperencesOfTermInDoc.put(doc_name,firstLineApperencesOfTermInDoc.get(doc_name) + firstLineOfCurrentTermInDocName + ",");
        double addToRanking = 1-((double)firstLineOfCurrentTermInDocName/dictionary.numberOfLinesInDoc.get(doc_name));
        rankingOfDocuments.put(doc_name, rankingOfDocuments.get(doc_name)+ addToRanking * weightOfLIneFirstAppearence);

    }

    private void addRankingBasedOnMinimalDistanceBetweenTermsInDoc(){
        for (Map.Entry<String, String> entry : firstLineApperencesOfTermInDoc.entrySet()) {
            String [] linesAsString = entry.getValue().split(",");
            if (linesAsString.length==1) continue;
            List<Integer> linesAsInteger = new ArrayList<>();
            for (String s: linesAsString){
                linesAsInteger.add(Integer.parseInt(s));
            }
            int min= dictionary.numberOfLinesInDoc.get(entry.getKey());
            for (int i=0; i<linesAsInteger.size(); i++){
                for (int j=1; j<linesAsInteger.size(); j++){
                     min = Math.min(Math.abs(linesAsInteger.get(i)-linesAsInteger.get(j)),min);
                }
            }
            double toAdd =  2 - ((double)min /  dictionary.numberOfLinesInDoc.get(entry.getKey()));
            rankingOfDocuments.put(entry.getKey(),rankingOfDocuments.get(entry.getKey()) + toAdd);
        }
    }
}
