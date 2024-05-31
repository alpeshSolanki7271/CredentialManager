package com.example.passwordmanager.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.data.model.PasswordData
import com.example.passwordmanager.databinding.ItemLogoHolderBinding

class LogoAdapter(
    val context: Context,
    private val logoList: List<Int>,
    val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<LogoAdapter.LogoHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class LogoHolder(var binding: ItemLogoHolderBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogoHolder {
        val view = DataBindingUtil.inflate<ItemLogoHolderBinding>(
            LayoutInflater.from(parent.context), R.layout.item_logo_holder, parent, false
        )
        return LogoAdapter.LogoHolder(view)
    }

    override fun getItemCount(): Int {
        return logoList.size
    }

    override fun onBindViewHolder(holder: LogoHolder, position: Int) {
        val data = logoList[position]
        holder.binding.logoIc.setImageResource(data)

        if (position == selectedPosition) {
            holder.binding.cardview.strokeColor = ContextCompat.getColor(context, R.color.blue)
            holder.binding.cardview.strokeWidth = 2
        } else {
            holder.binding.cardview.strokeWidth = 0
        }


        holder.binding.root.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onItemClicked(data)
        }
    }

}