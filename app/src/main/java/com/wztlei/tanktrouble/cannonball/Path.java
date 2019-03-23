package com.wztlei.tanktrouble.cannonball;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.UUID;

public class Path {
    private ArrayList<Coordinate> coords;
    private long uuid;

    @SuppressWarnings("unused")
    Path() {}

    Path(ArrayList<Coordinate> coords, long uuid) {
        this.uuid = uuid;
        this.coords = coords;
    }

    @PropertyName("coords")
    public ArrayList<Coordinate> getCoordinates() {
        return coords;
    }

    public Coordinate get(int index) {
        return coords.get(index);
    }

    public int size() {
        return coords.size();
    }

    @PropertyName("uuid")
    public long getUUID() {
        return uuid;
    }


}
