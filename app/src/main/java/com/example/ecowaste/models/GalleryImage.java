package com.example.ecowaste.models;

public class GalleryImage {
    private int imageResId;
    private String title;

    public GalleryImage(int imageResId, String title) {
        this.imageResId = imageResId;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
}
