package com.mypackage.adoptatree.Maintainance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.models.Question

private val mdbref = FirebaseDatabase.getInstance().reference

class UAQ_gardener_Adapter(val questionList: ArrayList<Question>, val tree_id: String) :
    RecyclerView.Adapter<UAQ_gardener_Adapter.UAQ_VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UAQ_VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.unanswered_question_gardener, parent, false)
        return UAQ_VH(view).linkAdapter(this)
    }

    override fun onBindViewHolder(holder: UAQ_VH, position: Int) {
        holder.question_text.text = questionList[position].question

        //recycler view recycles the views, so if this views display is changed when used before, reset it
        holder.curr_question = questionList[position]
        holder.tree_id = tree_id
        if (holder.answer_edit_text.visibility == View.VISIBLE)
            holder.answer_edit_text.visibility = View.GONE
        holder.answer_edit_text.setText("")
        if (holder.respond_btn.visibility != View.VISIBLE)
            holder.respond_btn.visibility = View.VISIBLE
        if (holder.submit_btn.visibility == View.VISIBLE)
            holder.submit_btn.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    class UAQ_VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var respond_btn: Button
        var submit_btn: Button
        var answer_edit_text: EditText
        var uaq_gardener_adapter: UAQ_gardener_Adapter? = null
        var question_text: TextView
        var tree_id: String = ""
        var curr_question: Question? = null

        init {
            respond_btn = itemView.findViewById(R.id.btn_add_answer)
            submit_btn = itemView.findViewById(R.id.btn_submit_answer)
            question_text = itemView.findViewById(R.id.question)
            answer_edit_text = itemView.findViewById(R.id.entered_response)
            respond_btn.setOnClickListener {
                Log.d(TAG, "Clicked on item = " + adapterPosition)
                answer_edit_text.visibility = View.VISIBLE
                submit_btn.visibility = View.VISIBLE
                respond_btn.visibility = View.GONE
            }
            submit_btn.setOnClickListener {
                    answer_edit_text.isEnabled = false
                    curr_question?.answer = answer_edit_text.text.toString()
                    curr_question?.answeredOn = System.currentTimeMillis()
                    submit_btn.text = "submitting..."
                    val current_tree_reference =
                        mdbref.child(Trees).child(Adopted_trees).child(tree_id)
                    current_tree_reference.child(Tree_question_list_unanswered)
                        .child(curr_question?.askedOn.toString())
                        .removeValue().addOnSuccessListener {
                            current_tree_reference.child(Tree_question_list_answered)
                                .child(curr_question?.answeredOn.toString())
                                .setValue(curr_question)
                                .addOnSuccessListener {
                                    current_tree_reference.child(Unread_Question_count)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                var count = 0L
                                                if (snapshot.exists())
                                                    count = snapshot.value as Long
                                                Log.d(TAG, "current count = " + count)
                                                snapshot.ref.setValue(count + 1)
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    submit_btn.text = "Done"
                                    // remove it from list
                                    Log.d(
                                        TAG,
                                        "current list size = " + uaq_gardener_adapter?.questionList?.size
                                    )
                                    try {
                                        uaq_gardener_adapter?.questionList?.removeAt(adapterPosition)
                                        uaq_gardener_adapter?.notifyItemRemoved(adapterPosition)
                                    } catch (e: Exception) {
                                        Log.d(TAG, "Error occured while removing from adapter")
                                    }
                                }
                        }

                }

        }

        fun linkAdapter(adapter: UAQ_gardener_Adapter?): UAQ_VH {
            uaq_gardener_adapter = adapter
            return this
        }

    }
}

