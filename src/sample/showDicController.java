package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class showDicController extends Application{

    Main main = new Main();
    Stage primaryStage;
    public TableView tableViewShowDic;

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

    public void showDic(ActionEvent actionEvent) throws IOException {
        /*
        TableColumn term = new TableColumn("Term");
        TableColumn number = new TableColumn("Number of appearance in corpus");
        tableViewShowDic.getColumns().addAll(term,number);

        for (Map.Entry<String,Integer> insideEntry : main.indexer.numberOfDocsPerTerm.entrySet())
        {
            insideEntry.getKey()
        }
        final ObservableList<String[]> e = FXCollections.observableList(keyList);
        /*
        List<String> keyList = new ArrayList<String>();
        keyList.addAll(main.indexer.numberOfDocsPerTerm.keySet());

        List valueList = new ArrayList(main.indexer.numberOfDocsPerTerm.values());

        ObservableList<String> names = FXCollections.observableList(keyList);

        tableViewShowDic.setItems(names);
        tableViewShowDic.
      //  final ObservableMap<String, Integer> obsMap = FXCollections.observableHashMap();

//        obsMap.addListener(main.indexer.numberOfDocsPerTerm);


  //      tableViewShowDic.getColumns().addAll();
  */
    }
}
