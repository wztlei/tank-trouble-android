package com.wztlei.tanktrouble;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String mUsername;
    private int mX;
    private int mY;

    User(String username) {
        mUsername = username;
    }

    public void setX(int x) {
        mX = x;
    }

    public void setY(int y) {
        mY = y;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public String getUsername() {
        return mUsername;
    }

}
