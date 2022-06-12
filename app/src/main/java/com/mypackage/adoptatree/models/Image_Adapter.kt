package com.mypackage.adoptatree.models

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.utilities.ImageManager
import com.mypackage.adoptatree.utilities.ImageViewer
import java.text.SimpleDateFormat
import java.util.*

class Image_Adapter(
    private val context: Context,
    private val image_list: ArrayList<Image>
) : RecyclerView.Adapter<Image_Adapter.ImageViewHolder>() {
    private lateinit var temp_date: Date
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yy  hh:mm aa", Locale.getDefault())

    class ImageViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image)
        val image_date: TextView = view.findViewById(R.id.image_date)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.tree_image_view, parent, false)
        return ImageViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val current_image = image_list[position]

        holder.image_date.text = simpleDateFormat.format(Date(current_image.image_timestamp))
        ImageManager.loadImageIntoView(holder.imageView, current_image.image_url)

        holder.imageView.setOnClickListener {
            // start imageviewer activity with transition
            val intent = Intent(it.context, ImageViewer::class.java)
            intent.putExtra("Image_url", current_image.image_url)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                it.context as Activity, holder.imageView, "fade"
            )
            it.context.startActivity(intent, options.toBundle())
        }
    }

    override fun getItemCount() = image_list.size
}
