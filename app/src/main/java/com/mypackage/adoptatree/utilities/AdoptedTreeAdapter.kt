package com.mypackage.adoptatree.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.User.AdoptedTreesActivity
import kotlinx.coroutines.NonDisposableHandle.parent
import java.text.SimpleDateFormat
import java.util.*

class AdoptedTreeAdapter(private val adoptedTreeList: List<AdoptedTreesActivity.TreeData>) :
    RecyclerView.Adapter<AdoptedTreeAdapter.AdoptedTreeViewHolder>() {

    var onImagesButtonClick : ((String) -> Unit) ?= null

    class AdoptedTreeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname: TextView = itemView.findViewById(R.id.treeName)
        val adoptedOn: TextView = itemView.findViewById(R.id.adoptedOn)
        val lastWatered: TextView = itemView.findViewById(R.id.lastWatered)
        val lastWateredText : TextView = itemView.findViewById(R.id.lastWateredText)
        val imageButton : Button = itemView.findViewById(R.id.images)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdoptedTreeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tree_item_user_side, parent, false)
        return AdoptedTreeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdoptedTreeViewHolder, position: Int) {
        val adoptedTree = adoptedTreeList[position]

        holder.nickname.text = adoptedTree.nickname
        holder.adoptedOn.text = convertTimeInMillisToString(adoptedTree.adopted_on)

        val zero : Long = 0
        if(adoptedTree.last_watered == zero){
            holder.lastWatered.visibility = View.GONE
            holder.lastWateredText.visibility = View.GONE
        }else{
            holder.lastWatered.text = convertTimeInMillisToString(adoptedTree.last_watered)
        }

        holder.imageButton.setOnClickListener{
            onImagesButtonClick?.invoke(adoptedTree.id!!)
        }

    }

    override fun getItemCount(): Int {
        return adoptedTreeList.size
    }

    fun convertTimeInMillisToString(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy hh:mm aa", Locale.getDefault())
        val date = Date(time)
        return simpleDateFormat.format(date)
    }
}