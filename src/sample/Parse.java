package sample;

import javafx.util.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import javax.management.Query;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Parse {

    boolean firstword=false;
    int counterForEntities=0;
    public long totalLengthOfAllDocumentsNotIncludingStopWords=0;
    private String pathOfCorpusAndStopWord;
    private String postingAndDictionary;
    private   Map<String, String> months;
    private   HashSet<String> stopWords;
    private Map<String,String> numberMonths;
    private int currentLine;
    private Stemmer stemmer;
    private int jumpToNextWord = 0;
    private Map <String,Integer> mapOfEntitiesLineNumber;
    private Map <String,Integer> mapOfEntitiesCounter;
    public Set <String> citiesList;
    public StringBuilder docInfo = new StringBuilder();
    public StringBuilder entitiesInDoc = new StringBuilder();
    public Map<String, Map<String, Double>> docsByTerm = new HashMap<>(); // key = term , value = { key = doc id , value = number of appearance in specific doc . first appearence in doc }
    public Map<String, Integer> termsIndoc = new HashMap<>();// key = doc id , value = <term, tf>
    public boolean isStemming;
    public Map<String,Map <String,String>> cities = new TreeMap<>(); // key = city , value = { key = doc id , value = indexes of city in doc }



    private boolean isQuery;
    private String docName;
    private Set<String> queryTerms ;



    public Parse (boolean isStemming , Set<String> citiesList, String pathOfCorpusAndStopWord, String postingAndDictionary ){
        this.citiesList = citiesList;
        this.pathOfCorpusAndStopWord = pathOfCorpusAndStopWord;
        this.postingAndDictionary = postingAndDictionary;
        this.isStemming = isStemming;
        stemmer = new Stemmer();
        months = new HashMap<>();
        stopWords = new HashSet<>();
        numberMonths = new HashMap<>();
        for (String s: citiesList){
            cities.put(s,new TreeMap<>());
        }
        BufferedReader in = null;////
        try{
            String currentWord;
            in = new BufferedReader(new FileReader(pathOfCorpusAndStopWord + "/stop_words.txt"));
            while ((currentWord = in.readLine())!= null )  {
                stopWords.add(currentWord);
                currentWord = currentWord.substring(0,1).toUpperCase() + currentWord.substring(1);
                stopWords.add(currentWord);
            }
            stopWords.remove("between");
            stopWords.remove("Between");
        }
        catch (IOException e){
            System.out.println("IOException");
        }
        months.put("January", "01"); months.put("February", "02"); months.put("March", "03"); months.put("April", "04");months.put("May", "05");months.put("June", "06");months.put("July", "07");months.put("August", "08");months.put("September", "09");months.put("October", "10");months.put("November", "11");months.put("December", "12");
        months.put("JANUARY", "01"); months.put("FEBRUARY", "02"); months.put("MARCH", "03"); months.put("APRIL", "04");months.put("MAY", "05");months.put("JUNE", "06");months.put("JULY", "07");months.put("AUGUST", "08");months.put("SEPTEMBER", "09");months.put("OCTOBER", "10");months.put("NOVEMBER", "11");months.put("DECEMBER", "12");
        months.put("Jan", "01");     months.put("Feb", "02");      months.put("Mar", "03");   months.put("Apr", "04");  months.put("Jun", "06");months.put("Jul", "07");months.put("Aug", "08");months.put("Sep", "09");months.put("Oct", "10");months.put("Nov", "11");months.put("Dec", "12");
        numberMonths.put("01","JANUARY");
        numberMonths.put("02","FEBRUARY");
        numberMonths.put("03","MARCH");
        numberMonths.put("04","APRIL");
        numberMonths.put("05","MAY");
        numberMonths.put("06","JUNE");
        numberMonths.put("07","JULY");
        numberMonths.put("08","AUGUST");
        numberMonths.put("09","SEPTEMBER");
        numberMonths.put("10","OCTOBER");
        numberMonths.put("11","NOVEMBER");
        numberMonths.put("12","DECEMBER");
    }



    /**
     * get Map with 50 value -the map include key= name doc ,value= text of the doc
     * the function send every doc to  parsingTextToText function.
     * @param mapOfDocs
     */
    public void startParsing50Files(List<Pair <String, String>> mapOfDocs){
        docInfo = new StringBuilder();
        entitiesInDoc = new StringBuilder();
        for (int i=0; i<mapOfDocs.size(); i++){
            Pair currentDoc = mapOfDocs.get(i);
            docName = (String)currentDoc.getKey();
            String doc = (String)currentDoc.getValue();
            entitiesInDoc.append(docName + "~");
            counterForEntities=0;
            mapOfEntitiesCounter = new HashMap<>();
            mapOfEntitiesLineNumber = new HashMap<>();
            parsingTextToText(doc);
        }

    }





    /**
     * Adding term to the maps(docsByTerm,termsIndoc) after changing to lowercase/uppercase
     * @param str

     */
    private void directAddingTerm(String str,int currentIndexInDoc) {
        if(isQuery)
        {
            if(!queryTerms.contains(str))
                queryTerms.add(str);
            return;
        }

        if(citiesList.contains(str)){
            if (cities.get(str)  == null){
                cities.put(str,new TreeMap<>());
                cities.get(str).put(docName,String.valueOf(currentIndexInDoc));
            }
            else if (cities.get(str).get(docName) == null ){
                cities.get(str).put(docName,String.valueOf(currentIndexInDoc));
            }
            else{
                cities.get(str).put(docName, cities.get(str).get(docName) + "," + String.valueOf(currentIndexInDoc));
            }
        }
        if (docsByTerm.get(str) == null) {
            Map<String, Double> docs = new HashMap<>();
            docs.put(docName, Double.parseDouble("1." + String.valueOf(currentLine)+"1"));
            docsByTerm.put(str, docs);
        }
        else {
            if (docsByTerm.get(str).get(docName) == null)
                docsByTerm.get(str).put(docName, Double.parseDouble("1." + String.valueOf(currentLine)+"1"));
            else{
                String [] splitDouble = String.valueOf(docsByTerm.get(str).get(docName)).split("\\.");
                int leftSide = Integer.parseInt(splitDouble[0])+1;
                Double doubleToPut = Double.parseDouble(String.valueOf(leftSide) + "." + splitDouble[1]);
                docsByTerm.get(str).put(docName,doubleToPut);
            }
        }
        if (termsIndoc.get(str) == null) termsIndoc.put(str, 1);
        else termsIndoc.put(str,termsIndoc.get(str) + 1);

    }

    /**
     * function that deal with lowercase/uppercase rules and send to directAddingTerm to add the term to the maps
     * @param str
     * @param isNumber
     */
    public void addToterms(String str, boolean isNumber,int currentIndexInDoc){
        if (str.length()>0){
            if (str.endsWith("'")) str = str.substring(0,str.length()-1);
        }
        if (str.length() == 0) return;
        if (isNumber || isQuery) {
            directAddingTerm(str,currentIndexInDoc);
        }
        else if (str.charAt(0)>=65 && str.charAt(0)<=90){

            if (docsByTerm.get(str.toUpperCase()) != null) str = str.toUpperCase();
            else if (docsByTerm.get(str.toLowerCase()) != null) str = str.toLowerCase();
            else str = str.toUpperCase();
            if (str.charAt(0)>=65 && str.charAt(0)<=90 && !stopWords.contains(str.toLowerCase())) {
                if (!str.contains(":")) {
                    if (mapOfEntitiesLineNumber.get(str) == null) {
                        counterForEntities++;
                        mapOfEntitiesLineNumber.put(str, currentLine);
                        mapOfEntitiesCounter.put(str, 1);

                    } else {
                        mapOfEntitiesCounter.put(str, mapOfEntitiesCounter.get(str) + 1);
                    }
                }
            }
            directAddingTerm(str,currentIndexInDoc);
        }
        else if (str.matches(".*[a-z]+.*")) {
            if (docsByTerm.get(str.toUpperCase()) != null) {
                mapOfEntitiesLineNumber.remove(str.toUpperCase());
                mapOfEntitiesCounter.remove(str.toUpperCase());
                docsByTerm.put(str.toLowerCase(),docsByTerm.remove(str.toUpperCase()));
                if (termsIndoc.get(docName) != null){
                    if(termsIndoc.get(str.toUpperCase()) != null){
                        termsIndoc.put(str.toLowerCase(),termsIndoc.remove(str.toUpperCase()));
                    }
                }
            }//
            directAddingTerm(str.toLowerCase(),currentIndexInDoc);
        }
        else return;
    }

    /**
     * Check if the string is number
     * @param str
     * @return number / null
     */
    private Double isNumber (String str) {
        if (str.length() == 0) return null;
        else if (months.containsKey(str)) return Double.valueOf(months.get(str));
        else if (str.equals("million") || str.equals("Million")) return 1000000.0;
        else if (str.equals("billion") || str.equals("Billion")) return 1000000000.0;
        else if (str.equals("trillion") || str.equals("Trillion")) return 1000000000000.0;
        if (!Character.isDigit(str.charAt(0))) return null;
        for (int i=1; i<str.length(); i++){
            if (!Character.isDigit(str.charAt(i)) && str.charAt(i) != ',' && str.charAt(i) != '.') return null;
        }

        try{
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = numberFormat.parse(str);
            double test = number.doubleValue();
            return test;

        }
        catch (ParseException e){//
            return null ;
        }
    }

    /**
     * getting the number the properties that we take from the symbol that was part of the number and check the next words to identify the term
     * @param number
     * @param i
     * @param length
     * @param nextword
     * @param onlyTextFromText
     * @param isBillionAsWord
     * @param isDollar
     * @param percent
     * @param fraction
     * @param isBillion
     * @param isMillion
     * @param betweenAsWord
     * @return
     */
    private String dealWithNumbers(Double number, int i, int length, String nextword, String[]onlyTextFromText,
                                   boolean isBillionAsWord, boolean isDollar, String percent, String fraction, boolean isBillion, boolean isMillion, boolean betweenAsWord) {
        boolean isThousand = false;
        boolean isMillionAsWord = false;
        boolean isTrillion = false;
        boolean isDollarAsWord = false;
        boolean isKilogram = false;
        boolean isGram = false;
        //boolean isUS = false;
        //boolean isPercent = false;
        //boolean andAsWord = false;
        String and="";
        String between = "";
        String rightSide = "";
        String makaf = "";
        if (number != null) {
            boolean stopLoop = false;
            int nextWordIndex = i + 1;
            while (!stopLoop && nextWordIndex < length) {
                nextword = onlyTextFromText[nextWordIndex];
                if (!nextword.equals("U.S.")){
                    if (nextword.endsWith(",") || nextword.endsWith(".") || nextword.endsWith(":") || nextword.endsWith(";") || nextword.endsWith("-") || nextword.endsWith("?") || nextword.endsWith("?")){
                        stopLoop = true;
                        nextword = nextword.substring(0,nextword.length()-1);
                    }
                }
                if (nextword.contains("-") && nextword.charAt(0) != '-' && nextword.charAt(nextword.length()-1) != '-'){
                    String [] split = nextword.split("-");
                    nextword = split[0];
                    makaf = "-";
                    rightSide = dealWithNumbers(isNumber(split[1]),i+1,length,nextword,onlyTextFromText,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
                }
                if (nextword.equals("and") && betweenAsWord){
                    rightSide = dealWithNumbers(isNumber(onlyTextFromText[nextWordIndex+1]),nextWordIndex+1,length,nextword,onlyTextFromText,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
                    and = "-";
                    //andAsWord = true;
                    jumpToNextWord++;
                }
                else if(nextword.equals("kilogram") ||nextword.equals("Kilogram") ||nextword.equals("Kg") || nextword.equals("kg"))
                    isKilogram = true;
                else if(nextword.equals("Gram") ||nextword.equals("gram") ||nextword.equals("g") )
                    isGram = true;
                else if (nextword.equals("Thousand") || nextword.equals("thousand")) {
                    isThousand = true;
                } else if (nextword.equals("million") || nextword.equals("Million") || nextword.equals("m") || nextword.equals("M")) {
                    isMillionAsWord = true;
                } else if (nextword.equals("billion") || nextword.equals("Billion") || nextword.equals("bn")) {
                    isBillionAsWord = true;
                } else if (nextword.equals("trillion") || nextword.equals("Trillion")) {
                    isTrillion = true;
                } else if ((nextword.equals("Dollars") || nextword.equals("dollars") || nextword.equals("Dollar") || nextword.equals("dollar")) && !isDollar) {
                    isDollarAsWord = true;
                }
                else if (nextword.equals("U.S.")) {
                    //isUS = true;
                } else if (nextword.equals("percent") || nextword.equals("percentage") || nextword.equals("Percent") || nextword.equals("Percentage")) {
                    percent = "%";
                    //isPercent = true;
                } else if (nextword.contains("/")) {
                    int indexOf = nextword.indexOf('/');
                    if ((isNumber(nextword.substring(0, indexOf)) != null) && (isNumber(nextword.substring(indexOf + 1, nextword.length())) != null)) {
                        fraction = " " + nextword;
                    }

                } else if (months.containsKey(nextword)) {
                    try {
                        if (number >= 1 && number <= 31) {
                            jumpToNextWord+=1;
                            if (number>=1 && number<=9) return months.get(nextword) + "-0" + String.valueOf(number.intValue());
                            else return months.get(nextword) + "-" + String.valueOf(number.intValue());
                        }
                    } catch (NumberFormatException e) {
                    }

                }
                else {stopLoop = true; continue;}
                nextWordIndex++;
                jumpToNextWord++;
            }
        }
        //if (isBillionAsWord || isMillionAsWord || isTrillion || isThousand) jumpToNextWord+=1;
        //if (isDollarAsWord) jumpToNextWord+=1;
        //if (isPercent) jumpToNextWord+=1;
        //if (!fraction.equals("")) jumpToNextWord+=1;
        //if (isUS) jumpToNextWord+=1;
        //if (andAsWord)jumpToNextWord+=1;
        if (number != null) return between + (numberToTerm(false,number,(isDollar || isDollarAsWord),(isBillion || isBillionAsWord),(isMillion || isMillionAsWord),isTrillion,isThousand, percent, fraction,isKilogram,isGram)) + and + rightSide;
        else return "";
    }

    /**
     * get the number with all his properties and chaining by the rules
     * @param number
     * @param isDollar
     * @param isBillion
     * @param isMillion
     * @param isTrillion
     * @param isThousand
     * @param percent
     * @param fraction
     * @param isKilogram
     * @param isGram
     * @return number with chaining properties
     */
    public static String numberToTerm (boolean isCity, double number, boolean isDollar, boolean isBillion, boolean isMillion, boolean isTrillion, boolean isThousand, String percent, String fraction , boolean isKilogram, boolean isGram){
        String numberToReturn = "";
        String kmb = "";
        String kg = "";
        if(isGram)
        {
            number = number / 1000;
            kg = " kg";
        }
        else if(isKilogram)
            kg = " kg";
        if (isDollar){
            if (isTrillion){
                number = number * 1000000;
                kmb = " M";
            }
            else if (isBillion) {
                number = number * 1000;
                kmb = " M";
            }
            else if(isMillion){
                kmb = " M";
            }
            else if (number >= 1000000){
                number = number / 1000000;
                kmb = " M";
            }
            else if (isThousand){
                number = number * 1000;
            }
        }
        else{
            if (isTrillion){
                number = number * 1000;
                kmb = "B";
            }
            else if (isBillion) {
                kmb = "B";
            }
            else if(isMillion){
                kmb = "M";
            }
            else if (isThousand){
                kmb = "K";
            }
            else if (number >= 1000000000000.0){
                number = number / 1000000000;
                kmb ="B";
            }
            else if (number >= 1000000000.0){
                number = number / 1000000000;
                kmb ="B";
            }
            else if (number >= 1000000.0){
                number = number / 1000000;
                kmb ="M";
            }
            else if (number >= 1000.0){
                number = number / 1000;
                kmb ="K";
            }
        }
        if (isCity) number = Math.round(number*100.0)/100.0;//
        numberToReturn = Double.toString(number);
        numberToReturn = numberToReturn.indexOf(".") < 0 ? numberToReturn : numberToReturn.replaceAll("0*$", "").replaceAll("\\.$", "");
        numberToReturn = numberToReturn +  fraction + kmb + percent + kg;
        if (isDollar) numberToReturn = numberToReturn + " Dollars";
        return numberToReturn;

    }

    /**
     * main function that parse the text
     * @param text
     */
    public void parsingTextToText(String text) {
        termsIndoc = new HashMap<>();
        queryTerms = new HashSet<>();
        currentLine = 1;
        if (text.length()<=1){
            docInfo.append(docName + ",,\n");
            return;
        }

        text = text.replaceAll("[{}()\\[\\]@#^*|]+"," ");

        String [] onlyTextFromText = null;
        onlyTextFromText = text.split(" ");
        int length = onlyTextFromText.length;
        /*
        List <String> listWithOutSograim = new ArrayList<>();
        for (int i = 0; i <length; i++) {
            int indexOf=0;
            int indexOfSograim = 0;
            while (indexOf < onlyTextFromText[i].length() ){
                if (onlyTextFromText[i].charAt(indexOf) == '(' || onlyTextFromText[i].charAt(indexOf) == ')' || onlyTextFromText[i].charAt(indexOf) == '[' || onlyTextFromText[i].charAt(indexOf) == ']' ||)
            }
        }*/
        boolean lastWord = false;

        for (int i = 0; i <length; i++) {
            if (onlyTextFromText[i].length()==0) continue;
            boolean isDollar = false;
            boolean isBillion = false;
            boolean isMillion = false;
            String percent = "";
            String fraction = "";
            String nextword = "";
            Double number = null;

            // help to deal with number of terms with capital letters
            firstword = false;
            if(lastWord) {
                firstword = true;
                currentLine++;
            }
            lastWord=false;

            // remove delimiters from the last char
            int currentIndexChar = onlyTextFromText[i].length()-1;
            boolean wordHasChanged=false;
            while (currentIndexChar >= 0 && !((onlyTextFromText[i].charAt(currentIndexChar) >= 65 && onlyTextFromText[i].charAt(currentIndexChar) <= 90) || (onlyTextFromText[i].charAt(currentIndexChar) >= 97 && onlyTextFromText[i].charAt(currentIndexChar) <= 122) || onlyTextFromText[i].charAt(currentIndexChar) == '%' || (onlyTextFromText[i].charAt(currentIndexChar) >= 48 && onlyTextFromText[i].charAt(currentIndexChar) <= 57))){
                lastWord = true;
                currentIndexChar--;
                wordHasChanged =true;
            }
            if (currentIndexChar == -1) continue;
            if (wordHasChanged){
                onlyTextFromText[i] = onlyTextFromText[i].substring(0,currentIndexChar+1);
            }


            // remove delimiters from the first char
            if (i < length - 1 && (onlyTextFromText[i + 1].startsWith("(") || onlyTextFromText[i + 1].startsWith("" + '"' + "") || onlyTextFromText[i].startsWith("'") || onlyTextFromText[i].startsWith("["))) {
                lastWord = true;
            }
            currentIndexChar = 0;
            wordHasChanged=false;
            while (currentIndexChar < onlyTextFromText[i].length() && !((onlyTextFromText[i].charAt(currentIndexChar) >= 65 && onlyTextFromText[i].charAt(currentIndexChar) <= 90) || (onlyTextFromText[i].charAt(currentIndexChar) >= 97 && onlyTextFromText[i].charAt(currentIndexChar) <= 122) || onlyTextFromText[i].charAt(currentIndexChar) == '$' || (onlyTextFromText[i].charAt(currentIndexChar) >= 48 && onlyTextFromText[i].charAt(currentIndexChar) <= 57))){//(onlyTextFromText[i].charAt(currentIndexChar) == '(' || onlyTextFromText[i].charAt(currentIndexChar) == '"' || onlyTextFromText[i].charAt(currentIndexChar) == '\'' ||  onlyTextFromText[i].charAt(currentIndexChar) == '[' || onlyTextFromText[i].charAt(currentIndexChar) == '/' || onlyTextFromText[i].charAt(currentIndexChar) == '\\')) {
                currentIndexChar++;
                wordHasChanged = true;
            }
            if (currentIndexChar == onlyTextFromText[i].length()) continue;
            if (wordHasChanged)
                onlyTextFromText[i] = onlyTextFromText[i].substring(currentIndexChar);


            // if the word is stop word continue to the next word
            if (stopWords.contains(onlyTextFromText[i])) continue;
            if (onlyTextFromText[i].contains("<") || onlyTextFromText[i].contains(">") || onlyTextFromText[i].equals(" ") || onlyTextFromText[i].length() == 0 || onlyTextFromText[i].equals("\n"))
                continue;
            jumpToNextWord = 0;
            boolean isBillionAsWord = false;

            //deal with range (xx-yy)
            if (onlyTextFromText[i].contains("-") && onlyTextFromText[i].charAt(0) != '-') {
                String [] split=null;
                if (onlyTextFromText[i].contains("--")){
                    split = onlyTextFromText[i].split("--");
                }
                else split = onlyTextFromText[i].split("-");
                if (split[1].equals("million") || split[1].equals("billion") || split[1].equals("Million") || split[1].equals("Billion") || split[1].equals("trillion")){
                    if (split[0].charAt(0) == '$'){}
                    else {
                        dealWithMakaf(split,isDollar,onlyTextFromText,i);
                        continue;
                    }
                }
                else if (split.length == 2) {
                    Double leftNumber = isNumber(split[0]);
                    Double rightNumber = isNumber(split[1]);

                    String leftSide = split[0];
                    String rightSide = split[1];
                    if (leftNumber != null && !lastWord) {
                        leftSide = dealWithNumbers(leftNumber, i, length, "", onlyTextFromText, false, false, "", "", false, false, false);
                        addToterms(leftSide,true,-1);
                    }
                    if (rightNumber != null && !lastWord) {
                        rightSide = dealWithNumbers(rightNumber, i, length, "", onlyTextFromText, false, false, "", "", false, false, false);
                        addToterms(rightSide,true,-1);
                    }

                    if (!leftSide.contains("-") && !rightSide.contains("-")) {//
                        addToterms(leftSide + "-" + rightSide,false,-1);
                    }
                    else if (leftNumber != null && rightNumber != null) {
                        jumpToNextWord = jumpToNextWord/2;
                        i += jumpToNextWord;
                        if (leftSide.contains("0")) leftSide = leftSide.substring(leftSide.lastIndexOf("0")+1);
                        else leftSide = leftSide.substring(leftSide.lastIndexOf("-")+1);
                        if (rightSide.contains("0")) rightSide = rightSide.substring(rightSide.lastIndexOf("0")+1);
                        else rightSide = rightSide.substring(rightSide.lastIndexOf("-")+1);
                        addToterms(leftSide,true,-1);
                        addToterms(rightSide,true,-1);
                        addToterms(leftSide + "-" + rightSide, true,-1);
                    }
                    continue;

                }
                else {
                    addToterms(onlyTextFromText[i],false,-1);
                    continue;
                }


                //deal with date (xx-yy)
            }
            else if (months.containsKey(onlyTextFromText[i])) {
                if (i < length - 1 && !lastWord) {
                    try {
                        String s = onlyTextFromText[i + 1];
                        Double d = isNumber(onlyTextFromText[i + 1]);
                        int dayOrYear=0;
                        if (d != null) dayOrYear = d.intValue();
                        else{
                            addToterms(numberMonths.get(months.get(onlyTextFromText[i])),false,-1);
                            continue;
                        }
                        if (dayOrYear >= 1 && dayOrYear <= 31) {
                            if (dayOrYear / 10 == 0)
                                onlyTextFromText[i] = months.get(onlyTextFromText[i]) + "-0" + String.valueOf(dayOrYear);
                            else
                                onlyTextFromText[i] = months.get(onlyTextFromText[i]) + "-" + String.valueOf(dayOrYear);
                        }
                        else
                            onlyTextFromText[i] = String.valueOf(dayOrYear) + "-" + months.get(onlyTextFromText[i]);
                        addToterms(onlyTextFromText[i], true,-1);

                        i++;
                        continue;

                    } catch (NumberFormatException e) { continue;}
                }
                addToterms(numberMonths.get(months.get(onlyTextFromText[i])),false,-1);
                continue;

            }
            // deal with between range
            else if ((onlyTextFromText[i].equals("between") || onlyTextFromText[i].equals("Between")) && i <length-1 ){
                String finalTerm = dealWithNumbers(isNumber(onlyTextFromText[i + 1]), i + 1, length,"", onlyTextFromText, false, false,"","", false, false, true);
                if (finalTerm != "") {
                    i++;
                    i += jumpToNextWord;
                    addToterms(finalTerm,true,-1);
                    if (finalTerm.contains("-")){
                        String[] split = finalTerm.split("-");
                        if (split.length == 2){
                            addToterms(split[0],true,-1);
                            addToterms(split[1],true,-1);
                        }
                    }
                }
                continue;
            }

            // update the properies of the terms by characters that part of the terms like $ % bn m
            if (Character.isDigit(onlyTextFromText[i].charAt(0)) || onlyTextFromText[i].charAt(0) == '$') {
                if (onlyTextFromText[i].charAt(0) == '$' && onlyTextFromText[i].length() > 1) {
                    isDollar = true;
                    onlyTextFromText[i] = onlyTextFromText[i].substring(1);
                    if (onlyTextFromText[i].contains("-")) {
                        String[] split = onlyTextFromText[i].split("-");
                        dealWithMakaf(split, isDollar, onlyTextFromText, i);
                        continue;
                    }

                }

                if (onlyTextFromText[i].endsWith("%") && onlyTextFromText[i].length() > 1) {
                    percent = "%";
                    number = isNumber(onlyTextFromText[i]);
                }
                if (onlyTextFromText[i].endsWith("bn") && onlyTextFromText[i].length() > 2) {
                    isBillion = true;
                    number = isNumber(onlyTextFromText[i].substring(0, onlyTextFromText[i].length() - 2));
                } else if (onlyTextFromText[i].endsWith("m") && onlyTextFromText[i].length() > 1) {
                    isMillion = true;
                    number = isNumber(onlyTextFromText[i].substring(0, onlyTextFromText[i].length() - 1));
                }
                else number = isNumber(onlyTextFromText[i]);
            }
            else number = isNumber(onlyTextFromText[i]);

            // if its number send the number to deal with number and than add to terms
            if (number != null) {
                if (!lastWord)
                    addToterms(dealWithNumbers(number, i, length, nextword, onlyTextFromText, isBillionAsWord, isDollar, percent, fraction, isBillion, isMillion, false),true,-1);
                else
                    addToterms(numberToTerm(false,number, isDollar, isBillion, isMillion, false, false, percent, fraction, false, false),true,-1);

            }

            // if its not number check if its expression of capital leters
            else{
                /*if (!firstword && onlyTextFromText[i].charAt(0) >= 65 && onlyTextFromText[i].charAt(0) <= 90 && !lastWord){
                    String capitalLetters ="";
                    boolean iGotBigger = false;
                    int j=i;
                    while (i<length && onlyTextFromText[i].charAt(0) >= 65 && onlyTextFromText[i].charAt(0) <= 90) {
                        iGotBigger=false;
                        if (onlyTextFromText[i].length() > 0 && (onlyTextFromText[i].endsWith(",") || onlyTextFromText[i].endsWith(".") || onlyTextFromText[i].endsWith(":") || onlyTextFromText[i].endsWith(";") || onlyTextFromText[i].endsWith("-") || onlyTextFromText[i].endsWith("?") || onlyTextFromText[i].endsWith(")") || onlyTextFromText[i].endsWith("" + '"' + "") || onlyTextFromText[i].endsWith("]") || onlyTextFromText[i].endsWith("'"))){
                            if (j != i) capitalLetters += " " + onlyTextFromText[i].substring(0, onlyTextFromText[i].length() - 1);
                            else capitalLetters += onlyTextFromText[i].substring(0, onlyTextFromText[i].length() - 1);
                            break;
                        }
                        if ( j!= i ) capitalLetters += " " + onlyTextFromText[i];
                        else capitalLetters += onlyTextFromText[i];
                        i++;
                        iGotBigger = true;
                    }
                    if (iGotBigger) i--;
                    addToterms(capitalLetters,docName,false);
                    continue;

                }*/

                // add the word to the terms after stemming
                if (firstword) onlyTextFromText[i] =onlyTextFromText[i].toLowerCase();
                if (isStemming && !citiesList.contains(onlyTextFromText[i].toUpperCase())) addToterms(stemmer.stemTerm(onlyTextFromText[i]),false,i);
                else addToterms(onlyTextFromText[i],false,i);
            }
            i += jumpToNextWord;

        }

        // after parsed the doc calculate the term with max appearance and insert to docInfo map
        if(!isQuery) {
            int max = 0;

            int totalTermsNotIncludingStopWords=0;

            try {
                for ( Map.Entry<String, Integer> entry : termsIndoc.entrySet() ) {
                    totalTermsNotIncludingStopWords += entry.getValue();
                    if (entry.getValue() > max) max = entry.getValue();
                }
            } catch (NullPointerException e) {
                System.out.println(docName);
            }
            double weightOfDocNormalizeByMostCommonWord=0;
            double weightOfDocNormalizeByLengthOfDoc=0;
            for ( Map.Entry<String, Integer> entry : termsIndoc.entrySet() ) {
                weightOfDocNormalizeByMostCommonWord += Math.pow(((double)entry.getValue()) / max,2) ;
                weightOfDocNormalizeByLengthOfDoc += Math.pow(((double)entry.getValue()) / totalTermsNotIncludingStopWords,2) ;
            }
            weightOfDocNormalizeByLengthOfDoc = Math.sqrt(weightOfDocNormalizeByLengthOfDoc);
            weightOfDocNormalizeByMostCommonWord = Math.sqrt(weightOfDocNormalizeByMostCommonWord);
            weightOfDocNormalizeByLengthOfDoc =  Math.round(weightOfDocNormalizeByLengthOfDoc*100.0)/100.0;
            weightOfDocNormalizeByMostCommonWord =  Math.round(weightOfDocNormalizeByMostCommonWord*100.0)/100.0;
            docInfo.append(docName + "," + max + "," + termsIndoc.size() + "," + totalTermsNotIncludingStopWords +  "," + weightOfDocNormalizeByMostCommonWord + "," + weightOfDocNormalizeByLengthOfDoc + "," + currentLine + "\n");
            Map <String,Double> mapOfEntitiesRanking = new TreeMap<>();
            totalLengthOfAllDocumentsNotIncludingStopWords+=totalTermsNotIncludingStopWords;
            for ( Map.Entry<String, Integer> entry : mapOfEntitiesLineNumber.entrySet() ) {
                mapOfEntitiesRanking.put(entry.getKey(),1.0/(0.9*mapOfEntitiesCounter.get(entry.getKey()) + 0.1 * (1.0/entry.getValue())));
            }
            List<Map.Entry<String, Double>> list = new ArrayList<>(mapOfEntitiesRanking.entrySet());
            list.sort(Map.Entry.comparingByValue());
            Map<String, Double> result = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            int i=0;
            for ( Map.Entry<String, Double> entry : result.entrySet() ) {
                entitiesInDoc.append(entry.getKey() + "-" + entry.getValue() +  ",");
                i++;
                if (i==5) break;
            }
            entitiesInDoc.append("\n");

            /*
            try {
                BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(postingAndDictionary + "/citiesPosting.txt",true), StandardCharsets.UTF_8));

            }
            catch (IOException e){e.printStackTrace();}*/

        }


    }

    private void dealWithMakaf (String [] split, boolean isDollar,String [] onlyTextFromText, int i){
        if (split.length==2){
            Double leftSide = isNumber(split[0]);
            if (leftSide != null){
                if (split[1].equals("million") || split[1].equals("Million") ) addToterms(numberToTerm(false,leftSide,isDollar,false,true,false,false,"","",false,false),true,-1);
                else if (split[1].equals("billion") || split[1].equals("Billion") ) addToterms(numberToTerm(false,leftSide,isDollar,true,false,false,false,"","",false,false),true,-1);
                else if (split[1].equals("trillion") || split[1].equals("Trllion") )
                    addToterms(numberToTerm(false,leftSide,isDollar,false,false,true,false,"","",false,false),true,-1);
                else {
                    addToterms(numberToTerm(false,leftSide,isDollar,false,false,false,false,"","",false,false),true,-1);
                    addToterms(onlyTextFromText[i],false,-1);
                }
            }
        }
    }

    public void makePostingForCities(){//
        try {
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(postingAndDictionary + "/citiesPosting.txt",true), StandardCharsets.UTF_8));
            for ( Map.Entry<String, Map<String,String>> entry : cities.entrySet() ) {
                if (entry.getValue().entrySet().size() == 0){
                    bufferWriter.write("No appearences of this city in the corpus");
                }
                else {
                    for (Map.Entry<String, String> insideEntry : entry.getValue().entrySet()) {
                        bufferWriter.write(insideEntry.getKey() + "," + insideEntry.getValue() + "~");
                    }
                }
                bufferWriter.write("\n");
            }
            bufferWriter.flush();
            bufferWriter.close();
        }
        catch (Exception e){}
    }


    /**
     * part B
     *
     */
    public Set<String> QueryParser(String query, String description, boolean isSemantic)
    {
        Set <String> dictionaryOfUnWantedWordsForDescription = new HashSet<>();
        if (!description.equals("")) {
            dictionaryOfUnWantedWordsForDescription.add("identify");
            dictionaryOfUnWantedWordsForDescription.add("Identify");
            dictionaryOfUnWantedWordsForDescription.add("discuss");
            dictionaryOfUnWantedWordsForDescription.add("Discuss");
            dictionaryOfUnWantedWordsForDescription.add("discussing");
            dictionaryOfUnWantedWordsForDescription.add("Discussing");
            dictionaryOfUnWantedWordsForDescription.add("Document");
            dictionaryOfUnWantedWordsForDescription.add("Documents");
            dictionaryOfUnWantedWordsForDescription.add("document");
            dictionaryOfUnWantedWordsForDescription.add("documents");
            dictionaryOfUnWantedWordsForDescription.add("information");
            dictionaryOfUnWantedWordsForDescription.add("contain");
            dictionaryOfUnWantedWordsForDescription.add("contains");
            dictionaryOfUnWantedWordsForDescription.add("identified");
            dictionaryOfUnWantedWordsForDescription.add("required");
            dictionaryOfUnWantedWordsForDescription.add("include");
            dictionaryOfUnWantedWordsForDescription.add("included");
            dictionaryOfUnWantedWordsForDescription.add("following");
            dictionaryOfUnWantedWordsForDescription.add("considered");
            dictionaryOfUnWantedWordsForDescription.add("regarding");
            dictionaryOfUnWantedWordsForDescription.add("mention");
            dictionaryOfUnWantedWordsForDescription.add("mentions");
            dictionaryOfUnWantedWordsForDescription.add("refer");
            dictionaryOfUnWantedWordsForDescription.add("refers");
            dictionaryOfUnWantedWordsForDescription.add("focus");
            dictionaryOfUnWantedWordsForDescription.add("associate");
            dictionaryOfUnWantedWordsForDescription.add("associates");
            dictionaryOfUnWantedWordsForDescription.add("associated");
        }//
        Set <String> finalSet = new HashSet<>();
        isQuery = true;
        String query2 = "";
        String [] queryArray = query.split(" ");

        boolean containsBigLetter =false;
        for (int i=0; i<queryArray.length && !containsBigLetter; i++){
            if (queryArray[i].charAt(0)>=65 && queryArray[i].charAt(0)<=90) containsBigLetter = true;
        }
        if (isSemantic) {

            String allWordstoAPI = "";
            for (int i = 0; i < queryArray.length; i++) {
                if (i == 0) {
                    allWordstoAPI = queryArray[0];
                } else {
                    allWordstoAPI = allWordstoAPI + "+" + queryArray[i];
                }
            }
            try {
                if (containsBigLetter) {
                    String urlString = "https://api.datamuse.com/words?ml=" + allWordstoAPI;
                    URL url = new URL(urlString);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    String detailsOfCity = content.toString();
                    int BracketCount = 0;
                    List<String> JsonItems = new ArrayList<>();
                    StringBuilder Json = new StringBuilder();
                    int currentCharIndex = 0;
                    for (char c : detailsOfCity.toCharArray()) {
                        if (currentCharIndex == 0 || currentCharIndex == detailsOfCity.length() - 1) {
                            currentCharIndex++;
                            continue;
                        }
                        if (c == '{')
                            ++BracketCount;
                        else if (c == '}')
                            --BracketCount;
                        Json.append(c);

                        if (BracketCount == 0 && c != ' ') {
                            if (!Json.toString().equals(",")) JsonItems.add(Json.toString());
                            Json = new StringBuilder();
                        }
                        currentCharIndex++;
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    for (int j = 0; j < queryArray.length * 3; j++) {//
                        Word word = objectMapper.readValue(JsonItems.get(j), Word.class);
                        query2 = query2 + " " + word.word.toUpperCase() + " " + word.word.toLowerCase();
                    }
                    con.disconnect();
                    parsingTextToText(query2);
                    finalSet = new HashSet(queryTerms);
                }
                else{

                    if (!description.equals("")) {
                        String[] descriptionArray = description.split(" ");
                        String finalDescription = "";
                        for (String s : descriptionArray) {
                            if (!dictionaryOfUnWantedWordsForDescription.contains(s)) finalDescription = finalDescription + " " + s;
                        }
                        descriptionArray = finalDescription.split(".");

                        int notRelevantIndex = -1;
                        int relevantIndex = -1;
                        int counter = 0;
                        for (String s : descriptionArray) {
                            if ((s.contains("relevant") || s.contains("Relevant") ) && (!s.contains("non-relevant") || !s.contains("not relevant"))) {
                                relevantIndex = counter;
                            } else if (s.contains("non-relevant") || s.contains("not relevant")) {
                                notRelevantIndex = counter;
                            }
                            counter++;
                        }
                        counter = 0;

                        if (notRelevantIndex == -1) {
                            parsingTextToText(finalDescription);
                            for (String s : queryTerms) {
                                finalSet.add(s);
                            }
                            queryTerms.clear();
                        } else if (notRelevantIndex > relevantIndex) {
                            while (counter < notRelevantIndex) {
                                parsingTextToText(descriptionArray[counter]);
                                counter++;
                            }
                            for (String s : queryTerms) {
                                finalSet.add(s);
                            }
                            queryTerms.clear();
                        } else {
                            counter = notRelevantIndex + 1;
                            while (counter < descriptionArray.length) {
                                parsingTextToText(descriptionArray[counter]);
                                counter++;
                            }
                            for (String s : queryTerms) {
                                finalSet.add(s);
                            }
                            queryTerms.clear();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else{
            if (!description.equals("") && !containsBigLetter) {
                String[] descriptionArray = description.split(" ");
                String finalDescription = "";
                for (String s : descriptionArray) {

                    if (!dictionaryOfUnWantedWordsForDescription.contains(s)){
                        finalDescription = finalDescription + " " + s;
                    }
                }
                descriptionArray = finalDescription.split(".");

                int notRelevantIndex = -1;
                int relevantIndex = -1;
                int counter = 0;
                for (String s : descriptionArray) {
                    if ((s.contains("relevant") || s.contains("Relevant") ) && (!s.contains("non-relevant") || !s.contains("not relevant"))) {
                        relevantIndex = counter;
                    } else if (s.contains("non-relevant") || s.contains("not relevant")) {
                        notRelevantIndex = counter;
                    }
                    counter++;
                }
                counter = 0;

                if (notRelevantIndex == -1) {
                    parsingTextToText(finalDescription);
                    for (String s : queryTerms) {
                        finalSet.add(s);
                    }
                    queryTerms.clear();
                } else if (notRelevantIndex > relevantIndex) {
                    while (counter < notRelevantIndex) {
                        parsingTextToText(descriptionArray[counter]);
                        counter++;
                    }
                    for (String s : queryTerms) {
                        finalSet.add(s);
                    }
                    queryTerms.clear();
                } else {
                    counter = notRelevantIndex + 1;
                    while (counter < descriptionArray.length) {
                        parsingTextToText(descriptionArray[counter]);
                        counter++;
                    }
                    for (String s : queryTerms) {
                        finalSet.add(s);
                    }
                    queryTerms.clear();
                }
            }
        }
        String queryWithBigLettersAndSmallLetters="";
        for(String s:queryArray){
            queryWithBigLettersAndSmallLetters = queryWithBigLettersAndSmallLetters + " " + s.toUpperCase() + " " + s.toLowerCase();
        }
        parsingTextToText(queryWithBigLettersAndSmallLetters);
        for (String s: queryTerms){
            finalSet.add(s);
        }
        isQuery = false;


        if (description != ""){
            dictionaryOfUnWantedWordsForDescription.add("relevant");
            Set <String> finalFinalSet = new HashSet<>();
            for (String s1: finalSet){
                boolean s1IsOK = true;
                for (String s2: dictionaryOfUnWantedWordsForDescription){
                    if (s2.toUpperCase().startsWith(s1) || s2.toLowerCase().startsWith(s1)) {
                        s1IsOK=false;
                        break;
                    }
                }
                if (s1IsOK) finalFinalSet.add(s1);
            }
            return finalFinalSet;
        }


        return finalSet;//
    }





}





