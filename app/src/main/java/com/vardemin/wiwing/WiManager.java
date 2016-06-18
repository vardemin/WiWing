package com.vardemin.wiwing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by xavie on 19.05.2016.
 */
public class WiManager implements WifiP2pManager.ChannelListener {
    private static final String TAG = WiManager.class.getSimpleName();

    private Context ctx;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice localDevice;
    private WifiP2pGroup group;
    private WifiReceiver wifiReceiver;

    public List<WifiP2pDevice> devList;

    private boolean isSupported = true;
    private boolean isEnabled;
    private boolean isConnected;
    private boolean isConnecting;


    private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
    private DiscoveryListener discoveryListener;

    public WiManager(Context context) {
        ctx = context;
        manager = (WifiP2pManager) ctx.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(ctx, ctx.getMainLooper(), this);
        wifiReceiver = new WifiReceiver();
        devList = new ArrayList<>();
        discoveryListener = new DiscoveryListener();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        ctx.registerReceiver(wifiReceiver, filter);
    }
    private static WiManager wiManager;
    public static synchronized WiManager newInstance(Context context) {
        if (wiManager==null)
            wiManager = new WiManager(context.getApplicationContext());
        return wiManager;
    }
    public List<WifiP2pDevice> getDevList(){
        return devList;
    }

    @Override
    public void onChannelDisconnected() {
        Log.d(TAG," CHANNEL DISCONNECTED");
    }

    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                onStateChanged(intent);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                onConnectionChanged(intent);
            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                onDiscoveryChanged(intent);
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                onConnectionChanged(intent);
            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                onPeersChanged(intent);
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                onLocalDeviceChanged(intent);
            }
        }

        private void onLocalDeviceChanged(Intent intent) {
            Log.i(TAG, "Local Wifi p2p device has changed ");
            localDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if (localDevice.status == WifiP2pDevice.CONNECTED)
                Log.i(TAG, "Local device is connected");
            else
                Log.i(TAG, "Local device is not connected");
        }

        private void onPeersChanged(Intent intent) {
            manager.requestPeers(channel, discoveryListener);
        }

        private void onConnectionChanged(Intent intent) {
            final WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.i(TAG, "Wifi p2p connection changed " + netInfo);
            isConnected = netInfo.isConnected();
            Log.i(TAG, "Wifip2p connection is connected ? " + isConnected);
            if (isConnected) {
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {

                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.i(TAG, "Wifi p2p connection is formed ? " + info.groupFormed);
                        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {

                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup group) {
                                if (group == null)
                                    return;
                                WiManager.this.group = group;
                                Log.i(TAG, "Wifi p2p connection group is  " + group.getNetworkName());
                                Log.i(TAG, "Group size " + group.getClientList().size());
                                fireOnConnectionSucceed(group.getNetworkName(), group.getPassphrase(), p2pInfo);


                            }
                        });
                        if (isConnected && !info.isGroupOwner) {
                            // normally stopped already
                            //		stopDiscovery();
                        } else {
                            // TODO if is master relaunch a discovery later ?
                            startDiscovery();
                        }

                    }
                });

            } else {
                group = null;
                fireOnConnectionLost();
            }

        }

        private void onDiscoveryChanged(Intent intent) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                Log.i(TAG, "Wifi P2P discovery started");
            } else {
                Log.i(TAG, "Wifi P2P discovery stopped");
            }
        }

        private void onStateChanged(Intent intent) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.i(TAG, "WifiP2P is enabled");
                isEnabled = true;
                fireOnWifiP2PStatechanged(true);
            } else {
                Log.i(TAG, "WifiP2P is disabled. state " + state);
                isEnabled = false;
                isConnected = false;
                isConnecting = false;
                group = null;
                fireOnWifiP2PStatechanged(false);
            }

        }
    }
    public void startDiscovery() {
        manager.discoverPeers(channel,new WiwingActionListener("Discover Peers"));
    }
    public void connect(WifiP2pDevice device) {
        Log.i(TAG, "Wifi device " + device.deviceName + "[" + device.deviceAddress +  "] status is " + device.status );
        Log.i(TAG, "Wifi device " + device.deviceName + "[" + device.deviceAddress + "] service discovery capable " + device.isServiceDiscoveryCapable());
        if (device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.INVITED || device.status == WifiP2pDevice.FAILED) {
            Log.i(TAG, "Trying to connect to " + device.deviceName + "[" + device.deviceAddress +  "]");
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            if (!isConnecting) {
                manager.connect(channel, config, new WiwingActionListener("connect") {
                    @Override
                    public void onFailure(int reason) {
                        super.onFailure(reason);
                        isConnecting = false;
                    }

                    public void onSuccess() {
                        super.onSuccess();
                        isConnecting = false;
                    }
                });
                isConnecting = true;
            }
        }
    }
    public void disconnect() {
        if (isConnected)
            manager.removeGroup(channel, new WiwingActionListener("removeGroup"));
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isGroupOwner() {
        if (group == null)
            return false;
        return group.isGroupOwner();
    }



    private class DiscoveryListener implements WifiP2pManager.PeerListListener {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Log.i(TAG, "on peers available");
            Collection<WifiP2pDevice> devices = peers.getDeviceList();
            // try to connect to any available
            devList = new ArrayList<>(devices);
            fireOnDeviceListChanged();
        }


    }

    class WiwingActionListener implements WifiP2pManager.ActionListener {
        private final String tag;

        public WiwingActionListener(String tag) {
            this.tag = tag;
        }

        @Override
        public void onFailure(int reason) {
            Log.e(TAG, "Error during Wifi P2P operation. operation " + tag + " error " +  reason);
            if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                Log.e(TAG, "Wifi P2P not supported");
                isSupported = false;
                isEnabled = false;
            }
            else if (reason==WifiP2pManager.ERROR)
                Log.e(TAG, "ERROR");
            else if (reason==WifiP2pManager.BUSY)
                Log.e(TAG, "BUSY");

        }

        @Override
        public void onSuccess() {
            Log.d(TAG, "Wifi P2P operation " + tag + " success");
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void fireOnConnectionSucceed(String networkName, String passphrase, WifiP2pInfo info) {
        for (ConnectionListener l : listeners) {
            l.onConnectionSucceed(networkName, passphrase, info);
        }
    }

    private void fireOnConnectionLost() {
        for (ConnectionListener l : listeners) {
            l.onConnectionLost();
        }
    }

    private void fireOnWifiP2PStatechanged(boolean enabled) {
        for (ConnectionListener l : listeners) {
            l.onWifiP2PStateChanged(enabled);
        }
    }
    private void fireOnDeviceListChanged() {
        for (ConnectionListener l : listeners) {
            l.onDeviceListChanged(devList);
        }
    }

    interface ConnectionListener {
        void onWifiP2PStateChanged(boolean enabled);
        void onConnectionSucceed(String networkName, String passphrase, WifiP2pInfo info);
        void onConnectionLost();
        void onDeviceListChanged(List<WifiP2pDevice> deviceList);
    }



}
