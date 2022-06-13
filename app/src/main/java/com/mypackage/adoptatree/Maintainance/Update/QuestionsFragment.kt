package com.mypackage.adoptatree.Maintainance.Update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.mypackage.adoptatree.Adopted_trees
import com.mypackage.adoptatree.Maintainance.UAQ_gardener_Adapter
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.Tree_question_list_unanswered
import com.mypackage.adoptatree.Trees
import com.mypackage.adoptatree.models.Question


class QuestionsFragment(val tree_id: String) : Fragment() {
    private lateinit var uaq_recyclerview: RecyclerView
    private lateinit var question_list: ArrayList<Question>
    private lateinit var adapter: UAQ_gardener_Adapter
    private lateinit var mdbref: DatabaseReference
    private var last_item_time = ""
    private lateinit var parent_scroll_view: NestedScrollView
    private var isCompleted = false
    private var isLoading = false
    private lateinit var questions_loading: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_questions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        last_item_time = System.currentTimeMillis().toString()
        mdbref = FirebaseDatabase.getInstance().reference
        parent_scroll_view = view.findViewById(R.id.parent_scroll_view)
        uaq_recyclerview = view.findViewById(R.id.uaq_recyclerview)
        questions_loading = view.findViewById(R.id.questions_loading)
        question_list = ArrayList()

        uaq_recyclerview.layoutManager = LinearLayoutManager(context)
        adapter = UAQ_gardener_Adapter(question_list)
        uaq_recyclerview.adapter = adapter
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
    }

    private fun LoadMore() {
        mdbref.child(Trees).child(Adopted_trees).child(tree_id).child(Tree_question_list_unanswered)
            .orderByKey().limitToLast(10).endBefore(last_item_time)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount < 10) {
                        isCompleted = true
                        for (x in snapshot.children)
                            question_list.add(x.getValue(Question::class.java)!!)
                        adapter.notifyItemRangeInserted(
                            adapter.itemCount,
                            (adapter.itemCount + snapshot.childrenCount).toInt()
                        )
                    } else {
                        last_item_time = snapshot.children.elementAt(0).key.toString()
                        for (x in snapshot.children)
                            question_list.add(x.getValue(Question::class.java)!!)
                        adapter.notifyItemRangeInserted(adapter.itemCount, adapter.itemCount + 10)
                    }
                    isLoading = false
                    questions_loading.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}