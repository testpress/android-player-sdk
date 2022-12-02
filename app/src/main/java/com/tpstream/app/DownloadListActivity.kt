package com.tpstream.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.tpstream.player.DownloadTask
import com.tpstream.player.models.VideoInfo

class DownloadListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_list)

        val downloads = DownloadTask(this).getAllDownloads()

        Toast.makeText(this, "${downloads?.size}", Toast.LENGTH_SHORT).show()

        val listView = findViewById<ListView>(R.id.download_list_view)

        listView.adapter = MyAdapter(this, downloads!!)

    }

    inner class MyAdapter(context: Context, data: List<VideoInfo>) :
        ArrayAdapter<VideoInfo>(context, R.layout.download_video_list_item, data) {

        private val context1 = context

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (convertView == null) {
                view = LayoutInflater.from(context1)
                    .inflate(R.layout.download_video_list_item, parent, false)
            }

            view!!.findViewById<TextView>(R.id.video_title).text = getItem(position)?.title!!
            view.findViewById<TextView>(R.id.video_id).text = getItem(position)?.videoId!!

            val status = if (DownloadTask(getItem(position)?.dashUrl!!,context1).isDownloaded()) 3 else 0

            view.findViewById<ImageView>(R.id.download_status).setImageResource(getDownloadStatusImage(status))


            return view
        }

        private fun getDownloadStatusImage(int: Int): Int {
            return when (int) {
                0 -> R.drawable.download_for_offline
                2 -> R.drawable.downloading
                else -> R.drawable.download_done
            }
        }
    }

}

