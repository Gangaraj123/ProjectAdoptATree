package com.mypackage.adoptatree.User

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

private val TAB_TITLES = arrayOf(
    "Answered", "Not Answered"
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, val tree_id: String) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        if (position == 0)
            return userAQ(tree_id, true)
        else return userAQ(tree_id, false)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return TAB_TITLES[position]
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}