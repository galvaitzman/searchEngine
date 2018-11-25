package sample;

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

    private long start;
    private long finish;
    private String path = "";
    public int jumping50=0;
    List<File> filesInFolder = null;
    public Map <String, String> documents = new HashMap<>();
    public volatile boolean finishReadingAllDocuments =false;
    StringBuilder stringBuilder = new StringBuilder();
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
        finishReadingAllDocuments=false;
        start = System.nanoTime();
        for (int i = jumping50; i < filesInFolder.size() && i < jumping50 + 50; i += 1){
            try {
                Document doc = Jsoup.parse(new String(Files.readAllBytes(filesInFolder.get(i).toPath())));
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
                        int y = startOfCity + 9;
                        for (; !foundF; y++){
                            if (s.charAt(y) == '<') foundF = true;
                        }
                        y -= 2;
                        city = s.substring(startOfCity + 9 , y).split(" ")[1];
                    }
                    int startOfLanguage = s.indexOf("<F P=105>");
                    if (startOfLanguage != -1){
                        boolean foundF = false;
                        int y = startOfLanguage +9;
                        for (; !foundF; y++){
                            if (s.charAt(y) == '<') foundF = true;
                        }
                        y-=2;
                        language = s.substring(startOfCity + 9 , y).split(" ")[1];
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
                    text = headLine + "." + text;
                    documents.put(name, text );
                    stringBuilder.append("city:" + city + " " + "language:" + language + " " + "headLine:" + headLine +"\n");
                    //Parse.startSpecificParser(s,name,threadNumber,l.getPath());
                }
            }
            catch (IOException e){e.printStackTrace();}}
        finish = System.nanoTime();
        System.out.println((finish-start) * Math.pow(10,-9));
        System.out.println(documents.size());
        finishReadingAllDocuments = true;
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


