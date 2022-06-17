package com.mypackage.adoptatree.User

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.Adopted_trees
import com.mypackage.adoptatree.Tree_question_list_unanswered
import com.mypackage.adoptatree.Trees
import com.mypackage.adoptatree.databinding.ActivityUserQaBinding
import com.mypackage.adoptatree.models.Question
import com.mypackage.adoptatree.utilities.BottomSheet

class ActivityUserQA : AppCompatActivity() {

    private lateinit var binding: ActivityUserQaBinding
    private val pageViewModel: PageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserQaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val id = intent.getStringExtra("id").toString()
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, id)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        binding.addQuestion.setOnClickListener {
            val fdbref = FirebaseDatabase.getInstance().reference
            val bottomSheet = BottomSheet(id!!)
            bottomSheet.show(supportFragmentManager, "TAG")
            bottomSheet.onQuestionAdded = {
                val curr_time = System.currentTimeMillis()
                val new_question = Question(question = it, askedon = curr_time)
                fdbref.child(Trees).child(Adopted_trees).child(id!!)
                    .child(Tree_question_list_unanswered).child(curr_time.toString())
                    .setValue(new_question).addOnSuccessListener {
                        pageViewModel.setData(new_question)
                        bottomSheet.dismiss()
                    }
            }
        }
    }
}