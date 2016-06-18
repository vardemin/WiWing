package com.vardemin.wiwing;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by xavie on 20.05.2016.
 */
public class DevListAdapter extends RecyclerView.Adapter<DevListAdapter.DevListViewHolder> {

    private List<WifiP2pDevice> deviceList;
    private WiManager manager;
    private WiMenu ctx;

    public static class DevListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView deviceName;
        TextView deviceStatus;
        public DevClickHolder devListener;

        public DevListViewHolder(View itemView, DevClickHolder devListener) {
            super(itemView);
            this.devListener = devListener;
            cv = (CardView)itemView.findViewById(R.id.devlist_cv);
            deviceName = (TextView)itemView.findViewById(R.id.device_name);
            deviceStatus = (TextView)itemView.findViewById(R.id.device_status);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            devListener.onDevClick(v, getAdapterPosition());
        }
        public static interface DevClickHolder {
            public void onDevClick (View caller, int position);
        }
    }

    public DevListAdapter(WiMenu ctx,List<WifiP2pDevice> deviceList, WiManager manager) {
        this.deviceList = deviceList;
        this.manager = manager;
        this.ctx = ctx;
    }

    @Override
    public DevListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.devlist_item, parent, false);
        DevListViewHolder devListViewHolder = new DevListViewHolder(v, new DevListViewHolder.DevClickHolder() {
            @Override
            public void onDevClick(View caller, int position) {
                String status = getDeviceStatus(deviceList.get(position).status);
                if (status.equals("Connected")) {
                    WiSync.getInstance().broadcastExit(ctx.getCurrentUser().getUuid());
                    manager.disconnect();
                }
                else if (status.equals("Invited"))
                    manager.disconnect();
                else manager.connect(deviceList.get(position));
            }
        });
        return devListViewHolder;
    }

    @Override
    public void onBindViewHolder(DevListViewHolder holder, int position) {
        holder.deviceName.setText(deviceList.get(position).deviceName);
        holder.deviceStatus.setText(getDeviceStatus(deviceList.get(position).status));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}
