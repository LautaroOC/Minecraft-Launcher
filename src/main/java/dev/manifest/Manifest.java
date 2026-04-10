package dev.manifest;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manifest {
    private VersionManifest versionManifest;
    private ObjectMapper objectMapper;

    public void prepareManifest() throws MalformedURLException, IOException {
        objectMapper = new ObjectMapper();
        String manifestString = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
        URL MANIFEST_URL = new URL(manifestString);

        try (InputStream in = MANIFEST_URL.openStream()) {
            versionManifest = objectMapper.readValue(in, VersionManifest.class);
        }
    }

    public String versionSelector() {
       System.out.println("Minecraft versions: ");
       List<String> releasedVersions = getReleasedVersions();
       for (String version : releasedVersions) {
           System.out.println(version);
       }
       System.out.println("***********************************");

       Scanner scanner = new Scanner(System.in);
       while(true){
           System.out.println("Select minecraft version (1.xx.xx):  ");
           String selectedVersion = scanner.nextLine();

               if (releasedVersions.contains(selectedVersion)) {
                   return selectedVersion;
               }
               else {
                   System.out.println("Select a correct version");
               }
       }
    }

    public List<String> getReleasedVersions() {
        List<String> versions = new ArrayList<String>();

        for (Version version : versionManifest.getVersions()) {
            if (version.getType().equals("release")) {
                versions.add(version.getId());
            }
        }
        return versions;
    }
}
