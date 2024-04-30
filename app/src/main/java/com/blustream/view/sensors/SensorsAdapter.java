package com.blustream.view.sensors;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blustream.demo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.blustream.sulley.sensor.Sensor;
import io.blustream.sulley.sensor.SensorConnectionState;

/**
 * Created by Ruzhitskii Sviatoslav on 8/28/19.
 */
public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.ViewHolder> {
    // CSR OTAU Service
    public static final String ASOTAUBootServiceUUID             = "00001010-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUControlTransferCharacteristicUUID = "00001015-d102-11e1-9b23-00025b00a5a5";

    public static final String ASOTAUApplicationServiceUUID = "00001016-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUKeyCharacteristicUUID = "00001017-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUCurrentAppCharacteristicUUID = "00001013-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUKeyBlockCharacteristicUUID = "00001018-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUDataTransferCharacteristicUUID = "00001014-d102-11e1-9b23-00025b00a5a5";
    public static final String ASOTAUVersionCharacteristicUUID   = "00001011-d102-11e1-9b23-00025b00a5a5";

    private List<PinnableSensorWrapper> sensorList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private OnSensorClickListener onSensorClickListener;


    public SensorsAdapter(OnSensorClickListener onSensorClickListener) {
        this.onSensorClickListener = onSensorClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        context = recyclerView.getContext();
        inflater = LayoutInflater.from(context);
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = inflater.inflate(R.layout.sensor_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean pined = sensorList.get(position).isPinned();
        holder.pinTopSwitch.setOnCheckedChangeListener(null); //clear previous listener.
        holder.pinTopSwitch.setChecked(pined);
        holder.pinTopSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
                    sensorList.get(position).setPinned(b);
                    onSensorClickListener.onSensorCheckedChanged(sensorList.get(position));
                }
        );
        holder.sensorItemContainer.setOnClickListener(view -> onSensorClickListener.onSensorClick(sensorList.get(position).getSensor()));
        if (sensorList.get(position).getSensor().isWithinProximity()) {
            holder.sensorItemContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
        } else {
            holder.sensorItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
        holder.serialTextView.setText(context.getString(R.string.serial_number, sensorList.get(position).getSensor().getSerialNumber()));
        if (sensorList.get(position).getSensor().getVisibilityStatus() != null) {
            long lastSeen = (new Date().getTime() - sensorList.get(position).getSensor().getVisibilityStatus().getDate().getTime()) / 1000;
            holder.lastSeenTextView.setText(context.getString(R.string.last_seen, lastSeen));
        } else {
            holder.lastSeenTextView.setText(context.getString(R.string.last_seen, -1));
        }
        holder.rssiTextView.setText(context.getString(R.string.rssi, sensorList.get(position).getSensor().getAdvertisedRssi()));
        holder.bootModeTextView.setVisibility(sensorList.get(position).getSensor().getVisibilityStatus().isOTAUBootModeAdvertised()  ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public void setSensorList(List<PinnableSensorWrapper> sensorList) {
        this.sensorList = sensorList;
        notifyDataSetChanged();
    }

    public interface OnSensorClickListener {
        void onSensorClick(Sensor sensor);

        void onSensorCheckedChanged(PinnableSensorWrapper sensor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Switch pinTopSwitch;
        TextView serialTextView;
        TextView rssiTextView;
        TextView lastSeenTextView;
        TextView bootModeTextView;
        ConstraintLayout sensorItemContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sensorItemContainer = itemView.findViewById(R.id.sensorItemContainer);
            pinTopSwitch = itemView.findViewById(R.id.pinTopSwitch);
            serialTextView = itemView.findViewById(R.id.serialTextView);
            rssiTextView = itemView.findViewById(R.id.rssiTextView);
            lastSeenTextView = itemView.findViewById(R.id.lastSeenTextView);
            bootModeTextView = itemView.findViewById(R.id.tv_is_boot_mode);
        }
    }

}
