package com.mypackage.adoptatree.User

import android.os.Bundle
import android.transition.Fade
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.models.Image
import com.mypackage.adoptatree.models.Image_Adapter

class Tree_Images_Activity : AppCompatActivity() {
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var images_loading: ProgressBar
    private lateinit var image_RV_Adapter: Image_Adapter
    private lateinit var parent_scroll_view: NestedScrollView
    private lateinit var tree_images_list: ArrayList<Image>
    private var last_item_time = ""
    private var isCompleted = false
    private var tree_id = ""
    private var isLoading = false
    private lateinit var mdbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tree_images)

        // pass tree id to it while starting activity
        tree_id = intent.getStringExtra("id").toString()
        tree_images_list = ArrayList()
        val name = intent.getStringExtra("tree_name").toString()
        val back_btn = findViewById<ImageButton>(R.id.back_btn)
        back_btn.setOnClickListener {
            onBackPressed()
        }
        findViewById<TextView>(R.id.tree_nick_name).text = name
        imageRecyclerView = findViewById(R.id.image_recycler_view)
        images_loading = findViewById(R.id.items_loading)
        gridLayoutManager = GridLayoutManager(this, 2)
        imageRecyclerView.layoutManager = gridLayoutManager
        image_RV_Adapter = Image_Adapter(this, tree_images_list)
        imageRecyclerView.adapter = image_RV_Adapter
        mdbRef = FirebaseDatabase.getInstance().reference
        parent_scroll_view = findViewById(R.id.parent_scroll_view)
        val fade = Fade()

        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        window.enterTransition = fade
        window.exitTransition = fade
        last_item_time = System.currentTimeMillis().toString()
        LoadMore()
        parent_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (!isLoading && !isCompleted) {
                    isLoading = true
                    images_loading.visibility = View.VISIBLE
                    LoadMore()
                }
            }
        })
    }

    private fun LoadMore() {
        Log.d(TAG, tree_id)
        mdbRef.child(Trees).child(Adopted_trees).child(tree_id).child(Tree_photos_list)
            .orderByKey().limitToLast(10).endBefore(last_item_time)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount < 10) {
                        isCompleted = true
                        var temp: Image
                        for (x in snapshot.children) {
                            temp = Image()
                            temp.image_url = x.getValue(String::class.java)
                            temp.image_timestamp = x.key?.toLong()!!
                            tree_images_list.add(temp)
                            image_RV_Adapter.notifyItemRangeInserted(
                                image_RV_Adapter.itemCount,
                                image_RV_Adapter.itemCount + snapshot.childrenCount.toInt()
                            )
                        }
                    } else {
                        last_item_time = snapshot.children.elementAt(0).key!!
                        for (x in snapshot.children) {
                            var temp: Image
                            temp = Image()
                            temp.image_url = x.getValue(String::class.java)
                            temp.image_timestamp = x.key?.toLong()!!
                            tree_images_list.add(temp)
                            image_RV_Adapter.notifyItemRangeInserted(
                                image_RV_Adapter.itemCount,
                                image_RV_Adapter.itemCount + 10
                            )
                        }
                    }
                    isLoading = false
                    images_loading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.animate_slide_in_left, R.anim.animate_slide_out_right)
    }
}