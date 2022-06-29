package com.mypackage.adoptatree.utilities

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG
import com.mypackage.adoptatree.User.AdoptedTreesActivity
import com.mypackage.adoptatree.User.MapsActivity
import java.text.SimpleDateFormat
import java.util.*

class AdoptedTreeAdapter(private val adoptedTreeList: List<AdoptedTreesActivity.TreeData>) :
    RecyclerView.Adapter<AdoptedTreeAdapter.AdoptedTreeViewHolder>() {

    var onImagesButtonClick: ((String, String) -> Unit)? = null
    var onQuestionsButtonClick: ((String, String) -> Unit)? = null

    class AdoptedTreeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname: TextView = itemView.findViewById(R.id.treeName)
        val adoptedOn: TextView = itemView.findViewById(R.id.adoptedOn)
        val lastWatered: TextView = itemView.findViewById(R.id.lastWatered)
        val lastWateredText: TextView = itemView.findViewById(R.id.lastWateredText)
        val imageButton: Button = itemView.findViewById(R.id.images)
        val questionButton: Button = itemView.findViewById(R.id.questions)
        val locatebutton: MaterialCardView = itemView.findViewById(R.id.locate_tree)
        val btn_locate:ImageButton=itemView.findViewById(R.id.btn_locate_tree)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdoptedTreeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.tree_item_user_side, parent, false)
        return AdoptedTreeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdoptedTreeViewHolder, position: Int) {
        val adoptedTree = adoptedTreeList[position]
        holder.btn_locate.isEnabled=false
        holder.nickname.text = adoptedTree.tree_nick_name
        holder.adoptedOn.text = convertTimeInMillisToString(adoptedTree.adopted_on)

        val zero: Long = 0
        if (adoptedTree.last_watered == zero) {
            holder.lastWatered.visibility = View.GONE
            holder.lastWateredText.visibility = View.GONE
        } else {
            holder.lastWatered.text = convertTimeInMillisToString(adoptedTree.last_watered)
        }
        Log.d(TAG, adoptedTree.toString())
        Log.d(TAG, "id = " + adoptedTree.id)
        holder.imageButton.setOnClickListener {
            onImagesButtonClick?.invoke(adoptedTree.id, adoptedTree.tree_nick_name)
        }

        holder.questionButton.setOnClickListener {
            onQuestionsButtonClick?.invoke(adoptedTree.id, adoptedTree.tree_nick_name)
        }

        holder.locatebutton.setOnClickListener {
            val intent = Intent(it.context, MapsActivity::class.java)
            intent.putExtra("tree_id", adoptedTree.id)
            intent.putExtra("tree_name", adoptedTree.tree_nick_name)
            it.context.startActivity(intent)
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