package com.mypackage.adoptatree.User

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.databinding.ActivityAdoptedTreesBinding
import com.mypackage.adoptatree.models.Tree
import com.mypackage.adoptatree.utilities.AdoptedTreeAdapter

class AdoptedTreesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdoptedTreesBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private var adoptedTrees = ArrayList<TreeData>()
    private lateinit var fdbref: DatabaseReference
    private lateinit var treeAdapter: AdoptedTreeAdapter
    private lateinit var shimmerLayout:ShimmerFrameLayout
    data class TreeData(
        val nickname: String,
        val adopted_on: Long,
        var last_watered: Long,
        val id: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdoptedTreesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
         recyclerView.layoutManager = LinearLayoutManager(this)
        treeAdapter = AdoptedTreeAdapter(adoptedTrees)
        recyclerView.adapter=treeAdapter
        fdbref = FirebaseDatabase.getInstance().reference
        treeAdapter.onImagesButtonClick = {
            val intent =
                Intent(this@AdoptedTreesActivity, Tree_Images_Activity::class.java)
            intent.putExtra("id", it)
            startActivity(intent)
        }
        shimmerLayout=binding.shimmerLayout
        shimmerLayout.startShimmer()
        treeAdapter.onQuestionsButtonClick = {
            val intent = Intent(this@AdoptedTreesActivity, ActivityUserQA::class.java)
            intent.putExtra("id", it)
            startActivity(intent)
        }
        mAuth = FirebaseAuth.getInstance()
        if (mAuth.uid == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        refresh()


        binding.adoptTree.setOnClickListener {
            //make button unclickable, so that spamming doesn't add multiple trees
            binding.adoptTree.isClickable = false
            
            fdbref.child(Trees).child(Registered_trees).limitToFirst(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // if there is a tree to be adopted, then perform following
                        if (snapshot.exists()) {
                            val first_tree=snapshot.children.elementAt(0)
                            //move that tree's record from Registered_Trees to Adopted_Trees of given user.
                            val tree_node=first_tree.value
                            val tree=first_tree.child(Tree_details).getValue(Tree::class.java)
                            tree?.adopted_by=mAuth.uid
                            tree?.adopted_on=System.currentTimeMillis()
                            var last_watered:Long=0
                            if(first_tree.hasChild(Last_watered_time))
                                last_watered=first_tree.child(Last_watered_time).value as Long
                            val tree_data=TreeData("abc",tree?.adopted_on!!,last_watered,first_tree.key!!)
                            fdbref.child(Trees).child(Adopted_trees)
                                .child(first_tree.key!!).setValue(tree_node)
                                .addOnSuccessListener {
                                    fdbref.child(Trees).child(Adopted_trees)
                                        .child(first_tree.key!!).child(Tree_details).setValue(tree)
                                        .addOnSuccessListener {
                                         first_tree.ref.removeValue()
                                             .addOnSuccessListener {
                                                 fdbref.child(Users).child(mAuth.uid!!)
                                                     .child(Adopted_trees).child(first_tree.key!!)
                                                     .setValue(tree_data)
                                                     .addOnSuccessListener {
                                                         adoptedTrees.add(0,tree_data)
                                                         treeAdapter.notifyItemInserted(0)
                                                     }
                                             }
                                        }
                                }
                                 //return after the first iteration, which means only the first tree will get added
                            }
                        else{
                            Toast.makeText(
                                this@AdoptedTreesActivity,
                                "No more Trees to adopt",
                                Toast.LENGTH_LONG
                            ).show()
                        }}
                    override fun onCancelled(error: DatabaseError) {}

                })
        }
    }

    fun refresh() {
        val current_path = fdbref.child(Users).child(mAuth.uid!!)

        //fill adoptedTrees with TreeData objects which have nickname, adopted_on and last_watered
        current_path.child(Adopted_trees)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var counter = 1
                    if (snapshot.exists()) {
                        snapshot.children.forEach {
                            var nickname = "Tree #$counter"
                            if (it.hasChild(Tree_Nick_Name)) {
                                nickname = it.child(Tree_Nick_Name).value as String
                            } else counter++
                            var last_watered: Long = 0
                            if (it.hasChild(Last_watered_time))
                                last_watered = it.child(Last_watered_time).value as Long
                            val adoptedOn = it.child(Adopted_on).value as Long
                            adoptedTrees.add(
                                TreeData(
                                    nickname,
                                    adoptedOn,
                                    last_watered,
                                    it.key.toString()
                                )
                            )
                        }
                        treeAdapter.notifyItemRangeInserted(0, adoptedTrees.size)
                        recyclerView.visibility= View.VISIBLE
                        shimmerLayout.visibility=View.GONE
                        shimmerLayout.startShimmer()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

