package com.cse4100g10.taskmanager.ui.main

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cse4100g10.taskmanager.*

class ProjectPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    val TAB_TITLES = arrayOf(
        R.string.info,
        R.string.tasks,
        R.string.pictures,
        R.string.files
    )

    val FAB_ICONS = arrayOf(
        -1,
        R.drawable.ic_add_white_24dp,
        R.drawable.ic_add_a_photo_24dp,
        R.drawable.ic_file_upload_white_24dp
    )

    private val FRAGMENTS = arrayOf<Fragment?>(null, null, null, null)

    private val TAB_FRAGMENTS_CLASSES = arrayOf(
        ProjectInfoFragment::class.java,
        ProjectTasksFragment::class.java,
        ProjectPicturesFragment::class.java,
        ProjectFilesFragment::class.java
    )

    private var currentFragment : Fragment? = null
    private var currentFragmentType : Int = 0

    fun getInfoFragment() : ProjectInfoFragment?{
        return FRAGMENTS[0] as ProjectInfoFragment?
    }

    fun getTasksFragment() : ProjectTasksFragment?{
        return FRAGMENTS[1] as ProjectTasksFragment?
    }

    fun getPicturesFragment() : ProjectPicturesFragment?{
        return FRAGMENTS[2] as ProjectPicturesFragment?
    }

    fun getFilesFragment() : ProjectFilesFragment?{
        return FRAGMENTS[3] as ProjectFilesFragment?
    }

    fun getCurrentFragment() : Fragment?{
        return currentFragment
    }

    fun getCurrentFragmentType() : Int{
        return currentFragmentType
    }

    override fun getItem(position: Int): Fragment {
        var fragment = TAB_FRAGMENTS_CLASSES[position].newInstance()
        FRAGMENTS[position] = fragment
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
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