package org.robotics.blinkworld.models

import com.mapbox.geojson.Point

data class MyMarker(
    val uid: String,
    val name: String,
    val imageUrl:String,
    val point: Point,
    val state:String,
    val Time:String
)
