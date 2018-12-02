package sample;

public class Term {
    private String termName;
    private Integer termTf;

    public Term(String name, Integer tf){
        termName = name;
        termTf = tf;
    }

    public String getTermName(){return termName;}
    public  Integer getTermTf(){return  termTf;}

}
