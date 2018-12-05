package sample;

import javafx.util.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Parse {

    private String pathOfCorpusAndStopWord;
    private String postingAndDictionary;
    private   Map<String, String> months;
    private   HashSet<String> stopWords;
    private int currentLine;
    private Stemmer stemmer;
    private int jumpToNextWord = 0;
    private Set <String> citiesList;
    public StringBuilder docInfo = new StringBuilder();
    public Map<String, Map<String, Double>> docsByTerm = new HashMap<>(); // key = term , value = { key = doc id , value = number of appearance in specific doc . first appearence in doc }
    public Map<String, Integer> termsIndoc = new HashMap<>();// key = doc id , value = <term, tf>
    public boolean isStemming;
    public Map<String,Map <String,String>> cities = new TreeMap<>(); // key = city , value = { key = doc id , value = indexes of city in doc }

    public Parse (boolean isStemming , Set<String> citiesList, String pathOfCorpusAndStopWord, String postingAndDictionary ){
        this.citiesList = citiesList;
        this.pathOfCorpusAndStopWord = pathOfCorpusAndStopWord;
        this.postingAndDictionary = postingAndDictionary;
        this.isStemming = isStemming;
        stemmer = new Stemmer();
        months = new HashMap<>();
        stopWords = new HashSet<>();
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
    }

    /**
     * get Map with 50 value -the map include key= name doc ,value= text of the doc
     * the function send every doc to  parsingTextToText function.
     * @param mapOfDocs
     */
    public void startParsing50Files(List<Pair <String, String>> mapOfDocs){
        docInfo = new StringBuilder();
        for (int i=0; i<mapOfDocs.size(); i++){
            Pair currentDoc = mapOfDocs.get(i);
            String docName = (String)currentDoc.getKey();
            String doc = (String)currentDoc.getValue();
            parsingTextToText(doc,docName,isStemming);
        }

    }

    /**
     * Adding term to the maps(docsByTerm,termsIndoc) after changing to lowercase/uppercase
     * @param str
     * @param docName
     */
    private void directAddingTerm(String str, String docName,int currentIndexInDoc) {
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
     * @param docName
     * @param isNumber
     */
    public void addToterms(String str, String docName, boolean isNumber,int currentIndexInDoc){
        if (str.length()>0){
            if (str.endsWith("'")) str = str.substring(0,str.length()-1);
        }
        if (str.length() == 0) return;
        if (isNumber) {
            directAddingTerm(str,docName,currentIndexInDoc);
        }
        else if (str.charAt(0)>=65 && str.charAt(0)<=90){

            if (docsByTerm.get(str.toUpperCase()) != null) str = str.toUpperCase();
            else if (docsByTerm.get(str.toLowerCase()) != null) str = str.toLowerCase();
            else str = str.toUpperCase();
            directAddingTerm(str,docName,currentIndexInDoc);
        }
        else if (str.matches(".*[a-z]+.*")) {
            if (docsByTerm.get(str.toUpperCase()) != null) {
                docsByTerm.put(str.toLowerCase(),docsByTerm.remove(str.toUpperCase()));
                if (termsIndoc.get(docName) != null){
                    if(termsIndoc.get(str.toUpperCase()) != null){
                        termsIndoc.put(str.toLowerCase(),termsIndoc.remove(str.toUpperCase()));
                    }
                }
            }//
            directAddingTerm(str.toLowerCase(),docName,currentIndexInDoc);
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
        catch (ParseException e){
            return null ;
        }
    }

    /**
     * getting the number the properties that we take from the symbol that was part of the number and check the next words to identify the term
     * @param docName
     * @param number
     * @param i
     * @param length
     * @param nextword
     * @param onlyTextFromDoc
     * @param isBillionAsWord
     * @param isDollar
     * @param percent
     * @param fraction
     * @param isBillion
     * @param isMillion
     * @param betweenAsWord
     * @return
     */
    private String dealWithNumbers(String docName,Double number, int i, int length, String nextword, String[]onlyTextFromDoc,
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
                nextword = onlyTextFromDoc[nextWordIndex];
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
                    rightSide = dealWithNumbers(docName,isNumber(split[1]),i+1,length,nextword,onlyTextFromDoc,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
                }
                if (nextword.equals("and") && betweenAsWord){
                    rightSide = dealWithNumbers(docName,isNumber(onlyTextFromDoc[nextWordIndex+1]),nextWordIndex+1,length,nextword,onlyTextFromDoc,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
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
                        addToterms(months.get(nextword),docName,true,-1);
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
     * main function that parse the doc
     * @param doc
     * @param docName
     */
    public void parsingTextToText(String doc, String docName, boolean isStemming) {
        termsIndoc = new HashMap<>();
        currentLine = 1;
        if (doc.length()<=1){
            docInfo.append(docName + ",,\n");
            return;
        }
        String [] onlyTextFromDoc = null;
        onlyTextFromDoc = doc.split(" ");
        boolean lastWord = false;
        int length = onlyTextFromDoc.length;
        for (int i = 0; i <length; i++) {
            boolean isDollar = false;
            boolean isBillion = false;
            boolean isMillion = false;
            String percent = "";
            String fraction = "";
            String nextword = "";
            Double number = null;

            // help to deal with number of terms with capital letters
            boolean firstword = false;
            if(lastWord) {
                firstword = true;
                currentLine++;
            }
            lastWord=false;

            // remove delimiters from the last char
            int currentIndexChar = onlyTextFromDoc[i].length()-1;
            boolean wordHasChanged=false;
            while (currentIndexChar >= 0 && !((onlyTextFromDoc[i].charAt(currentIndexChar) >= 65 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 90) || (onlyTextFromDoc[i].charAt(currentIndexChar) >= 97 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 122) || onlyTextFromDoc[i].charAt(currentIndexChar) == '%' || (onlyTextFromDoc[i].charAt(currentIndexChar) >= 48 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 57))){
                lastWord = true;
                currentIndexChar--;
                wordHasChanged =true;
            }
            if (currentIndexChar == -1) continue;
            if (wordHasChanged){
                onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(0,currentIndexChar+1);
            }


            // remove delimiters from the first char
            if (i < length - 1 && (onlyTextFromDoc[i + 1].startsWith("(") || onlyTextFromDoc[i + 1].startsWith("" + '"' + "") || onlyTextFromDoc[i].startsWith("'") || onlyTextFromDoc[i].startsWith("["))) {
                lastWord = true;
            }
            currentIndexChar = 0;
            wordHasChanged=false;
            while (currentIndexChar < onlyTextFromDoc[i].length() && !((onlyTextFromDoc[i].charAt(currentIndexChar) >= 65 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 90) || (onlyTextFromDoc[i].charAt(currentIndexChar) >= 97 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 122) || onlyTextFromDoc[i].charAt(currentIndexChar) == '$' || (onlyTextFromDoc[i].charAt(currentIndexChar) >= 48 && onlyTextFromDoc[i].charAt(currentIndexChar) <= 57))){//(onlyTextFromDoc[i].charAt(currentIndexChar) == '(' || onlyTextFromDoc[i].charAt(currentIndexChar) == '"' || onlyTextFromDoc[i].charAt(currentIndexChar) == '\'' ||  onlyTextFromDoc[i].charAt(currentIndexChar) == '[' || onlyTextFromDoc[i].charAt(currentIndexChar) == '/' || onlyTextFromDoc[i].charAt(currentIndexChar) == '\\')) {
                currentIndexChar++;
                wordHasChanged = true;
            }
            if (currentIndexChar == onlyTextFromDoc[i].length()) continue;
            if (wordHasChanged)
                onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(currentIndexChar);


            // if the word is stop word continue to the next word
            if (stopWords.contains(onlyTextFromDoc[i])) continue;
            if (onlyTextFromDoc[i].contains("<") || onlyTextFromDoc[i].contains(">") || onlyTextFromDoc[i].equals(" ") || onlyTextFromDoc[i].length() == 0 || onlyTextFromDoc[i].equals("\n"))
                continue;
            jumpToNextWord = 0;
            boolean isBillionAsWord = false;

            //deal with range (xx-yy)
            if (onlyTextFromDoc[i].contains("-") && onlyTextFromDoc[i].charAt(0) != '-') {
                String [] split=null;
                if (onlyTextFromDoc[i].contains("--")){
                    split = onlyTextFromDoc[i].split("--");
                }
                else split = onlyTextFromDoc[i].split("-");
                if (split[1].equals("million") || split[1].equals("billion") || split[1].equals("Million") || split[1].equals("Billion") || split[1].equals("trillion")){
                    if (split[0].charAt(0) == '$'){}
                    else {
                        dealWithMakaf(split,isDollar,onlyTextFromDoc,i,docName);
                        continue;
                    }
                }
                else if (split.length == 2) {
                    Double leftNumber = isNumber(split[0]);
                    Double rightNumber = isNumber(split[1]);

                    String leftSide = split[0];
                    String rightSide = split[1];
                    if (leftNumber != null && !lastWord) {
                        leftSide = dealWithNumbers(docName,leftNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
                        addToterms(leftSide, docName,true,-1);
                    }
                    if (rightNumber != null && !lastWord) {
                        rightSide = dealWithNumbers(docName,rightNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
                        addToterms(rightSide, docName,true,-1);
                    }

                    if (!leftSide.contains("-") && !rightSide.contains("-")) {//
                        addToterms(leftSide + "-" + rightSide,docName,false,-1);
                    }
                    else if (leftNumber != null && rightNumber != null) {
                        jumpToNextWord = jumpToNextWord/2;
                        i += jumpToNextWord;
                        if (leftSide.contains("0")) leftSide = leftSide.substring(leftSide.lastIndexOf("0")+1);
                        else leftSide = leftSide.substring(leftSide.lastIndexOf("-")+1);
                        if (rightSide.contains("0")) rightSide = rightSide.substring(rightSide.lastIndexOf("0")+1);
                        else rightSide = rightSide.substring(rightSide.lastIndexOf("-")+1);
                        addToterms(leftSide,docName,true,-1);
                        addToterms(rightSide,docName,true,-1);
                        addToterms(leftSide + "-" + rightSide, docName,true,-1);
                    }
                    continue;

                }
                else {
                    addToterms(onlyTextFromDoc[i], docName,false,-1);
                    continue;
                }


                //deal with date (xx-yy)
            }
            else if (months.containsKey(onlyTextFromDoc[i])) {
                if (i < length - 1 && !lastWord) {
                    try {
                        int dayOrYear = Integer.parseInt(onlyTextFromDoc[i + 1]);
                        if (dayOrYear >= 1 && dayOrYear <= 31) {
                            if (dayOrYear / 10 == 0)
                                onlyTextFromDoc[i] = months.get(onlyTextFromDoc[i]) + "-0" + String.valueOf(dayOrYear);
                            else
                                onlyTextFromDoc[i] = months.get(onlyTextFromDoc[i]) + "-" + String.valueOf(dayOrYear);
                        } else
                            onlyTextFromDoc[i] = String.valueOf(dayOrYear) + "-" + months.get(onlyTextFromDoc[i]);
                        addToterms(onlyTextFromDoc[i], docName,true,-1);
                        i++;
                        continue;

                    } catch (NumberFormatException e) { }
                }
                addToterms(months.get(onlyTextFromDoc[i]), docName,true,-1);
                continue;

            }
            // deal with between range
            else if ((onlyTextFromDoc[i].equals("between") || onlyTextFromDoc[i].equals("Between")) && i <length-1 ){
                String finalTerm = dealWithNumbers(docName,isNumber(onlyTextFromDoc[i + 1]), i + 1, length,"", onlyTextFromDoc, false, false,"","", false, false, true);
                if (finalTerm != "") {
                    i++;
                    i += jumpToNextWord;
                    addToterms(finalTerm,docName,true,-1);
                    if (finalTerm.contains("-")){
                        String[] split = finalTerm.split("-");
                        if (split.length == 2){
                            addToterms(split[0],docName,true,-1);
                            addToterms(split[1],docName,true,-1);
                        }
                    }
                }
                continue;
            }

            // update the properies of the terms by characters that part of the terms like $ % bn m
            if (Character.isDigit(onlyTextFromDoc[i].charAt(0)) || onlyTextFromDoc[i].charAt(0) == '$') {
                if (onlyTextFromDoc[i].charAt(0) == '$' && onlyTextFromDoc[i].length() > 1) {
                    isDollar = true;
                    onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(1);
                    if (onlyTextFromDoc[i].contains("-")) {
                        String[] split = onlyTextFromDoc[i].split("-");
                        dealWithMakaf(split, isDollar, onlyTextFromDoc, i, docName);
                        continue;
                    }

                }

                if (onlyTextFromDoc[i].endsWith("%") && onlyTextFromDoc[i].length() > 1) {
                    percent = "%";
                    number = isNumber(onlyTextFromDoc[i]);
                }
                if (onlyTextFromDoc[i].endsWith("bn") && onlyTextFromDoc[i].length() > 2) {
                    isBillion = true;
                    number = isNumber(onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 2));
                } else if (onlyTextFromDoc[i].endsWith("m") && onlyTextFromDoc[i].length() > 1) {
                    isMillion = true;
                    number = isNumber(onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 1));
                }
                else number = isNumber(onlyTextFromDoc[i]);
            }
            else number = isNumber(onlyTextFromDoc[i]);

            // if its number send the number to deal with number and than add to terms
            if (number != null) {
                if (!lastWord)
                    addToterms(dealWithNumbers(docName,number, i, length, nextword, onlyTextFromDoc, isBillionAsWord, isDollar, percent, fraction, isBillion, isMillion, false), docName,true,-1);
                else
                    addToterms(numberToTerm(false,number, isDollar, isBillion, isMillion, false, false, percent, fraction, false, false), docName,true,-1);

            }

            // if its not number check if its expression of capital leters
            else{
                /*if (!firstword && onlyTextFromDoc[i].charAt(0) >= 65 && onlyTextFromDoc[i].charAt(0) <= 90 && !lastWord){
                    String capitalLetters ="";
                    boolean iGotBigger = false;
                    int j=i;
                    while (i<length && onlyTextFromDoc[i].charAt(0) >= 65 && onlyTextFromDoc[i].charAt(0) <= 90) {
                        iGotBigger=false;
                        if (onlyTextFromDoc[i].length() > 0 && (onlyTextFromDoc[i].endsWith(",") || onlyTextFromDoc[i].endsWith(".") || onlyTextFromDoc[i].endsWith(":") || onlyTextFromDoc[i].endsWith(";") || onlyTextFromDoc[i].endsWith("-") || onlyTextFromDoc[i].endsWith("?") || onlyTextFromDoc[i].endsWith(")") || onlyTextFromDoc[i].endsWith("" + '"' + "") || onlyTextFromDoc[i].endsWith("]") || onlyTextFromDoc[i].endsWith("'"))){
                            if (j != i) capitalLetters += " " + onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 1);
                            else capitalLetters += onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 1);
                            break;
                        }
                        if ( j!= i ) capitalLetters += " " + onlyTextFromDoc[i];
                        else capitalLetters += onlyTextFromDoc[i];
                        i++;
                        iGotBigger = true;
                    }
                    if (iGotBigger) i--;
                    addToterms(capitalLetters,docName,false);
                    continue;

                }*/

                // add the word to the terms after stemming
                if (isStemming && !citiesList.contains(onlyTextFromDoc[i].toUpperCase())) addToterms(stemmer.stemTerm(onlyTextFromDoc[i]), docName,false,i);
                else addToterms(onlyTextFromDoc[i],docName,false,i);
            }
            i += jumpToNextWord;

        }

        // after parsed the doc calculate the term with max appearance and insert to docInfo map

        int max =0;
        try{
        for (Integer Int:termsIndoc.values()){
            if (Int > max) max = Int;
        }}
        catch (NullPointerException e){
            System.out.println(docName);
        }
        docInfo.append(docName + "," + max + "," + termsIndoc.size() + "\n");


    }
    private void dealWithMakaf (String [] split, boolean isDollar,String [] onlyTextFromDoc, int i, String docName){
        if (split.length==2){
            Double leftSide = isNumber(split[0]);
            if (leftSide != null){
                if (split[1].equals("million") || split[1].equals("Million") ) addToterms(numberToTerm(false,leftSide,isDollar,false,true,false,false,"","",false,false),docName,true,-1);
                else if (split[1].equals("billion") || split[1].equals("Billion") ) addToterms(numberToTerm(false,leftSide,isDollar,true,false,false,false,"","",false,false),docName,true,-1);
                else if (split[1].equals("trillion") || split[1].equals("Trllion") )
                    addToterms(numberToTerm(false,leftSide,isDollar,false,false,true,false,"","",false,false),docName,true,-1);
                else {
                    addToterms(numberToTerm(false,leftSide,isDollar,false,false,false,false,"","",false,false),docName,true,-1);
                    addToterms(onlyTextFromDoc[i],docName,false,-1);
                }
            }
        }
    }

    public void makePostingForCities(){//
        try {
            BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(postingAndDictionary + "/citiesPosting.txt", true));

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


}





