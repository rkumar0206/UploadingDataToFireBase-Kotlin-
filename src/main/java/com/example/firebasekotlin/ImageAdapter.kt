package com.example.firebasekotlin

import android.content.Context
import android.view.*

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.image_item.view.*

class ImageAdapter(var context: Context, private val uploads: List<Upload>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    //private var mUploads:List<Upload>?=uploads

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false)

        return ImageViewHolder(view)

    }

    override fun getItemCount(): Int = uploads.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        holder.setData(uploads[position], position)

    }


    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {


        init {
            itemView.setOnClickListener(this)
            itemView.setOnCreateContextMenuListener(this)
        }

        fun setData(item: Upload, position: Int) {

            itemView.text_view_name.text = item.mName
            Glide.with(context)
                .load(item.mImageUrl)
                .centerCrop()
                .fitCenter()
                .placeholder(R.mipmap.ic_launcher)
                .into(itemView.image_view_upload)
        }

        override fun onClick(v: View?) {

            if (mListener != null) {

                val pos: Int? = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    mListener?.OnItemClick(pos!!)
                }
            }

        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.setHeaderTitle("Select Action")
            val doWhatever:MenuItem?=menu?.add(Menu.NONE,1,1,"Do Whatever")
            val delete:MenuItem?=menu?.add(Menu.NONE,2,2,"Delete")

            doWhatever?.setOnMenuItemClickListener(this)
            delete?.setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            if (mListener != null) {

                val pos: Int? = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {

                    when(item?.itemId){

                        1-> {
                            mListener?.OnDoWhateverClicked(pos!!)
                            return true
                        }

                        2-> {
                            mListener?.OnDeleteClicked(pos!!)
                            return true
                        }
                    }
                }
            }
           return false
        }


    }

    interface OnItemClickListener {

        fun OnItemClick(pos: Int)

        fun OnDoWhateverClicked(pos: Int)

        fun OnDeleteClicked(pos: Int)

    }

    fun setOnItemClickListener(listener: OnItemClickListener) {

        mListener = listener
    }

}