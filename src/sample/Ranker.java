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
    Map<String,Integer []> appearancesCountingOfTermsInDoc =new TreeMap<>();// String=docName, String = num of appearences of term 1 from query, num of appearences of term 2 from query...
    Map<String,Integer []> numberOfLineOfTermInDoc = new TreeMap<>();
    int sizeOfIntegerArray = 0;
    int currentIndexInIntegerArray=0;
    public Ranker(boolean isSemantic,String pathOfPostingAndDictionary){
        this.isSemantic =isSemantic;
        this.pathOfPostingAndDictionary = pathOfPostingAndDictionary;
    }

    private Set<String> addTermsWithSameSemanticAndRankingCurrentQueryTerms(Set<String> queryAfterParsing){
        sizeOfIntegerArray = queryAfterParsing.size() * 2;
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
                Thread currentThread = null;
                while (line != null && !termFound){
                    StringBuilder stringBuilder = new StringBuilder();
                    int charAt = 0;
                    while (line.charAt(0) != '^'){
                        stringBuilder.append(line.charAt(charAt));
                        charAt++;
                    }
                    if (stringBuilder.toString().equals(s)){

                        String [] docs = s.split("~");
                        String [] docsInfoOfFirstDoc;
                        docsInfoOfFirstDoc = new String[] {docs[0].split("\\^")[1],docs[0].split("\\^")[2],docs[0].split("\\^")[3]};
                        currentThread = new Thread(()-> {addToTempRankingOfDocuments(docsInfoOfFirstDoc); });
                        currentThread.start();
                        for (int i=1; i<docs.length; i++){
                            try {
                                currentThread.join();
                            }
                           catch (InterruptedException e){e.printStackTrace();}
                            String [] docsInfo = docs[i].split("\\^");
                            currentThread = new Thread(()-> {addToTempRankingOfDocuments(docsInfo); });
                            currentThread.start();

                        }
                        termFound = true;
                    }
                    line = br.readLine();
                }
            }
            catch (IOException e){e.printStackTrace();}
        }
        return null;
    }

    private void addToTempRankingOfDocuments(String [] docsInfo){

        for (String s: docsInfo){
            if (tempRankingOfDocuments.get(docsInfo[0]) != null){
                tempRankingOfDocuments
            }
        }

    }
}
