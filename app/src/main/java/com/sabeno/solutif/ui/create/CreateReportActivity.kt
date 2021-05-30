package com.sabeno.solutif.ui.create

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityCreateReportBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class CreateReportActivity : AppCompatActivity(), View.OnClickListener {

    private val createReportViewModel: CreateReportViewModel by inject()

    private lateinit var binding: ActivityCreateReportBinding
    private lateinit var photoFile: File

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationManager: LocationManager

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private const val FILE_NAME = "report_image_"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        takePicture()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        binding.btnSubmit.setOnClickListener(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createReportViewModel.spinner.observe(this, { value ->
            value.let { show ->
                binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
                binding.btnSubmit.isEnabled = !show
                binding.tietDesc.isEnabled = !show
            }
        })

        createReportViewModel.toast.observe(this, { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                createReportViewModel.onToastShown()
            }
        })

        generateCurrentLocation()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile()
        val fileProvider =
            FileProvider.getUriForFile(this, "com.sabeno.solutif.fileprovider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (intent.resolveActivity(this.packageManager) != null) {
            resultLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(FILE_NAME, ".jpg", storageDir)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            photoFile.delete()
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

                val ei = ExifInterface(photoFile.absolutePath)
                val rotatedBitmap: Bitmap? = when (ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> {
                        rotateImage(takenImage, 90f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_180 -> {
                        rotateImage(takenImage, 180f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> {
                        rotateImage(takenImage, 270f)
                    }
                    else -> {
                        takenImage
                    }
                }
                binding.ivPhotoPreview.setImageBitmap(rotatedBitmap)
            } else if (result.resultCode == 0) {
                finish()
            }
        }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun validateDescription(): Boolean {
        val desc = binding.tietDesc.text.toString().trim()

        return when {
            desc.isEmpty() -> {
                binding.tilDesc.error = resources.getString(R.string.desc_is_empty)
                false
            }
            desc.length > 50 -> {
                binding.tilDesc.error = resources.getString(R.string.desc_is_51)
                false
            }
            else -> {
                binding.tilDesc.error = ""
                true
            }
        }
    }

    private fun generateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_submit -> {
                if (validateDescription()) {
                    Log.d("LATLONG", "$latitude - $longitude")
                    coroutineScope.launch {
                        createReportViewModel.uploadPhoto(
                            binding.tietDesc.text.toString(),
                            latitude,
                            longitude,
                            photoFile.toUri(),
                            this@CreateReportActivity
                        )
                    }
                }
            }
        }
    }
}