package ch.fhnw.ip6.powerconsumptionmanager.view;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import ch.fhnw.ip6.powerconsumptionmanager.R;
import ch.fhnw.ip6.powerconsumptionmanager.activity.MainActivity;
import ch.fhnw.ip6.powerconsumptionmanager.network.DataLoader;
import ch.fhnw.ip6.powerconsumptionmanager.network.DataLoaderCallback;
import ch.fhnw.ip6.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;

/**
 * Holds the different settings in a preference fragment and displays them accordingly.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, DataLoaderCallback {
    private static final String TAG = "SettingsFragment";

    private PowerConsumptionManagerAppContext mAppContext;
    private SettingsFragment mContext;

    private SharedPreferences mSettings;

    private SeekBar mUpdateInterval;
    private TextView mUpdateIntervalLabel;

    private NumberPicker mCostStatisticsPeriod;

    private EditText mIP1;
    private EditText mIP2;
    private EditText mIP3;
    private EditText mIP4;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mAppContext = (PowerConsumptionManagerAppContext) getContext().getApplicationContext();
        mContext = this;
        mSettings = getPreferenceScreen().getSharedPreferences();

        // Setup google calendar checkbox preference
        final CheckBoxPreference googleCalendar = (CheckBoxPreference) findPreference("googleCalendar");
        setupGoogleCalendarPreference(googleCalendar);

        // Setup update automatically checkbox preference
        final CheckBoxPreference updateAutomatically = (CheckBoxPreference) findPreference("updateAutomatically");
        setupUpdateAutomaticallyPreference(updateAutomatically);

        // Setup update interval preference
        final Preference updateIntervalDialog = findPreference("updateInterval");
        setupUpdateIntervalDialog(updateIntervalDialog);

        // Setup cost statistics period preference
        final Preference costStatisticsPeriod = findPreference("costStatisticsPeriod");
        setupCostStatisticsPeriodDialog(costStatisticsPeriod);

        // Setup sync preference
        final Preference syncPreference = findPreference("syncPlan");
        setupSyncProcessPreference(syncPreference);

        // Setup ip dialog preference
        final Preference ipDialog = findPreference("IP");
        setupIPDialog(ipDialog);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update summaries of preferences
        switch (key) {
            case "updateAutomatically":
                findPreference("updateInterval").setEnabled(mSettings.getBoolean("updateAutomatically", false));
                break;
            case "updateInterval":
                findPreference(key).setSummary(
                    getString(R.string.text_pref_update_interval_summary) +
                    " " +
                    mSettings.getInt("updateInterval", 10) +
                    " seconds"
                );
                break;
            case "costStatisticsPeriod":
                findPreference(key).setSummary(
                    getString(R.string.text_pref_cost_statistics_period_summary) +
                    " " +
                    mSettings.getInt("costStatisticsPeriod", 10) +
                    " days"
                );
                break;
            case "IP":
                findPreference(key).setSummary(mSettings.getString("IP", ""));
                break;
            default:
                // Do nothing
                break;
        }
    }

    /**** Return point from the sync request ****/
    @Override
    public void DataLoaderDidFinish() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), getString(R.string.toast_sync_ended_success), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void DataLoaderDidFail() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), getString(R.string.toast_sync_ended_error_loading), Toast.LENGTH_LONG).show();
            }
        });
    }
    /********/

    private void setupGoogleCalendarPreference(CheckBoxPreference googleCalendarPreference) {
        googleCalendarPreference.setChecked(mSettings.getBoolean("googleCalendar", false));
        googleCalendarPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if(value instanceof Boolean) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean("googleCalendar", (Boolean) value);
                    editor.apply();
                    mAppContext.setGoogleCalendar((Boolean) value);
                }

                return true;
            }
        });
    }

    private void setupUpdateAutomaticallyPreference(CheckBoxPreference updateAutomaticallyPreference) {
        updateAutomaticallyPreference.setChecked(mSettings.getBoolean("updateAutomatically", false));
        updateAutomaticallyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if(value instanceof Boolean) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putBoolean("updateAutomatically", (Boolean) value);
                    editor.apply();
                    mAppContext.setUpdatingAutomatically((Boolean) value);
                }

                return true;
            }
        });
    }

    private void setupUpdateIntervalDialog(Preference updateIntervalDialog) {
        updateIntervalDialog.setSummary(
            getString(R.string.text_pref_update_interval_summary) +
            " " +
            mSettings.getInt("updateInterval", 10) +
            " seconds"
        );
        updateIntervalDialog.setEnabled(mSettings.getBoolean("updateAutomatically", true));

        updateIntervalDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogStyle);
                builder.setTitle(getString(R.string.text_pref_update_interval_title));

                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View updateIntervalDialogView = layoutInflater.inflate(R.layout.dialog_update_interval_settings, null);
                builder.setView(updateIntervalDialogView);

                // Get the previously stored update interval
                int updateInterval = mSettings.getInt("updateInterval", 10);

                mUpdateInterval = (SeekBar) updateIntervalDialogView.findViewById(R.id.sbUpdateInterval);
                mUpdateIntervalLabel = (TextView) updateIntervalDialogView.findViewById(R.id.tvUpdateIntervalLabel);

                mUpdateInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mUpdateIntervalLabel.setText(Integer.toString(progress) + "s");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                // Fill layout with stored values
                mUpdateInterval.setProgress(updateInterval);
                mUpdateIntervalLabel.setText(Integer.toString(updateInterval) + "s");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt("updateInterval", mUpdateInterval.getProgress());
                        editor.apply();
                        mAppContext.setUpdateInterval(mUpdateInterval.getProgress());
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

                return true;
            }
        });
    }

    private void setupCostStatisticsPeriodDialog(Preference costStatisticsPeriodDialog) {
        costStatisticsPeriodDialog.setSummary(
            getString(R.string.text_pref_cost_statistics_period_summary) +
            " " +
            mSettings.getInt("costStatisticsPeriod", 10) +
            " days"
        );

        costStatisticsPeriodDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogStyle);
                builder.setTitle(getString(R.string.text_pref_cost_statistics_period_title));

                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View costStatisticsPeriodDialogView = layoutInflater.inflate(R.layout.dialog_cost_statistics_period_settings, null);
                builder.setView(costStatisticsPeriodDialogView);

                // Get the previously stored update interval
                int costStatisticsPeriod = mSettings.getInt("costStatisticsPeriod", 10);

                mCostStatisticsPeriod = (NumberPicker) costStatisticsPeriodDialogView.findViewById(R.id.npCostStatistics);
                mCostStatisticsPeriod.setMinValue(7);
                mCostStatisticsPeriod.setMaxValue(30);

                // Fill layout with stored values
                mCostStatisticsPeriod.setValue(costStatisticsPeriod);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt("costStatisticsPeriod", mCostStatisticsPeriod.getValue());
                        editor.apply();
                        mAppContext.setCostStatisticsPeriod(mCostStatisticsPeriod.getValue());
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

                return true;
            }
        });
    }

    private void setupIPDialog(Preference ipDialog) {
        ipDialog.setSummary(mSettings.getString("IP", "192.168.0.1"));

        ipDialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialogStyle);
                builder.setTitle(getString(R.string.text_pref_ip_title));

                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                View ipDialogView = layoutInflater.inflate(R.layout.dialog_ip_settings, null);
                builder.setView(ipDialogView);

                // Get the previously stored ip
                String ip = mSettings.getString("IP", "192.168.0.1");

                mIP1 = (EditText) ipDialogView.findViewById(R.id.etIP1);
                mIP2 = (EditText) ipDialogView.findViewById(R.id.etIP2);
                mIP3 = (EditText) ipDialogView.findViewById(R.id.etIP3);
                mIP4 = (EditText) ipDialogView.findViewById(R.id.etIP4);

                // Fill layout with stored values
                StringTokenizer token = new StringTokenizer(ip, ".");
                mIP1.setText(token.nextToken());
                mIP2.setText(token.nextToken());
                mIP3.setText(token.nextToken());
                mIP4.setText(token.nextToken());

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isValidIPNumber(mIP1) && isValidIPNumber(mIP2) && isValidIPNumber(mIP3) && isValidIPNumber(mIP4)) {
                            // ... edit preference file and update the ip address
                            String ip = mIP1.getText().toString() + "." +
                                    mIP2.getText().toString() + "." +
                                    mIP3.getText().toString() + "." +
                                    mIP4.getText().toString();

                            if(!ip.equals(mSettings.getString("IP", "192.168.0.1"))) {
                                SharedPreferences.Editor editor = mSettings.edit();
                                editor.putString("IP", ip);
                                editor.apply();
                                mAppContext.setIPAdress(mSettings.getString("IP", "192.168.0.1"));

                                // Settings have changed
                                MainActivity.SETTINGS_UPDATED = true;
                            }
                        } else {
                            Toast.makeText(getContext(), getContext().getString(R.string.toast_ip_invalid), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

                return true;
            }
        });
    }

    private void setupSyncProcessPreference(Preference syncPreference) {
        syncPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toast_sync_started), Toast.LENGTH_SHORT).show();
                DataLoader loader = new DataLoader(mAppContext, mContext);
                try {
                    loader.synchronizeChargePlan("http://" + mAppContext.getIPAdress() + ":" + getString(R.string.webservice_putChargePlan));
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Exception while synchronizing charge plan.");
                    Toast.makeText(getActivity(), getString(R.string.toast_sync_ended_error_interrupted), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    /**
     * Error detection for IP
     * @param ip The textfield where the content needs to be checked
     * @return true when ip number is valid, false otherwise
     */
    private boolean isValidIPNumber(EditText ip) {
        // IP field can't be empty
        if(ip.getText().length() <= 0) {
            return false;
        }

        // Entered number needs to be between 0 and 255
        int ipNumber = Integer.parseInt(ip.getText().toString());
        return !(ipNumber < 0 || ipNumber > 255);
    }
}
