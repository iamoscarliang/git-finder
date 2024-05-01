package com.oscarliang.gitfinder.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        val dataBinding = FragmentBookmarksBinding.inflate(
            inflater,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.bookmarks = viewModel.bookmarks
        this.adapter = RepoListAdapter(
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
            adapter = this@BookmarksFragment.adapter
            itemAnimator?.changeDuration = 0
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        viewModel.bookmarks.observe(viewLifecycleOwner) { result ->
            adapter.submitList(result)
        }
    }

}