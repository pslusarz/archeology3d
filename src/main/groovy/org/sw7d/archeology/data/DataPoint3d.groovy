package org.sw7d.archeology.data

class DataPoint3d {
    String name
    int x, y, z
    Object delegate
    String color = "Blue"
    String type = "box"
    int size = 1
    Scale3d scale = new Scale3d()
    
    int getScaledX() {        
        return x * scale.x
    }
    int getScaledY() {
        return y * scale.y
    }
    
    int getScaledZ() {
        return z * scale.z
    }
}

