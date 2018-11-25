package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Parse {


    public static Map<String, String> months;
    public static Map<String, String> numbersAsWords;
    public static HashSet<String> stopWords;
    //  public static Map <String, TermInCorpus> termsInCorpusMap; //String=term
    //  public Map <String,TermInDoc> termsInDocMap = new HashMap<>();//
    public static Stemmer stemmer;
    public int jumpToNextWord = 0;

    /// GAL
    // terms
    public Map<String, Map<String, Integer>> docsByTerm = new HashMap<>(); // key = term , value = { key = doc id , value = number of appearance in specific doc }

    //docs
    public Map<String, Map<String, Integer>> termsIndoc = new HashMap<>(); // key = doc id , value = <term, tf>

    public void startParsing50Files(Map <String,String> mapOfDocs ){
        for ( Map.Entry<String, String> entry : mapOfDocs.entrySet() ) {
            String docName = entry.getKey();
            String doc = entry.getValue();
            parsingTextToText(doc,docName);
        }
    }
    //adding term after changing to lowercase/uppercase
    private void directAddingTerm(String str, String docName) {
        if (docsByTerm.get(str) == null) {
            Map<String, Integer> docs = new HashMap<>();
            docs.put(docName, 1);
            docsByTerm.put(str, docs);
        }
        else {
            if (docsByTerm.get(str).get(docName) == null)
                docsByTerm.get(str).put(docName, 1);
            else
                docsByTerm.get(str).put(docName,docsByTerm.get(str).get(docName) + 1);

        }
        if (termsIndoc.get(docName) == null) {
            Map<String, Integer> terms = new HashMap<>();
            terms.put(str, 1);
            termsIndoc.put(docName, terms);
        }
        else
        if (termsIndoc.get(docName).get(str) == null)
            termsIndoc.get(docName).put(str, 1);
        else
            termsIndoc.get(docName).put(str,termsIndoc.get(docName).get(str) + 1);
    }


    private void addToterms(String str, String docName, boolean isNumber){
        if (str.length()>0){
            if (str.endsWith("'")) str = str.substring(0,str.length()-1);
        }
        if (str.length() == 0) return;
        if (isNumber) {
            directAddingTerm(str,docName);
        }
        else if (str.charAt(0) == str.toUpperCase().charAt(0)){

            if (docsByTerm.get(str.toUpperCase()) != null) str = str.toUpperCase();
            else if (docsByTerm.get(str.toLowerCase()) != null) str = str.toLowerCase();
            else str = str.toUpperCase();
            directAddingTerm(str,docName);
        }
        else if (str.matches(".*[a-z]+.*")) {
            if (docsByTerm.get(str.toUpperCase()) != null) {
                docsByTerm.put(str.toLowerCase(),docsByTerm.remove(str.toUpperCase()));
                if (termsIndoc.get(docName) != null){
                    if(termsIndoc.get(docName).get(str.toUpperCase()) != null){
                        termsIndoc.get(docName).put(str.toLowerCase(),termsIndoc.get(docName).remove(str.toUpperCase()));
                    }
                }

            }
            directAddingTerm(str.toLowerCase(),docName);
        }
        else directAddingTerm(str,docName);
        int x=0;

    }



    public static void startParser(){
        stemmer = new Stemmer();
        months = new HashMap<>();
        numbersAsWords = new HashMap<>();
        stopWords = new HashSet<>();

        BufferedReader in = null;
        try{
            String currentWord;
            in = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/reasources/stop_words.txt"));
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

    private Double isNumber (String str) {
        if (str.length() == 0) return null;
        else if (months.containsKey(str)) return Double.valueOf(months.get(str));
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

    private String dealWithNumbers(String docName,Double number, int i, int length, String nextword, String[]onlyTextFromDoc,
                                   boolean isBillionAsWord, boolean isDollar, String percent, String fraction, boolean isBillion, boolean isMillion, boolean betweenAsWord) {
        boolean isThousand = false;
        boolean isMillionAsWord = false;
        boolean isTrillion = false;
        boolean isDollarAsWord = false;
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
                        addToterms(months.get(nextword),docName,true);
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
        if (number != null) return between + (numberToTerm(number,(isDollar || isDollarAsWord),(isBillion || isBillionAsWord),(isMillion || isMillionAsWord),isTrillion,isThousand, percent, fraction)) + and + rightSide;
        else return "";
    }

    private String numberToTerm (double number, boolean isDollar, boolean isBillion, boolean isMillion, boolean isTrillion, boolean isThousand, String percent, String fraction){
        String numberToReturn = "";
        String kmb = "";
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
        numberToReturn = Double.toString(number);
        numberToReturn = numberToReturn.indexOf(".") < 0 ? numberToReturn : numberToReturn.replaceAll("0*$", "").replaceAll("\\.$", "");
        numberToReturn = numberToReturn +  fraction + kmb + percent ;
        if (isDollar) numberToReturn = numberToReturn + " Dollars";
        //if (numberToReturn.equals("780K") && terms.containsKey("780K")) System.out.println(terms.get("780K").numberOfOccurencesInDoc);
        return numberToReturn;

    }

    public void parsingTextToText(String doc, String docName) {
        //long start = System.nanoTime();
        String [] onlyTextFromDoc = null;
        onlyTextFromDoc = doc.split(" ");
        boolean lastWord = false;
        int length = onlyTextFromDoc.length;
        for (int i = 0; i <length; i++) {
            boolean isDollar = false;
            boolean isBillion = false;
            boolean isMillion = false;
            boolean firstWord =false;
            String percent = "";
            String fraction = "";
            String nextword = "";
            Double number = null;
            if (lastWord) firstWord=true;
            lastWord=false;
            while (onlyTextFromDoc[i].length()>0 && (onlyTextFromDoc[i].endsWith(",") || onlyTextFromDoc[i].endsWith(".") || onlyTextFromDoc[i].endsWith(":") || onlyTextFromDoc[i].endsWith(";") || onlyTextFromDoc[i].endsWith("-") || onlyTextFromDoc[i].endsWith("?") || onlyTextFromDoc[i].endsWith(")") || onlyTextFromDoc[i].endsWith(""+'"'+"") || onlyTextFromDoc[i].endsWith("]") || onlyTextFromDoc[i].endsWith("'"))) {
                lastWord = true;
                onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 1);
            }
            if (i < length - 1 && (onlyTextFromDoc[i + 1].startsWith("(") || onlyTextFromDoc[i + 1].startsWith("" + '"' + "") || onlyTextFromDoc[i].startsWith("'"))) {
                lastWord = true;
            }
            while (onlyTextFromDoc[i].length()>0 && (onlyTextFromDoc[i].startsWith("(") || onlyTextFromDoc[i].startsWith(""+'"'+"") || onlyTextFromDoc[i].startsWith("'") ||  onlyTextFromDoc[i].startsWith("["))) {
                onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(1);
            }
            if (stopWords.contains(onlyTextFromDoc[i])) continue;
            if (onlyTextFromDoc[i].contains("<") || onlyTextFromDoc[i].contains(">") || onlyTextFromDoc[i].equals(" ") || onlyTextFromDoc[i].length() == 0 || onlyTextFromDoc[i].equals("\n"))
                continue;
            jumpToNextWord = 0;
            boolean isBillionAsWord = false;
            if (onlyTextFromDoc[i].contains("-") && onlyTextFromDoc[i].charAt(0) != '-') {
                String[] split = onlyTextFromDoc[i].split("-");
                if (split.length == 2) {
                    Double leftNumber = isNumber(split[0]);
                    Double rightNumber = isNumber(split[1]);

                    String leftSide = split[0];
                    String rightSide = split[1];
                    if (leftNumber != null && !lastWord) {
                        leftSide = dealWithNumbers(docName,leftNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
                        addToterms(leftSide, docName,true);
                    }
                    if (rightNumber != null && !lastWord) {
                        rightSide = dealWithNumbers(docName,rightNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
                        addToterms(rightSide, docName,true);
                    }

                    if (!leftSide.contains("-") && !rightSide.contains("-")) {
                        addToterms(leftSide + "-" + rightSide,docName,true);
                    }
                    else if (leftNumber != null && rightNumber != null) {
                        jumpToNextWord = jumpToNextWord/2;
                        i += jumpToNextWord;
                        if (leftSide.contains("0")) leftSide = leftSide.substring(leftSide.lastIndexOf("0")+1);
                        else leftSide = leftSide.substring(leftSide.lastIndexOf("-")+1);
                        if (rightSide.contains("0")) rightSide = rightSide.substring(rightSide.lastIndexOf("0")+1);
                        else rightSide = rightSide.substring(rightSide.lastIndexOf("-")+1);
                        addToterms(leftSide,docName,true);
                        addToterms(rightSide,docName,true);
                        addToterms(leftSide + "-" + rightSide, docName,true);
                    }

                }
                else {
                    addToterms(onlyTextFromDoc[i], docName,false);
                }
                continue;
            } else if (months.containsKey(onlyTextFromDoc[i])) {
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
                        addToterms(onlyTextFromDoc[i], docName,true);
                        i++;
                        continue;

                    } catch (NumberFormatException e) {

                    }
                }
                addToterms(months.get(onlyTextFromDoc[i]), docName,true);
                continue;

            }
            else if (onlyTextFromDoc[i].equals("between") || onlyTextFromDoc[i].equals("Between")) {
                String finalTerm = dealWithNumbers(docName,isNumber(onlyTextFromDoc[i + 1]), i + 1, length,"", onlyTextFromDoc, false, false,"","", false, false, true);
                if (finalTerm != "") {
                    i++;
                    i += jumpToNextWord;
                    addToterms(finalTerm,docName,true);
                    if (finalTerm.contains("-")){
                        String[] split = finalTerm.split("-");
                        if (split.length == 2){
                            addToterms(split[0],docName,true);
                            addToterms(split[1],docName,true);
                        }
                    }
                }

                continue;
            }


            if (Character.isDigit(onlyTextFromDoc[i].charAt(0)) || onlyTextFromDoc[i].charAt(0) == '$') {
                if (onlyTextFromDoc[i].charAt(0) == '$' && onlyTextFromDoc[i].length() > 1) {
                    isDollar = true;
                    onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(1);
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
            if (number != null) {
                if (!lastWord)
                    addToterms(dealWithNumbers(docName,number, i, length, nextword, onlyTextFromDoc, isBillionAsWord, isDollar, percent, fraction, isBillion, isMillion, false), docName,true);
                else
                    addToterms(numberToTerm(number, isDollar, isBillion, isMillion, false, false, percent, fraction), docName,true);

            }
            else{
                if (onlyTextFromDoc[i].charAt(0) == onlyTextFromDoc[i].toUpperCase().charAt(0) && !lastWord){
                    while (!lastWord && i<length-1 ){
                        i++;
                        if (onlyTextFromDoc[i].charAt(0) == onlyTextFromDoc[i].toUpperCase().charAt(0)){

                        }
                    }
                }
                addToterms(stemmer.stemTerm(onlyTextFromDoc[i]), docName,false);


            }
            i += jumpToNextWord;

        }

    }


}





