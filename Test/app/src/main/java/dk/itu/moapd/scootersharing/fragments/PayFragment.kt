package dk.itu.moapd.scootersharing.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentPayBinding
import dk.itu.moapd.scootersharing.models.User
import kotlinx.android.synthetic.main.fragment_user_view.*

class PayFragment : Fragment() {
    private var _binding: FragmentPayBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser?.email!!.replace(".", "(dot)")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUser(user)
    }

    @SuppressLint("SetTextI18n")
    private fun getUser(userEmail: String) {
        database = Firebase.database("https://scooter-sharing-2ac71-default-rtdb.europe-west1.firebasedatabase.app/").reference
        database.keepSynced(true)

        val query = database.child("users").child(userEmail)
        query.get().addOnSuccessListener {
            val currentUser = it.getValue<User>()
            with(binding){
                user_displayname.setText("Hello " + currentUser?.displayname)
                user_debt.text = "You currently owe our company $" + currentUser?.debt.toString() + ". Pay now with MobilePay by clicking the button below."
                mobilepayButton.setOnClickListener{
                    database.child("users").child(userEmail).child("debt").setValue(0)
                    val toast = Toast.makeText(requireContext(), "Payment received! Thank you so much for sending the money voluntarily, so we avoid sending thugs after you.", Toast.LENGTH_LONG)
                    toast.show()
                    val intent = Intent(requireContext(), ScooterSharingActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}