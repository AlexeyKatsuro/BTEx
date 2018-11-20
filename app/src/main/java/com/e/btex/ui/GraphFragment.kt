package com.e.btex.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.e.btex.R
import com.e.btex.databinding.FragmentGraphBinding

class GraphFragment: Fragment() {


    private lateinit var binding: FragmentGraphBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding= FragmentGraphBinding.inflate(inflater,container,false)

        binding.appBar.toolBar.inflateMenu(R.menu.main_menu)
        binding.appBar.toolBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_setting -> {
                    findNavController().navigate(R.id.showSettingFragment)
                    true
                }
                else -> false
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {

        }
    }

}