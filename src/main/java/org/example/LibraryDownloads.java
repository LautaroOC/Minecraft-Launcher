package org.example;

public class LibraryDownloads {
    public Artifact getArtifact() {
        return artifact;
    }

    private Artifact artifact;

    @Override
    public String toString() {
       return artifact.toString();
    }
}
