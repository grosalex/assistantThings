package com.grosalex.androidassistantthings

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.philips.lighting.hue.sdk.wrapper.connection.*
import kotlinx.android.synthetic.main.activity_lamp.*
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges
import java.util.*
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType
import com.philips.lighting.hue.sdk.wrapper.domain.*
import com.philips.lighting.hue.sdk.wrapper.domain.device.Device


/**
 * Created by grosalex on 02/04/2018.
 */

class LampActivity : Activity() {

    private var bridge: Bridge? = null

    private var bridgeDiscovery: BridgeDiscovery? = null

    internal enum class UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    private var bridgeDiscoveryResults: List<BridgeDiscoveryResult>? = null
    private lateinit var rvDevices: RecyclerView

    private lateinit var devicesAdapter: DevicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_lamp)
        ibBack.setOnClickListener { finish() }

        rvDevices = findViewById(R.id.rv_lamp)

        rvDevices.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        devicesAdapter = DevicesAdapter(this, devices)
        rvDevices.adapter = devicesAdapter
        //HUE stuff
        val bridgeIp = getLastUsedBridgeIp()
        if (bridgeIp == null) {
            startBridgeDiscovery()
        } else {
            connectToBridge(bridgeIp)
        }
    }

    /**
     * The callback that receives bridge connection events
     */
    private val bridgeConnectionCallback: BridgeConnectionCallback = object : BridgeConnectionCallback() {
        override fun onConnectionEvent(bridgeConnection: BridgeConnection?, connectionEvent: ConnectionEvent?) {
            when (connectionEvent) {
                ConnectionEvent.LINK_BUTTON_NOT_PRESSED ->
                    updateUI(UIState.Pushlinking, "Press the link button to authenticate.");

                ConnectionEvent.COULD_NOT_CONNECT ->
                    updateUI(UIState.Connecting, "Could not connect.");

                ConnectionEvent.CONNECTION_LOST ->
                    updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");

                ConnectionEvent.CONNECTION_RESTORED ->
                    updateUI(UIState.Connected, "Connection restored.");

                ConnectionEvent.DISCONNECTED -> return
            // User-initiated disconnection.

                else -> return
            }
        }

        override fun onConnectionError(p0: BridgeConnection?, p1: MutableList<HueError>?) {
        }

    }
    private var devices: MutableList<Device>? = null
    private val bridgeStateUpdatedCallback: BridgeStateUpdatedCallback = object : BridgeStateUpdatedCallback() {
        override fun onBridgeStateUpdated(bridge: Bridge?, bridgeStateUpdatedEvent: BridgeStateUpdatedEvent?) {
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);

            when (bridgeStateUpdatedEvent) {
                BridgeStateUpdatedEvent.INITIALIZED -> {
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    updateUI(UIState.Connected, "Connected!");
                    devices = bridge?.bridgeState?.devices
                    devices?.let { updateDevicesList(it) }
                }



                BridgeStateUpdatedEvent.LIGHTS_AND_GROUPS -> return
                else -> return
            }
        }
    }

    private fun updateDevicesList(devices: MutableList<Device>) {
        runOnUiThread {
            devicesAdapter.devices = devices
            devicesAdapter.notifyDataSetChanged()
        }
    }

    /**
     * The callback that receives the results of the bridge discovery
     */
    private val bridgeDiscoveryCallback = object : BridgeDiscoveryCallback() {
        override fun onFinished(results: List<BridgeDiscoveryResult>, returnCode: ReturnCode) {
            // Set to null to prevent stopBridgeDiscovery from stopping it
            runOnUiThread {
                when (returnCode) {
                    ReturnCode.SUCCESS -> {
                        // bridgeDiscoveryListView.setAdapter(BridgeDiscoveryResultAdapter(applicationContext, results))
                        bridgeDiscoveryResults = results
                        connectToBridge(results[0].ip)
                        updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size + " bridge(s) in the network.")
                    }
                    ReturnCode.STOPPED -> Log.i(TAG, "Bridge discovery stopped.")
                    else -> updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode)
                }
            }
        }
    }

    /**
     * Use the KnownBridges API to retrieve the last connected bridge
     * @return Ip address of the last connected bridge, or null
     */
    private fun getLastUsedBridgeIp(): String? {
        val bridges = KnownBridges.getAll()

        return if (bridges.isEmpty()) {
            null
        } else Collections.max(bridges) { a, b -> a.lastConnected.compareTo(b.lastConnected) }.ipAddress

    }

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    private fun connectToBridge(bridgeIp: String) {
        stopBridgeDiscovery()
        disconnectFromBridge()

        bridge = BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIp)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build()

        bridge?.connect()

        updateUI(UIState.Connecting, "Connecting to bridge...")
    }

    /**
     * Start the bridge discovery search
     * Read the documentation on meethue for an explanation of the bridge discovery options
     */
    private fun startBridgeDiscovery() {
        disconnectFromBridge()
        bridgeDiscovery = BridgeDiscovery()
        // ALL Include [UPNP, IPSCAN, NUPNP] but in some nets UPNP and NUPNP is not working properly
        bridgeDiscovery?.search(BridgeDiscovery.BridgeDiscoveryOption.ALL, bridgeDiscoveryCallback)
        updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...")
    }

    /**
     * Stops the bridge discovery if it is still running
     */
    private fun stopBridgeDiscovery() {
        bridgeDiscovery?.stop()
    }

    /**
     * Disconnect a bridge
     * The hue SDK supports multiple bridge connections at the same time,
     * but for the purposes of this demo we only connect to one bridge at a time.
     */
    private fun disconnectFromBridge() {
        bridge?.disconnect()
    }

    private fun updateUI(state: UIState, status: String) {
        runOnUiThread {
            Log.i(TAG, "Status: " + status)
            /*switch(state) {
                case Idle :
                bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                break;
                case BridgeDiscoveryRunning :
                bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                break;
                case BridgeDiscoveryResults :
                bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                break;
                case Connecting :
                bridgeIpTextView.setVisibility(View.VISIBLE);
                bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                break;
                case Pushlinking :
                bridgeIpTextView.setVisibility(View.VISIBLE);
                pushlinkImage.setVisibility(View.VISIBLE);
                bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                break;
                case Connected :
                bridgeIpTextView.setVisibility(View.VISIBLE);
                randomizeLightsButton.setVisibility(View.VISIBLE);
                bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                break;
            }*/
        }


    }

    companion object {
        val TAG: String = LampActivity::javaClass.name
    }

}
