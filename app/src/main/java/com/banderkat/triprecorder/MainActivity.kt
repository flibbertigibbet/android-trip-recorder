package com.banderkat.triprecorder

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.navigation.core.MapboxNavigation

class MainActivity : AppCompatActivity(), PermissionsListener {

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
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            recording = !recording

            if (recording) nav?.startTripSession() else nav?.stopTripSession()
            nav?.toggleHistory(recording)

            var recordingString = if (recording) this.getString(R.string.start_recording)
            else this.getString(R.string.stop_recording)

            if (!recording) {
                Log.d("MainActivity", "Stopped recording")
                val history = nav?.retrieveHistory()
                val historyString = if (history.isNullOrBlank()) "" else history.toString()
                Log.d("MainActivity", historyString)
                recordingString += ": "
                recordingString += historyString
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
        } else {
            Log.d("MainActivity", "permission denied")
        }
    }
}