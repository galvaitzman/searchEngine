package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;
import sun.awt.Mutex;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class Main extends Application {
    Map<String, Integer> termsInDocMap = new HashMap<>();
    Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
    /*
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Welcome to World-cup Maze");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Sample.fxml").openStream());
        Scene scene = new Scene(root, 800, 300);
        // scene.getStylesheets().add(getClass().getResource("WelcomeStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }*/


        String doc = "<text>21-22 Jan 10% 10 percent 10 percentage between 1,000,000 and 2,000,000 10,123 123 Thousand 1010.56 10,123,000 55 Billion 7 Trillion 1.7320 Dollars 22 3/4 Dollars $450,000 1,000,000 Dollars " +
                "$450,000,000 $100 Million 10.6m 20.6m Dollars $100 Billion 100bn Dollars 100 Billion U.S. Dollars 320 Million U.S. Dollars 1 trillion U.S. Dollars 18 April APRIL 18 Apr 18 Jun 1994 JUNE 1994 June 1994 between 1 million and 2 million</text>";
        Parse parser = new Parse();
        parser.startParser();
        ReadFile readFile = new ReadFile(System.getProperty("user.dir") + "/src/reasources/corpus");
        List <Pair<String, String>> readyDocumentsFromReadFile = readFile.documents;
        BufferedWriter bufferWriter1 = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/reasources/docInfoCityLanguageHeadLine.txt",true));
        BufferedWriter bufferWriter2 = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/reasources/docInfoFrequencyNumberOfUniqueWords.txt",true));
        long strt = System.nanoTime();
        boolean isStemming = true;
        Indexer indexer = new Indexer(System.getProperty("user.dir") + "/src/reasources/final posting");
        int i=0;
        for (; i < readFile.filesInFolder.size() / 50 + 1; i += 1) {
            System.out.println(i + " start");
            readFile.run();
            Thread t1 = new Thread(() -> {
                try {
                    bufferWriter1.write(readFile.stringBuilder.toString());
                } catch (IOException e) {
                }
                System.out.println("finish writer1");
            });
            t1.start();

            parser.startParsing50Files(readyDocumentsFromReadFile, isStemming);
            readFile.documents.clear();
            parser.termsIndoc.clear();
            Thread t2 = new Thread(() -> {
                try {
                    bufferWriter2.write(parser.docInfo.toString());
                } catch (IOException e) {
                }
                System.out.println("finish writer 2");
            });
            t2.start();

            indexer.index50Files(parser.docsByTerm , i,System.getProperty("user.dir") + "/src/reasources/posting/");
            parser.docsByTerm.clear();
            //
            //}
            t1.join();
            t2.join();
            System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        }
        bufferWriter1.flush();
        bufferWriter2.flush();
        bufferWriter1.close();
        bufferWriter2.close();
        indexer.mergePost(System.getProperty("user.dir") + "/src/reasources/posting");
        indexer.writeToFinalPosting(System.getProperty("user.dir") + "/src/reasources/posting");
        indexer.writeDictionary(System.getProperty("user.dir") + "/src/reasources/dictionary.txt");
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));

    }




    //System.out.println("G1GGG34324".matches(".*[a-z]+.*"));
    //Parse[]p=Parse.allParsers();
    //Parse p2 = new Parse();
    //p2.parsingTextToText(doc,"bibi",0,"11");

    //Thread.sleep(360000);
    //Parse.termsInCorpusMap.forEach((key,value) -> System.out.println(key + ":" + value.numOfOccursInCorpus));
    //Map <String, Map<String,TermInDoc>> m= p2.termsInDocMap;
    //Map <String,TermInDoc> x = m.get("GONI");
    //x.forEach((key,value) -> System.out.println(key + ":" + value.numberOfOccurencesInDoc));


    //System.out.println("gal");
    //finish = System.nanoTime();
    //System.out.println((finish-start) * Math.pow(10,-9));
    //finish = System.nanoTime();
    //System.out.println( "time of running is:" + (finish-start) * Math.pow(10,-9));

                /*
        long start = System.nanoTime();
        str = str.toUpperCase();
        long finish = System.nanoTime();
        System.out.println((finish-start) * Math.pow(10,-9));
        str = "gonilevinhaimi";
        start = System.nanoTime();
        StringBuilder s = new StringBuilder();
        for (int i=0; i < str.length();i++){
            if (str.charAt(i) >= 97 && str.charAt(i)<=122){
                char t = (char)(str.charAt(i)+26);
                s.append((char)(str.charAt(i)+26));
            }
        }
        finish = System.nanoTime();
        System.out.println((finish-start) * Math.pow(10,-9));
        System.out.println(s.toString());
       // p.parsingTextToText(doc,"l");*/
