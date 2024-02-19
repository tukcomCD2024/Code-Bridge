package com.example.sharenote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sharenote.databinding.FragmentHomeBinding
import com.example.sharenote.databinding.FragmentMyPageBinding


class MyPageFragment : Fragment() {

    private var mBinding : FragmentMyPageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        mBinding = binding
        return mBinding?.root
        */
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

}