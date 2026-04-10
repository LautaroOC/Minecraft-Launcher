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
    private Scanner scanner = new Scanner(System.in);

    public void prepareManifest() throws MalformedURLException, IOException {
        objectMapper = new ObjectMapper();
        String manifestString = "https://piston-meta.mojang.com/mc/game/version_manifest.json";
        URL MANIFEST_URL = new URL(manifestString);

        try (InputStream in = MANIFEST_URL.openStream()) {
            versionManifest = objectMapper.readValue(in, VersionManifest.class);
        }
    }

    public Version versionSelector() {
       System.out.println("Minecraft versions: ");
       List<Version> releasedVersions = getReleasedVersions();
       for (Version version : releasedVersions) {
           System.out.println(version.getId());
       }
       System.out.println("***********************************");

       while(true){
           System.out.println("Select minecraft version (1.xx.xx):  ");
           String selectedVersion = scanner.nextLine().trim();
           for (Version version : releasedVersions) {
               if (version.getId().equals(selectedVersion)) {
                   return version;
               }
           }
           System.out.println("Select a correct version");
       }
    }

    public List<Version> getReleasedVersions() {
        List<Version> versions = new ArrayList<Version>();

        for (Version version : versionManifest.getVersions()) {
            if (version.getType().equals("release")) {
                versions.add(version);
            }
        }
        return versions;
    }
}
