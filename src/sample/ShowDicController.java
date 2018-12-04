package sample;

import javafx.application.Application;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;


import java.io.IOException;
import java.util.*;

public class ShowDicController extends Application{
    public TableView tableViewShowDic;
    public TableColumn colTerm;
    public TableColumn colTf;

    @Override
    public void start(Stage primaryStage) throws Exception {
    }

    public void showDic(Indexer indexer) throws IOException {

        TreeMap<String,Integer> dic = indexer.treeMapForfrequentOfTermInCorpus;
        colTerm.setCellValueFactory(new PropertyValueFactory<Term,String>("termName"));

        colTf.setCellValueFactory(new PropertyValueFactory<Term,Integer>("termTf"));

        for( Map.Entry<String,Integer> entry: dic.entrySet())
        {
            String term = entry.getKey();
            Integer tf= entry.getValue();
            Term t= new Term(term,tf);//
            tableViewShowDic.getItems().add(t);
        }
    }
}
