package org.example;

import java.util.ArrayList;

public class Download {
    private DownloadObject client;
    private DownloadObject client_mappings;
    private DownloadObject server;
    private DownloadObject server_mappings;

    public DownloadObject getServer() {
        return server;
    }

    public DownloadObject getServer_mappings() {
        return server_mappings;
    }

    public DownloadObject getClient() {
        return client;
    }

    public DownloadObject getClient_mappings() {
        return client_mappings;
    }


    @Override
    public String toString() {
        return client.toString();
    }
}
