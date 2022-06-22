package com.mypackage.adoptatree.User

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var dialog: Dialog

    data class TreeData(
        val tree_nick_name: String,
        val adopted_on: Long,
        var last_watered: Long,
        val id: String,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdoptedTreesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.mytoolbar)
        setSupportActionBar(toolbar)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        treeAdapter = AdoptedTreeAdapter(adoptedTrees)
        recyclerView.adapter = treeAdapter
        fdbref = FirebaseDatabase.getInstance().reference
        dialog = Dialog(this)
        treeAdapter.onImagesButtonClick = { id, name ->
            val intent =
                Intent(this@AdoptedTreesActivity, Tree_Images_Activity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("tree_name", name)
            startActivity(intent)
        }

        shimmerLayout = binding.shimmerLayout
        shimmerLayout.startShimmer()
        treeAdapter.onQuestionsButtonClick = { id, name ->
            val intent = Intent(this@AdoptedTreesActivity, ActivityUserQA::class.java)
            intent.putExtra("id", id)
            intent.putExtra("tree_name", name)
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
            Add_tree()
//            startActivity(Intent(this, MapsActivity::class.java))
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
                            var nickname = "Tree $counter"
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
                        recyclerView.visibility = View.VISIBLE
                        shimmerLayout.visibility = View.GONE
                        shimmerLayout.startShimmer()
                    } else {
                        // empty list
                        shimmerLayout.visibility = View.GONE
                        shimmerLayout.stopShimmer()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun Add_tree() {

        fdbref.child(Trees).child(Registered_trees).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // if there is a tree to be adopted, then perform following
                    if (snapshot.exists()) {
                        dialog.setContentView(R.layout.new_tree_details_add)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.show()
                        val cancel = dialog.findViewById<Button>(R.id.negative_btn)
                        val nickname_view = dialog.findViewById<EditText>(R.id.nick_name_view)
                        val adopt = dialog.findViewById<Button>(R.id.positive_btn)
                        nickname_view.addTextChangedListener {
                            if (it.toString().isNotEmpty()) {
                                if (!adopt.isEnabled)
                                    adopt.isEnabled = true
                            } else {
                                if (adopt.isEnabled) adopt.isEnabled = false
                            }
                        }
                        cancel.setOnClickListener {
                            dialog.dismiss()
                        }
                        adopt.setOnClickListener {
                            adopt.isEnabled = false
                            adopt.text = "adopting..."
                            val first_tree = snapshot.children.elementAt(0)
//                        move that tree's record from Registered_Trees to Adopted_Trees of given user.
                            val tree_node = first_tree.value
                            val tree = first_tree.child(Tree_details).getValue(Tree::class.java)
                            tree?.adopted_by = mAuth.uid
                            tree?.adopted_on = System.currentTimeMillis()
                            tree?.tree_nick_name = nickname_view.text.toString()
                            var last_watered: Long = 0
                            if (first_tree.hasChild(Last_watered_time))
                                last_watered = first_tree.child(Last_watered_time).value as Long
                            val tree_data =
                                TreeData(
                                    tree?.tree_nick_name!!,
                                    tree?.adopted_on!!,
                                    last_watered,
                                    first_tree.key!!
                                )
                            fdbref.child(Trees).child(Adopted_trees)
                                .child(first_tree.key!!).setValue(tree_node)
                                .addOnSuccessListener {
                                    fdbref.child(Trees).child(Adopted_trees)
                                        .child(first_tree.key!!).child(Tree_details).setValue(tree)
                                        .addOnSuccessListener {
                                            first_tree.ref.removeValue()
                                                .addOnSuccessListener {
                                                    fdbref.child(Users).child(mAuth.uid!!)
                                                        .child(Adopted_trees)
                                                        .child(first_tree.key!!)
                                                        .setValue(tree_data)
                                                        .addOnSuccessListener {
                                                            adopt.text = "done"
                                                            if (adoptedTrees.size == 0) {
                                                                recyclerView.visibility =
                                                                    View.VISIBLE
                                                                shimmerLayout.visibility = View.GONE
                                                                shimmerLayout.stopShimmer()
                                                            }
                                                            adoptedTrees.add(0, tree_data)
                                                            treeAdapter.notifyItemInserted(0)
                                                            dialog.dismiss()
                                                            Toast.makeText(
                                                                this@AdoptedTreesActivity,
                                                                "Adopted succesfully ",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                }
                                        }
                                }
                            //return after the first iteration, which means only the first tree will get added
                        }
                    } else {
                        Toast.makeText(
                            this@AdoptedTreesActivity,
                            "No more Trees to adopt",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pop_up_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signout_btn) {
            FirebaseAuth.getInstance().signOut()
            try {
                GoogleSignIn.getClient(
                    this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.my_web_client_ID))
                        .requestEmail().build()
                ).signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}

