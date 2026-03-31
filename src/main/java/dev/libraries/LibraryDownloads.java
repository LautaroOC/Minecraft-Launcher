package dev.libraries;

import dev.downloads.DownloadObject;

import java.util.Map;

public class LibraryDownloads {

    private Artifact artifact;
    private Map<String, DownloadObject> classifiers;

    public Artifact getArtifact() {
        return artifact;
    }
    @Override
    public String toString() {
       return artifact.toString();
    }
}
