package com.mypackage.adoptatree.Maintainance.Update

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.database.core.utilities.Tree

private val TAB_TITLES = arrayOf(
    "Questions",
    "Image"
)


class SectionsPagerAdapter(private val context: Context, fm: FragmentManager,val tree_id:String) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return if(position==0)
            QuestionsFragment(tree_id)
        else
            ImageUploadFragment(tree_id)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return TAB_TITLES[position]
    }

    override fun getCount(): Int {
        return 2
    }
}