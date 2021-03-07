package com.example.reminderappsp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

const val LOCATION_REQUEST_CODE = 123
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val GEOFENCE_ID = "REMIDNER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_DWELL_DELAY = 3 * 1000 // 3 secs

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        if (!isLocationPermissionGranted()) {
            val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), LOCATION_REQUEST_CODE)
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if(it != null) {
                    with(map) {
                        val latlng = LatLng(it.latitude, it.longitude)
                        moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13f))
                    }
                }
                else {
                    with(map) {
                        moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(65.01324, 25.46619), 13f))
                    }
                }
            }
        }
        val meters = intent.getStringExtra("radius")
        setMapOnLongCLick(map, meters!!.toDouble())
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMapOnLongCLick(map: GoogleMap, radius: Double) {
        map.setOnMapLongClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Lng: %2$.5f",
                it.latitude,
                it.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Marker here")
                    .snippet(snippet)
            ).showInfoWindow()
            map.addCircle(
                    CircleOptions()
                            .center(it)
                            .strokeColor(Color.argb(50, 70, 70, 70))
                            .fillColor(Color.argb(70, 150, 150, 150))
                            .radius(radius)
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 13f))

            val database = Firebase.database("https://amplified-bee-306521-default-rtdb.firebaseio.com/")
            val reference = database.getReference("locations")
            val key = reference.push().key
            if (key != null) {
                val location = LocationInfo(key, it.latitude, it.longitude)
                reference.child(key).setValue(location)
            }
            createGeofence(it, key!!, geofencingClient, radius.toFloat())
        }
    }

    private fun createGeofence(location: LatLng, key: String, geofencingClient: GeofencingClient, radius: Float) {
        val geofence = Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(location.latitude, location.longitude, radius)
                .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(GEOFENCE_DWELL_DELAY).build()

        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build()

        val intent = Intent(this, GeofenceReceiver::class.java).putExtra("key", key)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), GEOFENCE_LOCATION_REQUEST_CODE)
            }
            else {
                geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            }
        }
        else {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
        }
        Handler().postDelayed({
            finish()
        }, 3000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
            if(permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "This app needs background location enabled.", Toast.LENGTH_LONG).show()
            }
        }

        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                map.isMyLocationEnabled = true
                onMapReady(map)
            }
            else {
                Toast.makeText(this, "This app needs location permission to function.", Toast.LENGTH_LONG).show()
            }
        }
    }
}