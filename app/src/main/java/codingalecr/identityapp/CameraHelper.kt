package codingalecr.identityapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.widget.ImageView
import org.jetbrains.anko.doBeforeSdk
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


object CameraHelper {
    const val PICK_PHOTO_RC = 6001
    const val CAPTURE_PHOTO_RC = 6002
    const val CAMERA_PERMISSION_RC = 6003

    var bitmap: Bitmap? = null
    var file: Uri? = null

    fun openGallery(activity: Activity) {
        if (CameraHelper.checkCameraHardware(activity)) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            activity.startActivityForResult(intent, CameraHelper.PICK_PHOTO_RC)
        }
    }

    fun openCamera(activity: Activity) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile(activity)
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(activity, "codingalecr.identityapp", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activity.startActivityForResult(takePictureIntent, CameraHelper.CAPTURE_PHOTO_RC)
            }
        }
    }

    private var mCurrentPhotoPath: String = ""

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun galleryAddPic(context: Context) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(mCurrentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    private fun setPic(placeholder: ImageView) {

        // Get the dimensions of the View
        val targetW = placeholder.width
        val targetH = placeholder.height

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

        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
    }

    fun handlePhotoPicked(data: Intent, context: Context, resultCode: Int,
                          toastMessageString: String? = null,
                          toastMessageRes: Int? = null, placeholder: ImageView? = null) {
        var stream: InputStream? = null
        if (resultCode == Activity.RESULT_OK) {
            try {
                cleanBitmap()
                stream = context.contentResolver.openInputStream(data.data)
                bitmap = BitmapFactory.decodeStream(stream) as Bitmap
                showInImageView(placeholder)
                showToastMessage(toastMessageRes, context, toastMessageString)
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

    fun cleanBitmap() {
        bitmap?.recycle()
    }

    private fun showToastMessage(toastMessageRes: Int?, context: Context, toastMessageString: String?) {
        if (toastMessageRes != null) {
            context.toast(toastMessageRes)
        } else if (toastMessageString != null) {
            context.toast(toastMessageString)
        }
    }

    private fun showInImageView(placeholder: ImageView?) {
        placeholder?.setImageBitmap(bitmap!!)
    }

    /** Check if this device has a camera */
    fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun checkPermissions(context: Context): Boolean {
        var writePermission = true
        doBeforeSdk(Build.VERSION_CODES.KITKAT) {
            writePermission = checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return checkPermission(context, Manifest.permission.CAMERA) && writePermission
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(context, permission)
    }

    fun askPermissions(permissionRequestListener: Activity) {
        var permissions = arrayOf(Manifest.permission.CAMERA)
        doBeforeSdk(Build.VERSION_CODES.KITKAT) {
            permissions = permissions.plusElement(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(permissionRequestListener, permissions, CAMERA_PERMISSION_RC)
    }

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray,
                                permissionsGranted: (granted: Boolean) -> Unit) {
        if (CAMERA_PERMISSION_RC == requestCode) {
            var writePermission = true
            doBeforeSdk(Build.VERSION_CODES.KITKAT) {
                writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED
            }

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && writePermission) {
                permissionsGranted(true)
            } else {
                permissionsGranted(false)
            }
        }
    }
}