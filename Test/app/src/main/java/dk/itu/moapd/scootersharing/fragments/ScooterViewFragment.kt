package dk.itu.moapd.scootersharing.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentScooterViewBinding
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.models.User


class ScooterViewFragment : Fragment() {
    private var _binding: FragmentScooterViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var scooterid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterViewBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
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
                scootermodel.text = scooter?.model
                scooterlocation.text = scooter?.where
                scootertime.text = scooter?.timestamp.toString()
                scooterbatterylevel.text = scooter?.battery.toString()
                reserveButton.setOnClickListener{
                    val userEmail = auth.currentUser?.email!!.replace(".", "(dot)")
                    val userquery = database.child("users").child(userEmail)
                    userquery.get().addOnSuccessListener {
                        val currentUser = it.getValue<User>()
                        if (currentUser?.rentedScooterID == ""){
                            database.child("users").child(userEmail).child("rentedScooterID").setValue(scooterid)
                            database.child("scooters").child(scooterid).child("available").setValue(false)
                            val intent = Intent(requireContext(), ScooterSharingActivity::class.java)
                            startActivity(intent)
                        } else {
                            val toastText = "You are already renting ${currentUser?.rentedScooterID}."
                            Toast.makeText(requireContext(),toastText,Toast.LENGTH_SHORT).show()
                            AlertDialog.Builder(requireContext())
                                .setTitle("Already renting a scooter")
                                .setMessage("You are already renting ${currentUser?.rentedScooterID}. Do you want to change this to $scooterid?")
                                .setPositiveButton(
                                    getString(R.string.yes_button),
                                    DialogInterface.OnClickListener { dialog, which ->
                                        database.child("scooters").child(currentUser?.rentedScooterID!!).child("available").setValue(true)
                                        database.child("users").child(userEmail).child("rentedScooterID").setValue(scooterid)
                                        database.child("scooters").child(scooterid).child("available").setValue(false)
                                        val intent = Intent(requireContext(), ScooterSharingActivity::class.java)
                                        startActivity(intent)
                                    })
                                .setNegativeButton(getString(R.string.no_button),
                                    DialogInterface.OnClickListener { dialog, which -> })
                                .show()
                        }
                    }
                }
            }
        }
    }
}