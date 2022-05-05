package dk.itu.moapd.scootersharing.utils

import android.location.Location
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * A view model sensitive to changes in the `MainActivity` fragments.
 */
class MainActivityVM : ViewModel() {

    /**
     * A list of all available fragments used by the main activity. P.S.: These instances are
     * created only once, when the app executes the `onCreate()` method for the first time.s
     */
    private val fragments = ArrayList<Fragment>()

    private val fragmentScooterAdapter = HashMap<String, Fragment>()

    fun addAdapterFragment(id: String, fragment: Fragment){
        fragmentScooterAdapter.put(id, fragment)
    }


    /**
     * This method will be executed when the main activity adds a new fragment into the user
     * interface.
     *
     * @param fragment A new fragment to show in the user interface.
     */
    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }

    /**
     * This method returns a list of all instances of fragments used by the main activity.
     *
     * @return A list of all available fragments.
     */
    fun getFragmentList() : List<Fragment> = fragments

    fun getFragmentAdapterList(): List<Fragment> = fragmentScooterAdapter.values.toList()

    /**
     * The current selected fragment to show in the user interface.
     */
    private val fragment = MutableLiveData<Fragment>()

    /**
     * A `LiveData` which publicly exposes any update in the current shown fragment.
     */
    val fragmentState: LiveData<Fragment>
        get() = fragment

    /**
     * This method will be executed when the user selects a new fragment to show in the main
     * activity. It sets the text into the LiveData instance.
     *
     * @param index An ID of the selected fragment.
     */
    fun setFragment(index: Int) {
        fragment.value = fragments.elementAt(index)
    }

    fun setAdapterFragment(id: String){
        fragment.value = fragmentScooterAdapter.getValue(id)
    }

    /**
     * The ID of the latest button pressed by the user.
     */
    private var buttonId = 0

    /**
     * This method returns the ID resource of the latest button pressed by the user in the main UI.
     *
     * @return The ID resource of a Material Design button.
     */
    fun getButtonId() = buttonId

    /**
     * This method will be executed when the user press a new button in the user interface.
     *
     * @param buttonId The ID resource of a Material Design button.
     */
    fun setButtonId(buttonId: Int) {
        this.buttonId = buttonId
    }

    private val location = MutableLiveData<Location>()

    /**
     * A LiveData which publicly exposes any update in the location-aware service.
     *
     * @return The current user location.
     */
    val locationState: LiveData<Location>
        get() = location

    /**
     * This method will be executed when the location-aware service updates the current user
     * location. It sets the updated location into the LiveData instance.
     *
     * @param location The current user's location obtained from the location-aware service.
     */
    fun onLocationChanged(location: Location) {
        this.location.value = location
    }

    val displayname = MutableLiveData<String>()

    fun getDisplayname(): String? = displayname.value

    fun onDisplaynameChanged(name: String){
        this.displayname.value = name
    }


}