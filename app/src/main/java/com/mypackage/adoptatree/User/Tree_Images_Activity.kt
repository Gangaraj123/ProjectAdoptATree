package com.mypackage.adoptatree.User

import android.os.Bundle
import android.os.Handler
import android.transition.Fade
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.models.Image
import com.mypackage.adoptatree.models.Image_Adapter

val temp_url =
    "https://firebasestorage.googleapis.com/v0/b/project-ready-chat.appspot.com/o/Images%2Fd23d7216-c459-4755-aec8-b03f7b4302fa.jpg?alt=media&token=3b24d698-6cde-455c-bc2d-7b33c96b96b1"

class Tree_Images_Activity : AppCompatActivity() {
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var images_loading: ProgressBar
    private lateinit var image_RV_Adapter: Image_Adapter
    private lateinit var parent_scroll_view: NestedScrollView
    private var is_Loading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree_images)

        val mylist = ArrayList<Image>()
        for (i in 0 until 10)
            mylist.add(Image(temp_url))
        imageRecyclerView = findViewById(R.id.image_recycler_view)
        images_loading = findViewById(R.id.items_loading)
        gridLayoutManager = GridLayoutManager(this, 2)
        imageRecyclerView.layoutManager = gridLayoutManager
        image_RV_Adapter = Image_Adapter(this, mylist)
        imageRecyclerView.adapter = image_RV_Adapter
        parent_scroll_view = findViewById(R.id.parent_scroll_view)
        val fade = Fade()
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        window.enterTransition = fade
        window.exitTransition = fade

        parent_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (!is_Loading) {
                    is_Loading = true
                    images_loading.visibility = View.VISIBLE
                    for (x in 0..10)
                        mylist.add(Image(temp_url))
                    Handler().postDelayed(Runnable {
                        image_RV_Adapter.notifyItemRangeInserted(
                            image_RV_Adapter.itemCount,
                            image_RV_Adapter.itemCount + 10
                        )
                        is_Loading=false
                        images_loading.visibility=View.GONE
                    }, 3000)

                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.animate_slide_in_left,R.anim.animate_slide_out_right)
    }
}