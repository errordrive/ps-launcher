package com.nayem.pslauncher.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nayem.pslauncher.R

data class GameItem(val label: String, val packageName: String, val icon: Drawable)

class GameAdapter(private val onTap: (GameItem) -> Unit) :
    RecyclerView.Adapter<GameAdapter.Holder>() {

    private var items: List<GameItem> = emptyList()

    fun submit(list: List<GameItem>) {
        items = list
        notifyDataSetChanged()
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.gameIcon)
        val label: TextView = v.findViewById(R.id.gameLabel)
        val pkg: TextView = v.findViewById(R.id.gamePkg)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): Holder =
        Holder(LayoutInflater.from(p.context).inflate(R.layout.item_game, p, false))

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val g = items[pos]
        h.icon.setImageDrawable(g.icon)
        h.label.text = g.label
        h.pkg.text = g.packageName
        h.itemView.setOnClickListener { onTap(g) }
    }

    override fun getItemCount(): Int = items.size
}
