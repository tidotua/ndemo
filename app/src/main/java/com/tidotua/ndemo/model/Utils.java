package com.tidotua.ndemo.model;

import android.content.Context;

import com.nestapi.lib.API.Thermostat;
import com.tidotua.ndemo.R;

/**
 * Created by ti on 17.12.15.
 */
public class Utils {

    public static String getThermostatState(Thermostat thermostat, Context context, boolean isAway) {
        String modeText = "";
        String temperatureText = "";

        switch (thermostat.getHVACState()) {
            case HEATING:
                modeText = context.getString(R.string.heating);
                break;

            case COOLING:
                modeText = context.getString(R.string.cooling);
                break;
        }

        switch (thermostat.getHVACmode()) {
            case HEAT:
                if (modeText.isEmpty()) {
                    modeText = context.getString(R.string.heat_to);
                }
                temperatureText = Long.toString(thermostat.getTargetTemperatureF());
                break;

            case COOL:
                if (modeText.isEmpty()) {
                    modeText = context.getString(R.string.cool_to);
                }
                temperatureText = Long.toString(thermostat.getTargetTemperatureF());
                break;

            case HEAT_AND_COOL:
                if (modeText.isEmpty()) {
                    modeText = context.getString(R.string.heat_cool);
                }
                temperatureText = Long.toString(thermostat.getTargetTemperatureLowF())
                        + " " + context.getString(R.string.bullet) + " "
                        + Long.toString(thermostat.getTargetTemperatureHighF());
                break;

            default:
                modeText = context.getString(R.string.off);
        }

        if (isAway) {
            modeText = context.getString(R.string.away);
            temperatureText = "";
        }

        return modeText + "   " + temperatureText;
    }

    public static int getThermostatColor(Thermostat thermostat, Context context) {
        int resource = R.color.thermostat_off;
        switch (thermostat.getHVACState()) {
            case HEATING:
                resource = R.color.thermostat_heat;
                break;

            case COOLING:
                resource = R.color.thermostat_cool;
                break;
        }
        return context.getResources().getColor(resource);
    }
}
