package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import sun.awt.Mutex;
import javafx.scene.image.Image;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Main extends Application {
    public Parse parser;
    public ReadFile readFile;
    public Indexer indexer;
    public Ranker ranker;
    public Searcher searcher;
    Stage primaryStage;


    @Override
    public void start(Stage primaryStage) throws Exception {


        this.primaryStage = primaryStage;
        primaryStage.setTitle("Search engine - Goni and Gal");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("sample.fxml").openStream());
        Scene scene = new Scene(root, 937, 618);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void startBuild(boolean isStemming,String pathOfCorpusAndStopWord , String postingAndDictionary) {

        if (isStemming){
            postingAndDictionary = postingAndDictionary + "/stemmingSearchEngine";
            new File(postingAndDictionary).mkdirs();
        }
        else{
            postingAndDictionary = postingAndDictionary + "/notStemmingSearchEngine";
            new File(postingAndDictionary).mkdirs();
        }

        readFile = new ReadFile(pathOfCorpusAndStopWord,postingAndDictionary);
        readFile.makeCityListAndLanguageList();
        parser = new Parse(isStemming,readFile.cities,pathOfCorpusAndStopWord,postingAndDictionary);
        indexer = new Indexer(postingAndDictionary);
        //parser.QueryParser("Nobel prize winners");



        List<Pair<String, String>> readyDocumentsFromReadFile = readFile.documents;



        try {
            BufferedWriter bufferWriter1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(postingAndDictionary + "/docInfoCityLanguageHeadLine.txt",true), StandardCharsets.UTF_8));
            BufferedWriter bufferWriter2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(postingAndDictionary + "/docInfoFrequencyNumberOfUniqueWords.txt",true), StandardCharsets.UTF_8));

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
            } //5-6 minutes
            bufferWriter1.flush();
            bufferWriter2.flush();
            bufferWriter1.close();
            bufferWriter2.close();
            parser.makePostingForCities();//
            parser.cities.clear();
            indexer.mergePost("/big");
            indexer.mergePost("/small");
            indexer.mergeBigWithSmall();
            indexer.writeToFinalPosting();
            indexer.writeDictionary();
            indexer.IDFForBM25();

        }
        catch (Exception e) {}


    }

    public static void main(String[] args) {
        launch(args);
    }
}
