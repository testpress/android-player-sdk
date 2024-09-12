package com.tpstream.player.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tpstream.player.R
import com.tpstream.player.constants.PlaybackSpeed

internal class PlaybackSpeedAdapter(
    private val context: Context,
    private val selectedSpeed: Float,
    private val items: List<PlaybackSpeed>,
    private val onItemClick: (PlaybackSpeed) -> Unit
) : RecyclerView.Adapter<PlaybackSpeedAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val check: ImageView = itemView.findViewById(R.id.check)
        val text: TextView = itemView.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.playback_speed_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playbackSpeed = items[position]
        holder.text.text = if (playbackSpeed.value == 1.0f) "Normal" else playbackSpeed.text

        val isSelected = selectedSpeed == playbackSpeed.value
        holder.itemView.isSelected = isSelected
        holder.check.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(playbackSpeed)
        }
    }

    override fun getItemCount(): Int = items.size
}
