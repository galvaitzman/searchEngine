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
import org.controlsfx.control.CheckComboBox;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

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
    public CheckComboBox <String> comboBoxCities;
    public javafx.scene.control.CheckBox checkBoxSemantic;
    public TextField textBoxQuery;
    public TextField textBoxQueryPath;
    public ListView listViewDocs;
    public ListView listViewEntity;




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
            //return;
        }
        loadDictionaries();


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

    public void browseQuery(ActionEvent actionEvent ){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Path of the queries");
        File selectedFile = dc.showDialog(primaryStage);
        if (selectedFile != null)
            textBoxQueryPath.setText(selectedFile.getPath());
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
        Dictionary dictionary = new Dictionary();

            String dictionaryPath;
            String currentPath = "";
            if (checkBoxStem.isSelected()) {
                dictionaryPath = "/stemmingSearchEngine/dictionary.txt";
                currentPath = textPathToSave.getText() + "/stemmingSearchEngine";
            } else {
                dictionaryPath = "/notStemmingSearchEngine/dictionary.txt";
                currentPath = textPathToSave.getText() + "/notStemmingSearchEngine";
            }

        if(main.indexer != null) {
            dictionary.treeMapForfrequentOfTermInCorpus = main.indexer.treeMapForfrequentOfTermInCorpus;
            dictionary.treeMapForDocsPerTerm = main.indexer.treeMapForDocsPerTerm;
            dictionary.treeMapForLineNumberInPosting = main.indexer.treeMapForLineNumberInPosting;
            dictionary.IDF_BM25_Map = main.indexer.IDF_BM25_Map;
        }
        else{
            Map<String, Integer> mapForDoument = new TreeMap<>();
            Map<String, Integer> mapForCorpus = new TreeMap<>();
            Map<String, Integer> mapForLnes = new TreeMap<>();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(textPathToSave.getText() + dictionaryPath), StandardCharsets.UTF_8));
            String line1 = br1.readLine();
            while (line1 != null) {
                String[] x = line1.split("  ");
                mapForCorpus.put(x[0], Integer.parseInt(x[2]));
                mapForDoument.put(x[0], Integer.parseInt(x[1]));
                mapForLnes.put(x[0], Integer.parseInt(x[3]));
                line1 = br1.readLine();
            }
            BufferedReader br6 = new BufferedReader(new FileReader(currentPath + "/IDF_BM25.txt"));
            String line6 = br6.readLine();
            Map<String, Double> IDF_BM25_Map = new TreeMap<>();
            while (line6 != null) {
                String[] x = line6.split("\\^");
                IDF_BM25_Map.put(x[0], Double.parseDouble(x[1]));
                line6 = br6.readLine();
            }

            dictionary.treeMapForfrequentOfTermInCorpus = (TreeMap) ((mapForCorpus));
            dictionary.treeMapForDocsPerTerm = (TreeMap) ((mapForDoument));
            dictionary.treeMapForLineNumberInPosting = (TreeMap) ((mapForLnes));
            dictionary.IDF_BM25_Map = IDF_BM25_Map;
        }
        //gal remove
        /*
        main.indexer = new Indexer(textPathToSave.toString());
        main.indexer.treeMapForfrequentOfTermInCorpus = (TreeMap)((mapForCorpus));
        main.indexer.treeMapForDocsPerTerm = (TreeMap)((mapForDoument));
        main.indexer.treeMapForLineNumberInPosting = (TreeMap)((mapForLnes));
        main.indexer.IDF_BM25_Map = IDF_BM25_Map;
*/


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
        Map <String,Double> weightOfDocNormalizedByLengthOfDoc = new HashMap<>();
        Map <String,Double> weightOfDocNormalizedByMostCommonWordInDoc = new HashMap<>();
        Map <String,Integer> numberOfLinesInDoc = new HashMap<>();

        BufferedReader br4 = new BufferedReader(new FileReader(currentPath+"/docInfoFrequencyNumberOfUniqueWords.txt"));
        String line4 = br4.readLine();
        while (line4 != null ) {
            String[] x = line4.split(",");
            numberOfAppearancesOfMostCommonTermInDoc.put(x[0],Integer.parseInt(x[1]));
            numberOfUniqueTermsInDoc.put(x[0],Integer.parseInt(x[2]));
            numberOfTotalTermsInDoc.put(x[0],Integer.parseInt(x[3]));
            weightOfDocNormalizedByLengthOfDoc.put(x[0],Double.parseDouble(x[5]));
            weightOfDocNormalizedByMostCommonWordInDoc.put(x[0],Double.parseDouble(x[4]));
            numberOfLinesInDoc.put(x[0],Integer.parseInt(x[6]));

            line4= br4.readLine();
        }

        /////////////////////////////////////////////////////////////////////
        BufferedReader br5 = new BufferedReader(new FileReader(currentPath+"/avdl.txt"));
        String line5 = br5.readLine();
        main.avdl = Double.parseDouble(line5);

        //////////////////////////////////////////////////////////////////////
        Map <String,String> cityOfDoc = new HashMap<>();
        BufferedReader br7 = new BufferedReader(new FileReader(currentPath+"/docInfoCityLanguageHeadLine.txt"));
        String line7 = br7.readLine();
        while (line7 != null){
            String [] x = line7.split(",");
            if (x.length > 1){
                cityOfDoc.put(x[0],x[1]);
            }
            line7 = br7.readLine();

        }
        ///////////////////////////////////////////////////////////////////////
        Map <String,Set<String>> cityInDoc = new HashMap<>(); // key = docName, value = cities in doc
        BufferedReader br8 = new BufferedReader(new FileReader(currentPath+"/citiesPosting.txt"));
        String line8 = br8.readLine();
        for (String s: cities){
            if (line8.equals("No appearences of this city in the corpus\n"))continue;
            String [] x = line8.split("~");
            for (String st:x){
                if (cityInDoc.get(st.split(",")[0]) == null)
                    cityInDoc.put(st.split(",")[0],new HashSet<>());
                cityInDoc.get(st.split(",")[0]).add(s);
            }
            line8 = br8.readLine();
            if (line8 == null) break;
        }
        /////////////////////////////////////////////////////////////////////
        Map <String,String> entities = new HashMap<>();
        BufferedReader br9 = new BufferedReader(new FileReader(currentPath+"/entitiesOfDoc.txt"));
        String line9 = br9.readLine();
        while (line9 != null){
            String [] x = line9.split("~");
            if (x.length>1) entities.put(x[0],x[1]);
            else entities.put(x[0],"no entities in this document");
            line9 = br9.readLine();
        }
        //////////////////////////////////////////////////////////////////////


        dictionary.numberOfUniqueTermsInDoc = numberOfUniqueTermsInDoc;
        dictionary.numberOfAppearancesOfMostCommonTermInDoc = numberOfAppearancesOfMostCommonTermInDoc;
        dictionary.numberOfTotalTermsInDoc = numberOfTotalTermsInDoc;
        dictionary.weightOfDocNormalizedByLengthOfDoc = weightOfDocNormalizedByLengthOfDoc;
        dictionary.weightOfDocNormalizedByMostCommonWordInDoc = weightOfDocNormalizedByMostCommonWordInDoc;
        dictionary.cityOfDoc = cityOfDoc;
        dictionary.cityInDoc = cityInDoc;
        dictionary.entities=entities;
        dictionary.numberOfLinesInDoc = numberOfLinesInDoc;
     //   dictionary.queries = queries;



        main.searcher = new Searcher();
        main.searcher.ranker = new Ranker(currentPath,dictionary);
