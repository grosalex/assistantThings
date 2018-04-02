package com.grosalex.androidassistantthings

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.philips.lighting.hue.sdk.wrapper.domain.device.Device
import com.philips.lighting.hue.sdk.wrapper.domain.DomainType
import android.view.InputDevice.getDevice
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge
import com.philips.lighting.hue.sdk.wrapper.domain.HueError
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse


/**
 * Created by grosalex on 02/04/2018.
 */
class DevicesAdapter(var lampActivity: Activity, var devices: List<Device>?) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {
    override fun getItemCount(): Int = devices?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lamp_item, parent, false)
        return DevicesAdapter.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.let {
            if (devices?.get(position) is LightPoint) {
                val light = devices?.get(position) as LightPoint
                it.tvName?.text = light.name

                val lightState = light.lightState
                val switch = it.switch ?: return
                switch.isChecked = lightState.isOn
                it.v.setOnClickListener {
                    switch.isChecked = !switch.isChecked
                }
                switch.setOnCheckedChangeListener { _, isChecked ->
                    lightState.isOn = isChecked
                    light.updateState(lightState, BridgeConnectionType.LOCAL, object : BridgeResponseCallback() {
                        override fun handleCallback(p0: Bridge?, p1: ReturnCode?, p2: MutableList<ClipResponse>?, p3: MutableList<HueError>?) {
                        }
                    })
                }
            } else {
                Log.e("DEVICE TYPE " + position, devices?.get(position)?.javaClass?.name)
            }
        }
    }

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        var tvName: TextView? = v.findViewById(R.id.tv_lamp_name)
        var switch: Switch? = v.findViewById(R.id.switch_status)

    }
}