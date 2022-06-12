package com.mypackage.adoptatree.Maintainance

import android.content.ContentValues
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class QRCodeGenerator : AppCompatActivity() {
    private lateinit var qrCodeView: LinearLayout
    private lateinit var qrCodeImage: ImageView
    private lateinit var generate_btn: Button
    private lateinit var qr_generating_load: ProgressBar
    private lateinit var share_btn: Button
    private lateinit var save_btn: Button
    private var barCodeWriter = Code128Writer()
    private var qrcodewriter = QRCodeWriter()
    private var current_QR_URI: Uri? = null
    private lateinit var qr_select_radio_grp: RadioGroup
    private lateinit var curr_QR_Bitmap: Bitmap
    private var qr_already_saved: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_generator)

        qrCodeView = findViewById(R.id.QR_Code_View)
        qrCodeImage = findViewById(R.id.generated_qr_code_image)
        generate_btn = findViewById(R.id.qr_generate_btn)
        qr_generating_load = findViewById(R.id.qr_generating_loading)
        share_btn = findViewById(R.id.qr_share_btn)
        save_btn = findViewById(R.id.qr_save_btn)
        qr_select_radio_grp = findViewById(R.id.radio_grp)

        generate_btn.setOnClickListener {
            val qr_string = getRandomKey()
            val selectedbtn = qr_select_radio_grp.checkedRadioButtonId
            generate_btn.visibility = View.GONE
            qr_generating_load.visibility = View.VISIBLE
            if (selectedbtn == R.id.radio_qr_code)
                curr_QR_Bitmap = getQRCode(qr_string)
            else
                curr_QR_Bitmap = getBarCode(qr_string)
            qrCodeImage.setImageBitmap(curr_QR_Bitmap)
            qr_already_saved = false
            qr_generating_load.visibility = View.GONE
            qrCodeView.visibility = View.VISIBLE
        }

        save_btn.setOnClickListener {
            if (qr_already_saved) {
                Toast.makeText(this, "Already saved to Gallery", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            qr_already_saved = true
            saveImage(curr_QR_Bitmap)
            Toast.makeText(this, "saved to Gallery", Toast.LENGTH_SHORT).show()
        }

        share_btn.setOnClickListener {
            if (current_QR_URI == null)
                saveImage(curr_QR_Bitmap)
            qr_already_saved = true
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, current_QR_URI)
                intent.type = "image/png"
                intent.putExtra(Intent.EXTRA_TEXT, "Generated Code")
                startActivity(Intent.createChooser(intent, "share via"))
            } catch (e: Exception) {
                Toast.makeText(this, "Couldn't send the QR code", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Failed to send : " + e.message.toString())
            }
        }

        qr_select_radio_grp.setOnCheckedChangeListener { group, checkedId ->
            qrCodeView.visibility = View.GONE
            qr_already_saved = false
            generate_btn.visibility = View.VISIBLE
        }

    }

    private fun getRandomKey(): String {
        return UUID.randomUUID().toString().substring(0, 15).replace('-', 'A')
    }

    // returns QR code from string
    private fun getQRCode(text: String): Bitmap {
        val result = qrcodewriter.encode(text, BarcodeFormat.QR_CODE, 400, 400)
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h)
            for (x in 0 until w) {
                if (result.get(x, y))
                    pixels[y * w + x] = Color.BLACK
                else pixels[y * w + x] = Color.WHITE
            }
        val resultbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultbitmap.setPixels(pixels, 0, w, 0, 0, w, h)

        // add the text
        val canvas = Canvas(resultbitmap)
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        paintText.color = Color.BLACK
        paintText.textSize = 25f
        val rect = Rect()
        paintText.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, 100f, h - 15f, paintText)

        return resultbitmap
    }

    // returns BAR code from string
    private fun getBarCode(text: String): Bitmap {
        val result = barCodeWriter.encode(text, BarcodeFormat.CODE_128, 400, 50)
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h)
            for (x in 0 until w) {
                if (result.get(x, y))
                    pixels[y * w + x] = Color.BLACK
                else pixels[y * w + x] = Color.WHITE
            }
        val resultbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultbitmap.setPixels(pixels, 0, w, 0, 0, w, h)


        // add some padding
        val paddedbitmap = Bitmap.createBitmap(w + 20, h + 50, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paddedbitmap)

        canvas.drawARGB(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE)
        canvas.drawBitmap(resultbitmap, 10F, 20F, null)

        // add the text
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
        paintText.color = Color.BLACK
        paintText.textSize = 24f
        val rect = Rect()
        paintText.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, 100f, h + 45f, paintText)

        return paddedbitmap
    }

    private fun saveImage(bitmap: Bitmap) {
        val fos: OutputStream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis())
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "QR")
            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = contentResolver.openOutputStream(imageUri!!)!!
            current_QR_URI = imageUri
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
            current_QR_URI = image.toUri()
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
    }

}