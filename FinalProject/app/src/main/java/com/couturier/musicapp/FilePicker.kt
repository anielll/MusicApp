package com.couturier.musicapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class FilePicker(
    fragment: Fragment
) {
    private lateinit var onFileSelected: (Uri) -> Unit
    private val fileResultLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            onFileSelected(uri)
        } else {
            Toast.makeText(fragment.requireContext(), "Invalid File", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFilePicker(fileType: String, onFileSelected: (Uri) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = fileType
        }
        this.onFileSelected = onFileSelected
        fileResultLauncher.launch(intent)
    }
}