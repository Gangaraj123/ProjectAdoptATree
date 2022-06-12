package com.mypackage.adoptatree.Maintainance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG
import com.mypackage.adoptatree.models.Question

class UAQ_gardener_Adapter(val questionList: ArrayList<Question>) :
    RecyclerView.Adapter<UAQ_gardener_Adapter.UAQ_VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UAQ_VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.unanswered_question_gardener, parent, false)
        return UAQ_VH(view).linkAdapter(this)
    }

    override fun onBindViewHolder(holder: UAQ_VH, position: Int) {
        holder.question_text.text = "Question number " + position.toString()

        //recycler view recycles the views, so if this views display is changed when used before, reset it
        
        if(holder.answer_edit_text.visibility==View.VISIBLE)
            holder.answer_edit_text.visibility=View.GONE
        holder.answer_edit_text.setText("")
        if(holder.respond_btn.visibility!=View.VISIBLE)
        holder.respond_btn.visibility=View.VISIBLE
        if(holder.submit_btn.visibility==View.VISIBLE)
            holder.submit_btn.visibility=View.GONE
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
                submit_btn.text = "Done"
                Log.d(TAG, "Clicked on item number= " + adapterPosition)
                answer_edit_text.isEnabled = false
                uaq_gardener_adapter?.questionList?.removeAt(adapterPosition)
                uaq_gardener_adapter?.notifyItemRemoved(adapterPosition)
            }
        }

        fun linkAdapter(adapter: UAQ_gardener_Adapter?): UAQ_VH {
            uaq_gardener_adapter = adapter
            return this
        }

    }
}

//class UAQ_VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//    init {
//        respond_btn = itemView.findViewById(R.id.btn_add_answer)
//        submit_btn = itemView.findViewById(R.id.btn_submit_answer)
//        answer_edit_text = itemView.findViewById(R.id.entered_response)
//        respond_btn.setOnClickListener { view: View? ->
//            answer_edit_text.visibility = View.VISIBLE
//            respond_btn.visibility = View.GONE
//            submit_btn.visibility = View.VISIBLE
//        }
//        submit_btn.setOnClickListener {
//            submit_btn.text = "Done"
//            answer_edit_text.isEnabled = false
////            Handler().postDelayed(Runnable {
//            uaq_gardener_adapter!!.questionList.removeAt(adapterPosition)
//            uaq_gardener_adapter!!.notifyItemRemoved(adapterPosition)
////            }, 2000)
//        }
//    }
//}