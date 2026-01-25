package org.example;

import java.util.ArrayList;

public class Library {

    private String name;
    private LibraryDownloads downloads;
    private ArrayList<Object> rules;

    public String getName() {
        return name;
    }

    public LibraryDownloads getDownloads() {
        return downloads;
    }

    public ArrayList<Object> getRules() {
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
