package com.example.mahmoudsaed.bookstore_user;

import java.io.Serializable;


public class Publisher implements Serializable {
    private String email;
    private String name;
    private String id;
    private String address;
    private String imageUrl;

    public Publisher(String email, String id) {
        this.email = email;
        this.id = id;
    }

    public Publisher() {
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
