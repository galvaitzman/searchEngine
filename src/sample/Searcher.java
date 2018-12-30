package sample;

import java.io.*;
import java.util.*;

public class Searcher {

    Ranker ranker;


    public Searcher(){

    }

    /**
     * the function activate the ranker which ranking the current list. at the the end, it returns the 50 documents which was found the most relevant to the query
     * and filtered according to the map of the cities.
     * @param numberOfQuery
     * @param queryAfterParsing
     * @param cities
     * @return
     */
    public List<String> rankCurrentQuery (String numberOfQuery,Set<String> queryAfterParsing,  Map<String, Integer> cities, String patToSaveResults){
        List<String> list = new ArrayList<>();
        ranker.addTermsWithSameSemanticAndTempRankingCurrentQueryTerms(queryAfterParsing);
        List<String>result = ranker.rankEveryDocument(cities);
        try{
            int counter=0;
            BufferedWriter resBufferWriter = new BufferedWriter(new FileWriter(patToSaveResults + "/results.txt",true));
            for (String s2: result) {
                list.add(s2);
                resBufferWriter.append(numberOfQuery + " 0 " + s2 + " " + counter + " " + 3.0 + " test\n");
                counter++;
            }
            resBufferWriter.flush();
            resBufferWriter.close();

        }
        catch (IOException e){e.printStackTrace();}
        return list;

    }




}
