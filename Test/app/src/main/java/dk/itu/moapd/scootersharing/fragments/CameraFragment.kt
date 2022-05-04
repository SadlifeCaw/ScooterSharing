package dk.itu.moapd.scootersharing.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity
import dk.itu.moapd.scootersharing.databinding.FragmentCameraBinding
import dk.itu.moapd.scootersharing.databinding.FragmentScooterSharingBinding
import dk.itu.moapd.scootersharing.interfaces.QRCodeListener
import dk.itu.moapd.scootersharing.utils.QrCodeAnalyzer
import kotlinx.android.synthetic.main.activity_scooter_sharing.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private var qrCode: String = "No Information"
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted())
            startCamera()

        with (binding) {
            // Set up the listener for the change camera button.
            qrCodeFoundButton.visibility = View.VISIBLE
            qrCodeFoundButton.setOnClickListener{
                Toast.makeText(activity, qrCode, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        // Create an instance of the `ProcessCameraProvider` to bind the lifecycle of cameras to the
        // lifecycle owner.
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        // Add a listener to the `cameraProviderFuture`.
        cameraProviderFuture.addListener(Runnable{
            // Used to bind the lifecycle of cameras to the lifecycle owner.
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        val preview : Preview = Preview.Builder().build()

        val cameraSelector : CameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(view_finder.width, view_finder.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrResult ->
                    view_finder.post {
                        Toast.makeText(context, qrResult.text,Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    }
                })
            }
        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(view_finder.surfaceProvider);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(
                    context,
                    "Permissions granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }
}