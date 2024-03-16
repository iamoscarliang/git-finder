package com.oscarliang.gitfinder.util

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.IdlingResource
import java.util.UUID

class DataBindingIdlingResource<F : Fragment> : IdlingResource {

    // list of registered callbacks
    private val idlingCallbacks = mutableListOf<IdlingResource.ResourceCallback>()

    // give it a unique id to workaround an espresso bug where you cannot register/unregister
    // an idling resource w/ the same name.
    private val id = UUID.randomUUID().toString()

    // holds whether isIdle is called and the result was false. We track this to avoid calling
    // onTransitionToIdle callbacks if Espresso never thought we were idle in the first place.
    private var wasNotIdle = false

    private lateinit var scenario: FragmentScenario<F>

    override fun getName() = "DataBinding $id"

    /**
     * Sets the fragment from a [FragmentScenario] to be used from [DataBindingIdlingResource].
     */
    fun monitorFragment(fragmentScenario: FragmentScenario<F>) {
        scenario = fragmentScenario
    }

    override fun isIdleNow(): Boolean {
        val idle = !getBindings().any { it.hasPendingBindings() }
        if (idle) {
            if (wasNotIdle) {
                // notify observers to avoid espresso race detector
                idlingCallbacks.forEach { it.onTransitionToIdle() }
            }
            wasNotIdle = false
        } else {
            wasNotIdle = true
            // check next frame
            scenario.onFragment { fragment ->
                fragment.view?.postDelayed({
                    if (fragment.view != null) {
                        isIdleNow
                    }
                }, 16)
            }
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        idlingCallbacks.add(callback)
    }

    /**
     * Find all binding classes in all currently available fragments.
     */
    private fun getBindings(): List<ViewDataBinding> {
        lateinit var bindings: List<ViewDataBinding>
        scenario.onFragment { fragment ->
            bindings = fragment.requireView().flattenHierarchy().mapNotNull { view ->
                DataBindingUtil.getBinding(view)
            }
        }
        return bindings
    }

    private fun View.flattenHierarchy(): List<View> = listOf(this)

}