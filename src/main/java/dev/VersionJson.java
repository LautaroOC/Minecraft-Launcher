package dev;

import dev.arguments.Argument;
import dev.downloads.Download;
import dev.javaVersion.JavaVersion;
import dev.libraries.Library;
import dev.assetIndex.AssetIndex;

import java.util.ArrayList;

public class VersionJson {
    private String id;
    private Argument arguments;
    private AssetIndex assetIndex;
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

    public AssetIndex getAssetIndex () {
        return assetIndex;
    }
}
