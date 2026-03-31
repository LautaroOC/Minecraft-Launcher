package dev.libraries;

import java.util.ArrayList;
import java.util.Map;

public class Library {

    private String name;
    private LibraryDownloads downloads;
    private ArrayList<Rule> rules;
    private Map<String, String> natives;

    public String getName() {
        return name;
    }

    public LibraryDownloads getDownloads() {
        return downloads;
    }

    public Map<String, String> getNatives() {
        return natives;
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString(){
        return "Library{ " +
                "name: " + name + ' ' +
                "downloads: " + downloads + ' ' +
                "rules: " + rules;
    }

}
