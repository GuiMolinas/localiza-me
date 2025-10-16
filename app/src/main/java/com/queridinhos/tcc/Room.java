package com.queridinhos.tcc;

import android.graphics.Path;

public class Room {
    private final String name;
    private final int floorIndex; // 0 para Térreo, 1 para 1º Andar, etc.
    private final Path area;      // O polígono que define a área da sala

    public Room(String name, int floorIndex, Path area) {
        this.name = name;
        this.floorIndex = floorIndex;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public int getFloorIndex() {
        return floorIndex;
    }

    public Path getArea() {
        return area;
    }
}