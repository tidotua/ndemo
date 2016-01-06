package com.tidotua.ndemo.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.nestapi.lib.API.Structure;
import com.tidotua.ndemo.R;
import com.tidotua.ndemo.controller.NestManager;
import com.tidotua.ndemo.view.adapter.StructAdapter;

public class MainActivity extends Activity {

    private static final int AUTH_TOKEN_REQUEST_CODE = 101;

    private NestManager nestManager;
    private StructAdapter structAdapter;
    private Menu menu;
    private View loginButton;
    private ListView structuresList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == nestManager) {
            nestManager = NestManager.get(getApplicationContext());
        }
        structAdapter = new StructAdapter(this, onClickListener);
        showLogin(null);
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            return;
        }
        nestManager.receiveAccessToken(data);
    }

    @Override
    protected void onPause() {
        nestManager.setAuthEventListener(null);
        nestManager.setStructureListener(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nestManager.setAuthEventListener(authEventListener);
        nestManager.setStructureListener(structureListener);

        // manual update
        updateLoginState();
        structAdapter.setItems(nestManager.getStructures());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        updateLoginState();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutAction) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        structuresList = (ListView)findViewById(R.id.structureList);
        structuresList.setAdapter(structAdapter);
        loginButton = findViewById(R.id.loginButton);
    }

    private void updateLoginState() {
        boolean isLogined = nestManager.isLogined();
        loginButton.setVisibility(isLogined ? View.GONE : View.VISIBLE);
        structuresList.setVisibility(isLogined ? View.VISIBLE : View.GONE);
        if (null != menu) {
            menu.findItem(R.id.logoutAction).setVisible(isLogined);
        }
    }

    // AUTH DEBUG

    public void showLogin(View view) {
        if (!nestManager.hasToken()) {
            nestManager.requestAccessToken(this, AUTH_TOKEN_REQUEST_CODE);
        }
    }

    public void logout() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.logout_dialog_title)
            .setMessage(R.string.logout_dialog_text)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    nestManager.logout();
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    // CALLBACKS

    private NestManager.AuthEventListener authEventListener = new NestManager.AuthEventListener() {

        @Override
        public void onLogin() {
            updateLoginState();
        }

        @Override
        public void onLogout() {
            updateLoginState();
            structAdapter.reset();
        }
    };

    private NestManager.StructureListener structureListener = new NestManager.StructureListener() {

        @Override
        public void onStructureUpdate(Structure structure) {
            structAdapter.setItems(nestManager.getStructures());
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                String structureId = (String)v.getTag();
                Intent intent = new Intent(MainActivity.this, StructureActivity.class);
                intent.putExtra(StructureActivity.STRUCTURE_KEY, structureId);
                startActivity(intent);
            } catch (Exception e) {

            }
        }
    };
}
