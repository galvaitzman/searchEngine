package sample;

import javafx.util.Pair;//
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.JSONFunctions;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;



public class ReadFile {


    public static int numOfDocs=0; // counting the total number of docs
    public int jumping50=0; // current index of file in filesInFolder
    public  List<Pair <String, String>> documents = new ArrayList<>(); //every pair represents the document and its text: Pair<String = document name, String = document text>
    public  StringBuilder stringBuilder; // string builder which collect the information (doc name, city, language, headline)
                                         // about the docs from the current 50 files
    public  Set<String> cities = new TreeSet<>(); // all the cities in the corpus after the <F P=104> tag
    public  Set<String> languages = new TreeSet<>(); // all the languages in the corpus after the <F P=105> tag
    public  List<File> filesInFolder = null; // contains pointers for all files in the document
    private String pathOfPostingAndDictionary;

    /**
     * making pointers for all files in the corpus
     * @param pathOfCorpusAndStopWord
     * @param postingAndDictionary
     */
    public ReadFile (String pathOfCorpusAndStopWord, String postingAndDictionary){

        this.pathOfPostingAndDictionary = postingAndDictionary;
        try {
            filesInFolder = Files.walk(Paths.get(pathOfCorpusAndStopWord))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        catch (IOException e){}////
        for (int i=0; i<filesInFolder.size(); i++){
            if (filesInFolder.get(i).getPath().endsWith("stop_words.txt")){
                filesInFolder.remove(i);
                return;
            }

        }
    }

    /**
     * reading 50 files each time and filling the documents list. writing the string builder which contains the information
     * about the documents, as described above. in this function we are using Jsoup object from 'jsoup-1.11.3.jar', which separates
     * between tags ( <DOC></DOC> for example) in order to improve running time.
     */
    public void read() {
        stringBuilder = new StringBuilder();
        for (int i=jumping50; i<filesInFolder.size() && i<jumping50+50; i++){
            try {
                String currentFileInString = new String(Files.readAllBytes(filesInFolder.get(i).toPath()));
                Document doc = Jsoup.parse(currentFileInString);
                Elements elements = doc.getElementsByTag("DOC");
                int [] indexesOfCity = new int[0];
                int [] indexesOfLanguage = new int [0];
                int [] indexesOfDocs = new int [elements.size()];
                if (currentFileInString.indexOf("<DOC>") != -1){
                    int index = currentFileInString.indexOf("<DOC>");
                    for (int currentDoc = 0; currentDoc<indexesOfDocs.length; currentDoc++){
                        indexesOfDocs [currentDoc] = index;
                        index = currentFileInString.indexOf("<DOC>", index + 1);
                    }
                }
                if (currentFileInString.indexOf("<F P=104>") != -1){
                    indexesOfCity = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=104>")+9;
                    int indexOfIndexesOfCity=0;
                    while (currentIndex >= 0 && indexOfIndexesOfCity < indexesOfCity.length) {
                        if (indexOfIndexesOfCity<indexesOfCity.length-1 && indexesOfDocs[indexOfIndexesOfCity] < currentIndex && indexesOfDocs[indexOfIndexesOfCity+1] > currentIndex){
                            indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        else if (indexesOfDocs[indexOfIndexesOfCity] < currentIndex){
                            indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        indexOfIndexesOfCity++;
                    }
                }
                if (currentFileInString.indexOf("<F P=105>") != -1){
                    indexesOfLanguage = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=105>")+9;
                    int indexOfIndexesOfLanguage=0;
                    while (currentIndex >= 0 && indexOfIndexesOfLanguage < indexesOfLanguage.length) {
                        if (indexOfIndexesOfLanguage<indexesOfLanguage.length-1 && indexesOfDocs[indexOfIndexesOfLanguage] < currentIndex && indexesOfDocs[indexOfIndexesOfLanguage+1] > currentIndex){
                            indexesOfLanguage[indexOfIndexesOfLanguage] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        else if (indexesOfDocs[indexOfIndexesOfLanguage] < currentIndex){
                            indexesOfLanguage[indexOfIndexesOfLanguage] = currentIndex;
                            currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                            if (currentIndex != -1) currentIndex = currentIndex+9;
                        }
                        indexOfIndexesOfLanguage++;
                    }

                }

                int currentElement =0;
                for (Element element:elements){
                    String s = element.children().toString();
                    String text = element.getElementsByTag("TEXT").text();
                    String name = element.getElementsByTag("DOCNO").text();
                    String city="";
                    String language="";
                    if (indexesOfCity.length != 0){
                        int index = indexesOfCity[currentElement];
                        if (index != 0){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfCity = 0;
                            int startOfCity =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfCity = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfCity = index;
                                }
                            }
                            city = currentFileInString.substring(startOfCity , lastOfCity).toUpperCase();}
                        else{
                            city="";
                        }
                    }
                    if (indexesOfLanguage.length != 0){
                        int index = indexesOfLanguage[currentElement];
                        if (index != 0 ){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfLanguage = 0;
                            int startOfLanguage =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfLanguage = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfLanguage = index;
                                }


                            }
                            language = currentFileInString.substring(startOfLanguage , lastOfLanguage).toUpperCase();
                        }
                        else language="";
                    }
                    String headLine="";
                    String endOfPath = filesInFolder.get(i).getPath().substring(filesInFolder.get(i).getPath().lastIndexOf("\\")+1);
                    if (endOfPath.startsWith("FB")){
                        headLine =  element.getElementsByTag("TI").text();

                    }
                    else if (endOfPath.startsWith("LA")){//
                        if ((s.indexOf("<headline>")) != -1) {
                            String temp = s.substring(s.indexOf("<headline>") + 10, s.indexOf("</headline>"));
                            int y=0;
                            int x=0;
                            while (x<2){
                                if (temp.charAt(y) == '<') x++;
                                y++;
                            }
                            headLine = temp.substring(7, y-1);
                        }
                    }
                    else{
                        String temp =  element.getElementsByTag("HEADLINE").text();
                        headLine = temp.substring(temp.indexOf('/')+1);
                    }

                    text = headLine + ". " + text;
                    documents.add(new Pair<>(name,text));
                    stringBuilder.append(name + "," + city + "," + language + "\n");
                    currentElement++;
                    numOfDocs++;

                }
            }
            catch (IOException e){e.printStackTrace();}}
        jumping50 += 50;
    }

