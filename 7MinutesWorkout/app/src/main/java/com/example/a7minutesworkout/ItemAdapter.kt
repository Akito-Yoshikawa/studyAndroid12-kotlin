package com.example.a7minutesworkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.a7minutesworkout.databinding.ItemsRowBinding

class ItemAdapter(private val items: ArrayList<ExerciseHistoryEntity>): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val llMain = binding.llMain
        val tvID = binding.tvID
        val tvTime = binding.tvTimer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 取得してきた結果を反映
        holder.tvID.text = item.id.toString()
        holder.tvTime.text = item.time

        // 色かえる？
    }


}