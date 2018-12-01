package sample;

import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class ReadFile {


    public int numOfDocs=0;
    public int jumping50=0;
    public  List<Pair <String, String>> documents = new ArrayList<>();
    public  StringBuilder stringBuilder;
    public  Map <String,String> detailsOfCities = new TreeMap<>();
    public  Set<String> languages = new TreeSet<>();
    private List<File> filesInFolder = null;
    private String path;


    public ReadFile (String path){
        this.path=path;
        try {
            filesInFolder = Files.walk(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }

        catch (IOException e){}
        for (int i=0; i<filesInFolder.size(); i++){
            if (filesInFolder.get(i).getPath().endsWith("stop_words.txt")){
                filesInFolder.remove(i);
                return;
            }

        }
    }
    public void setCitiesList(){
        for (int i=0; i<filesInFolder.size(); i++){

        }
    }
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
                    else if (endOfPath.startsWith("LA")){
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

                    //documents.put(name, text );
                    documents.add(new Pair<>(name,text));
                    stringBuilder.append(name + "," + city + "," + language +  "," + headLine +"\n");
                    currentElement++;
                    numOfDocs++;

                }
            }
            catch (IOException e){e.printStackTrace();}}
        jumping50 += 50;
    }

    public void makeCityListAndLanguageList() {
        Set<String> cities = new TreeSet<>();
        for (int i=0; i<filesInFolder.size(); i++){
            try {
                String currentFileInString = new String(Files.readAllBytes(filesInFolder.get(i).toPath()));
                Document doc = Jsoup.parse(currentFileInString);
                Elements elements = doc.getElementsByTag("DOC");
                int [] indexesOfCity = new int[0];
                if (currentFileInString.indexOf("<F P=104>") != -1){
                    indexesOfCity = new int [elements.size()];
                    int currentIndex = currentFileInString.indexOf("<F P=104>")+9;
                    int indexOfIndexesOfCity=1;
                    indexesOfCity[0] = currentIndex;
                    currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
                    while (currentIndex != -1 && indexOfIndexesOfCity < indexesOfCity.length){
                        indexesOfCity[indexOfIndexesOfCity] = currentIndex;
                        currentIndex = currentFileInString.indexOf("<F P=104>", currentIndex + 1);
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
                            if (city.length()>=2) cities.add(city);
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
            catch (IOException e){e.printStackTrace();}
        }
        for ( String s:cities ) {
            try {
                String urlString = "https://restcountries.eu/rest/v2/capital/" + s + "?fields=name;population;currencies";
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
                String currency = detailsOfCity.substring(detailsOfCity.indexOf("code")+7,detailsOfCity.indexOf("code")+10);
                int indexOfCountry = detailsOfCity.lastIndexOf("name")+7;
                while (detailsOfCity.charAt(indexOfCountry) != '"') indexOfCountry++;
                String country = detailsOfCity.substring(detailsOfCity.lastIndexOf("name")+7,indexOfCountry);
                int indexOfPopulation = detailsOfCity.indexOf("population")+12;
                while (detailsOfCity.charAt(indexOfPopulation) != '}') indexOfPopulation++;
                String population = detailsOfCity.substring(detailsOfCity.lastIndexOf("population")+12,indexOfPopulation);
                con.disconnect();
                //detailsOfCities.put(s,)
            }
            catch (Exception e) {
            }
        }



    }
}