/*        main.ranker = new Ranker(currentPath,numberOfUniqueTermsInDoc,numberOfAppearancesOfMostCommonTermInDoc,
                numberOfTotalTermsInDoc,main.indexer,main.readFile,main.parser,weightOfDocNormalizedByLengthOfDoc,weightOfDocNormalizedByMostCommonWordInDoc);*/
        Alert alert = new Alert(Alert.AlertType.INFORMATION);//
        alert.setTitle("Complete successfully");
        alert.setHeaderText("Complete successfully");
        alert.setContentText("The dictionaries have been loaded");
        alert.showAndWait();
        ObservableList<String> data = FXCollections.observableArrayList(cities);
        comboBoxCities.getItems().addAll(data);




        // and listen to the relevant events (e.g. when the selected indices or
        // selected items change).
        /*checkComboBox.getCheckModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                System.out.println(checkComboBox.getCheckModel().getSelectedItems());
            }
        });
    }*/





    }

    public void startQuery(ActionEvent actionEvent) throws IOException {
        listViewDocs.setVisible(false);
        listViewDocs.getItems().clear();
        listViewEntity.setVisible(false);
        listViewEntity.getItems().clear();
        if (textBoxQuery.getText().equals("") && textBoxQueryPath.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("One of the query text need to be filled");
            alert.showAndWait();
            return;
        }

        if (!textBoxQuery.getText().equals("") && !textBoxQueryPath.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message");
            alert.setContentText("Just one of the query text need to be filled");
            alert.showAndWait();
            return;
        }

        Map<String, Integer> cities = new HashMap<>();
        ObservableList<Integer> data2 = comboBoxCities.getCheckModel().getCheckedIndices();
        for (Integer i : data2) {
            cities.put(comboBoxCities.getItems().get(i), i);
        }

        long start = System.nanoTime();

        if (!textBoxQueryPath.getText().equals("")) {
            //   Map<String, String> queries = new HashMap<>();
            BufferedReader br10 = new BufferedReader(new FileReader(textBoxQueryPath.getText() + "/queries.txt"));
            String line10 = br10.readLine();
            String numberOfQuery = "";
            String title = "";
            String description="";
            while (line10 != null) {
                if (line10.contains("<num> Number:")) {
                    numberOfQuery = line10.substring(line10.indexOf("<num> Number:") + 14);
                }
                else if (line10.contains("<title>")) {
                    title = line10.substring(line10.indexOf("<title>") + 8);
                }
                else if (line10.contains("Description")){
                    line10 = br10.readLine();
                    while (!line10.contains("Narrative")){
                        description = description + line10;
                        line10 = br10.readLine();
                    }
                }
                if (!title.equals("") && !numberOfQuery.equals("")) {
                    if (numberOfQuery.endsWith(" ") || numberOfQuery.endsWith("\n"))
                        numberOfQuery = numberOfQuery.substring(0, numberOfQuery.length() - 1);
                    if (title.endsWith(" ") || title.endsWith("\n")) title = title.substring(0, title.length() - 1);
                    main.searcher.rankCurrentQuery(numberOfQuery, main.parser.QueryParser(title,description, checkBoxSemantic.isSelected()), cities);
                    //  queries.put(number, title);
                    numberOfQuery = "";
                    title = "";
                }
                System.out.println(numberOfQuery);
                line10 = br10.readLine();
            }
        } else {
            List<String> list  = main.searcher.rankCurrentQuery("-1", main.parser.QueryParser(textBoxQuery.getText(), "",checkBoxSemantic.isSelected()), cities);
            Collections.reverse(list);
            int counter = 1;
            for(String s : list ) {
                listViewDocs.getItems().add(counter + ". " + s);
                counter += 1;
            }
            listViewDocs.setVisible(true);
        }
        System.out.println((System.nanoTime() - start) * Math.pow(10, -9));

        cmd();
        writeToCSV();

    }


    private void cmd() {
        String[] command = { "cmd" };
        String path = "C:\\Users\\gvaitzma\\IdeaProjects"; // insert here your path to directory !
        Process p;
        try{
            p= Runtime.getRuntime().exec(command);
            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println("cd "+ path);
            stdin.println("treceval -q qrels.txt results.txt > output.txt");

            stdin.close();
            p.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeToCSV() {
        String readFromFile="C:\\Users\\gvaitzma\\IdeaProjects\\output.txt"; // insert here your path to output.txt
        String writeToFile = "C:\\Users\\gvaitzma\\IdeaProjects\\Ans.csv"; // insert here your path to Ans.csv
        File file = new File(readFromFile);
        if(file.exists()){
            // first read
            BufferedReader br = null;
            FileReader fr = null;

            BufferedWriter bw = null;
            FileWriter fw = null;
            StringBuilder sb = new StringBuilder();

            try {
                fr = new FileReader(readFromFile);
                br = new BufferedReader(fr);
                fw= new FileWriter(writeToFile);
                bw = new BufferedWriter(fw);
                bw.write("ID,Retrieved,Relevant,Rel_ret\n");
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.startsWith("Queryid")){
                        String ID=line.substring(20);
                        sb.append(ID);
                        sb.append(",");
                        continue;
                    }
                    if(line.contains("Retrieved:")){
                        String Retrieved=line.substring(21);
                        if(Retrieved.startsWith(" ")){
                            Retrieved = Retrieved.substring(1);
                        }
                        sb.append(Retrieved);
                        sb.append(",");
                        continue;
                    }
                    if(line.contains("Relevant:")){
                        String Relevant=line.substring(20);
                        if(Relevant.startsWith(" ")){
                            Relevant = Relevant.substring(1);
                        }
                        sb.append(Relevant);
                        sb.append(",");
                        continue;
                    }
                    if(line.contains("Rel_ret:")){
                        String Rel_ret=line.substring(20);
                        if(Rel_ret.startsWith(" ")){
                            Rel_ret = Rel_ret.substring(1);
                        }
                        sb.append(Rel_ret);
                        sb.append("\n");
                        bw.write(sb.toString());
                        sb=new StringBuilder();

                    }
                }



            } catch (IOException e) {

            } finally {
                try {
                    if (br != null)
                        br.close();

                    if (fr != null)
                        fr.close();

                    if (bw != null)
                        bw.close();

                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                }
            }
        }
    }



    class SyncPipe implements Runnable
    {
        public SyncPipe(InputStream istrm, OutputStream ostrm) {
            istrm_ = istrm;
            ostrm_ = ostrm;
        }
        public void run() {
            try
            {
                final byte[] buffer = new byte[1024];
                for (int length = 0; (length = istrm_.read(buffer)) != -1; )
                {
                    ostrm_.write(buffer, 0, length);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        private final OutputStream ostrm_;
        private final InputStream istrm_;
    }
}