    /**
     * going through all the documents and filling the cities Set and languages Set.
     * additionally, we are using the  API restcountries which gives us an information about the cities
     * (actually, about the capital cities only - the capital city's country,currency and population). we are using the strings
     * which are returned from the api and written as Json objects, and converting it into City object, using the objectMapper object
     * which implemented in 'jackson-all-1.9.0.jar'.
     */
    public void makeCityListAndLanguageList() {
        Map <String,String> detailsOfCities = new TreeMap<>();
        for (int i=0; i<filesInFolder.size(); i++){
            try {
                String currentFileInString = new String(Files.readAllBytes(filesInFolder.get(i).toPath()));
                Document doc = Jsoup.parse(currentFileInString);
                Elements elements = doc.getElementsByTag("DOC");
                int [] indexesOfCity = new int[0];
                if (currentFileInString.indexOf("<F P=104>") != -1){//
                    indexesOfCity = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=104>")+9;
                    int indexOfIndexesOfCity=1;
                    indexesOfCity[0] = currentIndex;
                    currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                    while (currentIndex != -1 && indexOfIndexesOfCity < indexesOfCity.length){
                        indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                        currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                        if (currentIndex != -1) currentIndex = currentIndex+9;
                        indexOfIndexesOfCity++;
                    }
                }
                int currentElement = 0;
                if (indexesOfCity.length != 0){
                    while (currentElement < indexesOfCity.length){
                        String city="";
                        int index = indexesOfCity[currentElement];
                        if (index != 0){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfCity = 0;
                            int startOfCity =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfCity = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfCity = index;
                                }
                            }
                            city = currentFileInString.substring(startOfCity , lastOfCity).toUpperCase();
                            if (city.length()>=2 && city.charAt(0) != '[') cities.add(city);
                        }
                        currentElement++;
                    }

                }
                int [] indexesOfLanguage = new int[0];
                if (currentFileInString.indexOf("<F P=105>") != -1){
                    indexesOfLanguage = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=105>")+9;
                    int indexOfIndexesOfLanguage=1;
                    indexesOfLanguage[0] = currentIndex;
                    currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                    while (currentIndex != -1 && indexOfIndexesOfLanguage < indexesOfLanguage.length){
                        indexesOfLanguage[indexOfIndexesOfLanguage] = currentIndex;
                        currentIndex = currentFileInString.indexOf("<F P=105>", currentIndex + 1);
                        if (currentIndex != -1) currentIndex = currentIndex+9;
                        indexOfIndexesOfLanguage++;
                    }
                }
                currentElement = 0;
                if (indexesOfLanguage.length != 0){
                    while (currentElement < indexesOfLanguage.length){
                        String language="";
                        int index = indexesOfLanguage[currentElement];
                        if (index != 0){
                            boolean foundFirstLetter = false;
                            boolean foundLastLetter = false;
                            int lastOfLanguage = 0;
                            int startOfLanguage =0;
                            for (; !foundFirstLetter || !foundLastLetter; index++){
                                if(foundFirstLetter && (currentFileInString.charAt(index) >122 || currentFileInString.charAt(index) <65)  ){
                                    foundLastLetter = true;
                                    lastOfLanguage = index;
                                }
                                else if (!foundFirstLetter && currentFileInString.charAt(index) >=65 && currentFileInString.charAt(index) <=122 ){
                                    foundFirstLetter = true;
                                    startOfLanguage = index;
                                }
                            }
                            language= currentFileInString.substring(startOfLanguage , lastOfLanguage).toUpperCase();
                            if (language.length()>=2) languages.add(language);
                        }
                        currentElement++;
                    }

                }


            }
            catch (IOException e){
                System.out.println(1);}
        }

        try {
            String urlString = "https://restcountries.eu/rest/v2/all?fields=name;population;currencies;capital";
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
            for(char c:detailsOfCity.toCharArray())
            {
                if (currentCharIndex == 0 || currentCharIndex == detailsOfCity.length()-1){
                    currentCharIndex++;
                    continue;
                }
                if (c == '{')
                    ++BracketCount;
                else if (c == '}')
                    --BracketCount;
                Json.append(c);

                if (BracketCount == 0 && c != ' ')
                {
                    if (!Json.toString().equals(","))JsonItems.add(Json.toString());
                    Json = new StringBuilder();
                }
                currentCharIndex++;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i=0; i<JsonItems.size(); i++){
                City city = objectMapper.readValue(JsonItems.get(i), City.class);
                if (cities.contains(city.capital.toUpperCase())){
                    detailsOfCities.put(city.capital.toUpperCase(),city.name + "," + city.currencies[2] + "," + Parse.numberToTerm(true,Double.parseDouble(city.population + ".0"),false,false,false,false,false,"","",false,false));
                }
            }
            con.disconnect();
        }
        catch (Exception e) {
            System.out.println(2);
        }
        for (String s:cities){
            if (detailsOfCities.get(s) == null){
                detailsOfCities.put(s, ",,");
            }
        }
        try {
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathOfPostingAndDictionary + "/citiesDetails.txt",true), StandardCharsets.UTF_8));
            for ( Map.Entry<String,String> entry : detailsOfCities.entrySet() ) {
                bufferWriter.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            bufferWriter.flush();
            bufferWriter.close();
        }
        catch (Exception e){
            System.out.println(3);
        }
        try {
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathOfPostingAndDictionary + "/languages.txt",true), StandardCharsets.UTF_8));
            for ( String s : languages ) {
                bufferWriter.write(s + "\n");
            }
            bufferWriter.flush();
            bufferWriter.close();
        }
        catch (Exception e){
            System.out.println(4);
        }

    }


}









