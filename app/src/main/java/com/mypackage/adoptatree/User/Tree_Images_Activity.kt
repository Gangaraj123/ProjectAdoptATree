package com.mypackage.adoptatree.User

import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mypackage.adoptatree.Adopted_trees
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.Tree_photos_list
import com.mypackage.adoptatree.Trees
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
        tree_id = intent.getStringExtra("tree_id").toString()
        tree_images_list = ArrayList<Image>()

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