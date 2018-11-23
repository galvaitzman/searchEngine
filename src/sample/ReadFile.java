package sample;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadFile {

    private static int x=0;
    private long start;
    private long finish;
    public Map <String, String> documents = new HashMap<>();
    public ReadFile(String path){
        List<File> filesInFolder = null;

        try {
                    filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        catch (IOException e){}
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
        for (int currentThread=5; currentThread<threadsArray.length; currentThread++){
            threadsArray[currentThread].run();
        }




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

        public class runnableReadFile{
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
                        String headLine =  element.getElementsByTag("T1").text();
                        documents.put(name, "city:" + city + " " + "language:" + language + " " + "headLine:" + headLine )
                        Parse.startSpecificParser(s,name,threadNumber,l.getPath());
                    }
                }
                catch (IOException e){e.printStackTrace();}}
                finish = System.nanoTime();
                System.out.println((finish-start) * Math.pow(10,-9));
            }
        }
}

