package com.tidotua.ndemo.view.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nestapi.lib.API.SmokeCOAlarm;
import com.nestapi.lib.API.Thermostat;
import com.tidotua.ndemo.R;
import com.tidotua.ndemo.model.Utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ti on 13.12.15.
 */
public class DeviceAdapter extends BaseAdapter {

    private final static String EMERGENCY = "emergency";
    private final static String WARNING = "warning";
    private final static String REPLACE = "replace";

    private Context mContext;
    private ArrayList<Thermostat> mThermItems = new ArrayList<Thermostat>();
    private ArrayList<SmokeCOAlarm> mSmokeItems = new ArrayList<SmokeCOAlarm>();
    private boolean isAway = false;

    PorterDuffColorFilter heatingColorFilter;
    PorterDuffColorFilter coolingColorFilter;
    PorterDuffColorFilter offColorFilter;

    PorterDuffColorFilter emergencyColorFilter;
    PorterDuffColorFilter warningColorFilter;
    PorterDuffColorFilter okColorFilter;

    private View.OnClickListener onClickListner;

    public DeviceAdapter(Context context, View.OnClickListener onClickListner) {
        mContext = context;
        this.onClickListner = onClickListner;

        heatingColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.thermostat_heat),
                PorterDuff.Mode.SRC_IN);
        coolingColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.thermostat_cool),
                PorterDuff.Mode.SRC_IN);
        offColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.thermostat_off),
                PorterDuff.Mode.SRC_IN);

        emergencyColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.safety_emergency),
                PorterDuff.Mode.SRC_IN);
        warningColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.safety_warning),
                PorterDuff.Mode.SRC_IN);
        okColorFilter = new PorterDuffColorFilter(
                context.getResources().getColor(R.color.safety_ok),
                PorterDuff.Mode.SRC_IN);
    }

    public void reset() {
        mThermItems.clear();
        mSmokeItems.clear();
        notifyDataSetChanged();
    }

    public void setThermItems(Collection<Thermostat> items) {
        mThermItems.clear();
        mThermItems.addAll(items);
        notifyDataSetChanged();
    }

    public void setSmokeItems(Collection<SmokeCOAlarm> items) {
        mSmokeItems.clear();
        mSmokeItems.addAll(items);
        notifyDataSetChanged();
    }

    public void setIsAway(boolean isAway) {
        this.isAway = isAway;
    }

    public boolean isAway() {
        return isAway;
    }

    private int getSmokeGroupPosition() {
        return mThermItems.size() > 0 ? mThermItems.size() + 1 : 0;
    }

    private boolean isThermostat(int position) {
        return mThermItems.size() > 0 && position > 0 && position <= mThermItems.size();
    }

    private boolean isSmokeCOAlarm(int position) {
        int smokePosition = position - getSmokeGroupPosition();
        return mSmokeItems.size() > 0 && smokePosition > 0 && smokePosition <= mSmokeItems.size();
    }

    private boolean isGroup(int position) {
        return (mThermItems.size() > 0 && position == 0)
                || (mSmokeItems.size() > 0 && getSmokeGroupPosition() == position);
    }

    @Override
    public int getCount() {
        int count = mThermItems.size() + mSmokeItems.size();
        count += mThermItems.size() > 0 ? 1 : 0;
        count += mSmokeItems.size() > 0 ? 1 : 0;
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (isGroup(position)) {
            return "";
        }

        if (isThermostat(position)) {
            return mThermItems.get(position - 1);
        }

        if (isSmokeCOAlarm(position)) {
            return mSmokeItems.get(position - getSmokeGroupPosition() - 1);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup itemView = (ViewGroup)convertView;

        boolean isGroup = isGroup(position);
        if (null != itemView
                && (isGroup && itemView.getId() != R.id.deviceGroup
                || !isGroup && itemView.getId() != R.id.deviceItem)) {
            itemView = null;
        }

        if (isGroup) {
            if (null == itemView) {
                LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (ViewGroup)layoutInflater.inflate(R.layout.device_group_item, null);
            }
            setGroupContentToView(itemView, position);
        } else {
            if (null == itemView) {
                LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (ViewGroup)layoutInflater.inflate(R.layout.device_item, null);
            }

            if (isThermostat(position)) {
                setThermContentToView(itemView, position - 1);
            }

            if (isSmokeCOAlarm(position)) {
                setSmokeContentToView(itemView, position - getSmokeGroupPosition() - 1);
            }
        }

        return itemView;
    }

    private void setGroupContentToView(ViewGroup itemViewGroup, int position)
    {
        TextView titleText = (TextView)itemViewGroup.findViewById(R.id.deviceTitleTextView);
        itemViewGroup.setEnabled(false);
        if (position == getSmokeGroupPosition()) {
            titleText.setText(R.string.protects);
        } else {
            titleText.setText(R.string.thermostats);
        }
    }

    private void setThermContentToView(ViewGroup itemViewGroup, int position)
    {
        itemViewGroup.setOnClickListener(onClickListner);
        itemViewGroup.setTag(mThermItems.get(position).getDeviceID());
        itemViewGroup.setBackgroundResource(R.drawable.item_background);
        TextView titleText = (TextView)itemViewGroup.findViewById(R.id.deviceTitleTextView);
        TextView descText = (TextView)itemViewGroup.findViewById(R.id.deviceDescTextView);
        ImageView deviceImage = (ImageView)itemViewGroup.findViewById(R.id.deviceImageView);

        titleText.setText("");
        descText.setText("");

        if (null != mThermItems && !mThermItems.isEmpty()) {
            Thermostat thermostat = mThermItems.get(position);
            if (null != thermostat) {
                titleText.setText(thermostat.getName());
                deviceImage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_thermo));

                switch (thermostat.getHVACState()) {
                    case HEATING:
                        deviceImage.setColorFilter(heatingColorFilter);
                        break;

                    case COOLING:
                        deviceImage.setColorFilter(coolingColorFilter);
                        break;

                    default:
                        deviceImage.setColorFilter(offColorFilter);
                }

                descText.setText(Utils.getThermostatState(thermostat, mContext, isAway));
            }
        }
    }

    private void setSmokeContentToView(ViewGroup itemViewGroup, int position) {
        itemViewGroup.setEnabled(false);
        TextView titleText = (TextView)itemViewGroup.findViewById(R.id.deviceTitleTextView);
        TextView descText = (TextView)itemViewGroup.findViewById(R.id.deviceDescTextView);
        ImageView deviceImage = (ImageView)itemViewGroup.findViewById(R.id.deviceImageView);

        titleText.setText("");
        descText.setText("");

        if (null != mSmokeItems && !mSmokeItems.isEmpty()) {
            SmokeCOAlarm smokeCOAlarm = mSmokeItems.get(position);
            if (null != smokeCOAlarm) {
                titleText.setText(smokeCOAlarm.getName());
                deviceImage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_smoke));
                String stateSource = mContext.getString(R.string.ok);
                String stateText = "";

                if (smokeCOAlarm.getCOAlarmState().equals(EMERGENCY)) {
                    deviceImage.setColorFilter(emergencyColorFilter);
                    stateSource = mContext.getString(R.string.co);
                    stateText = mContext.getString(R.string.emergency);
                } else if (smokeCOAlarm.getSmokeAlarmState().equals(EMERGENCY)) {
                    deviceImage.setColorFilter(emergencyColorFilter);
                    stateSource = mContext.getString(R.string.smoke);
                    stateText = mContext.getString(R.string.emergency);
                } else if (smokeCOAlarm.getCOAlarmState().equals(WARNING)) {
                    deviceImage.setColorFilter(warningColorFilter);
                    stateSource = mContext.getString(R.string.co);
                    stateText = mContext.getString(R.string.warning);
                } else if (smokeCOAlarm.getSmokeAlarmState().equals(WARNING)) {
                    deviceImage.setColorFilter(warningColorFilter);
                    stateSource = mContext.getString(R.string.smoke);
                    stateText = mContext.getString(R.string.warning);
                } else if (smokeCOAlarm.getBatteryHealth().equals(REPLACE)) {
                    deviceImage.setColorFilter(warningColorFilter);
                    stateSource = mContext.getString(R.string.battery);
                } else {
                    deviceImage.setColorFilter(okColorFilter);
                }

                descText.setText(stateSource + " " + stateText);
            }
        }
    }
}
