package codingalecr.identityapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task




class MainActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PHOTO_RC = 1
        private const val CAPTURE_PHOTO_RC = 2
    }

    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        btn_addId.setOnClickListener {
            if (checkCameraHardware(this)) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, PICK_PHOTO_RC)
            }
        }
        btn_captureId.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, CAPTURE_PHOTO_RC);
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var stream: InputStream? = null
        when (requestCode) {
            PICK_PHOTO_RC -> {
                if(resultCode == Activity.RESULT_OK) {
                    try {
                        // recycle unused bitmaps
                        bitmap?.recycle()
                        data?.let {
                            stream = contentResolver.openInputStream(it.data)
                            bitmap = BitmapFactory.decodeStream(stream)
                            //Use bitmap for Firebase Text Recognition.
                            toast("The image was picked")
                            analizeText()
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } finally {
                        stream?.let {
                            try {
                                it.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            CAPTURE_PHOTO_RC -> {
                if(resultCode == Activity.RESULT_OK) {
                    data?.let {
                        val extras = data.extras
                        val imageBitmap = extras.get("data") as Bitmap
                        bitmap = imageBitmap
                        toast("I got an image")
                        analizeText()
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun analizeText() {
        bitmap?.let {
            iv_id.setImageBitmap(bitmap)
            val image =  FirebaseVisionImage.fromBitmap(it)

            val detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector(
                            FirebaseVisionBarcodeDetectorOptions
                                    .Builder()
                                    .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_PDF417)
                                    .build()
                    )
            val result = detector.detectInImage(image)
                    .addOnSuccessListener {
                        for (barcode in it) {
                            toast(barcode.rawValue.toString())
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        toast("Failed to recognize text.")
                    }


//            Text Reader.
//            val detector = FirebaseVision.getInstance().visionCloudTextDetector
//            val result = detector.detectInImage(image)
//                    .addOnSuccessListener {
//                        toast(it.text)
//                    }
//                    .addOnFailureListener {
//                        it.printStackTrace()
//                        toast("Failed to recognize text.")
//                    }
        }
    }

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }
}
