package com.mypackage.adoptatree

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var questionList: ArrayList<Question>
    private lateinit var questionAnswerAdapter: QuestionAnswerAdapter
    private lateinit var binding: ActivityUserQaBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fdbref:DatabaseReference
    var id: String = ""
    private var last_item_time=""
    private var isCompleted=false
    private lateinit var questions_loading: ProgressBar
    private var isLoading=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserQaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionList = ArrayList()

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        questionAnswerAdapter=QuestionAnswerAdapter(questionList)
        recyclerView.adapter=questionAnswerAdapter
        id = intent.getStringExtra("id").toString()
        Log.d(TAG,"Id received = "+id)
        fdbref = FirebaseDatabase.getInstance().reference
        // setting up add question button
        last_item_time=System.currentTimeMillis().toString()
        LoadMore()
        questions_loading=binding.questionsLoading
        binding.addQuestion.setOnClickListener {
            val bottomSheet = BottomSheet(id!!)
            bottomSheet.show(supportFragmentManager, "TAG")
            bottomSheet.onQuestionAdded = {
                val curr_time = System.currentTimeMillis()
                val new_question = Question(question = it, askedon = curr_time)
                fdbref.child(Trees).child(Adopted_trees).child(id!!)
                    .child(Tree_question_list_unanswered).child(curr_time.toString())
                    .setValue(new_question).addOnSuccessListener {
                        questionList.add(0, new_question)
                        questionAnswerAdapter.notifyItemInserted(0)
                        bottomSheet.dismiss()
                    }
            }
        }
    }

    private fun LoadMore() {
        Log.d(TAG,"Id while getting = "+id)
        fdbref.child(Trees).child(Adopted_trees).child(id).child(Tree_question_list_unanswered)
            .orderByKey().limitToLast(10).endBefore(last_item_time)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount < 10) {
                        isCompleted = true
                        for (x in snapshot.children)
                            questionList.add(x.getValue(Question::class.java)!!)
                        questionAnswerAdapter.notifyItemRangeInserted(
                            questionAnswerAdapter.itemCount,
                            (questionAnswerAdapter.itemCount + snapshot.childrenCount).toInt()
                        )
                    } else {
                        last_item_time = snapshot.children.elementAt(0).key.toString()
                        for (x in snapshot.children)
                            questionList.add(x.getValue(Question::class.java)!!)
                        questionAnswerAdapter.notifyItemRangeInserted(questionAnswerAdapter.itemCount, questionAnswerAdapter.itemCount + 10)
                    }
                    isLoading = false
                    questions_loading.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

}