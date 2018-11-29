package sample;

import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ReadFile implements Runnable {


    public int numOfDocs=0;
    private long start;
    private long finish;
    private String path = "";
    public int jumping50=0;
    List<File> filesInFolder = null;
    public List<Pair <String, String>> documents = new ArrayList<>();
    StringBuilder stringBuilder;
    public ReadFile (String path){
        this.path=path;
        try {
            filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        catch (IOException e){}}
    @Override
    public void run() {
        start = System.nanoTime();
        stringBuilder = new StringBuilder();
        for (int i=jumping50; i<filesInFolder.size() && i<jumping50+50; i++){
            try {
                String currentFileInString = new String(Files.readAllBytes(filesInFolder.get(i).toPath()));
                Document doc = Jsoup.parse(currentFileInString);
                Elements elements = doc.getElementsByTag("DOC");
                String path = filesInFolder.get(i).getPath();
                int [] indexesOfCity = new int[0];
                int [] indexesOfLanguage = new int [0];
                int [] indexesOfDocs = new int [elements.size()];
                if (currentFileInString.indexOf("<DOC>") != -1){
                    int index = currentFileInString.indexOf("<DOC>");
                    for (int currentDoc = 0; currentDoc<indexesOfDocs.length; currentDoc++){
                        indexesOfDocs [currentDoc] = index;
                        index = currentFileInString.indexOf("<DOC>", index + 1);
                    }
                }
                if (currentFileInString.indexOf("<F P=104>") != -1){
                    indexesOfCity = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=104>")+9;
                    int indexOfIndexesOfCity=0;
                    while (currentIndex >= 0 && indexOfIndexesOfCity < indexesOfCity.length) {
                        if (indexOfIndexesOfCity<indexesOfCity.length-1 && indexesOfDocs[indexOfIndexesOfCity] < currentIndex && indexesOfDocs[indexOfIndexesOfCity+1] > currentIndex){
                            indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        else if (indexesOfDocs[indexOfIndexesOfCity] < currentIndex){
                            indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        indexOfIndexesOfCity++;
                    }
                }
                if (currentFileInString.indexOf("<F P=105>") != -1){
                    indexesOfLanguage = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=105>")+9;
                    int indexOfIndexesOfLanguage=0;
                    while (currentIndex >= 0 && indexOfIndexesOfLanguage < indexesOfLanguage.length) {
                        if (indexOfIndexesOfLanguage<indexesOfLanguage.length-1 && indexesOfDocs[indexOfIndexesOfLanguage] < currentIndex && indexesOfDocs[indexOfIndexesOfLanguage+1] > currentIndex){
                            indexesOfLanguage[indexOfIndexesOfLanguage] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        else if (indexesOfDocs[indexOfIndexesOfLanguage] < currentIndex){
                            indexesOfLanguage[indexOfIndexesOfLanguage] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        indexOfIndexesOfLanguage++;
                    }

                }

                int currentElement =0;
                for (Element element:elements){
                    String s = element.children().toString();
                    String text = element.getElementsByTag("TEXT").text();
                    String name = element.getElementsByTag("DOCNO").text();
                    String city="";
                    String language="";
                    if (indexesOfCity.length != 0){
                        int index = indexesOfCity[currentElement];
                        if (index != 0){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfCity = 0;
                            int startOfCity =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfCity = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfCity = index;
                                }
                            }
                            city = currentFileInString.substring(startOfCity , lastOfCity).toUpperCase();}
                        else{
                            city="";
                        }
                    }
                    if (indexesOfLanguage.length != 0){
                        int index = indexesOfLanguage[currentElement];
                        if (index != 0 ){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfLanguage = 0;
                            int startOfLanguage =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfLanguage = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfLanguage = index;
                                }


                            }
                            language = currentFileInString.substring(startOfLanguage , lastOfLanguage).toUpperCase();
                        }
                        else language="";
                    }
                    String headLine="";
                    String endOfPath = filesInFolder.get(i).getPath().substring(filesInFolder.get(i).getPath().lastIndexOf("\\")+1);
                    if (endOfPath.startsWith("FB")){
                        headLine =  element.getElementsByTag("TI").text();

                    }
                    else if (endOfPath.startsWith("LA")){
                        if ((s.indexOf("<headline>")) != -1) {
                            String temp = s.substring(s.indexOf("<headline>") + 10, s.indexOf("</headline>"));
                            int y=0;
                            int x=0;
                            while (x<2){
                                if (temp.charAt(y) == '<') x++;
                                y++;
                            }
                            headLine = temp.substring(7, y-1);
                        }
                    }
                    else{
                        String temp =  element.getElementsByTag("HEADLINE").text();
                        headLine = temp.substring(temp.indexOf('/')+1);
                    }

                    //documents.put(name, text );
                    documents.add(new Pair<>(name,text));
                    stringBuilder.append(name + "," + city + "," + language +  "," + headLine +"\n");
                    currentElement++;
                    numOfDocs++;

                }
            }
            catch (IOException e){e.printStackTrace();}}
        jumping50 += 50;
    }


        /*
        runnableReadFile [] threadsArray = new runnableReadFile[(filesInFolder.size()/50)+1];

        int numOfCurrentFile=0;
        //long start = System.nanoTime();
        start = System.nanoTime();
        for (int currentThread=0; currentThread<threadsArray.length && numOfCurrentFile<filesInFolder.size(); currentThread++){
            File [] f;
            if (currentThread == threadsArray.length - 1) f = new File[filesInFolder.size()%50];
            else f = new File[50];
            for (int currentFileInRunnableReadFile=0; currentFileInRunnableReadFile<50 && numOfCurrentFile<filesInFolder.size(); currentFileInRunnableReadFile++){
                f[currentFileInRunnableReadFile]=filesInFolder.get(numOfCurrentFile);
                numOfCurrentFile++;
            }
            threadsArray[currentThread] = new runnableReadFile(f,currentThread);
        }
        Parse.startParser(threadsArray.length);
        for (int currentThread=20; currentThread<threadsArray.length; currentThread++){
            threadsArray[currentThread].run();
        }
        System.out.println(documents.size());




            /*
            corpusNum++;
            docNum=0;
            try {
                MyDocument doc = Jsoup.parse(new String(Files.readAllBytes(l.toPath())));
                Elements elements = doc.getElementsByTag("DOC");
                //System.out.println(elements.size());
                for (Element element:elements){
                    String s = element.children().toString();
                    String name = element.getElementsByTag("DOCNO").text();
                    System.out.print(l.toPath() + " " + docNum + " " + corpusNum + " ");
                    docNum++;
                    //parse.parsingTextToText(s,name);
                }
            }
            catch (IOException e){}*/
}

        /*public class runnableReadFile{
            private File []f;
            private int threadNumber;
            public runnableReadFile(File[]f,int num) {this.f = f;this.threadNumber=num;}
            public void run() {
                System.out.println(threadNumber + " start");
                for (File l:f){
                try {
                    Document doc = Jsoup.parse(new String(Files.readAllBytes(l.toPath())));
                    Elements elements = doc.getElementsByTag("DOC");
                    //System.out.println(elements.size());
                    for (Element element:elements){
                        String s = element.children().toString();
                        String text = element.getElementsByTag("TEXT").text();
                        String name = element.getElementsByTag("DOCNO").text();
                        String city="";
                        String language="";
                        int startOfCity = s.indexOf("<F P=104>");
                        if (startOfCity != -1){
                            boolean foundF = false;
                            int i = startOfCity +9;
                            for (; !foundF; i++){
                                if (s.charAt(i) == '<') foundF = true;
                            }
                            i-=1;
                            city = s.substring(startOfCity + 9 , i--).split(" ")[1];
                        }
                        int startOfLanguage = s.indexOf("<F P=105>");
                        if (startOfLanguage != -1){
                            boolean foundF = false;
                            int i = startOfLanguage +9;
                            for (; !foundF; i++){
                                if (s.charAt(i) == '<') foundF = true;
                            }
                            i-=1;
                            language = s.substring(startOfCity + 9 , i--).split(" ")[1];
                        }
                        String headLine="";
                        String endOfPath = l.getPath().substring(l.getPath().lastIndexOf("\\")+1);
                        if (endOfPath.startsWith("FB")){
                            headLine =  element.getElementsByTag("TI").text();

                        }
                        else if (endOfPath.startsWith("LA")){
                            if ((s.indexOf("<headline>")) != -1) {
                                String temp = s.substring(s.indexOf("<headline>") + 10, s.indexOf("</headline>"));
                                int i=0;
                                int x=0;
                                while (x<2){
                                    if (temp.charAt(i) == '<') x++;
                                    i++;
                                }
                                headLine = temp.substring(7, i-1);
                            }
                        }
                        else{
                            String temp =  element.getElementsByTag("HEADLINE").text();
                            headLine = temp.substring(temp.indexOf('/')+1);
                        }
                        System.out.println(headLine);
                        text = headLine + "." + text;
                        documents.put(name, text );
                        stringBuilder.append("city:" + city + " " + "language:" + language + " " + "headLine:" + headLine +"\n");

                        //Parse.startSpecificParser(s,name,threadNumber,l.getPath());
                    }
                }
                catch (IOException e){e.printStackTrace();}}
                finish = System.nanoTime();
                System.out.println((finish-start) * Math.pow(10,-9));
            }
        }*/






