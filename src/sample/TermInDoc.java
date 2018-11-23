package sample;

import java.util.ArrayList;
import java.util.List;

public class TermInDoc {
    public int firstAppearenceInDoc=0;
    public int numberOfOccurencesInDoc = 1;
    public String docName;
    public String term;
    //public List<Integer> indexesOflocationsInDoc = new ArrayList<>();


    public TermInDoc (String docName, String term){
        this.docName = docName;
        this.term = term;
        //this.firstAppearenceInDoc = firstAppearenceInDoc;
    }

    public TermInDoc (TermInDoc other){
       this.firstAppearenceInDoc = other.firstAppearenceInDoc;
       this.docName = other.docName;
       this.numberOfOccurencesInDoc = other.numberOfOccurencesInDoc;
       this.term = other.term;
    }
}
