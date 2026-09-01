package com.zuhri.jsontool

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JsonTreeAdapter(
    private var items: List<JsonNode> = emptyList()
) : RecyclerView.Adapter<JsonTreeAdapter.NodeViewHolder>() {

    class NodeViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    fun submitList(newItems: List<JsonNode>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_json_node, parent, false) as TextView
        return NodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = items[position]
        val density = holder.textView.context.resources.displayMetrics.density
        val indentPx = (node.depth * 16 * density).toInt()
        holder.textView.setPadding(indentPx + (8 * density).toInt(), holder.textView.paddingTop, holder.textView.paddingEnd, holder.textView.paddingBottom)
        holder.textView.text = if (node.value == "{...}" || node.value == "[...]") {
            "${node.key} ${node.value}"
        } else {
            "${node.key}: ${node.value}"
        }
    }

    override fun getItemCount(): Int = items.size
}
