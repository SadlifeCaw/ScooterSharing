package dk.itu.moapd.scootersharing.activities
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.enozom.poc.e_invoice.ZATCAScannerActivity
import com.enozom.poc.e_invoice.utils.ZATCAQRCode
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.databinding.ActivityScooterSharingBinding
import dk.itu.moapd.scootersharing.fragments.*
import dk.itu.moapd.scootersharing.models.User
import dk.itu.moapd.scootersharing.utils.MainActivityVM
import java.util.concurrent.TimeUnit

class ScooterSharingActivity : AppCompatActivity () {

    private lateinit var binding: ActivityScooterSharingBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
        lateinit var database : DatabaseReference
    }

    private val viewModel: MainActivityVM by lazy {
        ViewModelProvider(this)
            .get(MainActivityVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityScooterSharingBinding.inflate(layoutInflater)
        database = Firebase.database("https://scooter-sharing-2ac71-default-rtdb.europe-west1.firebasedatabase.app/").reference
        database.keepSynced(true)
        val userEmail = auth.currentUser?.email!!.replace(".", "(dot)")
        val query = database.child("users").child(userEmail)
        query.get().addOnSuccessListener{
            val currentUser = it.getValue<User>()
            if (currentUser?.email == null){
                val user = User(auth.currentUser?.email!!, auth.currentUser?.email!!, "", 0.0)
                database.child("users").child(userEmail).setValue(user)
            }
        }

        val lastFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        startLocationAware()
        if (lastFragment == null) {
            viewModel.addFragment(ScooterSharingFragment())
            viewModel.addFragment(MapsFragment())
            viewModel.addFragment(CameraFragment())
            viewModel.addFragment(UserViewFragment())
            viewModel.addFragment(QrFragment())
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
                    R.id.sign_out -> {
                        auth.signOut()
                        startLoginActivity()
                        true
                    }
                    R.id.profile -> {
                        viewModel.setFragment(3)
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
                        viewModel.setFragment(4)
                        viewModel.setButtonId(R.id.page_3)
                        true
                    }

                    else -> false
                }
            }
        }
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