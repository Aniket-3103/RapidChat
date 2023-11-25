package com.mishraaniket.project.Models;

import java.util.ArrayList;

public class UserStatus {
    private String name, profileImage;
    private long lastUpdated;
    private ArrayList<Status> status;

    public UserStatus() {
    }

    public UserStatus(String name, String profileImage, long lastUpdated, ArrayList<Status> status) {
        this.name = name;
        this.profileImage = profileImage;
        this.lastUpdated = lastUpdated;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatus() {
        return status;
    }

    public void setStatus(ArrayList<Status> status) {
        this.status = status;
    }
}
