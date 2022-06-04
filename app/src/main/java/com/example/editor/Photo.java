package com.example.editor;

public class Photo {
    long id;
    String uri;
    String date;
    int size;
    long bucketId;
    String bucketName;

    public Photo(long id, String uri, String date, int size, long bucketId, String bucketName) {
        this.id = id;
        this.uri = uri;
        this.date = date;
        this.size = size;
        this.bucketId = bucketId;
        this.bucketName = bucketName;
    }
}
