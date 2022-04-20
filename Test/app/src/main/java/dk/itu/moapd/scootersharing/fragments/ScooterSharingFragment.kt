package dk.itu.moapd.scootersharing.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.RidesDB
import dk.itu.moapd.scootersharing.activities.EditRideActivity
import dk.itu.moapd.scootersharing.activities.LoginActivity
import dk.itu.moapd.scootersharing.activities.StartRideActivity
import dk.itu.moapd.scootersharing.adapters.RidesListXX
import dk.itu.moapd.scootersharing.databinding.FragmentScooterSharingBinding
import dk.itu.moapd.scootersharing.interfaces.ItemClickListener
import dk.itu.moapd.scootersharing.models.Scooter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ScooterSharingFragment : Fragment(), ItemClickListener{
    private var _binding: FragmentScooterSharingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var customAlertDialogView: View
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    companion object {
        lateinit var ridesDB : RidesDB
        private lateinit var adapter : RidesListXX
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentScooterSharingBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        ridesDB = RidesDB.get(context)
        super.onViewCreated(view, savedInstanceState)
        database = Firebase.database("https://scooter-sharing-2ac71-default-rtdb.europe-west1.firebasedatabase.app/").reference
        database.keepSynced(true)

        val query = database.child("scooters").child(auth.currentUser?.uid ?: "None") //.orderByChild("timestamp")
        val options = FirebaseRecyclerOptions.Builder<Scooter>().setQuery(query, Scooter::class.java).setLifecycleOwner(this).build()
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)



        with (binding) {
            adapter = RidesListXX(options)
            listOfRides.layoutManager = LinearLayoutManager(context)
            listOfRides.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            listOfRides.adapter = adapter
            floatingActionButton.setOnClickListener {
                customAlertDialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_add_data, binding.root, false)
                launchInsertAlertDialog()
            }
            description.setText(auth.currentUser?.email)
        }
    }

    override fun onItemClickListener(scooter: Scooter, position: Int) {
        customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_add_data, binding.root, false)

        launchUpdateAlertDialog(scooter, position)
    }

    private fun launchInsertAlertDialog() {
        val nameField = customAlertDialogView.findViewById<TextInputLayout>(R.id.name_text_field)
        val whereField = customAlertDialogView.findViewById<TextInputLayout>(R.id.where_text_field)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_add_title))
            .setMessage(getString(R.string.dialog_add_message))
            .setPositiveButton(getString(R.string.add_button)) { dialog, _ ->
                val name = nameField.editText?.text.toString()
                val where = whereField.editText?.text.toString()
                if (name.isNotEmpty()) {
                    val timestamp = System.currentTimeMillis()
                    val scooter = Scooter(name, where, timestamp)

                    val uid = database.child("scooters")
                        .child(auth.currentUser?.uid!!)
                        .push()
                        .key

                    database.child("scooters")
                        .child(auth.currentUser?.uid!!)
                        .child(uid!!)
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
        val nameField = customAlertDialogView.findViewById<TextInputLayout>(R.id.name_text_field)
        val whereField = customAlertDialogView.findViewById<TextInputLayout>(R.id.where_text_field)
        nameField.editText?.setText(scooter.name)

        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle(getString(R.string.dialog_update_title))
            .setMessage(getString(R.string.dialog_update_message))
            .setPositiveButton(getString(R.string.update_button)) { dialog, _ ->
                val name = nameField.editText?.text.toString()
                val where = whereField.editText?.text.toString()
                if (name.isNotEmpty()) {
                    scooter.name = name
                    scooter.where = where
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
}