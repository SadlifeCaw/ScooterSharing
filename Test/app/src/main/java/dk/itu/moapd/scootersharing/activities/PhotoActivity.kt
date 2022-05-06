package dk.itu.moapd.scootersharing.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dk.itu.moapd.scootersharing.databinding.ActivityPhotoBinding

class PhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Migrate from Kotlin synthetics to Jetpack view binding.
        // https://developer.android.com/topic/libraries/view-binding/migration
        binding = ActivityPhotoBinding.inflate(layoutInflater)

        // Set up the listener for back button.
        binding.sendPictureButton.setOnClickListener {
            val intent = Intent(this, ScooterSharingActivity::class.java)
            startActivity(intent)
        }

        // Showing the last taken image.
        val uri = Uri.parse(intent.getStringExtra("uri"))
        binding.imageView.setImageURI(uri)

        // Inflate the user interface into the current activity.
        setContentView(binding.root)
    }

}