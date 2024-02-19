package com.example.sharenote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharenote.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var mBinding : FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        mBinding = binding
        return mBinding?.root
        */
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

}