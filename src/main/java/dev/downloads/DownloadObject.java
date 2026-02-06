package dev.downloads;

public class DownloadObject {
    private String sha1;
    private int size;
    private String url;

    public String getUrl() {
        return url;
    }

    public int getSize() {
        return size;
    }

    public String getSha1() {
        return sha1;
    }

    public String toString(){
        return "DownloadObject{ " +
                "sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                '}';
    }
}
