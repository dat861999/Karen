package com.androidcode.kraken.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androidcode.kraken.R
import com.androidcode.kraken.model.Board
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(private val context: Context,

                             private var list: ArrayList<Board>):
RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null
    // Nạp từng view item mà đã được ta thiết kế ở xml layout file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewHolder:
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board,
                    parent,
                    false))
    }
    // Liên kết từng item trong ArrayList vào view
    // Hàm được gọi khi RecyclerView cần một ViewHolder mớicủa loại đã cho để đại diện
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.iv_board_image)
            holder.itemView.tv_name.text = model.name
            holder.itemView.tv_created_by.text = "Created By: ${model.createBy}"

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }
    // interface cho sự kiện onClick
    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    // Lấy ra số lượng items trong danh sách để hiển thị
    override fun getItemCount(): Int {
        return list.size
    }
    // ViewHolder mô tả các item view và dữ liệu về vị trí của nó trong RecyclerView
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)


}