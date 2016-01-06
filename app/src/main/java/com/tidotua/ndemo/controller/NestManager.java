package com.tidotua.ndemo.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nestapi.lib.API.AccessToken;
import com.nestapi.lib.API.Listener;
import com.nestapi.lib.API.NestAPI;
import com.nestapi.lib.API.SmokeCOAlarm;
import com.nestapi.lib.API.Structure;
import com.nestapi.lib.API.Thermostat;
import com.nestapi.lib.AuthManager;
import com.nestapi.lib.ClientMetadata;
import com.tidotua.ndemo.model.Constants;
import com.tidotua.ndemo.model.Settings;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by ti on 11.12.15.
 */
public class NestManager implements
        NestAPI.AuthenticationListener,
        Listener.StructureListener,
        Listener.ThermostatListener,
        Listener.SmokeCOAlarmListener {

    private final static String TAG = NestManager.class.getSimpleName();

    public interface AuthEventListener {
        void onLogin();
        void onLogout();
    };

    public interface StructureListener {
        void onStructureUpdate(Structure structure);
    };

    public interface ThermostatListener {
        void onThermostatUpdate(Thermostat thermostat);
    };

    public interface SmokeCOAlarmListener {
        void onSmokeCOAlarmUpdate(SmokeCOAlarm smokeCOAlarm);
    };

    private NestAPI mNestApi;
    private Listener mUpdateListener;
    private AccessToken mToken;
    private boolean mIsLogined = false;
    private WeakReference<Context> mContext;
    private AuthEventListener authEventListener;
    private StructureListener structureListener;
    private ThermostatListener thermostatListener;
    private SmokeCOAlarmListener smokeCOAlarmListener;

    private Map<String, Structure> structuresMap = new LinkedHashMap<String, Structure>();
    private Map<String, Thermostat> termostatsMap = new LinkedHashMap<String, Thermostat>();
    private Map<String, SmokeCOAlarm> smokeAlarmsMap = new LinkedHashMap<String, SmokeCOAlarm>();

    private static NestManager nestManager;

    public static NestManager get(Context context) {
        if (null == nestManager) {
            nestManager = new NestManager(context);
        }
        return nestManager;
    }

    private NestManager(Context context) {
        mContext = new WeakReference<Context>(context);
        mNestApi = NestAPI.getInstance();
        mUpdateListener = new Listener.Builder()
                .setStructureListener(this)
                .setThermostatListener(this)
                .setSmokeCOAlarmListener(this)
                .build();
        mToken = Settings.loadAuthToken(mContext.get());
        if (mToken != null) {
            login(mToken);
        }
    }

    private void reset() {
        removeListners();
        mIsLogined = false;
        mToken = null;
        Settings.resetAuthToken(mContext.get());
        structuresMap.clear();
        termostatsMap.clear();
        smokeAlarmsMap.clear();
    }

    public boolean isLogined() {
        return mIsLogined;
    }

    public boolean hasToken() {
        return null != mToken;
    }

    public void login(AccessToken token) {
        if (!mIsLogined) {
            Log.i(TAG, "Login start");
            NestAPI.getInstance().authenticate(token, this);
        }
    }

    public void setAuthEventListener(AuthEventListener authEventListener) {
        this.authEventListener = authEventListener;
    }

    public void setStructureListener(StructureListener structureListener) {
        this.structureListener = structureListener;
    }

    public void setSmokeCOAlarmListener(SmokeCOAlarmListener smokeCOAlarmListener) {
        this.smokeCOAlarmListener = smokeCOAlarmListener;
    }

    public void setThermostatListener(ThermostatListener thermostatListener) {
        this.thermostatListener = thermostatListener;
    }

    @Override
    public void onAuthenticationSuccess() {
        mIsLogined = true;
        if (null != authEventListener) {
            authEventListener.onLogin();
        }
        addListners();
        Log.i(TAG, "Login complete");
    }

    @Override
    public void onAuthenticationFailure(int errorCode) {
        mIsLogined = false;
        Log.e(TAG, "Login failed: " + errorCode);
    }

    public void logout() {
        if (mIsLogined) {
            Log.i(TAG, "Logout start");
            NestAPI.getInstance().unAuthenticate(new NestAPI.CompletionListener() {
                @Override
                public void onComplete() {
                    reset();
                    if (null != authEventListener) {
                        authEventListener.onLogout();
                    }
                    Log.i(TAG, "Logout complete");
                }

                @Override
                public void onError(int errorCode) {
                    Log.e(TAG, "Logout failed: " + errorCode);
                }
            });

        }
    }

    public void requestAccessToken(Activity activity, int requestCode) {
        final ClientMetadata metadata = new ClientMetadata.Builder()
                .setClientID(Constants.CLIENT_ID)
                .setClientSecret(Constants.CLIENT_SECRET)
                .setRedirectURL(Constants.REDIRECT_URL)
                .build();
        AuthManager.launchAuthFlow(activity, requestCode, metadata);
    }

    public void receiveAccessToken(Intent data) {
        if (AuthManager.hasAccessToken(data) && null != mContext.get()) {
            mToken = AuthManager.getAccessToken(data);
            Settings.saveAuthToken(mContext.get(), mToken);
            login(mToken);
        }
    }

    private void addListners(){
        mNestApi.addUpdateListener(mUpdateListener);
    }

    private void removeListners() {
        mNestApi.removeUpdateListener(mUpdateListener);
    }

    @Override
    public void onStructureUpdated(@NonNull Structure structure) {

        Log.i(TAG, "Structure: " + structure.toString()); // DEBUG

        if (structure.getStructureID().isEmpty()) {
            return;
        }
        structuresMap.put(structure.getStructureID(), structure);
        if (null != structureListener) {
            structureListener.onStructureUpdate(structure);
        }
    }

    @Override
    public void onThermostatUpdated(@NonNull Thermostat thermostat) {

        Log.i(TAG, "Thermostat: " + thermostat.toString()); // DEBUG

        if (thermostat.getDeviceID().isEmpty()) {
            return;
        }
        termostatsMap.put(thermostat.getDeviceID(), thermostat);
        if (null != thermostatListener) {
            thermostatListener.onThermostatUpdate(thermostat);
        }
    }

    @Override
    public void onSmokeCOAlarmUpdated(@NonNull SmokeCOAlarm smokeCOAlarm) {

        Log.i(TAG, "SmokeCOAlarm: " + smokeCOAlarm.toString()); // DEBUG

        if (smokeCOAlarm.getDeviceID().isEmpty()) {
            return;
        }
        smokeAlarmsMap.put(smokeCOAlarm.getDeviceID(), smokeCOAlarm);
        if (null != smokeCOAlarmListener) {
            smokeCOAlarmListener.onSmokeCOAlarmUpdate(smokeCOAlarm);
        }
    }

    public Collection<Structure> getStructures() {
        return structuresMap.values();
    }

    public void setStructureAwayState(String id, Structure.AwayState awayState) {
        mNestApi.setStructureAway(id, awayState, null);
    }

    public Structure getStructure(String id) {
        return structuresMap.get(id);
    }

    public Collection<Thermostat> getThermostats(String strustureId) {
        LinkedList<Thermostat> thermostats = new LinkedList<Thermostat>();
        for (Thermostat therm : termostatsMap.values()) {
            if (therm.getStructureID().equals(strustureId)) {
                thermostats.add(therm);
            }
        }
        return thermostats;
    }

    public Thermostat getThermostat(String id) {
        return termostatsMap.get(id);
    }

    public void setThermostatMode(String id, Thermostat.HVACMode mode) {
        mNestApi.setHVACMode(id, mode, null);
    }

    public void setThermostatTemperature(String id, int temperature) {
        mNestApi.setTargetTemperatureF(id, (long) temperature, null);
    }

    public void setThermostatTemperatureLow(String id, int temperature) {
        mNestApi.setTargetTemperatureLowF(id, (long) temperature, null);
    }

    public void setThermostatTemperatureHigh(String id, int temperature) {
        mNestApi.setTargetTemperatureHighF(id, (long) temperature, null);
    }

    public Collection<SmokeCOAlarm> getSmokeCOAlarms(String strustureId) {
        LinkedList<SmokeCOAlarm> smokes = new LinkedList<SmokeCOAlarm>();
        for (SmokeCOAlarm smoke : smokeAlarmsMap.values()) {
            if (smoke.getStructureID().equals(strustureId)) {
                smokes.add(smoke);
            }
        }
        return smokes;
    }

    public SmokeCOAlarm getSmokeCOAlarm(String id) {
        return smokeAlarmsMap.get(id);
    }

}
