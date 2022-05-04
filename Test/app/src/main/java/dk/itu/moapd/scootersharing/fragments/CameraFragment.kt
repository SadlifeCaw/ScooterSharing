package dk.itu.moapd.scootersharing.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.PhotoActivity
import dk.itu.moapd.scootersharing.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var outputDirectory: File
    private var imageUri: Uri? = null
    private var imageCapture: ImageCapture? = null

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


        if (allPermissionsGranted())
            startCamera()
        else
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        with (binding) {
            // Set up the listener for the change camera button.
            cameraSwitchButton.let {
                // Disable the button until the camera is set up
                it.isEnabled = false

                // Listener for button used to switch cameras. Only called if the button is enabled
                it.setOnClickListener {
                    cameraSelector = if (CameraSelector.DEFAULT_FRONT_CAMERA == cameraSelector)
                        CameraSelector.DEFAULT_BACK_CAMERA
                    else
                        CameraSelector.DEFAULT_FRONT_CAMERA

                    // Re-start use cases to update selected camera.
                    startCamera()
                }
            }

            cameraCaptureButton.setOnClickListener {
                Log.d("Output file Secound", outputDirectory.toString())
                takePhoto()
            }

            // Set up the listener for the photo view button.
            photoViewButton.setOnClickListener {
                if (imageUri != null) {
                    val intent = Intent(requireContext(), PhotoActivity::class.java).apply {
                        putExtra("uri", imageUri.toString())
                    }
                    startActivity(intent)
                }
            }
        }

        outputDirectory = getOutputDirectory()
        Log.d("Output File First", outputDirectory.toString())

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        // Create an instance of the `ProcessCameraProvider` to bind the lifecycle of cameras to the
        // lifecycle owner.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Add a listener to the `cameraProviderFuture`.
        cameraProviderFuture.addListener(Runnable{
            // Used to bind the lifecycle of cameras to the lifecycle owner.
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        val preview : Preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            updateCameraSwitchButton(cameraProvider)
        } catch (ex: Exception){
            Log.e("ToMyInfo", "Use case binding failed", ex)
        }
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

    private fun updateCameraSwitchButton(provider: ProcessCameraProvider) {
        try {
            binding.cameraSwitchButton.isEnabled =
                hasBackCamera(provider) && hasFrontCamera(provider)
        } catch (exception: CameraInfoUnavailableException) {
            binding.cameraSwitchButton.isEnabled = false
        }
    }

    private fun hasFrontCamera(provider: ProcessCameraProvider) = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    private fun hasBackCamera(provider: ProcessCameraProvider) = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case.
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image.
        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata.
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken.
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    imageUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $imageUri"
                    toast(msg)
                    Log.d("OnImageSaved", msg)
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e("OnImageSavedError", "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }

    private fun toast(text: CharSequence,
                      duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), text, duration).show()
    }

}