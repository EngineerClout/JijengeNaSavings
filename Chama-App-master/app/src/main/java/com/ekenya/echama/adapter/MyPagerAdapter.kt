package com.ekenya.echama.adapter

import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val mfragmentList = ArrayList<Fragment>()
    //For keeping track the name of the Fragments
    private val mfragmentTitleList = ArrayList<String>()

    //Add Fragment to the fragment List
    fun addFragment(fragment: Fragment, title: String): Boolean {

        mfragmentList.add(fragment)
        mfragmentTitleList.add(title)
        return false
    }

    /**
     * @param fm of type FragmentManager
     */
    @Nullable
    override fun getPageTitle(position: Int): CharSequence {
        return mfragmentTitleList[position]
    }

    override fun getItem(position: Int): Fragment {
        return mfragmentList.get(position)
         }

    override fun getCount(): Int {
       return mfragmentList.size
    }
}