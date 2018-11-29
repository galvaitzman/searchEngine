package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class MainViewController extends Application{

    Main main = new Main();
    Stage primaryStage;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Welcome to World-cup Maze");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Welcome.fxml").openStream());
         Scene scene = new Scene(root, 800, 300);
       // scene.getStylesheets().add(getClass().getResource("WelcomeStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();


        //--------------
    }

    public void About(ActionEvent actionEvent) {
      //  new AboutController().start();
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
