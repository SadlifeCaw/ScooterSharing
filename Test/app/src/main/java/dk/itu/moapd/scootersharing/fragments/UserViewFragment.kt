package dk.itu.moapd.scootersharing.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.LoginActivity
import dk.itu.moapd.scootersharing.activities.PhotoActivity
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentScooterViewBinding
import dk.itu.moapd.scootersharing.databinding.FragmentUserViewBinding
import dk.itu.moapd.scootersharing.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.models.User
import kotlinx.android.synthetic.main.activity_scooter_sharing.*
import kotlinx.android.synthetic.main.fragment_user_view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt


class UserViewFragment : Fragment() {
    private var _binding: FragmentUserViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var customAlertDialogView: View
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    private lateinit var user: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserViewBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser?.email!!.replace(".", "(dot)")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
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
                user_email.text = currentUser?.email
                user_displayname.text = currentUser?.displayname
                if (currentUser?.debt!! > 0.0){
                    user_debt.text = "You currently owe our company $" + currentUser.debt.toString()
                }

                if (currentUser.rentedScooterID != "") {
                    stopRentingButton.text = getString(R.string.stop_renting_button)
                    user_bikerental.text = "You are currently renting scooter ${currentUser.rentedScooterID}. In case you want to stop renting it, you can click the button below."
                    stopRentingButton.setOnClickListener{
                        database.child("scooters").child(currentUser.rentedScooterID!!).child("available").setValue(true)
                        val scooterQuery = database.child("scooters").child(currentUser.rentedScooterID!!)
                        scooterQuery.get().addOnSuccessListener {
                            val currentScooter = it.getValue<Scooter>()
                            val df = DecimalFormat("#.##")
                            df.roundingMode = RoundingMode.DOWN
                            val changeInDebt = df.format(((System.currentTimeMillis() - currentScooter?.timestamp!!)/300000.toDouble()) + 5).toDouble()
                            database.child("users").child(userEmail).child("debt").setValue(currentUser.debt!! + changeInDebt)
                        }
                        database.child("scooters").child(currentUser.rentedScooterID!!).child("timestamp").setValue(System.currentTimeMillis())
                        database.child("users").child(userEmail).child("rentedScooterID").setValue("")

                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                val fragment = UserViewFragment()
                                val fragmentManager: FragmentManager =
                                    requireActivity().supportFragmentManager
                                val fragmentTransaction: FragmentTransaction =
                                    fragmentManager.beginTransaction()
                                fragmentTransaction.replace(R.id.fragment_container, fragment)
                                fragmentTransaction.addToBackStack(null)
                                fragmentTransaction.commit()
                            },
                            1000
                        )
                    }
                } else {
                    stopRentingButton.text = getString(R.string.rent_new_scooter_button)
                    user_bikerental.text = getString(R.string.not_renting_scooter)
                    stopRentingButton.setOnClickListener{
                        val intent = Intent(requireContext(), ScooterSharingActivity::class.java)
                        startActivity(intent)
                    }
                }
                changeDisplaynameButton.setOnClickListener{
                    customAlertDialogView = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_change_displayname, binding.root, false)
                    launchInsertAlertDialog()
                }
            }
        }
    }

    private fun launchInsertAlertDialog() {
        val nameField = customAlertDialogView.findViewById<TextInputLayout>(R.id.text_field)
        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_change_displayname_title))
            .setMessage(getString(R.string.dialog_change_displayname_message))
            .setPositiveButton(getString(R.string.confirm_button)) { dialog, _ ->
                val newName = nameField.editText?.text.toString()

                if (newName.isNotEmpty()) {
                    database.child("users").child(auth.currentUser?.email!!.replace(".", "(dot)")).child("displayname").setValue(newName)
                }
                dialog.dismiss()
                /*
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .hide(this)
                    .show(UserViewFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
                 */
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}