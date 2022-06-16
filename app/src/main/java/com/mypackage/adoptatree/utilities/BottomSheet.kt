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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.Adopted_trees
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.Trees

class BottomSheet(val id: String) : BottomSheetDialogFragment() {


    data class UnansweredQuestion(val question_text: String, val askedOn: Long)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.row_add_item, container, false)

        val fdbref = FirebaseDatabase.getInstance().reference

        val addButton = view.findViewById<Button>(R.id.btnAddItem)
        addButton.setOnClickListener {
            val editQuestion = view.findViewById<EditText>(R.id.editQuestion)

            if (!TextUtils.isEmpty(editQuestion.text.toString())) {
                //Add question to database
                val questionText = editQuestion.text.toString()
                val askedOn = System.currentTimeMillis()

                fdbref.child(Trees).child(Adopted_trees).child(id).child("unanswered_questions")
                    .child(askedOn.toString()).setValue(UnansweredQuestion(questionText, askedOn))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Question added successfully", Toast.LENGTH_LONG)
                            .show()
                        dismiss()
                    }
            } else {
                Toast.makeText(context, "Question is empty", Toast.LENGTH_LONG).show()
            }
        }
        return view
    }
}