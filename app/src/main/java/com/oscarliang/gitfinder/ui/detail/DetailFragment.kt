package com.oscarliang.gitfinder.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.oscarliang.gitfinder.databinding.FragmentDetailBinding
import com.oscarliang.gitfinder.util.autoCleared

class DetailFragment : Fragment() {

    var binding by autoCleared<FragmentDetailBinding>()

    private val params by navArgs<DetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dataBinding = FragmentDetailBinding.inflate(
            inflater,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.webView.loadUrl(params.repoUrl)
    }

}