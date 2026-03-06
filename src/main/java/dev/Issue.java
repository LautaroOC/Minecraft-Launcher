package dev;

import java.nio.file.Path;

public class Issue {
    private String issueName;
    private String sha1;
    private long size;
    private String url;
    private Path pathIssue;
    private

    public Issue(String issueName, String sha1, long size, String url, Path pathIssue) {
        this.issueName = issueName;
        this.sha1 = sha1;
        this.size = size;
        this.url = url;
        this.pathIssue = pathIssue;
    }

   public String getIssueName() {
       return issueName;
   }

   public String getSha1() {
       return sha1;
   }

   public long getSize() {
       return size;
   }

   public String getUrl() {
       return url;
   }

   public Path getPathIssue() {
       return pathIssue;
   }
}
