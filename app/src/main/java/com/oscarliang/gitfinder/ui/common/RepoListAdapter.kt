package com.oscarliang.gitfinder.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.oscarliang.gitfinder.R
import com.oscarliang.gitfinder.databinding.LayoutRepoItemBinding
import com.oscarliang.gitfinder.model.Repo

class NewsListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    private val itemClickListener: ((Repo) -> Unit)?,
    private val bookmarkClickListener: ((Repo) -> Unit)?
) : DataBoundListAdapter<Repo, LayoutRepoItemBinding>(
    diffCallback = object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun createBinding(parent: ViewGroup): LayoutRepoItemBinding {
        val binding = DataBindingUtil.inflate<LayoutRepoItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.layout_repo_item,
            parent,
            false,
            dataBindingComponent
        )
        binding.root.setOnClickListener {
            binding.repo?.let {
                itemClickListener?.invoke(it)
            }
        }
        binding.btnBookmark.setOnClickListener {
            binding.repo?.let {
                bookmarkClickListener?.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: LayoutRepoItemBinding, item: Repo) {
        binding.repo = item
    }

}