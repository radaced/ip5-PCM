package ch.fhnw.ip5.powerconsumptionmanager.network;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import ch.fhnw.ip5.powerconsumptionmanager.model.ConsumptionDataModel;
import ch.fhnw.ip5.powerconsumptionmanager.util.PowerConsumptionManagerAppContext;

/**
 * Created by Patrik on 02.12.2015.
 */
public class ConsumptionDataLoader {
    private PowerConsumptionManagerAppContext context;
    private ConsumptionDataLoaderCallback callback;
    private String url;

    public ConsumptionDataLoader(PowerConsumptionManagerAppContext context, ConsumptionDataLoaderCallback callback, String url) {
        this.context = context;
        this.callback = callback;
        this.url = url;
    }

    public void LoadUsageData() {

        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.UsageDataLoaderDidFail();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.UsageDataLoaderDidFail();
                    return;
                }
                try {
                    JSONArray dataJson = new JSONArray(response.body().string());
                    ConsumptionDataModel usageData = new ConsumptionDataModel(dataJson);
                    context.setUsageData(usageData);
                    //FileStorageHandler.saveObject(appContext, TrachtenAppContext.FILENAME_MAIN_CONFIG, mainConfig);
                    callback.UsageDataLoaderDidFinish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.UsageDataLoaderDidFail();
                }
            }
        });
    }
}
