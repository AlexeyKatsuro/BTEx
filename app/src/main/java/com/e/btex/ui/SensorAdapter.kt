package com.e.btex.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.diaryoffilms.ui.common.DataBoundListAdapter
import com.e.btex.R
import com.e.btex.data.dto.Sensor
import com.e.btex.databinding.ItemSensorBinding
import com.e.btex.utils.extensions.getString

class SensorAdapter() : DataBoundListAdapter<Sensor, ItemSensorBinding>(
        diffCallback = object : DiffUtil.ItemCallback<Sensor>() {
            override fun areItemsTheSame(oldItem: Sensor, newItem: Sensor) =
                    oldItem::class == newItem::class

            override fun areContentsTheSame(oldItem: Sensor, newItem: Sensor) =
                    oldItem.value == newItem.value

        }) {
    override fun createBinding(parent: ViewGroup): ItemSensorBinding {
        return ItemSensorBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
    }

    override fun bind(binding: ItemSensorBinding, item: Sensor) {
        binding.text.text = binding.root.context.resources.let {
            item.getString(it)
        }

    }

    fun clear() {
        submitList(mutableListOf())
    }
}