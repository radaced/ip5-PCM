package ch.fhnw.ip6.powerconsumptionmanager.model;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import ch.fhnw.ip6.powerconsumptionmanager.model.settings.PCMSetting;

/**
 * Represents a component that is connected to the PCM.
 */
public class PCMComponent {
    private static final String TAG = "PCMComponent";

    private String mName;
    private double mPower;
    private double mEnergy;
    private double mCost;
    private int mScaleMinArc, mScaleMaxArc;
    private boolean mIsDisplayedOnDashboard = false;
    private boolean mHasSettings;

    // List for settings
    private LinkedList<PCMSetting> mSettings = new LinkedList<>();
    // List for statistics
    private LinkedList<Double> mStatistics = new LinkedList<>();
    // List for consumption data
    private LinkedList<ConsumptionData> mConsumptionData = new LinkedList<>();

    /**
     * Standard constructor on initial read of all connected components.
     * @param name Name of the component.
     */
    public PCMComponent(String name, boolean hasSettings) {
        mName = name;
        mHasSettings = hasSettings;
        mPower = 0;
        mEnergy = 0;
        mCost = 0;
        mScaleMinArc = 0;
        mScaleMaxArc = 0;
    }

    /**
     * Renders all settings of this component into a linear layout as container.
     * @param context Context where the settings need to be rendered.
     * @param layoutContainer The layout container for the settings layouts.
     */
    public boolean renderSettings(Context context, LinearLayout layoutContainer) {
        boolean success = true;

        try {
            for(PCMSetting setting : mSettings) {
                setting.inflateLayout(context, layoutContainer);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            success = false;
        }

        return success;
    }

    /**
     * Fills the loaded current data of a connected component.
     * @param componentData JSONObject of the loaded data (power, energy and costs).
     * @param minScaleArc The minimum value possible of the arc progress scale per component on the dashboard.
     * @param scaleMaxArc The maximum value possible of the arc progress scale per component on the dashboard.
     * @throws JSONException when an error occurred while processing the JSON.
     */
    public void fillDashboardData(JSONObject componentData, int minScaleArc, int scaleMaxArc) throws JSONException {
        mIsDisplayedOnDashboard = true; // Flag that the component should be displayed on the dashboard
        mPower = componentData.getDouble("Leistung");
        mEnergy = componentData.getDouble("Energie");
        mCost = componentData.getDouble("Kosten");
        mScaleMinArc = minScaleArc;
        mScaleMaxArc = scaleMaxArc;
    }

    /**
     * Stores statistic data to this component in the mStatistics list.
     * @param data Statistics data as a JSONArray with the cost statistic per day.
     * @throws JSONException when an error occurred while processing the JSON.
     */
    public void fillStatistics(JSONArray data) throws JSONException {
        for (int i = 0; i < data.length(); i++) {
            JSONObject stats = (JSONObject) data.get(i);
            mStatistics.add(stats.getDouble("Kosten(CHF)"));
        }
    }

    /***********************
     * GETTERS AND SETTERS *
     ***********************/
    public String getName() {
        return mName;
    }

    public double getPower() {
        return mPower;
    }

    public double getEnergy() {
        return mEnergy;
    }

    public double getCost() {
        return mCost;
    }

    public int getScaleMinArc() {
        return mScaleMinArc;
    }

    public int getScaleMaxArc() {
        return mScaleMaxArc;
    }

    public boolean isDisplayedOnDashboard() {
        return mIsDisplayedOnDashboard;
    }

    public boolean hasSettings() {
        return mHasSettings;
    }

    public LinkedList<PCMSetting> getSettings() {
        return mSettings;
    }

    public LinkedList<Double> getStatistics() {
        return mStatistics;
    }

    public LinkedList<ConsumptionData> getConsumptionData() {
        return mConsumptionData;
    }
}
