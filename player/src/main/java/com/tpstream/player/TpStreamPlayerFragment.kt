package com.tpstream.player

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class TpStreamPlayerFragment : Fragment() {

    companion object {
        fun newInstance() = TpStreamPlayerFragment()
    }

    private lateinit var viewModel: TpStreamPlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tp_stream_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TpStreamPlayerViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun initialize(context: Context) {
    }
}