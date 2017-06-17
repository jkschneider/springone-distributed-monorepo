package com.gradle.summit.rewrite;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Step1DownloadFromGoogleCloudStorage {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ObjectMapper mapper = new ObjectMapper();
        String bucketUri = "https://www.googleapis.com/storage/v1/b/gradle-summit-rewrite/o";

        String listObjects = gcsRequest(bucketUri).parseAsString();

        List<Map<String, String>> objects = mapper.readValue(
                listObjects.substring(listObjects.indexOf("\"items\": ") + 9, listObjects.length()-2),
                new TypeReference<List<Map<String, String>>>() {});

        File localCache = new File(".rewrite/bucket");
        localCache.mkdirs();

        objects.parallelStream().map(o -> o.get("name")).forEach(name -> {
            System.out.println("Downloading " + name);
            File download = new File(localCache, name);
            if(!download.exists()) {
                try (FileOutputStream fos = new FileOutputStream(download)) {
                    gcsRequest(bucketUri + "/" + name + "?alt=media").download(fos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        System.out.println("Downloaded " + objects.size() + " files to ./bucket");
    }

    private static HttpResponse gcsRequest(String uri) {
        try {
            GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(
                    Collections.singleton("https://www.googleapis.com/auth/devstorage.read"));
            HttpRequestFactory requestFactory = GoogleNetHttpTransport.newTrustedTransport().createRequestFactory(credential);
            return requestFactory.buildGetRequest(new GenericUrl(uri)).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}