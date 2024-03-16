package com.oscarliang.gitfinder.ui.search

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.binding.FragmentDataBindingComponent
import com.oscarliang.gitfinder.databinding.FragmentSearchBinding
import com.oscarliang.gitfinder.ui.common.RepoListAdapter
import com.oscarliang.gitfinder.ui.common.RetryListener
import com.oscarliang.gitfinder.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    var binding by autoCleared<FragmentSearchBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent()
    private val viewModel by viewModel<SearchViewModel>()
    private var adapter by autoCleared<RepoListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentSearchBinding>(
            inflater,
            R.layout.fragment_search,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchResults = viewModel.searchResults
        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = RepoListAdapter(
            dataBindingComponent = dataBindingComponent,
            itemClickListener = {
                findNavController()
                    .navigate(
                        SearchFragmentDirections.actionToDetailFragment(
                            it.url
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.listener = object : RetryListener {
            override fun retry() {
                viewModel.retry()
            }
        }
        binding.repoList.apply {
            adapter = rvAdapter
            layoutManager = GridLayoutManager(
                this@SearchFragment.context,
                resources.getInteger(R.integer.columns_count)
            )
            itemAnimator?.changeDuration = 0
        }
        this.adapter = rvAdapter
        initRecyclerView()
        initSearchInputListener()
    }

    private fun initRecyclerView() {
        binding.nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            // Check is scroll to bottom
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                viewModel.loadNextPage()
            }
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { result ->
            adapter.submitList(result?.data)
        }

        viewModel.loadMoreStatus.observe(viewLifecycleOwner) { loadingMore ->
            if (loadingMore == null) {
                binding.loadingMore = false
            } else {
                binding.loadingMore = loadingMore.isRunning
                val error = loadingMore.errorMessageIfNotHandled
                if (error != null) {
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initSearchInputListener() {
        binding.editSearch.setOnEditorActionListener { view: View, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(view)
                true
            } else {
                false
            }
        }
        binding.editSearch.setOnKeyListener { view: View, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                doSearch(view)
                true
            } else {
                false
            }
        }
    }

    private fun doSearch(v: View) {
        dismissKeyboard(v.windowToken)
        val query = binding.editSearch.text.toString()
        if (query.isBlank()) {
            Snackbar.make(binding.root, getString(R.string.empty_search), Snackbar.LENGTH_LONG).show()
        } else {
            viewModel.setQuery(query, 10)
        }
    }

    private fun dismissKeyboard(windowToken: IBinder) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

}