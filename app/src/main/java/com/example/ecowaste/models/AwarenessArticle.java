package com.example.ecowaste.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class AwarenessArticle {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    @ServerTimestamp
    private Date publishedDate;

    public AwarenessArticle() {
        // Default constructor for Firestore
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }
}
