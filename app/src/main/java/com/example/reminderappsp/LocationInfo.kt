package com.example.reminderappsp

data class LocationInfo(var key: String = "", var lat: Double, var lng: Double) {
    constructor() : this("",65.01324, 25.46619) {
    }
}