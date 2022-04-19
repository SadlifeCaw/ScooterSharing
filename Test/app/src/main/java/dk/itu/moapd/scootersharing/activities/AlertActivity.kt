package dk.itu.moapd.scootersharing.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.itu.moapd.scootersharing.R

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.alert_title))
            .setMessage(getString(R.string.alert_supporting_text))
            .setNeutralButton(getString(R.string.cancel)) { dialog, which ->
                // TODO: Respond to neutral button press
                toast("Cancelled")
            }
            .setNegativeButton(getString(R.string.decline)) { dialog, which ->
                // TODO: Respond to negative button press
                toast("Declined")
            }
            .setPositiveButton(getString(R.string.accept)) { dialog, which ->
                // TODO: Respond to positive button press
                toast("Accepted")
            }
            .show()
    }

    private fun toast(text: CharSequence,
                      duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }
}