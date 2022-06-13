package com.mypackage.adoptatree.Maintainance.Update

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.mypackage.adoptatree.databinding.ActivityUpdateBinding
import com.yalantis.ucrop.UCrop

class Update_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    private val imguploadviewmodel: Image_upload_ViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tree_id = intent.getStringExtra("tree_id")
        val sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager, tree_id ?: "invalid_id")
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            imguploadviewmodel.setData(UCrop.getOutput(data!!).toString())
        }
    }
}