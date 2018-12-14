package sample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

public class Ranker {
    private boolean isSemantic;
    private String pathOfPostingAndDictionary;
    Set<String> rankingOfDocuments =new TreeSet<>();
    public Ranker(boolean isSemantic,String pathOfPostingAndDictionary){
        this.isSemantic =isSemantic;
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
        try {
            BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(pathOfPostingAndDictionary + "/"), StandardCharsets.UTF_8));
        }
        catch (IOException e){e.printStackTrace();}

    }
    public void
    public Set<String> setOfWordsWithSameSemantic(Set<String> queryAfterParsing){

    }
}
