package com.cse4100g10.taskmanager.ui.main

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cse4100g10.taskmanager.R

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class ProjectListPagerAdapter(private val context: Context, fm: FragmentManager) :

    FragmentPagerAdapter(fm) {

    val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3
    )

    val TAB_ICONS = arrayOf(
        R.drawable.ic_star_white_24dp,
        R.drawable.ic_donut_large_white_24dp,
        R.drawable.ic_timer_white_24dp
    )

    private val FRAGMENTS = arrayOf<ProjectListFragment?>(null, null, null)

    private var currentFragment : Fragment? = null
    private var currentFragmentType : Int = 0

    fun getFavouritesFragment() : ProjectListFragment?{
        return FRAGMENTS[0]
    }

    fun getMainFragment() : ProjectListFragment?{
        return FRAGMENTS[1]
    }

    fun getUpcomingFragment() : ProjectListFragment?{
        return FRAGMENTS[2]
    }

    fun getCurrentFragment() : Fragment?{
        return currentFragment
    }


    fun getCurrentFragmentType() : Int{
        return currentFragmentType
    }

    override fun getItem(position: Int): Fragment {
        var fragment = ProjectListFragment()
        FRAGMENTS[position] = fragment
        return fragment
    }


    override fun getCount(): Int {
        return TAB_TITLES.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        if (getCurrentFragment() !== obj) {
            currentFragment = obj as Fragment?
        }
        if (getCurrentFragmentType() != TAB_TITLES[position]) {
            currentFragmentType = TAB_TITLES[position]
        }
        super.setPrimaryItem(container, position, obj)
    }


}