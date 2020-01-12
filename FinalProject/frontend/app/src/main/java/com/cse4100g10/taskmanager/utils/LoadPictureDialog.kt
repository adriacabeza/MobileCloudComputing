package com.cse4100g10.taskmanager.utils

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yalantis.ucrop.UCrop

class LoadPictureDialog(var activity : AppCompatActivity, var cropMode : Int, var title : String) {

    companion object{
        const val ACTION_GALLERY = 0x01
        const val ACTION_CAMERA = 0x02

        const val CROP_NONE = 0x00
        const val CROP_ROUND = 0x01
        const val CROP_SQUARE = 0x02

        const val PERMISSION_ALL = 0x77
        val PERMISSIONS_CAMERA = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val PERMISSIONS_GALLERY = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private var tempUri = Uri.EMPTY
    private var requiredPermissions: Array<String> = arrayOf()
    private var pendingAction = {}

    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRequiredPermissions(): Boolean = hasPermissions(requiredPermissions)

    fun showDialog() {
        var array = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(activity)
            .setItems(array) { _: DialogInterface, which: Int ->
                when (array[which]) {
                    "Camera" -> {
                        requiredPermissions = PERMISSIONS_CAMERA
                        pendingAction = {
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            tempUri = getTempProviderFile(activity, getTempFileName("jpg"))
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            activity.startActivityForResult(cameraIntent, ACTION_CAMERA)
                        }
                    }
                    "Gallery" -> {
                        requiredPermissions = PERMISSIONS_GALLERY
                        pendingAction = {
                            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            galleryIntent.type = "image/*"
                            galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivityForResult(galleryIntent, ACTION_GALLERY)
                        }
                    }
                }
                if(!hasRequiredPermissions())
                    ActivityCompat.requestPermissions(activity, requiredPermissions, PERMISSION_ALL)
                else
                    pendingAction()
            }
            .setTitle(title)
            .show()
    }

    fun handleCameraResult(data: Intent?) : Uri? {
        if (cropMode == CROP_NONE) {
            return tempUri
        }else{
            cropImage(tempUri, getTempFile(activity))
        }
        return null
    }

    fun handleGalleryResult(data: Intent?) : Uri? {
        if (cropMode == CROP_NONE){
            return data?.data
        } else{
            data?.data?.let {
                cropImage(it, getTempFile(activity))
            }
        }
        return null
    }

    fun handleCropResult(data: Intent?) : Uri? {
        return data?.let { UCrop.getOutput(it) }
    }

    private fun cropImage(uriIn: Uri, uriOut: Uri) {
        val cropOptions = UCrop.Options()
        cropOptions.setCircleDimmedLayer(cropMode == CROP_ROUND)
        cropOptions.setHideBottomControls(true)
        UCrop.of(uriIn, uriOut)
            .withAspectRatio(1f, 1f)
            .withOptions(cropOptions)
            .start(activity)
    }

    fun commitPendingAction(){
        pendingAction()
    }
}