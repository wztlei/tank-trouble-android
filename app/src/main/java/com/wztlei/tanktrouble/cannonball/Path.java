package com.wztlei.tanktrouble.cannonball;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.UUID;

public class Path {
    private ArrayList<Coordinate> coords;
    private int uuid;

    @SuppressWarnings("unused")
    Path() {}

    Path(ArrayList<Coordinate> coords, int uuid) {
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
    public int getUUID() {
        return uuid;
    }


}
