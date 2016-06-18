package com.vardemin.wiwing;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevListFragment extends Fragment implements View.OnClickListener, WiManager.ConnectionListener{

    private Context ctx;
    private WiManager manager;



    protected RecyclerView rv;
    protected RecyclerView.LayoutManager layoutManager;
    protected DevListAdapter devListAdapter;

    protected List<WifiP2pDevice> deviceList;

    public DevListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_devlist, container, false);
        ctx = getActivity();
        deviceList = new ArrayList<>();

        rv=(RecyclerView)rootview.findViewById(R.id.devlist_recycler_view);

        layoutManager = new LinearLayoutManager(ctx);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        manager = WiManager.newInstance(ctx);
        manager.addConnectionListener(this);
        deviceList.addAll(manager.getDevList());
        devListAdapter = new DevListAdapter((WiMenu)getActivity(),deviceList, manager);
        rv.setAdapter(devListAdapter);

        Button discoverBtn = (Button) rootview.findViewById(R.id.devlist_button);
        discoverBtn.setOnClickListener(this);
        return rootview;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.devlist_button:
                manager.startDiscovery();
        }
    }

    @Override
    public void onWifiP2PStateChanged(boolean enabled) {
        String state = enabled ? " enabled ": " disabled ";
        Toast.makeText(ctx,"WiFi Direct "+ state,Toast.LENGTH_SHORT).show();
    }

    private WiServer server;
    private WiClient wiClient;
    @Override
    public void onConnectionSucceed(String networkName, String passphrase, WifiP2pInfo info) {
        Toast.makeText(ctx,"Connected to " + networkName + " pass: "+passphrase,Toast.LENGTH_SHORT).show();
        if (info.groupFormed && info.isGroupOwner) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server = WiServer.getServer();
                    if (server.isAvailable()) {
                        server.setCtx((WiMenu)getActivity());
                        server.setUsersListener((WiMenu) getActivity());
                        server.setMessagesListener((WiMenu) getActivity());
                        server.setNewsListener((WiMenu) getActivity());
                        server.setDialogListener((WiMenu) getActivity());
                        ((WiMenu) getActivity()).addMenuActivityListener(server);
                        new Thread(server).start();
                    }
                }
            });

        }
        else if (info.groupFormed) {
            wiClient = new WiClient((WiMenu)getActivity(),info.groupOwnerAddress.getHostAddress());
            wiClient.setUsersListener((WiMenu) getActivity());
            wiClient.setCurrentUser(((WiMenu) getActivity()).getCurrentUser());
            wiClient.setDialogListener((WiMenu) getActivity());
            wiClient.setNewsListener((WiMenu) getActivity());
            wiClient.setMessagesListener((WiMenu) getActivity());
            ((WiMenu) getActivity()).addMenuActivityListener(wiClient);
            new Thread(wiClient).start();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    server = WiServer.getServer();
                    if (server.isAvailable()) {
                        server.setCtx((WiMenu) getActivity());
                        server.setPort(7715);
                        server.setUsersListener((WiMenu) getActivity());
                        server.setMessagesListener((WiMenu) getActivity());
                        server.setNewsListener((WiMenu) getActivity());
                        server.setDialogListener((WiMenu) getActivity());
                        ((WiMenu) getActivity()).addMenuActivityListener(server);
                        new Thread(server).start();
                    }
                }
            });


        }
    }

    @Override
    public void onConnectionLost() {
        WiSync.getInstance().clearAll();
        ((WiMenu)getActivity()).NotifyLostConnection();
        Toast.makeText(ctx,"Connectuin Lost",Toast.LENGTH_SHORT).show();
        if(server!=null)
            server.onMenuActivityStop();
        if(wiClient!=null)
                wiClient.onMenuActivityStop();
        if (getActivity()!=null)
        if(((WiMenu)getActivity()).getClients().size()>0)
        {
            for (WiClient cl: ((WiMenu)getActivity()).getClients())
            {
                cl.onMenuActivityStop();
            }
        }
    }

    @Override
    public void onDeviceListChanged(List<WifiP2pDevice> devList) {
        deviceList.clear();
        deviceList.addAll(devList);
        /*for (WifiP2pDevice device: deviceList)
        {
            if (device.status != WifiP2pDevice.CONNECTED)
                deviceList.remove(device);
        }
        for (WifiP2pDevice dev: devList)
        {

            for (WifiP2pDevice localdev: deviceList) {
                if (dev.deviceAddress.equals(localdev.deviceAddress)) {
                    if (dev.status != localdev.status) {
                        deviceList.remove(localdev);
                        deviceList.add(dev);
                    }
                    break;
                } else if (!deviceList.contains(dev)) { devList.add(dev); break;}
            }
        }*/
        if (devList.size()>0) {
            Toast.makeText(ctx,"Got devices list",Toast.LENGTH_SHORT).show();
        }
        devListAdapter.notifyDataSetChanged();
    }

}
