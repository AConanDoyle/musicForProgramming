package magnesia.mapapps.de.musicforprogramming;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_LANGUAGE = "language";
    public String languagePref_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals(KEY_PREF_LANGUAGE)) {
                            setLocale(languagePref_ID = prefs.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en-rGB"));
                        }
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        addPreferencesFromResource(R.xml.preferences);
        getListView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setupActionBar();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_LANGUAGE)) {
            setLocale(languagePref_ID = sharedPreferences.getString(SettingsActivity.KEY_PREF_LANGUAGE, "en-rGB"));
        }
    }

    public void setLocale(String languagePref_ID) {

        Locale locale = null;
        switch (languagePref_ID) {
            case "en-rGB":
                locale = new Locale("en-rGB");
                break;
            case "de":
                locale = new Locale("de");
                break;
        }

        Locale.setDefault(locale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
