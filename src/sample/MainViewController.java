package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.text.html.ImageView;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

public class MainViewController extends Application{

    Main main = new Main();
    Stage primaryStage;
    public TextField textBrowseStopWordAndCorpus;
    public TextField textPathToSave;
    public javafx.scene.control.CheckBox checkBoxStem;
    public javafx.scene.image.ImageView imageView;

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

        main.startBuild(checkBoxStem.isSelected(),textBrowseStopWordAndCorpus.getText(),textPathToSave.getText());
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


    public void NewGame(ActionEvent actionEvent) {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        main.start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
        }
    }
    public void exit(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            primaryStage.setOnCloseRequest(e -> Platform.exit());
            System.exit(0);
        }
    }




}
