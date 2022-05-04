package dk.itu.moapd.scootersharing.fragments

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.adapters.RidesListXX
import dk.itu.moapd.scootersharing.databinding.FragmentScooterSharingBinding
import dk.itu.moapd.scootersharing.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.models.Scooter
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.models.User
import dk.itu.moapd.scootersharing.utils.BUCKET_URL
import dk.itu.moapd.scootersharing.utils.DATABASE_URL
import java.io.IOException
import java.util.*
import kotlin.random.Random

class ScooterSharingFragment : Fragment(), ItemClickListener{
    private var _binding: FragmentScooterSharingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var customAlertDialogView: View
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var storage: FirebaseStorage

    companion object {
        private lateinit var adapter : RidesListXX
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScooterSharingBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        super.onViewCreated(view, savedInstanceState)
        database = Firebase.database(DATABASE_URL).reference
        storage = Firebase.storage(BUCKET_URL)
        database.keepSynced(true)

        val query = database.child("scooters").orderByChild("available").equalTo(true)
        val options = FirebaseRecyclerOptions.Builder<Scooter>().setQuery(query, Scooter::class.java).setLifecycleOwner(this).build()
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)



        with (binding) {
            adapter = RidesListXX(options)
            adapter.onItemClick = { scooter ->
                val args = Bundle()
                args.putString("scooterid", scooter.id)
                val fragment = ScooterViewFragment()
                fragment.arguments = args
                val fragmentManager: FragmentManager =
                    requireActivity().supportFragmentManager
                val fragmentTransaction: FragmentTransaction =
                    fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
            listOfRides.layoutManager = LinearLayoutManager(context)
            listOfRides.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            listOfRides.adapter = adapter
            floatingActionButton.setOnClickListener {
                customAlertDialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_add_data, binding.root, false)
                launchInsertAlertDialog()
            }
            val userQuery = database.child("users").child(auth.currentUser?.email!!.replace(".", "(dot)"))
            userQuery.get().addOnSuccessListener {
                val currentUser = it.getValue<User>()
                description.text = "Hello " + currentUser?.displayname + "!"
            }
        }
    }

    override fun onItemClickListener(scooter: Scooter, position: Int) {
        customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_add_data, binding.root, false)
        launchUpdateAlertDialog(scooter, position)
    }

    private fun launchInsertAlertDialog() {
        val idField = customAlertDialogView.findViewById<TextInputLayout>(R.id.id_text_field)
        val latField = customAlertDialogView.findViewById<TextInputLayout>(R.id.lat_text_field)
        val lonField = customAlertDialogView.findViewById<TextInputLayout>(R.id.lon_text_field)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_add_title))
            .setMessage(getString(R.string.dialog_add_message))
            .setPositiveButton(getString(R.string.add_button)) { dialog, _ ->
                val id = idField.editText?.text.toString()
                val lat = latField.editText?.text.toString().toDouble()
                val lon = lonField.editText?.text.toString().toDouble()
                val where = getAddress(lat, lon)

                if (id.isNotEmpty()) {
                    val timestamp = System.currentTimeMillis()
                    val battery = Random.nextInt(1, 100)
                    val model = "Xiaomi Mi Pro 2"
                    val url = storage.reference.child("27-1108_xl_1.jpg").downloadUrl.toString()
                    val path = storage.reference.child("27-1108_xl_1.jpg").toString()
                    val scooter = Scooter(id, lat, lon, battery, timestamp, model)
                    scooter.where = where

                   /* val uid = database.child("scooters")
                        .child(auth.currentUser?.uid!!)
                        .push()
                        .key*/

                    database.child("scooters")
                        .child(scooter.id!!)
                        .setValue(scooter)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun launchUpdateAlertDialog(scooter: Scooter, position: Int) {
        val idField = customAlertDialogView.findViewById<TextInputLayout>(R.id.id_text_field)
        val latField = customAlertDialogView.findViewById<TextInputLayout>(R.id.lat_text_field)
        val lonField = customAlertDialogView.findViewById<TextInputLayout>(R.id.lon_text_field)
        idField.editText?.setText(scooter.id)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_update_title))
            .setMessage(getString(R.string.dialog_update_message))
            .setPositiveButton(getString(R.string.update_button)) { dialog, _ ->
                val id = idField.editText?.text.toString()
                val lat = latField.editText?.text.toString().toDouble()
                val lon = lonField.editText?.text.toString().toDouble()
                if (id.isNotEmpty()) {
                    scooter.id = id
                    scooter.lat = lat
                    scooter.lon = lon
                    scooter.timestamp = System.currentTimeMillis()

                    val adapter = binding.listOfRides.adapter as RidesListXX
                    adapter.getRef(position).setValue(scooter)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val stringBuilder = StringBuilder()
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                stringBuilder.apply{
                    append(address.getAddressLine(0)).append("\n")
                    append(address.locality).append("\n")
                    append(address.postalCode).append("\n")
                    append(address.countryName)
                }
            } else return "Address not found!"
        } catch (ex: IOException) {}
        return stringBuilder.toString()
    }
}