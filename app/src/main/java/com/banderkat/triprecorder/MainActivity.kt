package com.banderkat.triprecorder

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.core.trip.session.*

class MainActivity : AppCompatActivity(), PermissionsListener, LocationObserver,
    RoutesRequestCallback, RouteProgressObserver, TripSessionStateObserver, BannerInstructionsObserver {

    private var recording = false
    private var nav: MapboxNavigation? = null
    private var permissionsManager: PermissionsManager? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d("MainActivity", "permission already granted")
            val options = MapboxNavigation.defaultNavigationOptionsBuilder(
                this,
                getString(R.string.mapbox_access_token)
            ).build()
            nav = MapboxNavigation(options)
            nav?.registerLocationObserver(this)
            nav?.registerRouteProgressObserver(this)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            recording = !recording

            var recordingString = if (recording) this.getString(R.string.start_recording)
            else this.getString(R.string.stop_recording)

            if (!recording) {
                nav?.stopTripSession()
                Log.d("MainActivity", "Stopped recording")
                val sessionState = nav?.getTripSessionState()
                Log.d("MainActivity", "session state: $sessionState")

                val history = nav?.retrieveHistory()
                val historyString = if (history.isNullOrBlank()) "" else history.toString()
                Log.d("MainActivity", historyString)
                recordingString += ": "
                recordingString += historyString

                nav?.toggleHistory(recording)
            } else {
                nav?.toggleHistory(recording)
                nav?.startTripSession()
                Log.d("MainActivity", "Started recording")
                // request route on recording start
                val startPoint = Point.fromLngLat(-75.1541475, 39.9614572)
                val endPoint = Point.fromLngLat(-75.14907539, 39.9642152069)
                val routeOptions: RouteOptions = RouteOptions.builder()
                    .applyDefaultParams()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .coordinates(listOf(startPoint, endPoint))
                    .build()
                nav?.requestRoutes(routeOptions, this)
            }

            Snackbar.make(view, recordingString, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d("MainActivity", "TODO: location explanation needed")
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            Log.d("MainActivity", "permission granted")
            nav = MapboxNavigation(
                MapboxNavigation.defaultNavigationOptionsBuilder(
                    applicationContext,
                    getString(R.string.mapbox_access_token)
                ).build()
            )
            nav?.registerLocationObserver(this)
            nav?.registerRouteProgressObserver(this)
        } else {
            Log.d("MainActivity", "permission denied")
        }
    }

    override fun onDestroy() {
        nav?.unregisterLocationObserver(this)
        nav?.unregisterRouteProgressObserver(this)
        nav?.unregisterBannerInstructionsObserver(this)
        nav?.onDestroy()
        super.onDestroy()
    }

    override fun onEnhancedLocationChanged(enhancedLocation: Location, keyPoints: List<Location>) {
        Log.d("Main", "enhanced location changed $enhancedLocation")
    }

    override fun onRawLocationChanged(rawLocation: Location) {
        Log.d("Main", "raw location changed $rawLocation")
    }

    override fun onRoutesReady(routes: List<DirectionsRoute>) {
        Log.d("Main", "routes ready!!!")
        Log.d("Main", routes.toString())
    }

    override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
        Log.w("Main", "routes request canceled")
    }

    override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
        Log.e("Main", "routes request failed!")
        Log.e("Main", throwable.message.toString())
    }

    override fun onRouteProgressChanged(routeProgress: RouteProgress) {
        Log.d("Main", "route progress: $routeProgress")
    }

    override fun onSessionStateChanged(tripSessionState: TripSessionState) {
        Log.d("Main", "session state changed: $tripSessionState")
    }

    override fun onNewBannerInstructions(bannerInstructions: BannerInstructions) {
        Log.d("Main", "got banner instructions: $bannerInstructions")
    }
}