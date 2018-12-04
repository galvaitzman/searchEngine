package sample;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Indexer {
    Map <String,Integer> numberOfDocsPerTerm = new HashMap<>(); //counts the appearances of term in different documents
    Map <String,Integer> frequentOfTermInCorpus = new HashMap<>(); //counts the appearances of term in the corpus
    int numOfDifferentPosting = 37; // writing to 37 final posting, as described below
    BufferedWriter [] bufferedWritersArray;//as described below
    String path; //as described below
    boolean thereIsNoProblemWithBigLetters = true; //as described below
    public TreeMap<String,Integer> treeMapForDocsPerTerm; // an alphabetical sort of numberOfDocsPerTerm
    public TreeMap<String,Integer> treeMapForfrequentOfTermInCorpus; //an alphabetical sort of frequentOfTermInCorpus


    /**
     * initial bufferedWritersArray:
     * bufferedWritersArray[0]  - writs the terms which start with '$' to 0.txt
     * bufferedWritersArray[1]  - writs the terms which start with '0' to 1.txt
     * ...
     * bufferedWritersArray[10]  - writs the terms which start with '9' to 10.txt
     * bufferedWritersArray[11]  - writs the terms which start with 'a' or 'A' to 11.txt
     * ...
     * bufferedWritersArray[36]  - writs the terms which start with 'z' or 'Z' to 36.txt
     * @param path - the path which store all the indexing files (dictionary and posting)
     */
    public Indexer(String path){
        bufferedWritersArray = new BufferedWriter[numOfDifferentPosting];
        for (int i=0; i<bufferedWritersArray.length; i++){
            try {
                bufferedWritersArray[i] = new BufferedWriter(new FileWriter(path + "/" + i + ".txt",true));
            }
            catch (IOException e){}
        }
        this.path =path;
        new File(path + "/small").mkdirs();
        new File(path + "/big").mkdirs();
        new File(path + "/cities");////


    }

    /**
     * step 1: after the parser finish its job for 50 files, the indexer get his output. first of all, it updates the number of
     * different docs which the term appears in (Map numberOfDocsPerTerm) and total appearances in the courpus (Map frequentOfTermInCorpus)
     * after that it writes to the hard disk; if it start with digit, symbol or big letter, it writes it to 'big' Folder. otherwise,
     * the term starts with small letter and it writes it to 'small' folder. the temporary posting files are written in alphabetical
     * order, using the treeMap which sort it.
     * @param docsByTerm -  <String = name of the document <String = term, *Double = number of appearances of the term in the doc
     *                   .the first line which the term in the document>
     *                   *example of Double: 3.441 - means that the term appeared  3 times in the document, first time in line 44.
     *                   the right digit (1) is to avoid from zero to be the last digit
     * @param currentFileToWrite - every 50 files we must write the output of the parser to temporary posting file.
     *                           the currentFileToWrite represents the current file to write.
     */
    public void index50Files(Map<String,Map<String,Double>> docsByTerm , int currentFileToWrite){
        for ( Map.Entry<String, Map<String,Double>> entry : docsByTerm.entrySet() ) {
            if(numberOfDocsPerTerm.containsKey(entry.getKey())){
                numberOfDocsPerTerm.put(entry.getKey(),numberOfDocsPerTerm.get(entry.getKey()) + entry.getValue().size());
                int frequentOfTermInCurrent50Files=0;
                for (Map.Entry<String,Double> insideEntry : entry.getValue().entrySet()){
                    frequentOfTermInCurrent50Files += insideEntry.getValue().intValue();
                }
                frequentOfTermInCorpus.put(entry.getKey(),frequentOfTermInCorpus.get(entry.getKey()) + frequentOfTermInCurrent50Files);
            }
            else{
                numberOfDocsPerTerm.put(entry.getKey(),entry.getValue().size());
                int frequentOfTermInCurrent50Files=0;
                for (Map.Entry<String,Double> insideEntry : entry.getValue().entrySet()){
                    frequentOfTermInCurrent50Files += insideEntry.getValue().intValue();
                }
                frequentOfTermInCorpus.put(entry.getKey(),frequentOfTermInCurrent50Files);
            }
        }


        TreeMap<String,Map<String, Double>> treeMap = new TreeMap<>(docsByTerm);

        try {
            StringBuilder stringBuilderSmallLetters = new StringBuilder();
            StringBuilder stringBuilderBigLetters = new StringBuilder();
            BufferedWriter tempBufferWriterSmallLetters = new BufferedWriter(new FileWriter(path + "/small/" + currentFileToWrite + ".txt"));
            BufferedWriter tempBufferWriterBigLetters = new BufferedWriter(new FileWriter(path + "/big/" + currentFileToWrite + ".txt"));

            for ( Map.Entry<String, Map<String,Double>> entry : treeMap.entrySet() ) {
                Map <String,Double> currentTermMap = entry.getValue();
                for ( Map.Entry<String,Double> insideEntry : currentTermMap.entrySet() ) {
                    String [] s = String.valueOf(insideEntry.getValue()).split("\\.");
                    if (entry.getKey().charAt(0) <= 122 && entry.getKey().charAt(0)>=97) stringBuilderSmallLetters.append(entry.getKey() + "^" + insideEntry.getKey() + "^" + s[0] + "^" + s[1].substring(0,s[1].length()-1) + "\n");
                    else stringBuilderBigLetters.append(entry.getKey() + "^" + insideEntry.getKey() + "^" + s[0] + "^" + s[1].substring(0,s[1].length()-1) + "\n");
                }
            }
            tempBufferWriterSmallLetters.write(stringBuilderSmallLetters.toString());//
            tempBufferWriterSmallLetters.flush();
            tempBufferWriterSmallLetters.close();
            tempBufferWriterBigLetters.write(stringBuilderBigLetters.toString());
            tempBufferWriterBigLetters.flush();
            tempBufferWriterBigLetters.close();

        }
        catch (IOException e){}
    }

    /**
     * step 2: in this step, after finishing writing all the temporary postings to 'big' folder and 'small' folder, we are merging
     * the files to one merged temporary posting (one for 'small' folder and one for 'big' folder)
     * @param smallOrBig - represents the folder which contains all the temporary posting ('small' folder or 'big' folder)
     */
    public void mergePost(String smallOrBig) {
        List<File> filesInFolder = null;
        try {
            filesInFolder = Files.walk(Paths.get(path + smallOrBig))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        }

        int currentFileToWrite=0;
        while (filesInFolder.size() > 1) {

            boolean badFileFound = false;
            for (int i = 0; i < filesInFolder.size() && !badFileFound; i++) {
                if (filesInFolder.get(i).toString().endsWith("e")){
                    filesInFolder.remove(i);
                    badFileFound = true;
                }
            }
            if (filesInFolder.size() == 1) return;
            Thread[]threadsArray = new Thread[filesInFolder.size()/2];
            for (int i = 0; i < filesInFolder.size()-1; i+=2) {
                try {
                    BufferedReader br1 = new BufferedReader(new FileReader(filesInFolder.get(i)));
                    BufferedReader br2 = new BufferedReader(new FileReader(filesInFolder.get(i+1)));
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path +smallOrBig +"/merged" + currentFileToWrite + ".txt"));
                    threadsArray[i/2] = new Thread(new WriteToMergePost(br1,br2,bw));
                }
                catch (Exception e){

                }
                currentFileToWrite++;
            }
            for (int i=0; i<threadsArray.length; i++){
                threadsArray[i].start();
            }
            for (int i=0; i<threadsArray.length; i++){
                try {
                    threadsArray[i].join();
                }
                catch (InterruptedException e){}
            }

            for (int i = 0; i < filesInFolder.size()-1; i+=2) {
                filesInFolder.get(i).delete();
                filesInFolder.get(i+1).delete();
            }

            try {
                filesInFolder = Files.walk(Paths.get(path + smallOrBig))
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }
            catch (IOException e) {

            }

        }
    }

    /**
     * step 3: merging between the posting file in 'small' folder with the posting in 'big' folder. in this step we
     * look for terms which appear both with small letters and with big letters, and converting the big letters to be
     * written with small letters if it does. if there is at list one term which appear both with small letters and with big letters,
     * the boolean variable 'thereIsNoProblemWithBigLetters' will changed to false, which will be considered in the 'writeDictionary' function.
     */
    public void mergeBigWithSmall(){
        List<File> filesInFolderSmallLetters = null;
        try {
            filesInFolderSmallLetters = Files.walk(Paths.get(path + "/small"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        }
        List<File> filesInFolderBigLetters = null;//
        try {
            filesInFolderBigLetters = Files.walk(Paths.get(path + "/big"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        }
        boolean badFileFound = false;
        for (int i = 0; i < filesInFolderSmallLetters.size() && !badFileFound; i++) {
            if (filesInFolderSmallLetters.get(i).toString().endsWith("e")){
                filesInFolderSmallLetters.remove(i);
                badFileFound = true;
            }
        }
        badFileFound = false;
        for (int i = 0; i < filesInFolderBigLetters.size() && !badFileFound; i++) {
            if (filesInFolderBigLetters.get(i).toString().endsWith("e")) {
                filesInFolderBigLetters.remove(i);
                badFileFound = true;
            }
        }
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(filesInFolderSmallLetters.get(0)));
            BufferedReader br2 = new BufferedReader(new FileReader(filesInFolderBigLetters.get(0)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/mergedPosting.txt"));
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            List <String> listOfWordsThatShouldBeWithSmallLetters= new ArrayList<>();
            while (line2 != null){
                if (!line2.contains("^")) continue;
                String currentTerm = "";
                int i=0;
                while (line2.charAt(i) !=  '^') {
                    currentTerm += line2.charAt(i);
                    i++;
                }
                if (currentTerm.charAt(0)<=90 && currentTerm.charAt(0) >= 65 &&  numberOfDocsPerTerm.containsKey(currentTerm.toLowerCase())){
                    String [] split = line2.split("\\^");
                    listOfWordsThatShouldBeWithSmallLetters.add(split[0].toLowerCase() + "^" + split[1] + "^" + split[2] + "^" + split[3]);
                    thereIsNoProblemWithBigLetters = false;

                }
                else {
                    bw.write(line2 + "\n");
                }
                line2 = br2.readLine();
            }
            if (thereIsNoProblemWithBigLetters){
                    while (line1 != null) {
                        bw.write(line1 +"\n");
                        line1 = br1.readLine();
                    }

            }
            else {
                int i=0;
                while (line1 != null ) {
                    if (i == listOfWordsThatShouldBeWithSmallLetters.size()) {
                        bw.write(line1 +"\n");
                        line1 = br1.readLine();
                    }
                    else if (listOfWordsThatShouldBeWithSmallLetters.get(i).compareTo(line1) < 0) {
                        bw.write(listOfWordsThatShouldBeWithSmallLetters.get(i) + "\n");
                        i++;
                    }
                    else {
                        bw.write(line1 + "\n");
                        line1 = br1.readLine();
                    }
                }
            }
            br1.close();
            br2.close();
            bw.flush();
            bw.close();
            }
            catch (Exception e){}
    }


    /**
     * step 4: writing to the final 37 posting files, each file is written with his parallel bufferWriter
     */
    public void writeToFinalPosting(){
        int currentBufferWriter = 0;
        List<File> filesInFolder = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path + "/mergedPosting.txt"));
            String currentLine = br.readLine();
            String currentTerm = "";
            String previosTerm;
            char delimiter = '~';
            int i;
            boolean firstletter = false;
            boolean firstNumber =false;
            boolean firstdollar = false;
            while (currentLine != null) {
                previosTerm = currentTerm;
                currentTerm = "";
                if ( !currentLine.contains("^")) continue;
                else{
                    i=0;
                    while (currentLine.charAt(i) != '^'){
                        currentTerm += currentLine.charAt(i);
                        i++;
                    }
                }

                int startWithChar = currentLine.charAt(0);
                if (startWithChar<=90 && startWithChar>=65){
                    if (startWithChar-54 != currentBufferWriter){
                        bufferedWritersArray[currentBufferWriter].flush();
                        currentBufferWriter = startWithChar-54;
                    }
                    if (currentTerm.equals(previosTerm))
                        bufferedWritersArray[startWithChar-54].write(delimiter + currentLine.substring(i+1));
                    else
                        bufferedWritersArray[startWithChar-54].write("\n" + currentLine);
                    //bufferedWritersArray[startWithChar-65].write(currentLine + "\n");

                }
                else if (startWithChar<=122 && startWithChar>=97){
                    if (!firstletter){
                        firstletter = true;
                    }
                    if (startWithChar-86 != currentBufferWriter){
                        bufferedWritersArray[currentBufferWriter].flush();
                        currentBufferWriter = startWithChar-86;
                    }
                    if (currentTerm.equals(previosTerm))
                        bufferedWritersArray[startWithChar-86].write(delimiter + currentLine.substring(i+1));
                    else
                        bufferedWritersArray[startWithChar-86].write("\n" + currentLine);
                    //bufferedWritersArray[startWithChar-97].write(currentLine + "\n");
                }
                else if (startWithChar<=57 && startWithChar>=48){
                    if (!firstNumber){
                        firstNumber = true;

                    }
                    if (currentTerm.equals(previosTerm))
                        bufferedWritersArray[startWithChar-47].write(delimiter + currentLine.substring(i+1));
                    else
                        bufferedWritersArray[startWithChar-47].write("\n" + currentLine);
                    if (startWithChar-47 != currentBufferWriter){
                        bufferedWritersArray[currentBufferWriter].flush();
                        //bufferedWritersArray[currentBufferWriter].close();
                        currentBufferWriter = startWithChar-47;
                    }
                    //bufferedWritersArray[startWithChar-22].write(currentLine + "\n");
                }
                else if (startWithChar==36){
                    if (!firstdollar){
                        firstdollar = true;
                    }
                    if (currentTerm.equals(previosTerm))
                        bufferedWritersArray[0].write(delimiter + currentLine.substring(i+1));
                    else
                        bufferedWritersArray[0].write("\n" + currentLine);

                }
                currentLine = br.readLine();
            }

            br.close();
            bufferedWritersArray[currentBufferWriter].flush();
            bufferedWritersArray[currentBufferWriter].close();
        }
        catch (IOException e) {
            System.out.println("problem");
        }
    }

    /**
     * step 5: writing the dictionary of the terms. each term contains his total appearances in the corpus,
     * his total appearances in different documents and a pointer to the posting file (number of row in the posting file).
     * it does it by using 'numberOfDocsPerTerm' and 'frequentOfTermInCorpus' and sort the terms by alphabetical order using
     * 'treeMapForDocsPerTerm' and 'treeMapForfrequentOfTermInCorpus'. if thereIsNoProblemWithBigLetters==false
     * (changed to false in step 3), the dictionaries 'numberOfDocsPerTerm' and 'frequentOfTermInCorpus' will be updated accordingly.
     */
    public void writeDictionary (){
        if (!thereIsNoProblemWithBigLetters){
            //
            List <String> tempMapForBigLettersWhichSupposedToBeWithSmallLetters = new ArrayList<>();
            for (Map.Entry<String,Integer> insideEntry : numberOfDocsPerTerm.entrySet()) {
                if (insideEntry.getKey().charAt(0) <= 90 && insideEntry.getKey().charAt(0) >= 65 && numberOfDocsPerTerm.get(insideEntry.getKey().toLowerCase()) != null) {
                    tempMapForBigLettersWhichSupposedToBeWithSmallLetters.add(insideEntry.getKey());
                }
            }
            for (int i=0; i<tempMapForBigLettersWhichSupposedToBeWithSmallLetters.size(); i++) {
                Integer addToDocs = numberOfDocsPerTerm.get(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i));
                Integer addToCorpus = frequentOfTermInCorpus.get(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i));
                numberOfDocsPerTerm.put(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i).toLowerCase(),numberOfDocsPerTerm.get(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i).toLowerCase()) + addToDocs);
                frequentOfTermInCorpus.put(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i).toLowerCase(),frequentOfTermInCorpus.get(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i).toLowerCase()) + addToCorpus);
                numberOfDocsPerTerm.remove(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i));
                frequentOfTermInCorpus.remove(tempMapForBigLettersWhichSupposedToBeWithSmallLetters.get(i));

            }
        }
        treeMapForDocsPerTerm = new TreeMap<>(numberOfDocsPerTerm);
        treeMapForfrequentOfTermInCorpus = new TreeMap<>(frequentOfTermInCorpus);
        numberOfDocsPerTerm.clear();
        frequentOfTermInCorpus.clear();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedWriter dictionaryBufferWriter = new BufferedWriter(new FileWriter(path + "/dictionary.txt"));
            int [] countersForEachLetter = new int[numOfDifferentPosting];
            for ( Map.Entry<String, Integer> entry : treeMapForDocsPerTerm.entrySet() ) {
                int startingChar = entry.getKey().charAt(0);
                int currentCounter =0;
                if (startingChar>=65 && startingChar<=90) currentCounter = startingChar - 54;
                else if (startingChar>=97 && startingChar<=122) currentCounter = startingChar - 86;
                else if (startingChar>=48 && startingChar<=57) currentCounter = startingChar - 47;
                int lineNUmber = countersForEachLetter[currentCounter];
                countersForEachLetter[currentCounter]++;
                stringBuilder.append(entry.getKey() + "  " + entry.getValue().toString() + "  " + treeMapForfrequentOfTermInCorpus.get(entry.getKey()) + "  " + lineNUmber +"\n");
            }
            dictionaryBufferWriter.write(stringBuilder.toString());
            dictionaryBufferWriter.flush();
            dictionaryBufferWriter.close();

        }
        catch (IOException e){}
    }


    /**
     * class which merging simultaneously posting files from step 2.
     */
    private class WriteToMergePost implements Runnable{
        BufferedReader br1;
        BufferedReader br2;
        BufferedWriter bw;
        public  WriteToMergePost(BufferedReader br1, BufferedReader br2, BufferedWriter bw){
            this.br1 = br1;
            this.br2 = br2;
            this.bw = bw;
        }
        @Override
        public void run() {
            try {
                String line1 = br1.readLine();
                String line2 = br2.readLine();
                while (line1 != null || line2 != null) {
                    if (line1 != null && line2 != null) {
                        if (line1.split("\\^")[0].compareTo(line2.split("\\^")[0]) < 0) {
                            bw.write(line1 + "\n");
                            line1 = br1.readLine();
                        } else {
                            bw.write(line2 + "\n");
                            line2 = br2.readLine();
                        }
                    } else if (line1 != null) {
                        bw.write(line1 + "\n");
                        line1 = br1.readLine();
                    } else {
                        bw.write(line2 + "\n");
                        line2 = br2.readLine();
                    }
                }

                br1.close();
                br2.close();
                bw.flush();
                bw.close();
            }
            catch (IOException e){}
        }
    }

}
