// app/src/main/java/com/queridinhos/tcc/FloorMap.java
package com.queridinhos.tcc;

public class FloorMap {
    final String floorName;
    final int imageResId;

    public FloorMap(String floorName, int imageResId) {
        this.floorName = floorName;
        this.imageResId = imageResId;
    }

    public String getFloorName() {
        return floorName;
    }

    public int getImageResId() {
        return imageResId;
    }
}