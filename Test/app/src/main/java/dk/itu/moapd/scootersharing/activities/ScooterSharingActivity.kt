package dk.itu.moapd.scootersharing.activities
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.databinding.ActivityScooterSharingBinding
import dk.itu.moapd.scootersharing.fragments.EditRideFragment
import dk.itu.moapd.scootersharing.fragments.ScooterSharingFragment
import dk.itu.moapd.scootersharing.fragments.StartRideFragment
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.utils.MainActivityVM

class ScooterSharingActivity : AppCompatActivity () {
    private lateinit var binding: ActivityScooterSharingBinding

    private lateinit var auth: FirebaseAuth

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
            viewModel.addFragment(StartRideFragment())
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
}