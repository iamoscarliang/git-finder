package com.oscarliang.gitfinder.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.databinding.FragmentBookmarksBinding
import com.oscarliang.gitfinder.ui.common.RepoListAdapter
import com.oscarliang.gitfinder.util.autoCleared
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : Fragment() {

    var binding by autoCleared<FragmentBookmarksBinding>()
    private val viewModel: BookmarksViewModel by viewModel<BookmarksViewModel>()
    private var adapter by autoCleared<RepoListAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = DataBindingUtil.inflate<FragmentBookmarksBinding>(
            inflater,
            R.layout.fragment_bookmarks,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bookmarks = viewModel.bookmarks
        binding.lifecycleOwner = viewLifecycleOwner
        val rvAdapter = RepoListAdapter(
            itemClickListener = {
                findNavController()
                    .navigate(
                        BookmarksFragmentDirections.actionToDetailFragment(
                            it.url
                        )
                    )
            },
            bookmarkClickListener = {
                viewModel.toggleBookmark(it)
            }
        )
        binding.repoList.apply {
            adapter = rvAdapter
            layoutManager = GridLayoutManager(
                this@BookmarksFragment.context,
                resources.getInteger(R.integer.columns_count)
            )
            itemAnimator?.changeDuration = 0
        }
        this.adapter = rvAdapter
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { repos ->
            adapter.submitList(repos)
        }
    }

}