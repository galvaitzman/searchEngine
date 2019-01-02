package sample;

import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Ranker {


    private String pathOfPostingAndDictionary;
    Set<String> queryAfterParsing;
    Dictionary dictionary; // the dictionary which holds all the relevant dictionaries
    Map <String, Double> rankingOfDocuments; // key = docName, value = ranking of the doc
    Map <String, String> firstLineApperencesOfTermInDoc;
    double b = 0.75; // value of b in bm25
    double k1 = 1.2; // value of k in bm25
    double weightOfBM25 = 1.2;
    double weightOfCosSim = 0.2;
    double weightOfLIneFirstAppearence=0.1;


    public Ranker(String pathOfPostingAndDictionary, Dictionary dictionary) {
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        this.dictionary =dictionary;
    }

    /** step 1
     * the function pull out for each term from the query its matching line from the posting .for example:
     * for the term Z-SHAPED : 'Z-SHAPED^LA082090-0026^1^100~LA062589-0153^1^387~FBIS3-24463^1^41'
     * the function calls to addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(step 2)
     * @param queryAfterParsing
     */
    public void addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(Set<String> queryAfterParsing) {
        rankingOfDocuments = new HashMap<>();

        firstLineApperencesOfTermInDoc = new HashMap<>();
        this.queryAfterParsing = queryAfterParsing;
        for (String s : queryAfterParsing) {
            int startingChar = s.charAt(0);
            int numOfPosting = 0;
            if (startingChar >= 65 && startingChar <= 90) numOfPosting = startingChar - 54;
            else if (startingChar >= 97 && startingChar <= 122) numOfPosting = startingChar - 86;
            else if (startingChar >= 48 && startingChar <= 57) numOfPosting = startingChar - 47;
            if (dictionary.treeMapForLineNumberInPosting.get(s) == null){
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

    /** step 2
     * the function get the docs array from step 1.
     * for example, for the term Z-SHAPED : 'Z-SHAPED^LA082090-0026^1^100~LA062589-0153^1^387~FBIS3-24463^1^41' the function divides it into docs by the '~' sign
     * like this: docs [0] = Z-SHAPED^LA082090-0026^1^100, docs[1]= LA062589-0153^1^387, docs[2] = FBIS3-24463^1^41.
     * the function calls to addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc (step 3) function for each doc from the docs array
     * @param docs - for i: 0 < i < lengthOfDoc -1, docs[i] = "<name Of Doc>,<number Of Appearances Of current term>,<first line of appearance of current term in doc>"
     */
    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDocWraper(String [] docs){
        String [] docInfo;
        String currentTerm = docs[0].split("\\^")[0];
        docInfo = new String[]{docs[0].split("\\^")[1],docs[0].split("\\^")[2],docs[0].split("\\^")[3]};
        addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo,currentTerm);
        for (int i=1; i<docs.length; i++){
            docInfo = docs[i].split("\\^");
            addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(docInfo,currentTerm);
        }

    }


    /** step 3
     * the function gets the info about every doc in relation to the current term (from step 2)
     * and sends the relevant info for each ranker (BM25 rank, cosSim rank, first line appearance rank)
     * @param docInfo
     * @param currentTerm
     */
    private void addToAppearancesCountingOfTermsInDocAndToNumberOfLineOfTermInDoc(String [] docInfo, String currentTerm) {
       getBM25ForDoc(docInfo[0],Integer.parseInt(docInfo[1]),currentTerm);  //step 3.1
       getCosSim(docInfo[0],Integer.parseInt(docInfo[1]),currentTerm); // step 3.2
       getLineFirstAppearence(docInfo[0],Integer.parseInt(docInfo[2]),currentTerm); // step 3.3
    }


    /** step 4
     * filtering the documents by cities
     * @param cities - Map of cities. if a city from the Map (if the map isn't empty) is not in the document's <F P=104> tag or not in the document's text,
     *               than we ignore this document and not return it even if it relevant to the query
     * @return 50 documents which considered the most relevant to the query
     */
    public List<String> rankEveryDocument (Map<String,Integer>cities) {
        addRankingBasedOnAverageDistanceBetweenTermsInDoc();


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
                System.out.println(entry.getValue());
                System.out.println(entry.getKey());
            }
            counter++;
        }

        return result;
    }


    /** step 3.2
     *  adding score to the total score of specific document, based on the cosSim equation and normalize by number Of Appearances Of Most Common Term In Doc
     *  (from dictionary.numberOfAppearancesOfMostCommonTermInDoc). the result of the cosSim equation will be multiply by the weightOfCosSim
     * @param doc_name
     * @param numberOfAppearancesOfCurentTermInDoc
     * @param currentTerm
     */


    public void getCosSim(String doc_name,int numberOfAppearancesOfCurentTermInDoc, String currentTerm)
    {
        double up = 0;
        up +=  (double)numberOfAppearancesOfCurentTermInDoc / dictionary.numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);

        double down = dictionary.weightOfDocNormalizedByMostCommonWordInDoc.get(doc_name) * Math.sqrt((double)queryAfterParsing.size()/2);

        if (rankingOfDocuments.get(doc_name) == null){
            rankingOfDocuments.put(doc_name,(up/down)*weightOfCosSim);
        }
        else{
            rankingOfDocuments.put(doc_name, rankingOfDocuments.get(doc_name) + (up/down)*weightOfCosSim);
        }
    }


    /** step 3.2 (not in use)
     *  adding score to the total score of specific document, based on the cosSim equation and normalize by length of doc
     *  (from dictionary.numberOfAppearancesOfMostCommonTermInDoc). the result of the cosSim equation will be multiply by the weightOfCosSim
     * @param doc_name
     * @param numberOfAppearancesOfCurentTermInDoc
     * @param currentTerm
     */
    /*
    public void getCosSim(String doc_name,int numberOfAppearancesOfCurentTermInDoc, String currentTerm)
    {
        double up = 0;
        up +=  (double)numberOfAppearancesOfCurentTermInDoc / dictionary.numberOfTotalTermsInDoc.get(doc_name);

        double down = dictionary.weightOfDocNormalizedByLengthOfDoc.get(doc_name) * Math.sqrt(queryAfterParsing.size());

        if (rankingOfDocuments.get(doc_name) == null){
            rankingOfDocuments.put(doc_name,(up/down)*weightOfCosSim);
        }
        else{
            rankingOfDocuments.put(doc_name, rankingOfDocuments.get(doc_name) + (up/down)*weightOfCosSim);
        }
    }*/


    /** step 3.1
     *  adding score to the total score of specific document, based on the BM25 equation.
     *  the result of the BM25 equation will be multiply by the weightOfBM25
     * @param doc_name
     * @param numberOfAppearancesOfCurentTermInDoc
     * @param currentTerm
     */
    public void getBM25ForDoc(String doc_name,int numberOfAppearancesOfCurentTermInDoc, String currentTerm) {
        double rank_BM25P = 0;
        double d_avdl = (double) dictionary.numberOfTotalTermsInDoc.get(doc_name) / Main.avdl;
        //double k2 = k1 + (((dictionary.numberOfTotalTermsInDoc.get(doc_name) - Main.avdl) / 12 ) * 0.01);
       // k2 = Math.min(k2,2);
        double temp = k1 * ((1 - b) + b * d_avdl);
        int counter = 0;
        //for (String s : queryAfterParsing) {/////
            double tf = numberOfAppearancesOfCurentTermInDoc; /// numberOfAppearancesOfMostCommonTermInDoc.get(doc_name);
            double partA = ((double) ((k1 + 1) * tf)) / (temp + tf);
            //double partB = ((double) ((k2 + 1) * entry.getValue())) / (k2 + entry.getValue());
            //if (dictionary.IDF_BM25_Map.get(s) != null)
            rank_BM25P += (dictionary.IDF_BM25_Map.get(currentTerm) * partA);
            //counter += 1;
        //}
        if (rankingOfDocuments.get(doc_name) == null){
            rankingOfDocuments.put(doc_name,rank_BM25P*weightOfBM25);//lkdjflksdjflkds
        }
        else{
            rankingOfDocuments.put(doc_name,rankingOfDocuments.get(doc_name) + rank_BM25P*weightOfBM25);
        }
    }

    /** step 3.3
     * the function add score to the document based on the first time that the currentTerm appeared in the doc and normalized by the number of lines in the doc.
     * for example: the term 'engine' appeared in line 3 for the first time in doc FBIS3-1234. the doc has 20 lines. so the score is : 1-(3/20) = 17/20.
     * the final score that will be added is multiply by the weightOgLineFirstAppearance.
     * @param doc_name
     * @param firstLineOfCurrentTermInDocName
     * @param currentTerm
     */
    private void getLineFirstAppearence(String doc_name,int firstLineOfCurrentTermInDocName, String currentTerm){
        if (firstLineApperencesOfTermInDoc.get(doc_name) == null) firstLineApperencesOfTermInDoc.put(doc_name,firstLineOfCurrentTermInDocName + ",");
        else firstLineApperencesOfTermInDoc.put(doc_name,firstLineApperencesOfTermInDoc.get(doc_name) + firstLineOfCurrentTermInDocName + ",");
        double addToRanking = 1-((double)firstLineOfCurrentTermInDocName/dictionary.numberOfLinesInDoc.get(doc_name));
        rankingOfDocuments.put(doc_name, rankingOfDocuments.get(doc_name)+ (addToRanking * weightOfLIneFirstAppearence));

    }


    /** step 4.1
     * the function add score to the documents based on the average distance between the terms (or some of the term) in the query. the added score will be between
     * 1 (the minimum is 1: if the average is the number of lines in the document) to 2 (the maximum is 2: if the average is 0 or there are 12 or more terms from the query in the document)
     */

    private void addRankingBasedOnAverageDistanceBetweenTermsInDoc(){
        for (Map.Entry<String, String> entry : firstLineApperencesOfTermInDoc.entrySet()) {
            String [] linesAsString = entry.getValue().split(",");
            if (linesAsString.length<=1) continue;
            else if (linesAsString.length>=12){
                rankingOfDocuments.put(entry.getKey(),rankingOfDocuments.get(entry.getKey())+2);
                continue;
            }
            List<Integer> linesAsInteger = new ArrayList<>();
            for (String s: linesAsString){
                linesAsInteger.add(Integer.parseInt(s));
            }
            int totalDistanceBetweenTermsInDoc= 0;
            int mone = linesAsInteger.size();
            int machane1=Math.max(1,linesAsInteger.size()-2);
            for (int i=linesAsInteger.size()-1; i>=2; i--){
                mone = mone * i;
            }

            for (int i=linesAsInteger.size()-3; i>=2; i--){
                machane1 = machane1 * i;
            }
            int numberOfLinesChoose2 = mone / (machane1*2);


            for (int i=0; i<linesAsInteger.size(); i++){
                for (int j=i+1; j<linesAsInteger.size(); j++){
                    totalDistanceBetweenTermsInDoc += Math.abs(linesAsInteger.get(i)-linesAsInteger.get(j));
                }
            }
            double toAdd =  2 - (((double)(totalDistanceBetweenTermsInDoc/numberOfLinesChoose2)) /  dictionary.numberOfLinesInDoc.get(entry.getKey()));
            rankingOfDocuments.put(entry.getKey(),rankingOfDocuments.get(entry.getKey()) + toAdd);
        }
    }
}
