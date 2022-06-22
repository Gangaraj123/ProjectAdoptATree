package com.mypackage.adoptatree.User

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.models.Question
import com.mypackage.adoptatree.utilities.QuestionAnswerAdapter

class userAQ : Fragment() {
    private lateinit var tree_id: String
    private var answered_view = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionAnswerAdapter
    private lateinit var fdbref: DatabaseReference
    private var last_item_time = ""
    private lateinit var parent_scroll_view: NestedScrollView
    private var isCompleted = false
    private var isLoading = false
    private var questionList = ArrayList<Question>()
    private lateinit var questions_loading: ProgressBar
    private lateinit var path: String
    private val pageViewModel: PageViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tree_id = it.getString("tree_id").toString()
            answered_view = it.getBoolean("IsAnswered")
            Log.d(TAG, "id = $tree_id and answerd view = $answered_view")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_a_q, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        path = if (answered_view) Tree_question_list_answered else Tree_question_list_unanswered
        fdbref = FirebaseDatabase.getInstance().reference
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        parent_scroll_view = view.findViewById(R.id.parent_scroll_view)
        questions_loading = view.findViewById(R.id.questions_loading)
        adapter = QuestionAnswerAdapter(questionList)
        recyclerView.adapter = adapter
        last_item_time = System.currentTimeMillis().toString()
        LoadMore()
        parent_scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (!isLoading && !isCompleted) {
                    isLoading = true
                    questions_loading.visibility = View.VISIBLE
                    LoadMore()
                }
            }
        })
        if (!answered_view) {
            pageViewModel.question.observe(viewLifecycleOwner, Observer {
                questionList.add(0, it)
                adapter.notifyItemInserted(0)
            })
        }
    }

    private fun LoadMore() {
        fdbref.child(Trees).child(Adopted_trees).child(tree_id).child(path)
            .orderByKey().limitToLast(10).endBefore(last_item_time)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount < 10) {
                        isCompleted = true
                        for (x in snapshot.children)
                            questionList.add(x.getValue(Question::class.java)!!)
                        adapter.notifyItemRangeInserted(
                            adapter.itemCount,
                            (adapter.itemCount + snapshot.childrenCount).toInt()
                        )
                    } else {
                        last_item_time = snapshot.children.elementAt(0).key.toString()
                        for (x in snapshot.children)
                            questionList.add(x.getValue(Question::class.java)!!)
                        adapter.notifyItemRangeInserted(adapter.itemCount, adapter.itemCount + 10)
                    }
                    isLoading = false
                    questions_loading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Boolean) =
            userAQ().apply {
                arguments = Bundle().apply {
                    putString("tree_id", param1)
                    putBoolean("IsAnswered", param2)
                }
            }
    }
}