/*
        Map <String,Integer> k = new HashMap<>();
        k.put("a",1);
        k.put("b",1);
        k.put("c",1);
        k.put("d",1);
        k.put("e",1);
        k.put("f",1);
        k.put("g",1);
        k.put("h",1);
        k.put("i",1);
        k.put("j",1);
        k.put("k",1);
        k.put("l",1);
        k.put("m",1);
        k.put("n",1);
        k.put("o",1);
        k.put("p",1);
        k.put("q",1);
        k.put("r",1);
        k.put("s",1);
        String leftSide = "gal";
        String rightSide = "goni";
        //start = System.nanoTime();
        String term = leftSide + "-" + rightSide;
        if (term.equals("gal-goni")) System.out.println(term);
        //finish = System.nanoTime();
        //System.out.println((finish-start) * Math.pow(10,-9));
        //start = System.nanoTime();
        if ((leftSide + "-" + rightSide).equals("gal-goni")) System.out.println(leftSide + "-" + rightSide);


        /*
        if (k.containsKey("x")){
            k.put("x",k.get("x")+1);
        }
        System.out.println(k.get("x"));



        boolean a=true, b=true, c=false, d=false, e=true;
        int mask = (a ? 1 : 0) |
                (b ? 2 : 0) |
                (c ? 4 : 0) |
                (d ? 8 : 0) |
                (e ? 16 : 0);

*/

        /*
        String s = "a b c d e f g h i j k l m n o p q r s t u v w x y z";
        start = System.nanoTime();
        String [] h = s.split(" ");
        finish = System.nanoTime();
        System.out.println((finish-start) * Math.pow(10,-9));
        start = System.nanoTime();
        String [] parts = new String[26];
        int length = s.length();
        int offset = 0;
        int part = 0;
        for (int i = 0; i < length; i++) {
            if (i == length - 1 ||
                    s.charAt(i + 1) == ' ') {
                parts[part] =
                        s.substring(offset, i + 1);
                part++;
                offset = i + 2;
            }
        }
        finish = System.nanoTime();
        System.out.println((finish-start) * Math.pow(10,-9));*/

        /*
        System.out.println(numberToTerm(isNumber("123"),false,false,false,false,true));
        System.out.println(numberToTerm(isNumber("1010.56"),false,false,false,false,false));
        System.out.println(numberToTerm(isNumber("10,123,000"),false,false,false,false,false));
        System.out.println(numberToTerm(isNumber("55"),false,false,true,false,false));
        System.out.println(numberToTerm(isNumber("10,123,000,000"),false,false,false,false,false));
        System.out.println(numberToTerm(isNumber("55"),false,true,false,false,false));
        System.out.println(numberToTerm(isNumber("7.56"),false,false,false,true,false));
        System.out.println(numberToTerm(isNumber("1.7320"),true,false,false,false,false));
        System.out.println(numberToTerm(isNumber("450,000"),true,false,false,false,false));
        System.out.println(numberToTerm(isNumber("1,000,000"),true,false,false,false,false));
        System.out.println(numberToTerm(isNumber("1.42"),true,false,false,true,false));
        */






        /*
        long startTime = System.nanoTime();
        Integer.parseInt("04");
        String.valueOf(4);
        long finishTime = System.nanoTime()-startTime;
        System.out.println(finishTime * Math.pow(10,-9));
        Map<String, String> months = new HashMap<>();
        months.put("January", "01");
        startTime = System.nanoTime();
        months.get("January");
        finishTime = System.nanoTime()-startTime;
        System.out.println(finishTime * Math.pow(10,-9));*/







        /*
        BufferedReader in = new BufferedReader(new FileReader("/Users/galvaitzman/IdeaProjects/searchEngine/src/sample/FB396001"));
        String line;
        String doc="";
        List<String> s = new ArrayList<>();

        while((line = in.readLine()) != null)
        {

            if (line.equals("<DOC>")){

            }

        }
        in.close();
        String s = "123,456,789";
        String [] number = s.split(",");
        String t="";
        for (int y = 0; y < number.length; y++){
            t = t + number[y];
        }
        System.out.println(t);

        String y = "gal";
        y=y.substring(1);
        System.out.println(y);
        */
    //String doc = "<text>goni</text>";
    //long startTime = System.nanoTime();
    //doc.substring(doc.indexOf("<text>") + 6, doc.indexOf("</text>"));

    //new ReadFile("/Users/galvaitzman/IdeaProjects/searchEngine/src/reasources");
    //long endTime   = System.nanoTime();
    //System.out.println(endTime - startTime);
    //System.out.println(totalTime * Math.pow(10,-9));
    //String s = "<text>goni</text>";
        /*
        String s2 = "100,005,000,000,000,000";
        String [] number2 = s2.split(",");
        //String str = String.join("", number2);


        boolean onlyzero = false;

        String newNum="";
        for (int i  = 0 ; i < number2.length ; i = i + 1){
            if( i == 1)
                newNum += '.';
            newNum += number2[i];
            System.out.println(number2[i]);
        }
        
        long starttime,finishtime;
        starttime = System.nanoTime();
         newNum=  newNum.indexOf(".") < 0 ? newNum : newNum.replaceAll("0*$", "").replaceAll("\\.$", "");


        System.out.println(newNum);
        int counter = 0;
        boolean stop = false;

        for(int i = newNum.length() - 1 ; !stop && i >= 0 ; i = i - 1)
        {
            if(newNum.charAt(i) == '0')
                counter += 1;
            else if(newNum.charAt(i) == '.')
            {
                stop = true;
                newNum = newNum.substring(0,newNum.length()-counter-1);
            }
            else
            {
                stop = true;
                newNum = newNum.substring(0,newNum.length()-counter);
            }
        }
        System.out.println(newNum);
    }
        
        isr.
        BufferedReader br = new BufferedReader(isr);
        System.out.println(br.readLine());
        String line;

        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }*/




    private void addToTermsInDocMap(String str){
        if (str.charAt(0) == str.toUpperCase().charAt(0)) {
            if (termsInDocMap.get(str.toLowerCase()) == null) {
                if (termsInDocMap.get(str.toUpperCase()) == null)
                    termsInDocMap.put(str.toUpperCase(), 1);
            }
        }
        else  if (str.matches(".*[a-z]+.*"))  {
            if (termsInDocMap.get(str.toUpperCase()) != null) {
                termsInDocMap.put(str.toLowerCase(), 1);
                termsInDocMap.remove(str.toUpperCase());
            }
            else {
                if (termsInDocMap.get(str.toLowerCase()) != null) System.out.println("");
                else termsInDocMap.put(str.toLowerCase(), 1);
            }
        }
    }
    private Double isNumber (String str) {
        try{
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = numberFormat.parse(str);
            double test = number.doubleValue();
            return test;

        }
        catch (ParseException e){
            return null ;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
