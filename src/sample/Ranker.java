package sample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Ranker {
    private boolean isSemantic;
    private String pathOfPostingAndDictionary;
    Map<String,String> rankingOfDocuments =new TreeMap<>(); // String=docName, String = num of appearences of term 1, num of appearences of term 2...
    public Ranker(boolean isSemantic,String pathOfPostingAndDictionary){
        this.isSemantic =isSemantic;
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/docInfoFrequencyNumberOfUniqueWords.txt"), StandardCharsets.UTF_8));
        }
        catch (IOException e){e.printStackTrace();}

    }

    private Set<String> addTermsWithSameSemanticAndRanking(Set<String> queryAfterParsing){
        Set <String> setOfTermsWithSamrSemantic = new TreeSet<>();
        for (String s:queryAfterParsing){
            int startingChar = s.charAt(0);
            int numOfPosting=0;
            if (startingChar>=65 && startingChar<=90) numOfPosting = startingChar - 54;
            else if (startingChar>=97 && startingChar<=122) numOfPosting = startingChar - 86;
            else if (startingChar>=48 && startingChar<=57) numOfPosting = startingChar - 47;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/" + numOfPosting + ".txt"), StandardCharsets.UTF_8));
                String line = br.readLine();
                boolean termFound = false;
                while (line != null && !termFound){
                    StringBuilder stringBuilder = new StringBuilder();
                    int charAt = 0;
                    while (line.charAt(0) != '^'){
                        stringBuilder.append(line.charAt(charAt));
                        charAt++;
                    }
                    if (stringBuilder.toString().equals(s)){
                        String [] docs = s.split("~");
                        for (int i=0; i<docs.length; i++){
                            String [] docsInfo = docs[i].split("\\^");

                        }
                        termFound = true;
                    }
                    line = br.readLine();
                }
            }
            catch (IOException e){e.printStackTrace();}
        }
        return setOfTermsWithSamrSemantic;
    }
}
