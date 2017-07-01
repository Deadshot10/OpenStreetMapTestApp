package ru.volodya.apps.osmapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;

public class ObjectsActivity extends Activity {

    private static final String GAS = "gas";
    private static final String CAMP = "camp";
    private static final String TECH = "tech";
    private static final String WEIGHT = "weight";
    private static final String PARTS = "parts";
    private static final String ATM = "atm";
    private CheckBox cbGas, cbCamp, cbTech, cbWeight, cbParts, cbAtm;
    private ImageButton backButton;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objects);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        cbGas = (CheckBox) findViewById(R.id.checkBoxGas);
        cbCamp = (CheckBox) findViewById(R.id.checkBoxCamp);
        cbTech = (CheckBox) findViewById(R.id.checkBoxTech);
        cbWeight = (CheckBox) findViewById(R.id.checkBoxWeight);
        cbParts = (CheckBox) findViewById(R.id.checkBoxParts);
        cbAtm = (CheckBox) findViewById(R.id.checkBoxAtm);

        backButton = (ImageButton) findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(new AlphaAnimation(1F, 0.5F));
                onBackPressed();
            }
        });

        stateLoad();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stateSave();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stateSave();

    }

    private void stateSave(){
        preferences.edit().putBoolean(GAS, cbGas.isChecked()).apply();
        preferences.edit().putBoolean(CAMP, cbCamp.isChecked()).apply();
        preferences.edit().putBoolean(TECH, cbTech.isChecked()).apply();
        preferences.edit().putBoolean(WEIGHT, cbWeight.isChecked()).apply();
        preferences.edit().putBoolean(PARTS, cbParts.isChecked()).apply();
        preferences.edit().putBoolean(ATM, cbAtm.isChecked()).apply();

    }

    private void stateLoad(){
        cbGas.setChecked(preferences.getBoolean(GAS, false));
        cbCamp.setChecked(preferences.getBoolean(CAMP, false));
        cbTech.setChecked(preferences.getBoolean(TECH, false));
        cbWeight.setChecked(preferences.getBoolean(WEIGHT, false));
        cbParts.setChecked(preferences.getBoolean(PARTS, false));
        cbAtm.setChecked(preferences.getBoolean(ATM, false));
    }
}
