package com.couturier.musicapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PhotoPicker(
    fragment: Fragment,
    private val onPhotoSelected: (Uri?) -> Unit
) {

    private val photoResultLauncher: ActivityResultLauncher<Intent> =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoUri = result.data?.data
                onPhotoSelected(photoUri)
            }
        }

    fun openPhotoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/png"
        }
        photoResultLauncher.launch(intent)
    }
}