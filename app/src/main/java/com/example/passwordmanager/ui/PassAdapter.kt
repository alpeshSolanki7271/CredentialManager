package com.example.passwordmanager.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.R
import com.example.passwordmanager.data.model.PasswordData
import com.example.passwordmanager.databinding.ItemPasswordLayoutBinding
import kotlin.time.Duration.Companion.milliseconds

class PassAdapter(
    var userList: List<PasswordData>,
    val onItemClicked: (passwordData: PasswordData) -> Unit,
) : RecyclerView.Adapter<PassAdapter.PasswordHolder>() {

    private var filteredItemList: List<PasswordData> = userList

    class PasswordHolder(var binding: ItemPasswordLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordHolder {
        val view = DataBindingUtil.inflate<ItemPasswordLayoutBinding>(
            LayoutInflater.from(parent.context), R.layout.item_password_layout, parent, false
        )

        return PasswordHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: PasswordHolder, position: Int) {
        val data = userList[position]

        if (data.logo == 0) {
            holder.binding.icLogo.setImageResource(R.drawable.logo_bg)
        } else {
            holder.binding.icLogo.setImageResource(data.logo)
        }

        holder.binding.txtType.text = data.accountType
        holder.binding.txtUsername.text = data.username
        holder.binding.root.setOnClickListener {
            onItemClicked(data)
        }

    }

    fun updateList(userList: List<PasswordData>) {
        this.userList = userList
        notifyDataSetChanged()
    }



}