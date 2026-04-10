package dev.manifest;

import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VersionManifest {
    private Object latest;
    private List<Version> versions;

    public List<Version> getVersions() {
        return versions;
    }

    public Object getLatest() {
        return latest;
    }
}
