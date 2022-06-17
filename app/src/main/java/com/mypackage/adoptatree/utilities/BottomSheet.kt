package com.mypackage.adoptatree.utilities

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.R

class BottomSheet(val id: String) : BottomSheetDialogFragment() {


    data class UnansweredQuestion(val question_text: String, val askedOn: Long)
        var onQuestionAdded:((String)->(Unit))?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.question_add_bottom_sheet, container, false)
         val addButton = view.findViewById<Button>(R.id.btnAddItem)
        addButton.setOnClickListener {
            val editQuestion = view.findViewById<EditText>(R.id.editQuestion)

            if (!TextUtils.isEmpty(editQuestion.text.toString())) {
                //Add question to database
                val questionText = editQuestion.text.toString()
                onQuestionAdded?.invoke(questionText)
            } else {
                Toast.makeText(context, "Question is empty", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }

}