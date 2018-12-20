package sample;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Searcher {

    Ranker ranker;

    public Searcher(){

    }

    public void rankCurrentQuery (Set<String> queryAfterParsing,  Map<String, Integer> cities){
        ranker.addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(queryAfterParsing);
        List<String>result = ranker.rankEveryDocument(cities);
        String s = ranker.dictionary.entities.get(result.get(49));

        try{
            int counter=0;
            BufferedWriter resBufferWriter = new BufferedWriter(new FileWriter("/Users/galvaitzman/IdeaProjects/searchEngine/src/reasources/gal.txt"));
            for (String s2: result) {
                resBufferWriter.append("374" + " 0 " + s2 + " " + counter + " " + 3.0 + " test\n");
                counter++;
            }
            resBufferWriter.flush();
            resBufferWriter.close();
        }
        catch (IOException e){e.printStackTrace();}
        System.out.println("goni");
    }




}
