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
import dk.itu.moapd.scootersharing.databinding.FragmentStartRideBinding

class StartRideFragment : Fragment() {
    private var _binding: FragmentStartRideBinding? = null
    private val binding get() = _binding!!

    companion object {
        lateinit var ridesDB: RidesDB
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentStartRideBinding.inflate(inflater, container, false)
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
            startButton.setOnClickListener {
                if (nameText.text.toString().length > 3 && whereText.text.toString().length > 3) {
                    scooter.name = nameText.text.toString()
                    scooter.where = whereText.text.toString()
                    Toast.makeText(
                        context,
                        R.string.successfully_placed_bike,
                        Toast.LENGTH_SHORT)
                        .show()
                    ridesDB.addScooter(scooter.name!!, scooter.where!!)
                    binding.lastAddedText.setText(ridesDB.getLastScooterInfo())
                } else {
                    Toast.makeText(
                        context,
                        R.string.too_short,
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