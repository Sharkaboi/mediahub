package com.sharkaboi.mediahub.modules.anime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sharkaboi.mediahub.common.extensions.emptyString
import com.sharkaboi.mediahub.common.extensions.shortSnackBar
import com.sharkaboi.mediahub.databinding.LoadStateItemBinding

class AnimeLoadStateAdapter : LoadStateAdapter<AnimeLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = LoadStateViewHolder(
        LoadStateItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) = holder.bind(loadState)

    inner class LoadStateViewHolder(
        private val binding: LoadStateItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.isVisible = loadState is LoadState.Loading
            if (loadState is LoadState.Error) {
                binding.root.shortSnackBar(loadState.error.message ?: String.emptyString)
            }
        }
    }
}
