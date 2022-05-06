package dk.itu.moapd.scootersharing.utils

import androidx.fragment.app.Fragment
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class MainActivityVMTest {
    private lateinit var viewModel: MainActivityVM

    @Before
    fun setup() {
        viewModel = MainActivityVM()
    }

    @Test
    fun addFragmentModelActivity() {
        viewModel.addFragment(Fragment())
        assertEquals(1, viewModel.getFragmentList().size)
    }

    @Test
    fun addAdapterFragmentModelActivity() {
        viewModel.addAdapterFragment("ole", Fragment())
        assertEquals(1, viewModel.getFragmentAdapterList().size)
    }

    @Test
    fun getListModelActivity() {
        val fragment = Fragment()
        viewModel.addFragment(fragment)
        assertEquals(fragment, viewModel.getFragmentList()[0])
    }

    @Test
    fun getListAdapterModelActivity() {
        val fragment = Fragment()
        viewModel.addAdapterFragment("Ole", fragment)
        assertEquals(fragment, viewModel.getFragmentAdapterList()[0])
    }
}