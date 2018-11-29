package sample;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Indexer {
    Map <String,Integer> numberOfDocsPerTerm = new HashMap<>();
    Map <String,Integer> frequentOfTermInCorpus = new HashMap<>();
    int numOfDifferentPosting = 37; // based on the
    BufferedWriter [] bufferedWritersArray;


    long start = System.nanoTime();
    BufferedWriter aBufferWriter;
    BufferedWriter bBufferWriter;
    BufferedWriter cBufferWriter;
    BufferedWriter dBufferWriter;
    BufferedWriter eBufferWriter;
    BufferedWriter fBufferWriter;
    BufferedWriter gBufferWriter;
    BufferedWriter hBufferWriter;
    BufferedWriter iBufferWriter;
    BufferedWriter jBufferWriter;
    BufferedWriter kBufferWriter;
    BufferedWriter lBufferWriter;
    BufferedWriter mBufferWriter;
    BufferedWriter nBufferWriter;
    BufferedWriter oBufferWriter;
    BufferedWriter pBufferWriter;
    BufferedWriter qBufferWriter;
    BufferedWriter rBufferWriter;
    BufferedWriter sBufferWriter;
    BufferedWriter tBufferWriter;
    BufferedWriter uBufferWriter;
    BufferedWriter vBufferWriter;
    BufferedWriter wBufferWriter;
    BufferedWriter xBufferWriter;
    BufferedWriter yBufferWriter;
    BufferedWriter zBufferWriter;
    BufferedWriter BufferWriter0;
    BufferedWriter BufferWriter1;
    BufferedWriter BufferWriter2;
    BufferedWriter BufferWriter3;
    BufferedWriter BufferWriter4;
    BufferedWriter BufferWriter5;
    BufferedWriter BufferWriter6;
    BufferedWriter BufferWriter7;
    BufferedWriter BufferWriter8;
    BufferedWriter BufferWriter9;
    BufferedWriter BufferWriter$;

    /**
     * initial bufferedWritersArray:
     * bufferedWritersArray[0]  - represents term which starts with 'a' or 'A'
     * bufferedWritersArray[1]  - represents term which starts with 'b' or 'B'
     * ...
     * bufferedWritersArray[25]  - represents term which starts with 'z' or 'Z'
     * bufferedWritersArray[26]  - represents term which starts with '0'
     * bufferedWritersArray[27]  - represents term which starts with '1'
     * ...
     * bufferedWritersArray[35]  - represents term which starts with '9'
     * bufferedWritersArray[36]  - represents term which starts with '$'
     * @param path
     */
    public Indexer(String path){
        bufferedWritersArray = new BufferedWriter[numOfDifferentPosting];
        for (int i=0; i<bufferedWritersArray.length; i++){
            try {
                bufferedWritersArray[i] = new BufferedWriter(new FileWriter(path + "/" + i + ".txt",true));
            }
            catch (IOException e){}
        }


    }
    public void index50Files(Map<String,Map<String,Double>> docsByTerm , int currentFileToWrite, String path){
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
            StringBuilder stringBuilder = new StringBuilder();
            BufferedWriter tempBufferWriter = new BufferedWriter(new FileWriter(path + currentFileToWrite + ".txt"));
            for ( Map.Entry<String, Map<String,Double>> entry : treeMap.entrySet() ) {
                Map <String,Double> currentTermMap = entry.getValue();
                for ( Map.Entry<String,Double> insideEntry : currentTermMap.entrySet() ) {
                    String [] s = String.valueOf(insideEntry.getValue()).split("\\.");
                    stringBuilder.append(entry.getKey() + "," + insideEntry.getKey() + "," + s[0] + "," + s[1].substring(0,s[1].length()-1) + "\n");
                }
            }
            tempBufferWriter.write(stringBuilder.toString());
            tempBufferWriter.flush();
            tempBufferWriter.close();

        }
        catch (IOException e){}

        /*
       */

    }

    public void writeDictionary (String path){
        TreeMap<String,Integer> treeMapForDocsPerTerm = new TreeMap<>(numberOfDocsPerTerm);
        TreeMap<String,Integer> treeMapForfrequentOfTermInCorpus = new TreeMap<>(frequentOfTermInCorpus);
        numberOfDocsPerTerm.clear();
        frequentOfTermInCorpus.clear();

        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedWriter dictionaryBufferWriter = new BufferedWriter(new FileWriter(path));
            int [] countersForEachLetter = new int[numOfDifferentPosting];
            for ( Map.Entry<String, Integer> entry : treeMapForDocsPerTerm.entrySet() ) {
                int startingChar = entry.getKey().charAt(0);
                int currentCounter =0;
                if (startingChar>=65 && startingChar<=90) currentCounter = startingChar - 54;
                else if (startingChar>=97 && startingChar<=122) currentCounter = startingChar - 86;
                else if (startingChar>=48 && startingChar<=57) currentCounter = startingChar - 47;
                int lineNUmber = countersForEachLetter[currentCounter];
                countersForEachLetter[currentCounter]++;
                stringBuilder.append(entry.getKey() + "," + entry.getValue().toString() + "," + treeMapForfrequentOfTermInCorpus.get(entry.getKey()) + "," + lineNUmber +"\n");
            }
            dictionaryBufferWriter.write(stringBuilder.toString());

        }
        catch (IOException e){}
    }

    public void mergePost(String path) {
        List<File> filesInFolder = null;
        try {
            filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
        }
        int currentFileToWrite=0;
        while (filesInFolder.size() > 2) {

            boolean badFileFound = false;
            for (int i = 0; i < filesInFolder.size() && !badFileFound; i++) {
                if (filesInFolder.get(i).toString().endsWith("e")){
                    filesInFolder.remove(i);
                    badFileFound = true;
                }
            }
            Thread[]threadsArray = new Thread[filesInFolder.size()/2];
            for (int i = 0; i < filesInFolder.size()-1; i+=2) {
                try {
                    BufferedReader br1 = new BufferedReader(new FileReader(filesInFolder.get(i)));
                    BufferedReader br2 = new BufferedReader(new FileReader(filesInFolder.get(i+1)));
                    BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/merged" + currentFileToWrite + ".txt"));
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
            System.out.println("finish step");
            for (int i = 0; i < filesInFolder.size()-1; i+=2) {
               filesInFolder.get(i).delete();
               filesInFolder.get(i+1).delete();
            }

            try {
                filesInFolder = Files.walk(Paths.get(path))
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }
            catch (IOException e) {

            }
            System.out.println(currentFileToWrite);
        }
    }
    /*public void sortPosting (){
        try{
        FileReader fileReader = new FileReader(System.getProperty("user.dir") + "/src/reasources/posting/0.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String inputLine;
        List<String> lineList = new ArrayList<String>();
        while ((inputLine = bufferedReader.readLine()) != null) {
            lineList.add(inputLine);
        }
        fileReader.close();

        Collections.sort(lineList);

        FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + "/src/reasources/posting/41.txt");
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);
        }
        out.flush();
        out.close();
        fileWriter.close();}
        catch (IOException e){}
    }*/

    public void writeToFinalPosting(String path){
        int currentBufferWriter = 36;
        List<File> filesInFolder = null;
        BufferedReader br = null;
        try {
            filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            boolean badFileFound = false;
            for (int i = 0; i < filesInFolder.size() && !badFileFound; i++) {
                if (filesInFolder.get(i).toString().endsWith("e")){
                    filesInFolder.remove(i);
                    badFileFound = true;
                }
            }
            br = new BufferedReader(new FileReader(filesInFolder.get(0)));
            String currentLine = br.readLine();
            String currentTerm = "";
            String previosTerm = "";
            char delimiter = '~';
            int i;
            boolean firstletter = false;
            boolean firstNumber =false;
            boolean firstdollar = false;
            while (currentLine != null) {
                previosTerm = currentTerm;
                currentTerm = "";
                if ( !currentLine.contains(",")) continue;
                else{
                    i=0;
                    while (currentLine.charAt(i) != ','){
                        currentTerm += currentLine.charAt(i);
                        i++;
                    }
                }

                int startWithChar = currentLine.charAt(0);
                if (startWithChar<=90 && startWithChar>=65){
                    if (startWithChar-52 != currentBufferWriter){
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
                        System.out.println("start letters");
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
                    //bufferedWritersArray[36].write(currentLine + "\n");
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
                        if (line1.compareTo(line2) < 0) {
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
