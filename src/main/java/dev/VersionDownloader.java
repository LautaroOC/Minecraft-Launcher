package dev;

import dev.arguments.game.Game;
import dev.arguments.game.GameArgumentObject;
import dev.arguments.game.GameRule;
import dev.arguments.jvm.Jvm;
import dev.arguments.jvm.JvmArgumentObject;
import dev.arguments.jvm.JvmRule;
import dev.libraries.Artifact;
import dev.libraries.Library;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VersionDownloader {

    private VersionJson versionJson;
    private ObjectMapper objectMapper;
    private Path versionsDirPathRelative;
    private Path versionDirPathRelative;
    private Path librariesDirPathRelative;
    private ArrayList<Library> libraries;

    public void downloadVersion() throws MalformedURLException, IOException {

        //Creamos los paths aca por ahora
        //Debo de buscar hadcer un metodo o fijarme en el diseño para crear los directorios.
        // No se si hacerlos en un metodo específico o chequear en cada metodo al descargar archivos nuevos si sus
        // respectivos dirs ya han sido creados, si no crearlos.
        String userHomeName = System.getProperty("user.home");
        Path userHomePath = Paths.get(userHomeName);
        Path minecraftDir = Paths.get(".minecraft");
        Path minecraftPathRelative = userHomePath.resolve(minecraftDir);

        Path versionsDirName = Paths.get("versions");
        Path versionsDirPathRelative = minecraftPathRelative.resolve(versionsDirName);
        Path librariesDirName = Paths.get("libraries");
        Path librariesDirPathRelative = minecraftPathRelative.resolve(librariesDirName);


        //Creating .minecraft and its subdirs
        Files.createDirectories(minecraftPathRelative);
        Files.createDirectory(versionsDirPathRelative);
        Files.createDirectory(librariesDirPathRelative);


        URL MOJANG_META_VERSION = new URL("https://piston-meta.mojang.com/v1/packages/a58855d96a196f67d2240cd903011463e73df88f/1.21.json");
        BufferedReader in = new BufferedReader(new InputStreamReader(MOJANG_META_VERSION.openStream()));

        String jsonText = "";
        String input;
        while ((input = in.readLine()) != null) {
            jsonText = jsonText.concat(input);
        }
        objectMapper = new ObjectMapper();
        versionJson = objectMapper.readValue(jsonText, VersionJson.class);
    }

    public void downloadClient() throws MalformedURLException, IOException {

        String downloadClientUrl = versionJson.getDownloads().getClient().getUrl();
        URL CLIENT_VERSION = new URL(downloadClientUrl);
        ByteArrayOutputStream byteArrayOutputStreamClient = new ByteArrayOutputStream();
        try (BufferedInputStream inputStreamClient =
                     new BufferedInputStream((CLIENT_VERSION.openStream()))) {

            int bytesRead = 0;
            byte[] bytesArray = new byte[8192];
            while ((bytesRead = inputStreamClient.read(bytesArray)) != -1) {
                byteArrayOutputStreamClient.write(bytesArray, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] clientBytes = byteArrayOutputStreamClient.toByteArray();
        createVersionDirectory();
        Path clientVersionFileName = Paths.get("client-" + versionJson.getId() + ".jar");
        Path clientVersionFilePath = versionDirPathRelative.resolve(clientVersionFileName);
        Files.write(clientVersionFilePath, clientBytes);

    }

    public void downloadLibraries() throws MalformedURLException, IOException {

        //How many libraries
        libraries = versionJson.getLibraries();
        int librariesAmount = libraries.size();

        //Getting all the attributes we will need for every single library
        for (int i = 0; i < librariesAmount; i++) {
            Library library = libraries.get(i);
            String name = library.getName();
            if (library.getDownloads().getArtifact() != null) {
                Artifact artifact = library.getDownloads().getArtifact();
                Path artifactPath = Paths.get(artifact.getPath());
                String artifactSha1 = artifact.getSha1();
                String artifactUrlString = artifact.getUrl();

                URL artifactURL = new URL(artifactUrlString);
                ByteArrayOutputStream byteArrayOutputStreamArtifact = new ByteArrayOutputStream();
                try (BufferedInputStream bufferedInputStream =
                             new BufferedInputStream(artifactURL.openStream())) {

                    int bytesRead = 0;
                    byte[] byteArray = new byte[8192];

                    while ((bytesRead = bufferedInputStream.read(byteArray)) != -1) {
                        byteArrayOutputStreamArtifact.write(byteArray, 0, bytesRead);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Path artifactLibrariesPathRelative = librariesDirPathRelative.resolve(artifactPath);
                Files.createDirectories(artifactLibrariesPathRelative.getParent());
                byte[] artifactBytes = byteArrayOutputStreamArtifact.toByteArray();
                Files.write(artifactLibrariesPathRelative, artifactBytes);
            }
        }
    }

    public void argumentsReformat() {

        //Reformat of the Arguments
        List<JsonNode> gameArgument = versionJson.getArguments().getGame();

        Game game = new Game();

        for (int i = 0; i < gameArgument.size(); i++) {
            JsonNode object = gameArgument.get(i);

            if (object.isTextual()) {
                String value = object.stringValue();
                game.addGameArgument(value);
            } else if (object.isObject()) {

                List<GameRule> rules = Arrays.asList(
                        objectMapper.treeToValue(object.get("rules"), GameRule[].class)
                );

                List<String> finalValues = new ArrayList<>();
                if (object.get("value").isArray()) {
                    List<String> values = Arrays.asList(
                            objectMapper.treeToValue(object.get("value"), String[].class)
                    );
                    finalValues.addAll(values);
                } else if (object.get("value").isTextual()) {
                    String value = objectMapper.treeToValue(object.get("value"), String.class);
                    finalValues.add(value);
                }

                game.addGameArgumentObject(new GameArgumentObject(rules, finalValues));
            }
        }
    }

    public void mapJvm() {
        List<JsonNode> jvmArgument = versionJson.getArguments().getJvm();
        Jvm jvm = new Jvm();

        for (int i = 0; i < jvmArgument.size(); i++) {

            JsonNode argument = jvmArgument.get(i);

            if (argument.isObject()) {

                //Rules
                JsonNode rulesJsonNode = argument.get("rules");
                //Rules siempre viene en Array
                List<JvmRule> finalRules = Arrays.asList(
                        objectMapper.treeToValue(rulesJsonNode, JvmRule[].class)
                );

                //Value
                JsonNode valueJsonNode = argument.get("value");
                List<String> finalValue = new ArrayList<String>();

                //Si el value viene en array
                if (valueJsonNode.isArray()) {
                    List<String> value = Arrays.asList(
                            objectMapper.treeToValue(valueJsonNode, String[].class)
                    );
                    finalValue.addAll(value);
                }
                //Si el value viene solo en String
                else if (valueJsonNode.isTextual()) {
                    String value = objectMapper.treeToValue(valueJsonNode, String.class);
                    finalValue.add(value);
                }

                //Resultado final para agregar a la lista de los argumentObject de un objecto Jvm
                jvm.addArgumentObject(new JvmArgumentObject(finalRules, finalValue));

                //Si el arguemento no es un objeto es un stringg
            } else if (argument.isTextual()) {
                String flag = objectMapper.treeToValue(argument, String.class);
                //Agregar a la lista de flags del objeto jvm
                jvm.addFlags(flag);
            }
        }
    }

    public void createNatives() throws IOException {
        Path nativesDirName = Paths.get("natives");
        Path nativesDirPathRelative = versionDirPathRelative.resolve(nativesDirName);
        Files.createDirectory(nativesDirPathRelative);

        //Adding the natives into the directory
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);

            if (library.getRules() != null) {
                if (library.getName().contains("native") && (library.getRules().getFirst().getOs().getName().equals("linux"))) {
                    String fileZipPath = librariesDirPathRelative + library.getDownloads().getArtifact().getPath();
                    File destFile = new File(nativesDirPathRelative.toString());

                    ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZipPath));
                    ZipEntry zipEntry;

                    while ((zipEntry = zis.getNextEntry()) != null) {
                        if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".so")) {
                            File nativeFile = newFile(destFile, zipEntry);

                            try (FileOutputStream fos = new FileOutputStream(nativeFile)) {
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        zis.closeEntry();
                    }
                    zis.close();
                }
            }

        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        String fileName = new File(zipEntry.getName()).getName();
        File destFile = new File(destinationDir, fileName);
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void createVersionDirectory() throws IOException {
        Path versionDirName = Paths.get(versionJson.getId());
        Path versionDirPathRelative = versionsDirPathRelative.resolve(versionDirName);
        Files.createDirectory(versionDirPathRelative);

    }
}
