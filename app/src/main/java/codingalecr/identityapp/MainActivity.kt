package codingalecr.identityapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        btn_barcode.setOnClickListener { view ->
//            searchBarCodes(bitmap)
        }
        btn_addId.setOnClickListener {
            CameraHelper.openGallery(this)
        }
        btn_captureId.setOnClickListener {
            dispatchTakePictureIntent()
        }

        if (CameraHelper.checkCameraHardware(this)) {
            if (!CameraHelper.checkPermissions(this)) {
                btn_captureId.isEnabled = false
                CameraHelper.askPermissions(this)
            }
        } else {
            btn_captureId.isEnabled = false
            // Also Let them know they need a camera.
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        CameraHelper.handlePermissionsResult(requestCode, grantResults) { granted ->
            when {
                granted -> btn_captureId.isEnabled = true
                else -> Log.i("MainActivity", "Permissions not granted.")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CameraHelper.PICK_PHOTO_RC -> {
                data?.let {
                    CameraHelper.handlePhotoPicked(it, this, resultCode,
                            placeholder = iv_id,
                            toastMessageString = "The photo was picked!")
                    searchBarCodes(CameraHelper.bitmap!!)
                }
            }
            CameraHelper.CAPTURE_PHOTO_RC -> {
                galleryAddPic()
                setPic()

//                searchBarCodes(CameraHelper.bitmap!!)
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun galleryAddPic() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW = iv_id.width
        val targetH = iv_id.height

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        searchBarCodes(bitmap)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(this, "codingalecr.identityapp", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CameraHelper.CAPTURE_PHOTO_RC)
            }
        }
    }

    private var mCurrentPhotoPath: String = ""

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    @SuppressLint("SetTextI18n")
    private fun searchBarCodes(bitmap: Bitmap) {
        iv_id.setImageBitmap(bitmap)
        tv_text.text = null
        toast("I am searching for barcodes. Bip! Bop! Bop!")

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val options = FirebaseVisionBarcodeDetectorOptions.Builder().setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build()

        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        detector.detectInImage(image).addOnSuccessListener {
            Log.d("MainActivity", "Barcodes detected: ${it.size}")
            for (barcode in it) {
                val bounds = barcode.boundingBox
                val corners = barcode.cornerPoints

                val rawValue = barcode.rawValue

                Log.d("MainActivityValue", barcode.valueType.toString())

                Log.d("MainActivity", rawValue)
                val p = IdentityUtils.parse(barcode.rawValue!!.toByteArray(StandardCharsets.ISO_8859_1)) as IdentityUtils.Person
                tv_text.text = p.toString()
            }
            Log.d("MainActivity", "Barcodes done!")
        }.addOnFailureListener {
            Log.e("MainActivity", it.message)
        }

    }

}
