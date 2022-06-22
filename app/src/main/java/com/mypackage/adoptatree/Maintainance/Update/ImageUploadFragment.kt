package com.mypackage.adoptatree.Maintainance.Update

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mypackage.adoptatree.*
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


class ImageUploadFragment : Fragment() {

    private lateinit var update_btn: Button
    private lateinit var capture_btn: Button
    private lateinit var message: TextView
    private lateinit var tree_id: String
    private lateinit var imageview: ImageView
    private var current_image_url: Uri? = null
    private var image_changed = false
    private lateinit var edit_btn: Button
    private val image_view_model: Image_upload_ViewModel by activityViewModels()
    private lateinit var image_layout: LinearLayout
    private lateinit var uploading_layout: LinearLayout
    private lateinit var uploaded_percent: TextView
    private lateinit var current_photo_path: String
    private lateinit var mdbRef: DatabaseReference
    private lateinit var uploaded: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tree_id = it.getString("tree_id").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update_btn = view.findViewById(R.id.btn_update)
        message = view.findViewById(R.id.add_image_message)
        capture_btn = view.findViewById(R.id.btn_capture)
        uploading_layout = view.findViewById(R.id.uploading_view)
        image_layout = view.findViewById(R.id.Image_upload_view)
        imageview = view.findViewById(R.id.update_image_view)
        uploaded_percent = view.findViewById(R.id.uploaded_percent)
        uploaded = view.findViewById(R.id.uploaded)
        capture_btn.setOnClickListener {
            val filename: String = "photo"
            val storagedir: File? = view.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val imageFile = File.createTempFile(filename, ".jpg", storagedir)
                current_photo_path = imageFile.absolutePath
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val imageUri = FileProvider.getUriForFile(
                    view.context,
                    "com.mypackage.adoptatree.fileprovider",
                    imageFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                camera_launcher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        edit_btn = view.findViewById(R.id.btn_crop)
        edit_btn.setOnClickListener {
            val destUri = StringBuilder(UUID.randomUUID().toString()).append(".jpg")
                .toString()
            val options = UCrop.Options()
            current_image_url?.let { it1 ->
                UCrop.of(it1, Uri.fromFile(File((context as Activity).cacheDir, destUri)))
                    .withOptions(options)
                    .withAspectRatio(0F, 0F)
                    .useSourceImageAspectRatio()
                    .withMaxResultSize(2000, 2000)
                    .start(context as Activity)
            }
        }
        mdbRef = FirebaseDatabase.getInstance().reference
        image_view_model.text.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            current_image_url = Uri.parse(it)
            imageview.setImageURI(current_image_url)
        })

        update_btn.setOnClickListener {
            // Logic to save image in storage
            Log.d(TAG, "Clicked")
            val linear_progress_bar =
                view.findViewById<LinearProgressIndicator>(R.id.uploaded_progessbar)
            image_layout.visibility = View.GONE
            uploading_layout.visibility = View.VISIBLE
            val scaleDivider = 4
            val final_bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                current_image_url
            )
            val scaleWidth: Int = final_bitmap.getWidth() / scaleDivider
            val scaleHeight: Int = final_bitmap.getHeight() / scaleDivider
            val bytes = getDownsizedImageBytes(final_bitmap, scaleWidth, scaleHeight)
            val storageReference = FirebaseStorage.getInstance().reference
            storageReference.child("Images").child(current_image_url?.lastPathSegment!!)
                .putBytes(bytes!!)
                .addOnSuccessListener {
                    it.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener {
                            val new_image_url = it.toString()
                            mdbRef.child(Trees).child(Adopted_trees)
                                .child(tree_id).child(Tree_photos_list)
                                .child(System.currentTimeMillis().toString())
                                .setValue(new_image_url)
                                .addOnSuccessListener {
                                    uploaded.visibility = View.VISIBLE
                                    uploading_layout.visibility = View.GONE
                                }
                                .addOnFailureListener {
                                    uploading_layout.findViewById<TextView>(R.id.upload_success).text =
                                        "upload Failed"
                                }
                        }
                }
                .addOnProgressListener { dit ->
                    val progress: Double = (100.0 * dit.bytesTransferred) / dit.totalByteCount
                    linear_progress_bar.progress = progress.toInt()
                    uploaded_percent.text = "${progress.toInt()} %"
                }
        }

    }

    private var camera_launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            if (it.resultCode == Activity.RESULT_OK) {
                image_changed = true
                current_image_url = Uri.fromFile(File(current_photo_path))
                imageview.setImageURI(current_image_url)
                if (message.visibility == View.VISIBLE)
                    message.visibility = View.GONE
                if (update_btn.visibility != View.VISIBLE)
                    update_btn.visibility = View.VISIBLE
                if (imageview.visibility != View.VISIBLE)
                    imageview.visibility = View.VISIBLE
                if (edit_btn.visibility != View.VISIBLE)
                    edit_btn.visibility = View.VISIBLE
            }
        }


    @Throws(IOException::class)
    fun getDownsizedImageBytes(
        fullBitmap: Bitmap?,
        scaleWidth: Int,
        scaleHeight: Int
    ): ByteArray? {
        val scaledBitmap =
            Bitmap.createScaledBitmap(fullBitmap!!, scaleWidth, scaleHeight, true)

        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            ImageUploadFragment().apply {
                arguments = Bundle().apply {
                    putString("tree_id", param1)
                }
            }
    }
}

