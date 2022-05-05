package dk.itu.moapd.scootersharing.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.maps.*
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentMapsBinding
import dk.itu.moapd.scootersharing.models.Scooter
import java.io.IOException
import java.util.*


class MapsFragment : Fragment(), OnMapsSdkInitializedCallback {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var scooterArrayList: ArrayList<Scooter>
    private lateinit var markerName: String
    private lateinit var where: String

    companion object {
        private val TAG = MapsFragment::class.qualifiedName
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        ScooterSharingActivity.database.child("scooters").orderByChild("available").equalTo(true).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val pos = LatLng(it.child("lat").getValue(Double::class.java)!!,
                        it.child("lon").getValue(Double::class.java)!!)
                    googleMap.addMarker(MarkerOptions()
                        .position(pos)
                        .title(it.child("id").getValue(String::class.java))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

                    googleMap.setOnMarkerClickListener { marker ->
                        markerName = marker.title!!
                        where = getAddress(marker.position.latitude, marker.position.longitude)
                        AlertDialog.Builder(requireContext())
                            .setTitle(markerName)
                            .setMessage(where)
                            .setPositiveButton(
                                getString(R.string.more_info_button),
                                DialogInterface.OnClickListener { dialog, which ->
                                    val args = Bundle()
                                    args.putString("scooterid", markerName)
                                    val fragment = ScooterViewFragment()
                                    fragment.arguments = args
                                    val fragmentManager: FragmentManager =
                                        activity!!.supportFragmentManager
                                    val fragmentTransaction: FragmentTransaction =
                                        fragmentManager.beginTransaction()
                                    fragmentTransaction.replace(R.id.fragment_container, fragment)
                                    fragmentTransaction.addToBackStack(null)
                                    fragmentTransaction.commit()
                                })
                            .setNegativeButton(getString(R.string.back_button),
                                DialogInterface.OnClickListener { dialog, which -> })
                            .show()
                        false
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                val toast = Toast.makeText(requireContext(), "Failed to connect to database", Toast.LENGTH_SHORT)
                toast.show()
            }
        })

        val itu = LatLng(55.6596, 12.5910)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 18f))
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if (!checkPermission()) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), Renderer.LATEST, this)
        scooterArrayList = arrayListOf<Scooter>()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST ->
                Log.d(TAG, "The latest version of the renderer is used.")
            Renderer.LEGACY ->
                Log.d(TAG, "The legacy version of the renderer is used.")
        }
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
