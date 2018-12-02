package sample;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.awt.Label;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

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

    @Override
    public void start(Stage primaryStage) throws Exception {
      /*  this.primaryStage = primaryStage;
        primaryStage.setTitle("Serach engine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("sample.fxml").openStream());
         Scene scene = new Scene(root, 800, 300);
       // scene.getStylesheets().add(getClass().getResource("WelcomeStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

*/
        //--------------
    }

    public void About(ActionEvent actionEvent) {
      //  new AboutController().start();
    }

 /*   public void browse(ActionEvent actionEvent) {
        //  new AboutController().start();
    }*/

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

        lableTime.setText((System.nanoTime() - start) * Math.pow(10, -9)+"");
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
        File dir = new File(textPathToSave.getText());
        File[] listFiles = dir.listFiles();
        for(File file : listFiles){
            if(file.isDirectory()){
                File[] listFiles2 = file.listFiles();
                for(File file2 : listFiles2)
                    file2.delete();
            }
            file.delete();
        }
    }


    public void showDic(ActionEvent actionEvent) throws IOException {
        Stage s=new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("showDic.fxml"));
        Scene scene = new Scene(root, 727, 618);
        s.setScene(scene);
        s.setTitle("Show dictionary");
        s.setResizable(false);
        s.sizeToScene();
        s.show();

    }
}
