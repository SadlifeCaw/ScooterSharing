package dk.itu.moapd.scootersharing.fragments
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.RidesDB
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentEditRideBinding

class EditRideFragment : Fragment() {

    private var _binding: FragmentEditRideBinding? = null
    private val binding get() = _binding!!

    companion object {
        lateinit var ridesDB : RidesDB
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val scooter : Scooter = Scooter ("", "", System.currentTimeMillis())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        ridesDB = RidesDB.get(context)

        super.onViewCreated(view, savedInstanceState)

        // Define the UI components behavior.
        with (binding) {
            updateButton.setOnClickListener {
                scooter.name = nameText.text.toString()
                scooter.where = whereText.text.toString()
                var containsscooter = false
                for (Scooter in ridesDB.getScooters()) {
                    if (Scooter.name == scooter.name) {
                        containsscooter = true
                        ridesDB.updateScooter(scooter.name!!, scooter.where!!)
                        Toast.makeText(
                            context,
                            R.string.successfully_placed_bike,
                            Toast.LENGTH_SHORT)
                            .show()
                        binding.lastAddedText.setText(ridesDB.getLastScooterInfo())
                        break
                    }
                }
                if (!containsscooter) {
                    Toast.makeText(
                        context,
                        R.string.could_not_find_bike,
                        Toast.LENGTH_SHORT)
                        .show()
                }
                updateUI()
            }
            backButton.setOnClickListener {
                val intent = Intent(context, ScooterSharingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun updateUI () {
        binding.whereText.text?.clear()
        binding.nameText.text?.clear()
    }
}