package com.tidotua.ndemo.view.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nestapi.lib.API.Structure;
import com.nestapi.lib.API.Thermostat;
import com.tidotua.ndemo.R;
import com.tidotua.ndemo.controller.NestManager;
import com.tidotua.ndemo.model.Utils;
import com.tidotua.ndemo.view.adapter.SpinnerAdapter;
import com.tidotua.ndemo.view.control.ArcSlider;
import com.tidotua.ndemo.view.control.IntervalArcSlider;

public class ThermostatActivity extends Activity {

    private final static String TAG = StructureActivity.class.getSimpleName();
    public final static String THERMOSTAT_KEY = "thermostat_id";

    private static int[] MODE_NAMES = {
        R.string.heat,
        R.string.cool,
        R.string.heat_cool,
        R.string.off
    };

    private static int[] MODE_IMAGES = {
        R.mipmap.ic_heat,
        R.mipmap.ic_cool,
        R.mipmap.ic_heat_cool,
        R.mipmap.ic_off
    };

    private static Thermostat.HVACMode[] MODES = {
            Thermostat.HVACMode.HEAT,
            Thermostat.HVACMode.COOL,
            Thermostat.HVACMode.HEAT_AND_COOL,
            Thermostat.HVACMode.OFF
    };

    private String thermostatId;
    private ActionBar actionBar;
    private NestManager nestManager;
    private Spinner modeSpinner;
    private ArcSlider thermostatSlider;
    private IntervalArcSlider thermostatIntervalSlider;
    private boolean isAway = false;
    private TextView currentTextView;
    private ImageView nestLeafImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermostat);

        actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (null == nestManager) {
            nestManager = NestManager.get(getApplicationContext());
        }

        if (null != savedInstanceState) {
            thermostatId = savedInstanceState.getString(THERMOSTAT_KEY);
        } else {
            thermostatId = this.getIntent().getStringExtra(THERMOSTAT_KEY);
        }
        initViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(THERMOSTAT_KEY, thermostatId);
    }

    @Override
    protected void onPause() {
        nestManager.setThermostatListener(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nestManager.setThermostatListener(thermostatListener);

        updateViews();
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
        modeSpinner = (Spinner) findViewById(R.id.modeSpinner);
        SpinnerAdapter adapter = new SpinnerAdapter(this,
                SpinnerAdapter.initSpinnerList(this, MODE_NAMES, MODE_IMAGES),
                R.layout.mode_spinner_row,
                new String[] { SpinnerAdapter.IMAGE_FIELD, SpinnerAdapter.TITLE_FIELD },
                new int[] { R.id.modeImageView, R.id.modeTitleTextView });
        modeSpinner.setAdapter(adapter);
        modeSpinner.setOnItemSelectedListener(spinnerListener);

        thermostatSlider = (ArcSlider) findViewById(R.id.thermostatSlider);
        thermostatSlider.setMin(50);
        thermostatSlider.setMax(90);
        thermostatSlider.setArcSliderListener(arcSliderListener);
        thermostatIntervalSlider = (IntervalArcSlider) findViewById(R.id.thermostatIntervalSlider);
        thermostatIntervalSlider.setMin(50);
        thermostatIntervalSlider.setMax(90);
        thermostatIntervalSlider.setMinInterval(3);
        thermostatIntervalSlider.setArcSliderListener(intervalArcSliderListener);

        currentTextView = (TextView) findViewById(R.id.currentTextView);
        nestLeafImageView = (ImageView) findViewById(R.id.nestLeafImageView);

        updateViews();
    }

    private void updateViews() {
        Thermostat thermostat = nestManager.getThermostat(thermostatId);
        if (null == thermostat) {
            return;
        }
        updateThermostatViews(thermostat);
    }

    private void updateThermostatViews(Thermostat thermostat) {
        if (null != actionBar) {
            actionBar.setTitle(thermostat.getName() + " " + getString(R.string.thermostat));
        }
        try {
            isAway = nestManager.getStructure(thermostat.getStructureID()).getAwayState()
                    != Structure.AwayState.HOME;
        } catch (Exception e) {

        }
        updateMode(thermostat.getHVACmode());
        updateSlider(thermostat);

        currentTextView.setText(Long.toString(thermostat.getAmbientTemperatureF()));
        nestLeafImageView.setVisibility(thermostat.hasLeaf() ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateMode(Thermostat.HVACMode mode) {
        for (int position = 0; position < MODES.length; ++position) {
            if (MODES[position].equals(mode)) {
                modeSpinner.setSelection(position);
            }
        }
    }

    private void setMode(int position) {
        nestManager.setThermostatMode(thermostatId, MODES[position]);
    }

    private void updateSlider(Thermostat thermostat) {
        String text = Utils.getThermostatState(thermostat, this, isAway);
        int color = Utils.getThermostatColor(thermostat, this);

        switch (thermostat.getHVACmode()) {
            case HEAT:
            case COOL:
                thermostatIntervalSlider.setVisibility(View.GONE);
                thermostatSlider.setVisibility(View.VISIBLE);
                thermostatSlider.setProgress((int) thermostat.getTargetTemperatureF());
                thermostatSlider.setText(text);
                thermostatSlider.setColor(color);
                thermostatSlider.setCurrentMark((int) thermostat.getAmbientTemperatureF());
                break;

            case HEAT_AND_COOL:
                thermostatSlider.setVisibility(View.GONE);
                thermostatIntervalSlider.setVisibility(View.VISIBLE);
                thermostatIntervalSlider.setStartProgress((int) thermostat.getTargetTemperatureLowF());
                thermostatIntervalSlider.setProgress((int) thermostat.getTargetTemperatureHighF());
                thermostatIntervalSlider.setText(text);
                thermostatIntervalSlider.setColor(color);
                thermostatIntervalSlider.setCurrentMark((int) thermostat.getAmbientTemperatureF());
                break;

            case OFF:
                thermostatSlider.setVisibility(View.GONE);
                thermostatIntervalSlider.setVisibility(View.GONE);
        }
    }

    // CALLBACKS

    private NestManager.ThermostatListener thermostatListener = new NestManager.ThermostatListener() {

        @Override
        public void onThermostatUpdate(Thermostat thermostat) {
            if (thermostat.getDeviceID().equals(thermostatId)) {
                updateThermostatViews(thermostat);
            }
        }
    };

    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setMode(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    ArcSlider.ArcSliderListener arcSliderListener = new ArcSlider.ArcSliderListener() {
        @Override
        public void onProgressChange(int progress) {
            nestManager.setThermostatTemperature(thermostatId, progress);
        }
    };

    IntervalArcSlider.IntervalArcSliderListener intervalArcSliderListener =
            new IntervalArcSlider.IntervalArcSliderListener() {
        @Override
        public void onProgressChange(int progress) {
            nestManager.setThermostatTemperatureHigh(thermostatId, progress);
        }

        @Override
        public void onStartProgressChange(int startProgress) {
            nestManager.setThermostatTemperatureLow(thermostatId, startProgress);
        }
    };
}
