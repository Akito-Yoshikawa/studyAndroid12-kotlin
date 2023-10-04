package com.example.happyplaces.adapters

import android.app.Activity
import android.content.ClipData.Item
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activitys.AddHappyPlaceActivity
import com.example.happyplaces.activitys.MainActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlacesAdapter(private val context: Context,
                         private var items: ArrayList<HappyPlaceModel>): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemHappyPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val ivPlaceImage = binding.ivPlaceImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)

        val isDeleted = dbHandler.deleteHappyPlace(items[position])
        if (isDeleted > 0) {
            // 配列リストからアイテムを削除
            items.removeAt(position)

            // アイテムが削除されたことをアダプタに通知
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)

        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, items[position])

        activity.startActivityForResult(intent, requestCode)

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = items[position]

        holder.tvTitle.text = model.title
        holder.tvDescription.text = model.description
        holder.ivPlaceImage.setImageURI(Uri.parse(model.image))

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

}