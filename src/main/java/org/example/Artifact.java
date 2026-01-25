package org.example;

public class Artifact {

    private String path;
    private String sha1;
    private int size;
    private String url;

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Artifact{ " +
                "path: " + path + ' ' +
                "sha1:" + sha1 + ' ' +
                "size: " + size + ' ' +
                "url: " + url;
    }

}
