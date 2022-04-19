package dk.itu.moapd.scootersharing.activities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dk.itu.moapd.scootersharing.fragments.EditRideFragment
import dk.itu.moapd.scootersharing.R

class EditRideActivity : AppCompatActivity () {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ride)
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_edit_container)
        if (currentFragment == null) {
            val fragment = EditRideFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_edit_container, fragment)
                .commit()
        }
    }
}