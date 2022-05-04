package dk.itu.moapd.scootersharing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.databinding.FragmentScooterViewBinding
import dk.itu.moapd.scootersharing.models.Scooter


class ScooterViewFragment : Fragment() {
    private var _binding: FragmentScooterViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference

    private lateinit var scooterid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterViewBinding.inflate(inflater, container, false)
        scooterid = arguments?.getString("scooterid", "CPH001")!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getScooter(scooterid)
    }

    private fun getScooter(scooterid: String) {
        database = Firebase.database("https://scooter-sharing-2ac71-default-rtdb.europe-west1.firebasedatabase.app/").reference
        database.keepSynced(true)

        val query = database.child("scooters").child(scooterid)
        query.get().addOnSuccessListener {
            val scooter = it.getValue<Scooter>()
            with(binding){
                scootertitle.text = scooter?.id
                scooterlocation.text = scooter?.where
                scootertime.text = scooter?.timestamp.toString()
                scooterbatterylevel.text = scooter?.battery.toString()
            }
        }
    }
}