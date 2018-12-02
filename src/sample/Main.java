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

        if (isStemming);
        readFile = new ReadFile(pathOfCorpusAndStopWord,postingAndDictionary);
        long strt = System.nanoTime();
        readFile.makeCityListAndLanguageList();
        System.out.println("make city and language:" + (System.nanoTime() - strt) * Math.pow(10, -9));
        parser = new Parse(isStemming,readFile.cities,pathOfCorpusAndStopWord,postingAndDictionary);
        indexer = new Indexer(postingAndDictionary);


        List<Pair<String, String>> readyDocumentsFromReadFile = readFile.documents;



        try {
            BufferedWriter bufferWriter1 = new BufferedWriter(new FileWriter(postingAndDictionary + "/docInfoCityLanguageHeadLine.txt", true));
            BufferedWriter bufferWriter2 = new BufferedWriter(new FileWriter(postingAndDictionary + "/docInfoFrequencyNumberOfUniqueWords.txt", true));

            int i = 0;
            for (; i < readFile.filesInFolder.size() / 50 + 1; i += 1) {
                System.out.println(i + " start");
                readFile.read();

                Thread t1 = new Thread(() -> {
                    try {
                        bufferWriter1.write(readFile.stringBuilder.toString());
                    } catch (IOException e) {
                    }
                });
                t1.start();

                parser.startParsing50Files(readyDocumentsFromReadFile);
                readFile.documents.clear();
                parser.termsIndoc.clear();
                Thread t2 = new Thread(() -> {
                    try {
                        bufferWriter2.write(parser.docInfo.toString());
                    } catch (IOException e) {
                    }

                });
                t2.start();

                indexer.index50Files(parser.docsByTerm, i);
                parser.docsByTerm.clear();
                t1.join();
                t2.join();
                System.out.println((System.nanoTime() - strt) * Math.pow(10, -9));
            } //5-6 minutes
            bufferWriter1.flush();
            bufferWriter2.flush();
            bufferWriter1.close();
            bufferWriter2.close();
            parser.makePostingForCities();
            parser.cities.clear();
            indexer.mergePost("/big"); //8 seconds
            System.out.println("finish big letters" + (System.nanoTime() - strt) * Math.pow(10, -9));
            indexer.mergePost("/small");//40 seconds
            System.out.println("finish small letters" + (System.nanoTime() - strt) * Math.pow(10, -9));
            indexer.mergeBigWithSmall(); //15 sseconds
            System.out.println("finish merging small and big" + (System.nanoTime() - strt) * Math.pow(10, -9));
            indexer.writeToFinalPosting(); // 1 minute
            System.out.println("finish writing to 37 files" + (System.nanoTime() - strt) * Math.pow(10, -9));
            indexer.writeDictionary();//4 seconds
            System.out.println("finish write dictionary" + (System.nanoTime() - strt) * Math.pow(10, -9));

        }
        catch (Exception e) {}


    }

    public static void main(String[] args) {
        launch(args);
    }
}
