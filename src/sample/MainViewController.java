package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Stream;

public class MainViewController extends Application{

    Main main = new Main();
    Stage primaryStage;
    public TextField textBrowseStopWordAndCorpus;
    public TextField textPathToSave;
    public javafx.scene.control.CheckBox checkBoxStem;
    public ComboBox<String> comboBoxLanguage;
    public Button buttonStart;
    public  Text lableNumberOfDoc;
    public Text lableNumTerms;
    public Text lableTime;
    public Button buttonLoaDicToMemory;


    @Override
    public void start(Stage primaryStage) throws Exception {
    }

    public void loadDicToMemory(ActionEvent actionEvent) throws IOException {
        if(textPathToSave.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("The path to save the files need to fill");
            alert.showAndWait();
            return;
        }
        if(main.indexer != null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("Dictionary already exist in the memory");
            alert.showAndWait();
            return;
        }


    }


    public void startBuild(ActionEvent actionEvent) {
        long start = System.nanoTime();
        if(textPathToSave.getText().equals("") || textBrowseStopWordAndCorpus.getText().equals("") )
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("Both of the path should be filled");
            alert.showAndWait();
            return;
        }

        main.startBuild(checkBoxStem.isSelected(),textBrowseStopWordAndCorpus.getText(),textPathToSave.getText());
        ObservableList<String> data = FXCollections.observableArrayList(main.readFile.languages);
        comboBoxLanguage.setItems(data);
        double x = (System.nanoTime() - start) * Math.pow(10, -9);
        NumberFormat formatter = new DecimalFormat("#0.00");
        lableTime.setText(formatter.format(x));
        lableNumberOfDoc.setText(main.readFile.numOfDocs+"");
        lableNumTerms.setText(main.indexer.treeMapForfrequentOfTermInCorpus.size()+"");
    }


    public void browse(ActionEvent actionEvent ){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Corpus and Stop word path");
        File selectedFile = dc.showDialog(primaryStage);
        if (selectedFile != null)
            textBrowseStopWordAndCorpus.setText(selectedFile.getPath());
    }
    public void browse2(ActionEvent actionEvent ){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Save");
        File selectedFile = dc.showDialog(primaryStage);
        if (selectedFile != null)
            textPathToSave.setText(selectedFile.getPath());
    }

    public void reset(ActionEvent actionEvent ){
        if(textPathToSave.getText().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("The path to save the files need to fill");
            alert.showAndWait();
            return;
        }
        String path;
        if(checkBoxStem.isSelected())
            path = "/stemmingSearchEngine";
        else
            path = "/notStemmingSearchEngine";
        File dir = new File(textPathToSave.getText()+path);
        File[] listFiles = dir.listFiles();
        for(File file : listFiles){
            if(file.isDirectory()){
                File[] listFiles2 = file.listFiles();
                for(File file2 : listFiles2)
                    file2.delete();
            }
            file.delete();
        }
        File dir2 = new File(textPathToSave.getText()+path);
        dir2.delete();
        main.indexer = null;
        main.readFile = null;
        main.parser = (null);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setContentText("The folder have been deleted");
        alert.showAndWait();
        return;
    }


    public void showDic(ActionEvent actionEvent) throws IOException {
        if(textPathToSave.getText().equals("")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Message");
                alert.setContentText("Please insert the path of the dictionary");
                alert.showAndWait();////
                return;
        }

        FXMLLoader fxmlLoader =new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("showDic.fxml").openStream());
        Stage stage =new Stage(StageStyle.DECORATED);
        stage.setTitle("Show dictionary");
        stage.setScene(new Scene(root));
        ShowDicController showDicController =fxmlLoader.getController();
        if(main.indexer == null)
        {
            String path = "/dictionary.txt";
            if(checkBoxStem.isSelected())
                path = "/stemmingSearchEngine"+path;
            else
                path = "/notStemmingSearchEngine"+path;
        Indexer ind = new Indexer(textPathToSave.getText());
            Map<String, Integer> map = new TreeMap<>();
            BufferedReader br1 = new BufferedReader(new FileReader(textPathToSave.getText()+path));
            String line1 = br1.readLine();
            while (line1 != null ) {
                String[] x = line1.split("  ");
                map.put(x[0],Integer.parseInt(x[2]));
                line1= br1.readLine();
            }
            ind.treeMapForfrequentOfTermInCorpus =  (TreeMap)((map));
            if(ind.treeMapForfrequentOfTermInCorpus.size() != 0){
                showDicController.showDic(ind);
                stage.show();
            }
        }
       else
        if(main.indexer.treeMapForfrequentOfTermInCorpus.size() != 0){
            showDicController.showDic(main.indexer);
            stage.show();
        }
    }

    private void loadDictionaries() throws IOException{
        String dictionary = "/dictionary.txt";
        String currentPath="";
        if(checkBoxStem.isSelected()) {
            dictionary = "/stemmingSearchEngine" + dictionary;
            currentPath = textPathToSave.getText() + "/stemmingSearchEngine";
        }
        else {
            dictionary = "/notStemmingSearchEngine" + dictionary;
            currentPath = textPathToSave.getText() + "/notStemmingSearchEngine";
        }
        /////////////////////////////////////////////////////////
        Map<String, Integer> mapForDoument = new TreeMap<>();
        Map<String, Integer> mapForCorpus = new TreeMap<>();
        Map<String, Integer> mapForLnes = new TreeMap<>();
        BufferedReader br1 = new BufferedReader(new FileReader(textPathToSave.getText()+dictionary));
        String line1 = br1.readLine();
        while (line1 != null ) {
            String[] x = line1.split("  ");
            mapForCorpus.put(x[0],Integer.parseInt(x[2]));
            mapForDoument.put(x[0],Integer.parseInt(x[1]));
            mapForLnes.put(x[0],Integer.parseInt(x[3]));
            line1= br1.readLine();
        }
        BufferedReader br6 = new BufferedReader(new FileReader(currentPath + "/IDF_BM25.txt"));
        String line6 = br6.readLine();
        Map<String, Double> IDF_BM25_Map = new TreeMap<>();
        while (line6 != null ) {
            String[] x = line6.split("\\^");
            IDF_BM25_Map.put(x[0],Double.parseDouble(x[1]));
            line6= br6.readLine();
        }
        main.indexer = new Indexer(textPathToSave.toString());
        main.indexer.treeMapForfrequentOfTermInCorpus = (TreeMap)((mapForCorpus));
        main.indexer.treeMapForDocsPerTerm = (TreeMap)((mapForDoument));
        main.indexer.treeMapForLineNumberInPosting = (TreeMap)((mapForLnes));
        main.indexer.IDF_BM25_Map = IDF_BM25_Map;
        ////////////////////////////////////////////////////////////////////
        /*Set<String> languages = new TreeSet<>();
        BufferedReader br2 = new BufferedReader(new FileReader(currentPath+"/languages.txt"));
        String line2 = br2.readLine();
        while (line2 != null ) {
            languages.add(line2.substring(0,line2.length()-1));
            line2= br2.readLine();
        }
        main.readFile = new ReadFile(textBrowseStopWordAndCorpus.getText(),currentPath);
        main.readFile.languages = languages;*/
        /////////////////////////////////////////////////////////////////////
        Set<String> cities = new TreeSet<>();
        BufferedReader br3 = new BufferedReader(new FileReader(currentPath+"/citiesDetails.txt"));
        String line3 = br3.readLine();
        while (line3 != null ) {
            String[] x = line3.split(",");
            cities.add(x[0]);
            line3= br3.readLine();
        }
        main.parser = new Parse(checkBoxStem.isSelected(),cities,textBrowseStopWordAndCorpus.getText(),currentPath);
        ////////////////////////////////////////////////////////////////////
        Map <String,Integer> numberOfUniqueTermsInDoc = new HashMap<>();  // key = doc, value= מספר המילים הייחודיות במסמך
        Map <String,Integer> numberOfAppearancesOfMostCommonTermInDoc = new HashMap<>(); // key = doc, value = מספר ההופעות של המילה הכי נפוצה במסמך
        Map <String,Integer> numberOfTotalTermsInDoc = new HashMap<>(); //

        BufferedReader br4 = new BufferedReader(new FileReader(currentPath+"/docInfoFrequencyNumberOfUniqueWords.txt"));
        String line4 = br4.readLine();
        while (line4 != null ) {
            String[] x = line4.split(",");
            numberOfAppearancesOfMostCommonTermInDoc.put(x[0],Integer.parseInt(x[1]));
            numberOfUniqueTermsInDoc.put(x[0],Integer.parseInt(x[2]));
            numberOfTotalTermsInDoc.put(x[0],Integer.parseInt(x[3]));
            line4= br3.readLine();
        }
        /////////////////////////////////////////////////////////////////////
        BufferedReader br5 = new BufferedReader(new FileReader(currentPath+"/avdl.txt"));
        String line5 = br5.readLine();
        main.avdl = Double.parseDouble(line5);
        //////////////////////////////////////////////////////////////////////

        main.ranker = new Ranker(currentPath,numberOfUniqueTermsInDoc,numberOfAppearancesOfMostCommonTermInDoc,
                numberOfTotalTermsInDoc,main.indexer,main.readFile,main.parser);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);//
        alert.setTitle("Complete successfully");
        alert.setHeaderText("Complete successfully");
        alert.setContentText("The dictionaries have been loaded");
        alert.showAndWait();

    }
}
