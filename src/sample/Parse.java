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
    public static Map <String, TermInCorpus> termsInCorpusMap; //String=term
    public Map <String,TermInDoc> termsInDocMap = new HashMap<>();//
    public static Stemmer [] stemmers;
    public static Parse [] parsers;
    public int jumpToNextWord=0;


    public static Parse[] allParsers (){
        return parsers;
    }

    //adding term after changing to lowercase/uppercase
    private void directAddingTerm(String str, String docName){
        synchronized (termsInCorpusMap) {
            if (termsInCorpusMap.get(str) == null) {
                termsInCorpusMap.put(str, new TermInCorpus());
                termsInDocMap.put(docName + str, new TermInDoc(docName,str));

            }
            else {
                //termsInDocMap.get(str).get(docName).numberOfOccurencesInDoc++;
                termsInCorpusMap.get(str).numOfOccursInCorpus++;
                if (termsInDocMap.get(docName + str) == null){
                    termsInDocMap.put(docName + str,new TermInDoc(docName,str));
                    termsInCorpusMap.get(str).numOfDocs++;
                }
                else termsInDocMap.get(docName + str).numberOfOccurencesInDoc++;
            }
        }
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
            synchronized (termsInCorpusMap) {
                if (termsInCorpusMap.get(str.toUpperCase()) != null) str = str.toUpperCase();
                else if (termsInCorpusMap.get(str.toLowerCase()) != null) str = str.toLowerCase();
                else str = str.toUpperCase();
            }
                directAddingTerm(str,docName);
        }
        else if (str.matches(".*[a-z]+.*")) {
            synchronized (termsInCorpusMap) {
                if (termsInCorpusMap.get(str.toUpperCase()) != null) {
                    termsInCorpusMap.put(str.toLowerCase(),termsInCorpusMap.remove(str.toUpperCase()));
                    if (termsInDocMap.get(docName + str.toUpperCase()) != null){
                        termsInDocMap.put(docName + str.toLowerCase(),termsInDocMap.remove(str.toUpperCase()));
                    }

                }
            }
            directAddingTerm(str.toLowerCase(),docName);
        }
        else directAddingTerm(str,docName);





    }
    public static void startSpecificParser(String doc, String docName, int numberOfThread,String path){
        parsers[numberOfThread].parsingTextToText(doc,docName,numberOfThread,path);
    }
    public static void startParser(int numberofThreads){
        termsInCorpusMap = Collections.synchronizedMap(new HashMap<>());

        stemmers = new Stemmer[numberofThreads];
        parsers = new Parse[numberofThreads];
        for (int i=0; i<37; i++){
            stemmers[i] = new Stemmer();
            parsers[i] = new Parse();
        }
        months = new HashMap<>();
        numbersAsWords = new HashMap<>();
        stopWords = new HashSet<>();

        BufferedReader in = null;
        try{
            String currentWord;
            in = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/reasources/stop_words.txt"));
            while ((currentWord = in.readLine())!= null )  {
                stopWords.add(currentWord);
            }
            stopWords.remove("between");
        }
        catch (IOException e){
            System.out.println("IOException");
        }

        months.put("January", "01"); months.put("February", "02"); months.put("March", "03"); months.put("April", "04");months.put("May", "05");months.put("June", "06");months.put("July", "07");months.put("August", "08");months.put("September", "09");months.put("October", "10");months.put("November", "11");months.put("December", "12");
        months.put("JANUARY", "01"); months.put("FEBRUARY", "02"); months.put("MARCH", "03"); months.put("APRIL", "04");months.put("MAY", "05");months.put("JUNE", "06");months.put("JULY", "07");months.put("AUGUST", "08");months.put("SEPTEMBER", "09");months.put("OCTOBER", "10");months.put("NOVEMBER", "11");months.put("DECEMBER", "12");
        months.put("Jan", "01");     months.put("Feb", "02");      months.put("Mar", "03");   months.put("Apr", "04");  months.put("Jun", "06");months.put("Jul", "07");months.put("Aug", "08");months.put("Sep", "09");months.put("Oct", "10");months.put("Nov", "11");months.put("Dec", "12");

        numbersAsWords.put("zero","0");
        numbersAsWords.put("Zero","0");
        numbersAsWords.put("one","1");
        numbersAsWords.put("One","1");
        numbersAsWords.put("two","2");
        numbersAsWords.put("Two","2");
        numbersAsWords.put("Three","3");
        numbersAsWords.put("three","3");
        numbersAsWords.put("four","4");
        numbersAsWords.put("Four","4");
        numbersAsWords.put("five","5");
        numbersAsWords.put("Five","5");
        numbersAsWords.put("six","6");
        numbersAsWords.put("Six","6");
        numbersAsWords.put("seven","7");
        numbersAsWords.put("Seven","7");
        numbersAsWords.put("eight","8");
        numbersAsWords.put("Eight","8");
        numbersAsWords.put("nine","9");
        numbersAsWords.put("Nine","9");
        numbersAsWords.put("ten","10");
        numbersAsWords.put("Ten","10");
        numbersAsWords.put("eleven","11");
        numbersAsWords.put("Eleven","11");
        numbersAsWords.put("Twelve","12");
        numbersAsWords.put("twelve","12");
        numbersAsWords.put("thirteen","13");
        numbersAsWords.put("Thirteen","13");
        numbersAsWords.put("Fourteen","14");
        numbersAsWords.put("fourteen","14");
        numbersAsWords.put("fifteen","15");
        numbersAsWords.put("Fifteen","15");
        numbersAsWords.put("Sixteen","16");
        numbersAsWords.put("sixteen","16");
        numbersAsWords.put("Seventeen","17");
        numbersAsWords.put("seventeen","17");
        numbersAsWords.put("eighteen","18");
        numbersAsWords.put("Eighteen","18");
        numbersAsWords.put("nineteen","19");
        numbersAsWords.put("Nineteen","19");
        numbersAsWords.put("Twenty","20");
        numbersAsWords.put("twenty","20");
        numbersAsWords.put("Thirty","30");
        numbersAsWords.put("thirty","30");
        numbersAsWords.put("Forty","40");
        numbersAsWords.put("forty","40");
        numbersAsWords.put("fifty","50");
        numbersAsWords.put("Fifty","50");
        numbersAsWords.put("Sixty","60");
        numbersAsWords.put("sixty","60");
        numbersAsWords.put("seventy","70");
        numbersAsWords.put("Seventy","70");
        numbersAsWords.put("eighty","80");
        numbersAsWords.put("Eighty","80");
        numbersAsWords.put("Ninety","90");
        numbersAsWords.put("ninety","90");
        numbersAsWords.put("hundred","100");
        numbersAsWords.put("Hundred","100");
        numbersAsWords.put("thousand","1000");
        numbersAsWords.put("Thousand","1000");
        numbersAsWords.put("Million","1000000");
        numbersAsWords.put("million","1000000");
        numbersAsWords.put("Billion","1000000000");
        numbersAsWords.put("billion","1000000000");
        numbersAsWords.put("Trillion","1000000000000");
        numbersAsWords.put("trillion","1000000000000");

    }

    private Double isNumber (String str) {
        if (str.length() == 0) return null;
        if (numbersAsWords.containsKey(str)) return Double.valueOf(numbersAsWords.get(str));
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

    private String dealWithNumbers(Double number, int i, int length, String nextword, String[]onlyTextFromDoc,
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
                    rightSide = dealWithNumbers(isNumber(split[1]),i+1,length,nextword,onlyTextFromDoc,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
                }
                if (nextword.equals("and") && betweenAsWord){
                    rightSide = dealWithNumbers(isNumber(onlyTextFromDoc[nextWordIndex+1]),nextWordIndex+1,length,nextword,onlyTextFromDoc,isBillionAsWord,isDollar,percent,fraction,isBillion,isMillion,betweenAsWord);
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

    public void parsingTextToText(String doc, String docName, int numberOfThread,String path) {
        //long start = System.nanoTime();
        String [] onlyTextFromDoc = null;
        if (doc.contains("<text>")){
        onlyTextFromDoc = doc.substring(doc.indexOf("<text>") + 6, doc.indexOf("</text>")).split(" ");}
        else return;
        int length = onlyTextFromDoc.length;
        for (int i = 0; i <length; i++) {
            boolean isDollar = false;
            boolean isBillion = false;
            boolean isMillion = false;
            String percent = "";
            String fraction = "";
            String nextword = "";
            Double number = null;
            boolean lastWord = false;
            while (onlyTextFromDoc[i].length()>0 && (onlyTextFromDoc[i].endsWith(",") || onlyTextFromDoc[i].endsWith(".") || onlyTextFromDoc[i].endsWith(":") || onlyTextFromDoc[i].endsWith(";") || onlyTextFromDoc[i].endsWith("-") || onlyTextFromDoc[i].endsWith("?") || onlyTextFromDoc[i].endsWith(")") || onlyTextFromDoc[i].endsWith(""+'"'+"") || onlyTextFromDoc[i].endsWith("]") || onlyTextFromDoc[i].endsWith("'"))) {
                lastWord = true;
                onlyTextFromDoc[i] = onlyTextFromDoc[i].substring(0, onlyTextFromDoc[i].length() - 1);
            }
            if (i < length - 1 && (onlyTextFromDoc[i + 1].startsWith("(") || onlyTextFromDoc[i + 1].startsWith("" + '"' + "") || onlyTextFromDoc[i].startsWith("'"))) {
                lastWord = true;
            }
            while (onlyTextFromDoc[i].length()>0 && (onlyTextFromDoc[i].startsWith("(") || onlyTextFromDoc[i].startsWith(""+'"'+"") || onlyTextFromDoc[i].startsWith("'"))) {
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
                            leftSide = dealWithNumbers(leftNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
                            addToterms(leftSide, docName,true);
                        }
                        if (rightNumber != null && !lastWord) {
                            rightSide = dealWithNumbers(rightNumber, i, length, "", onlyTextFromDoc, false, false, "", "", false, false, false);
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
                        /*

                        else{
                            if (leftNumber != null) addToterms(leftSide,docName,true);
                            else addToterms(leftSide,docName,false);
                            if (rightNumber != null) addToterms(rightSide,docName,true);
                            else addToterms(leftSide,docName,false);
                            addToterms(onlyTextFromDoc[i], docName,false);
                        }

                        if (!leftSide.contains("-") && !rightSide.contains("-")) {
                            if (leftNumber == null || rightNumber == null)
                            addToterms(leftSide + "-" + rightSide, docName,false);
                            else
                                addToterms(leftSide + "-" + rightSide, docName,true);

                        }*/

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
                } else if (numbersAsWords.containsKey(onlyTextFromDoc[i])) {
                    onlyTextFromDoc[i] = numbersAsWords.get(onlyTextFromDoc[i]);
                }
                else if (onlyTextFromDoc[i].equals("between") || onlyTextFromDoc[i].equals("Between")) {
                    String finalTerm = dealWithNumbers(isNumber(onlyTextFromDoc[i + 1]), i + 1, length,"", onlyTextFromDoc, false, false,"","", false, false, true);
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
                    addToterms(dealWithNumbers(number, i, length, nextword, onlyTextFromDoc, isBillionAsWord, isDollar, percent, fraction, isBillion, isMillion, false), docName,true);
                else
                        addToterms(numberToTerm(number, isDollar, isBillion, isMillion, false, false, percent, fraction), docName,true);

            }
            else{
                addToterms(stemmers[numberOfThread].stemTerm(onlyTextFromDoc[i]), docName,false);

            }
            i += jumpToNextWord;

        }

    }


}





