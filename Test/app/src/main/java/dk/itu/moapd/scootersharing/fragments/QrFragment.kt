package dk.itu.moapd.scootersharing.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.enozom.poc.e_invoice.ZATCAScannerActivity
import com.enozom.poc.e_invoice.utils.ZATCAQRCode
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.databinding.FragmentCameraBinding
import dk.itu.moapd.scootersharing.databinding.FragmentQrBinding

class QrFragment : Fragment() {

    private lateinit var binding: FragmentQrBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityLuncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scannedQRCode = result.data?.getSerializableExtra(ZATCAScannerActivity.ZATCA_BILL_INFO) as? ZATCAQRCode
                //Do something with scanned QR code
            }
        }

        binding.backButton.setOnClickListener {
            activityLuncher.launch(ZATCAScannerActivity.newIntent(context))
        }

    }
}