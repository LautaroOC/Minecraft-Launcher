package org.example;

import java.util.ArrayList;
import java.util.Objects;

public class VersionJson {
    private String id;
    private Argument arguments;
    private Download downloads;
    private ArrayList<Library> libraries;
    private String mainClass;
    private JavaVersion javaVersion;

    public Download getDownloads() {
        return downloads;
    }

    public JavaVersion getJavaVersion() {
        return javaVersion;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Library> getLibraries() {
        return libraries;
    }

    public Argument getArguments() {
        return arguments;
    }
}
