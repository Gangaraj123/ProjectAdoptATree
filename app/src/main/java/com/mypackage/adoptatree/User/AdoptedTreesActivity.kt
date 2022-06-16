package com.mypackage.adoptatree.User

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.databinding.ActivityAdoptedTreesBinding
import com.mypackage.adoptatree.models.Tree
import com.mypackage.adoptatree.utilities.AdoptedTreeAdapter

class AdoptedTreesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdoptedTreesBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var recyclerView : RecyclerView
    private var adoptedTrees = mutableListOf<TreeData>()
    private lateinit var fdbref : DatabaseReference

    data class TreeData(val nickname: String, val adopted_on: Long, val last_watered: Long, val id: String?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdoptedTreesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth.uid == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }


        fdbref = FirebaseDatabase.getInstance().reference
        val current_path = fdbref.child("users").child(mAuth.uid!!)

        current_path.child(Adopted_trees).addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                refresh()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                refresh()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                refresh()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                refresh()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.adoptTree.setOnClickListener {
            //make button unclickable, so that spamming doesn't add multiple trees
            binding.adoptTree.isClickable = false

            fdbref.child(Trees).child(Registered_trees)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // if there is a tree to be adopted, then perform following
                        if (snapshot.exists()) {

                            //move that tree's record from Registered_Trees to Adopted_Trees of given user.
                            snapshot.children.forEach { snapshot1 ->

                                //making a Tree object, inorder to get tree_id
                                val tree = snapshot1.child(Tree_details).getValue(Tree::class.java)
//                                Toast.makeText(this@AdoptedTreesActivity, tree?.tree_id, Toast.LENGTH_LONG).show()

                                //setting the value of tree_id in adoptedTrees to that of the registered tree.
                                fdbref.child(Trees).child(Adopted_trees).child(tree?.tree_id!!)
                                    .setValue(snapshot1.value)
                                fdbref.child(Trees).child(Adopted_trees).child(tree.tree_id).child(
                                    Tree_details
                                ).child("adopted_by").setValue(mAuth.uid)
                                val adoptionTime = System.currentTimeMillis()
                                fdbref.child(Trees).child(Adopted_trees).child(tree.tree_id).child(
                                    Tree_details
                                ).child("adopted_on").setValue(adoptionTime)

                                //updating user database
                                val nickname = tree.tree_nick_name

                                fdbref.child(Trees).child(Adopted_trees).child(tree.tree_id)
                                    .child(Last_watered_time)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            var lastWatered: Long = 0
                                            if (snapshot.value != null) {
                                                lastWatered = snapshot.value as Long
                                            }
                                            val user_tree_details = mapOf(
                                                "nickname" to nickname,
                                                "last_watered" to lastWatered,
                                                "adopted_on" to adoptionTime
                                            )
                                            fdbref.child("users").child(mAuth.uid!!).child(
                                                Adopted_trees).child(tree.tree_id).setValue(user_tree_details)
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })

                                //removing that registered tree
                                snapshot1.ref.removeValue()

                                binding.adoptTree.isClickable = true

                                //return after the first iteration, which means only the first tree will get added
                                return
                            }

                            Toast.makeText(
                                this@AdoptedTreesActivity,
                                "No more Trees to adopt",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Toast.makeText(
                            this@AdoptedTreesActivity,
                            "No more Trees to adopt",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    fun refresh(){
        val current_path = fdbref.child("users").child(mAuth.uid!!)

        //fill adoptedTrees with TreeData objects which have nickname, adopted_on and last_watered
        current_path.child(Adopted_trees).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 1
                adoptedTrees.clear()
                snapshot.children.forEach { it ->
                    if (it.key != "user_details") {

                        var nickname = "Tree #$counter"
                        if(it.hasChild("nickname")){
                            nickname = it.child("nickname").value as String
                        }else{
                            counter++
                        }
                        var lastWatered : Long = 0
                        if(it.hasChild(Last_watered_time)){
                            lastWatered = it.child(Last_watered_time).value as Long
                        }

                        var adoptedOn : Long = 0
                        if(it.hasChild("adopted_on")){
                            adoptedOn = it.child("adopted_on").value as Long
                        }
                        adoptedTrees.add(
                            TreeData(
                                nickname,
                                adoptedOn,
                                lastWatered,
                                it.key
                            )
                        )
                    }
                }

                val treeAdapter = AdoptedTreeAdapter(adoptedTrees)
                recyclerView.adapter = treeAdapter

                treeAdapter.onImagesButtonClick = {
                    val intent = Intent(this@AdoptedTreesActivity, Tree_Images_Activity::class.java)
                    intent.putExtra("id", it)
                    startActivity(intent)
                }

                treeAdapter.onQuestionsButtonClick = {
                    val intent = Intent(this@AdoptedTreesActivity, ActivityUserQA::class.java)
                    intent.putExtra("id", it)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

