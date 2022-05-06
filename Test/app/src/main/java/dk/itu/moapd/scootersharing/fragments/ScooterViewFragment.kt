package dk.itu.moapd.scootersharing.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.enozom.poc.e_invoice.ZATCAScannerActivity
import com.enozom.poc.e_invoice.utils.ZATCAQRCode
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.R
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentScooterViewBinding
import dk.itu.moapd.scootersharing.models.Scooter
import dk.itu.moapd.scootersharing.models.User
import dk.itu.moapd.scootersharing.utils.MainActivityVM
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class ScooterViewFragment : Fragment() {
    private var _binding: FragmentScooterViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var scooterid: String
    private lateinit var viewModel: MainActivityVM
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private var qrCodeMatchID: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScooterViewBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        scooterid = arguments?.getString("scooterid", "CPH001")!!
        viewModel = ViewModelProvider(context as ScooterSharingActivity).get(MainActivityVM::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val scannedQRCode = result.data?.getSerializableExtra(
                        ZATCAScannerActivity.ZATCA_BILL_INFO) as? ZATCAQRCode

                    if(result.resultCode == Activity.RESULT_OK){
                        val toast = Toast.makeText(requireContext(), "QR-code accepted!", Toast.LENGTH_LONG)
                        toast.show()
                        database.child("users").child(auth.currentUser?.email!!.replace(".", "(dot)")).child("rentedScooterID").setValue(scooterid)
                        database.child("scooters").child(scooterid).child("available").setValue(false)
                        database.child("scooters").child(scooterid).child("timestamp").setValue(System.currentTimeMillis())
                        val intent = Intent(requireContext(), ScooterSharingActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
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
                val imgid = resources.getIdentifier(scooter?.imgstring, "drawable", requireContext().getPackageName())
                imageView.setImageDrawable(getDrawable(requireContext(), imgid))
                scootermodel.text = scooter?.model
                scooterlocation.text = scooter?.where
                scootertime.text = scooter?.timestamp!!.toDateString()
                scooterbatterylevel.text = scooter.battery.toString()
                rentButton.setOnClickListener{
                    val userEmail = auth.currentUser?.email!!.replace(".", "(dot)")
                    val userquery = database.child("users").child(userEmail)
                    userquery.get().addOnSuccessListener {
                        val currentUser = it.getValue<User>()
                        if (currentUser?.rentedScooterID == ""){
                            activityLauncher.launch(ZATCAScannerActivity.newIntent(requireActivity()))
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Already renting a scooter")
                                .setMessage("You are already renting ${currentUser?.rentedScooterID}. Do you want to change this to $scooterid?")
                                .setPositiveButton(
                                    getString(R.string.yes_button),
                                    DialogInterface.OnClickListener { dialog, which ->
                                        database.child("scooters").child(currentUser?.rentedScooterID!!).child("available").setValue(true)

                                        val oldScooterQuery = database.child("scooters").child(currentUser.rentedScooterID!!)
                                        oldScooterQuery.get().addOnSuccessListener {
                                            val oldScooter = it.getValue<Scooter>()
                                            val df = DecimalFormat("#.##")
                                            df.roundingMode = RoundingMode.DOWN
                                            val changeInDebt = df.format(((System.currentTimeMillis() - oldScooter?.timestamp!!)/300000.toDouble()) + 5).replace(",",".").toDouble()
                                            database.child("users").child(userEmail).child("debt").setValue(currentUser.debt!! + changeInDebt)
                                        }
                                        activityLauncher.launch(ZATCAScannerActivity.newIntent(requireActivity()))
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
    fun Long.toDateString() : String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
}