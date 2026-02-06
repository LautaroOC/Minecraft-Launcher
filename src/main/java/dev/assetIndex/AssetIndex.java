package dev.assetIndex;

public class AssetIndex {
    private int Id;
    private String sha1;
    private int size;
    private int totalSize;
    private String url;


    public String getSha1() {
        return sha1;
    }

    public int getId() {
        return Id;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }



}
