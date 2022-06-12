package com.mypackage.adoptatree.Maintainance.Update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var mdbref:DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_questions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mdbref=FirebaseDatabase.getInstance().reference
        uaq_recyclerview = view.findViewById(R.id.uaq_recyclerview)
        question_list = ArrayList<Question>()
        uaq_recyclerview.layoutManager = LinearLayoutManager(context)
        uaq_recyclerview.adapter = adapter
        mdbref.child(Trees).child(Adopted_trees).child(tree_id).child(Tree_question_list_unanswered)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            for(x in snapshot.children)
                            {
                                question_list.add(x.getValue(Question::class.java)!!)
                            }
                            adapter.notifyItemRangeInserted(0,question_list.size)
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}