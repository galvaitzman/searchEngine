package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Pair;
import sun.awt.Mutex;
import javafx.scene.image.Image;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Main extends Application {
    public Parse parser;
    public ReadFile readFile;
    public Indexer indexer;
    Stage primaryStage;


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Search engine - Goni and Gal");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Sample.fxml").openStream());
        Scene scene = new Scene(root, 937, 618);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void startBuild(boolean isStemming,String pathOfCorpusAndStopWord , String postingAndDictionary) {


        readFile = new ReadFile(pathOfCorpusAndStopWord);
        readFile.makeCityListAndLanguageList();
        parser = new Parse(pathOfCorpusAndStopWord,isStemming,readFile.detailsOfCities);
        indexer = new Indexer(postingAndDictionary);


        List<Pair<String, String>> readyDocumentsFromReadFile = readFile.documents;

        BufferedWriter bufferWriter1 = null;
        try {
            bufferWriter1 = new BufferedWriter(new FileWriter(postingAndDictionary + "/docInfoCityLanguageHeadLine.txt",true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bufferWriter2 = null;
        try {
            bufferWriter2 = new BufferedWriter(new FileWriter(postingAndDictionary + "/docInfoFrequencyNumberOfUniqueWords.txt",true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        long strt = System.nanoTime();

        int i=0;
        for (; i < readFile.filesInFolder.size() / 50 + 1; i += 1) {
            System.out.println(i + " start");
            readFile.read();
            BufferedWriter finalBufferWriter = bufferWriter1;
            Thread t1 = new Thread(() -> {
                try {
                    finalBufferWriter.write(readFile.stringBuilder.toString());
                } catch (IOException e) {
                }
                System.out.println("finish writer1");
            });
            t1.start();

            parser.startParsing50Files(readyDocumentsFromReadFile);
            readFile.documents.clear();
            parser.termsIndoc.clear();
            BufferedWriter finalBufferWriter1 = bufferWriter2;
            Thread t2 = new Thread(() -> {
                try {
                    finalBufferWriter1.write(parser.docInfo.toString());
                } catch (IOException e) {
                }
                System.out.println("finish writer 2");
            });
            t2.start();

            indexer.index50Files(parser.docsByTerm ,i);
            parser.docsByTerm.clear();
            //
            //}
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        } //5-6 minutes
        try {
            bufferWriter1.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufferWriter2.flush();
        } catch (IOException e) {

        }
        try {
            bufferWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bufferWriter2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        indexer.mergePost("/big"); //8 seconds
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        indexer.mergePost("/small");//40 seconds
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        indexer.mergeBigWithSmall(); //15 sseconds
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        indexer.writeToFinalPosting(); // 1 minute
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
        indexer.writeDictionary();//4 seconds
        System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));

    }

    public static void main(String[] args) {
        launch(args);
    }
}
