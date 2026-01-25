package org.example;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {

            URL MOJANG_META_VERSION = new URL("https://piston-meta.mojang.com/v1/packages/a58855d96a196f67d2240cd903011463e73df88f/1.21.json");
            BufferedReader in = new BufferedReader(new InputStreamReader(MOJANG_META_VERSION.openStream()));

            String jsonText = "";
            String input;
            while ((input = in.readLine()) != null) {
                jsonText = jsonText.concat(input);
            }

            System.out.println(jsonText);

            ObjectMapper objectMapper = new ObjectMapper();
            VersionJson versionJson = objectMapper.readValue(jsonText, VersionJson.class);

        System.out.println("ID: " + versionJson.getId());
        System.out.println("Arguments: " + "game: " + versionJson.getArguments().getGame() + "jvm: " + versionJson.getArguments().getJvm());
        System.out.println("Downloads: " + versionJson.getDownloads().getClient());
        System.out.println("Main class: " + versionJson.getMainClass());
        System.out.println("Java version: " + versionJson.getJavaVersion().getMajorVersion());
        System.out.println("Libraries: " + versionJson.getLibraries());

        String userHomePath = System.getProperty("user.home");
        String minecraftDir = ".minecraft";
        String minecraftPath = userHomePath + "/" + minecraftDir;
        //Creating .minecraft
        boolean createMinecraftDirectory = new File(minecraftPath).mkdir();

        //clientDownloadURl
        String downloadClientUrl = versionJson.getDownloads().getClient().getUrl();

        //There is no path in the downloads.client in any of them at least in this version.
        //Path is made by the launcher
        String versionPath = minecraftPath + "/versions/";
        //Versions directory creation
        boolean createVersionsDirectory = new File(versionPath).mkdir();
        //Creating the version directory
        String versionDirectoryPath = versionPath + "/" + versionJson.getId();
        boolean createVersionDirectory = new File(versionDirectoryPath).mkdir();
        //Getting the clientFile
        URL CLIENT_VERSION = new URL(downloadClientUrl);
        ByteArrayOutputStream byteArrayOutputStreamClient = new ByteArrayOutputStream();
        try {
            BufferedInputStream inputStreamClient = new BufferedInputStream((CLIENT_VERSION.openStream()));

            int bytesRead = 0;
            byte[] bytesArray = new byte[8192];
            while ((bytesRead = inputStreamClient.read(bytesArray)) != -1) {
                byteArrayOutputStreamClient.write(bytesArray, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] clientBytes = byteArrayOutputStreamClient.toByteArray();
        String versionClientPathString = versionDirectoryPath + "/" + "client-" + versionJson.getId() + ".jar";
        Path versionClientPath = Paths.get(versionClientPathString);
        Files.write(versionClientPath, clientBytes);


        //Libraries path
        String librariesPath = minecraftPath + "/libraries/";
        boolean createLibrariesDirectory = new File(librariesPath).mkdir();
        //How many libraries
        ArrayList<Library> libraries = versionJson.getLibraries();
        int librariesAmount = libraries.size();

        //Getting all the attributes we will need for every single library
        for (int i = 0; i < librariesAmount; i ++) {
            Library library = libraries.get(i);
            String name = library.getName();
            if (library.getDownloads().getArtifact() != null) {
                Artifact artifact = library.getDownloads().getArtifact();
                String artifactPathString = artifact.getPath();
                String artifactSha1 = artifact.getSha1();
                String artifactUrlString = artifact.getUrl();

                URL artifactURL = new URL(artifactUrlString);
                ByteArrayOutputStream byteArrayOutputStreamArtifact = new ByteArrayOutputStream();
                try {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(artifactURL.openStream());
                    int bytesRead = 0;
                    byte[] byteArray = new byte[8192];

                    while((bytesRead = bufferedInputStream.read(byteArray)) != -1) {
                        byteArrayOutputStreamArtifact.write(byteArray, 0, bytesRead);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String artifactPathStringInLibraries = librariesPath + artifactPathString;
                Path artifactPath = Paths.get(artifactPathStringInLibraries);
                byte[] artifactBytes = byteArrayOutputStreamArtifact.toByteArray();
                Files.write(artifactPath, artifactBytes);
            }
        }

    }
}