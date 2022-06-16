package com.mypackage.adoptatree.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.mypackage.adoptatree.ActivityUserQA
import com.mypackage.adoptatree.R
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class QuestionAnswerAdapter(private val questionsList : ArrayList<ActivityUserQA.QA>) : RecyclerView.Adapter<QuestionAnswerAdapter.QuestionAnswerViewHolder>() {

    class QuestionAnswerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val questionText : TextView = itemView.findViewById(R.id.question)
        val answerText : TextView = itemView.findViewById(R.id.answer)
        val askedOn : TextView = itemView.findViewById(R.id.asked_on)
        val answeredOn : TextView = itemView.findViewById(R.id.answered_on)
        val answerTextBox : TextView = itemView.findViewById(R.id.answerTextBox)
        val answeredOnTextBox : TextView = itemView.findViewById(R.id.answeredOnTextBox)
        val divider : MaterialDivider = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.answered_questions_layout, parent, false)
        return QuestionAnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionAnswerViewHolder, position: Int) {
        val q = questionsList[position]

        holder.questionText.text = q.question
        holder.askedOn.text = convertTimeInMillisToString(q.askedOn)
        holder.answerText.text = if(q.answer != null) q.answer else ""
        holder.answeredOn.text = if(q.answeredOn != null) convertTimeInMillisToString(q.answeredOn) else ""

        if(q.answer == null){
            holder.divider.visibility = View.GONE
            holder.answerText.visibility = View.GONE
            holder.answeredOn.visibility = View.GONE
            holder.answerTextBox.visibility = View.GONE
            holder.answeredOnTextBox.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return questionsList.size
    }


    fun convertTimeInMillisToString(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy hh:mm aa", Locale.getDefault())
        val date = Date(time)
        return simpleDateFormat.format(date)
    }
}