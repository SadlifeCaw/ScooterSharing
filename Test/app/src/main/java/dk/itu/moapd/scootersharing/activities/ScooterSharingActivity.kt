package dk.itu.moapd.scootersharing.activities
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.databinding.ActivityScooterSharingBinding
import dk.itu.moapd.scootersharing.fragments.EditRideFragment
import dk.itu.moapd.scootersharing.fragments.MapsFragment
import dk.itu.moapd.scootersharing.fragments.ScooterSharingFragment
import dk.itu.moapd.scootersharing.fragments.StartRideFragment
import dk.itu.moapd.scootersharing.utils.MainActivityVM
import java.util.concurrent.TimeUnit

class ScooterSharingActivity : AppCompatActivity () {
    private lateinit var binding: ActivityScooterSharingBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    private val viewModel: MainActivityVM by lazy {
        ViewModelProvider(this)
            .get(MainActivityVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityScooterSharingBinding.inflate(layoutInflater)

        val lastFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (lastFragment == null) {
            viewModel.addFragment(ScooterSharingFragment())
            viewModel.addFragment(MapsFragment())
            viewModel.addFragment(EditRideFragment())
            viewModel.setFragment(0)
        }

        // Add the fragment into the activity.
        for (fragment in viewModel.getFragmentList())
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .hide(fragment)
                .commit()

        // The current activity.
        var activeFragment: Fragment = viewModel.fragmentState.value!!

        // Execute this when the user sets a specific fragment.
        viewModel.fragmentState.observe(this) { fragment ->
            supportFragmentManager
                .beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
            activeFragment = fragment
        }


        with (binding) {
            bottomNavigation?.selectedItemId = viewModel.getButtonId()

            topAppBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.more -> {
                        auth.signOut()
                        startLoginActivity()
                        true
                    }
                    else -> false
                }
            }

            bottomNavigation?.setOnItemSelectedListener { item ->
                when(item.itemId) {

                    // Select the Fragment 1 bottom.
                    R.id.page_1 -> {
                        viewModel.setFragment(0)
                        viewModel.setButtonId(R.id.page_1)
                        true
                    }

                    // Select the Fragment 2 bottom.
                    R.id.page_2 -> {
                        viewModel.setFragment(1)
                        viewModel.setButtonId(R.id.page_2)
                        true
                    }

                    // Select the Fragment 3 bottom.
                    R.id.page_3 -> {
                        viewModel.setFragment(2)
                        viewModel.setButtonId(R.id.page_3)
                        true
                    }

                    else -> false
                }
            }
        }

        // MÅSKE DETTE: setContentView(R.layout.activity_scooter_sharing)
        setContentView(binding.root)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
            startLoginActivity()
        val user = auth.currentUser
        println(user)
    }

    override fun onResume() {
        super.onResume()
        subscribeToLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        unsubscribeToLocationUpdates()
    }

    private fun startLocationAware() {

        // Show a dialog to ask the user to allow the application to access the device's location.
        requestUserPermissions()

        // Start receiving location updates.
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)

        // Initialize the `LocationCallback`.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                viewModel.onLocationChanged(locationResult.lastLocation)
            }
        }
    }

    private fun requestUserPermissions() {
        // An array with location-aware permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission)
        return result
    }

    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun subscribeToLocationUpdates() {
        // Check if the user allows the application to access the location-aware resources.
        if (checkPermission())
            return

        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(5)
            fastestInterval = TimeUnit.SECONDS.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Subscribe to location changes.
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun unsubscribeToLocationUpdates() {
        // Unsubscribe to location changes.
        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
    }
}