package com.mypackage.adoptatree

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mypackage.adoptatree.databinding.ActivityUserQaBinding
import com.mypackage.adoptatree.models.Question
import com.mypackage.adoptatree.utilities.BottomSheet
import com.mypackage.adoptatree.utilities.QuestionAnswerAdapter

class ActivityUserQA : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var questionList : ArrayList<QA>
    private lateinit var questionAnswerAdapter : QuestionAnswerAdapter
    private lateinit var binding : ActivityUserQaBinding
    private lateinit var mAuth : FirebaseAuth
    var id : String? = null

    data class QA(val question : String, val askedOn : Long, val answer : String?, val answeredOn : Long?)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserQaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionList = ArrayList()

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        id = intent.getStringExtra("id")

        val fdbref = FirebaseDatabase.getInstance().reference
        fdbref.child(Trees).child(Adopted_trees).child(id!!).child("unanswered_questions").addChildEventListener(
            object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) { refresh() }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { refresh() }
                override fun onChildRemoved(snapshot: DataSnapshot) { refresh() }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { refresh() }
                override fun onCancelled(error: DatabaseError) { refresh() }
            }
        )

        // setting up add question button
        binding.addQuestion.setOnClickListener{
            val bottomSheet = BottomSheet(id!!)
            bottomSheet.show(supportFragmentManager, "TAG")
        }
    }

    fun refresh(){
        val fdbref = FirebaseDatabase.getInstance().reference

        fdbref.child(Trees).child(Adopted_trees).child(id!!).child("unanswered_questions").addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    questionList.clear()
                    snapshot.children.forEach{
                        val questionText = it.child("question_text").value
                        val askedOn = it.child("askedOn").value

                        questionList.add(QA(questionText as String, askedOn as Long, null, null))
                    }

                    questionAnswerAdapter = QuestionAnswerAdapter(questionList)
                    recyclerView.adapter = questionAnswerAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            }
        )
    }
}