package com.mypackage.adoptatree.Maintainance.Update

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import android.widget.Button
import android.widget.ImageView
 import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.mypackage.adoptatree.R
 import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ImageUploadFragment(val tree_id: String) : Fragment() {

    private lateinit var update_btn:Button
    private lateinit var capture_btn:Button
     private lateinit var message:TextView
     private lateinit var imageview:ImageView
     private var soucre: Uri?=null
    private var image_changed=false
    private lateinit var edit_btn:Button
    private lateinit var curr_bitmap:Bitmap
    private val image_view_model:Image_upload_ViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
          update_btn=view.findViewById(R.id.btn_update)
        message=view.findViewById(R.id.add_image_message)
        capture_btn=view.findViewById(R.id.btn_capture)
        imageview=view.findViewById(R.id.update_image_view)
        capture_btn.setOnClickListener {
            val camera_intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
            camera_launcher.launch(camera_intent)
        }
        edit_btn=view.findViewById(R.id.btn_crop)
        edit_btn.setOnClickListener {
            if(image_changed)
                saveImage(curr_bitmap)
            val destUri = StringBuilder(UUID.randomUUID().toString()).append(".jpg")
                .toString()
            val options = UCrop.Options()
            soucre?.let { it1 ->
                UCrop.of(it1, Uri.fromFile(File((context as Activity).cacheDir, destUri)))
                    .withOptions(options)
                    .withAspectRatio(0F, 0F)
                    .useSourceImageAspectRatio()
                    .withMaxResultSize(2000, 2000)
                    .start(context as Activity)
            }
        }
        image_view_model.text.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            soucre=Uri.parse(it)
            imageview.setImageURI(soucre)
        })
        update_btn.setOnClickListener {
            // Logic to save image in storage
        }
     }
    private var camera_launcher=registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        if(it.resultCode==Activity.RESULT_OK){
            image_changed=true
            curr_bitmap=it.data?.extras?.get("data") as Bitmap
            imageview.setImageBitmap(curr_bitmap)
            if(message.visibility==View.VISIBLE)
                message.visibility=View.GONE
            if(update_btn.visibility!=View.VISIBLE)
                update_btn.visibility=View.VISIBLE
            if(imageview.visibility!=View.VISIBLE)
                imageview.visibility=View.VISIBLE
            if(edit_btn.visibility!=View.VISIBLE)
                edit_btn.visibility=View.VISIBLE
        }
    }
    private fun saveImage(bitmap: Bitmap) {
        val fos: OutputStream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "QR")
            val imageUri =
                context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = context?.contentResolver?.openOutputStream(imageUri!!)!!
            soucre = imageUri
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator + "QR"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, "${System.currentTimeMillis()}.png")
            fos = FileOutputStream(image)
            soucre = image.toUri()
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    }


}

