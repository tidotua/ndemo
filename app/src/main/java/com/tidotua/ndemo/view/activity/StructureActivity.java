package com.tidotua.ndemo.view.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.nestapi.lib.API.SmokeCOAlarm;
import com.nestapi.lib.API.Structure;
import com.nestapi.lib.API.Thermostat;
import com.tidotua.ndemo.R;
import com.tidotua.ndemo.controller.NestManager;
import com.tidotua.ndemo.view.adapter.DeviceAdapter;

public class StructureActivity extends Activity {

    private final static String TAG = StructureActivity.class.getSimpleName();
    public final static String STRUCTURE_KEY = "home_structure_id";

    private String structureId;
    private ActionBar actionBar;
    private NestManager nestManager;
    private Switch awaySwitch;
    private DeviceAdapter deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure);
        actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (null == nestManager) {
            nestManager = NestManager.get(getApplicationContext());
        }

        if (null != savedInstanceState) {
            structureId = savedInstanceState.getString(STRUCTURE_KEY);
        } else {
            structureId = this.getIntent().getStringExtra(STRUCTURE_KEY);
        }
        deviceAdapter = new DeviceAdapter(this, onClickListener);
        initViews();
    }

    @Override
    protected void onPause() {
        nestManager.setStructureListener(null);
        nestManager.setThermostatListener(null);
        nestManager.setSmokeCOAlarmListener(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nestManager.setStructureListener(structureListener);
        nestManager.setThermostatListener(thermostatListener);
        nestManager.setSmokeCOAlarmListener(smokeCOAlarmListener);

        updateHome();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STRUCTURE_KEY, structureId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        awaySwitch = (Switch)findViewById(R.id.awaySwitch);
        awaySwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        ListView devicesList = (ListView)findViewById(R.id.devicesList);
        devicesList.setAdapter(deviceAdapter);
        updateHome();
    }

    private void updateHome() {
        Structure structure = nestManager.getStructure(structureId);
        if (null == structure) {
            return;
        }
        updateStructureView(structure);
        updateDevices();
    }

    private void updateDevices() {
        deviceAdapter.reset();
        deviceAdapter.setThermItems(nestManager.getThermostats(structureId));
        deviceAdapter.setSmokeItems(nestManager.getSmokeCOAlarms(structureId));
    }

    private void updateStructureView(Structure structure) {
        if (null != actionBar) {
            actionBar.setTitle(structure.getName());
        }

        awaySwitch.setChecked(structure.getAwayState() == Structure.AwayState.HOME);
        deviceAdapter.setIsAway(structure.getAwayState() != Structure.AwayState.HOME);
    }

    // CALLBACKS

    private NestManager.StructureListener structureListener = new NestManager.StructureListener() {

        @Override
        public void onStructureUpdate(Structure structure) {
            if (structure.getStructureID().equals(structureId)) {
                updateStructureView(structure);
            }
        }
    };

    private NestManager.ThermostatListener thermostatListener = new NestManager.ThermostatListener() {

        @Override
        public void onThermostatUpdate(Thermostat thermostat) {
            deviceAdapter.setThermItems(nestManager.getThermostats(structureId));
        }
    };

    private NestManager.SmokeCOAlarmListener smokeCOAlarmListener = new NestManager.SmokeCOAlarmListener() {

        @Override
        public void onSmokeCOAlarmUpdate(SmokeCOAlarm smokeCOAlarm) {
            deviceAdapter.setSmokeItems(nestManager.getSmokeCOAlarms(structureId));
        }
    };

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    nestManager.setStructureAwayState(structureId,
                            isChecked ? Structure.AwayState.HOME : Structure.AwayState.AWAY);
                }
            };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                if (deviceAdapter.isAway()) {
                    return;
                }
                String deviceId = (String)v.getTag();
                if (null != nestManager.getThermostat(deviceId)) {
                    Intent intent = new Intent(StructureActivity.this, ThermostatActivity.class);
                    intent.putExtra(ThermostatActivity.THERMOSTAT_KEY, deviceId);
                    startActivity(intent);
                }
            } catch (Exception e) {

            }
        }
    };
}
