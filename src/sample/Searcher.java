package sample;

import java.util.HashSet;

public class Searcher {

    Parse parse;

    public Searcher(Parse p){
     parse = p;

    }



    public void resolveQuery(String query){
        HashSet<String> queryTerms = parse.QueryParser(query);
    }
}
