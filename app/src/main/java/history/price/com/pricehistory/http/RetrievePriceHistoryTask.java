package history.price.com.pricehistory.http;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import history.price.com.pricehistory.MainActivity;
import history.price.com.pricehistory.entity.Product;
import history.price.com.pricehistory.http.constant.Constants;
import history.price.com.pricehistory.utils.Tools;


/**
 * Created by bugri_000 on 4/7/2015.
 */
public class RetrievePriceHistoryTask extends AsyncTask<String, Void, String> {
    private MainActivity activity;

    private ProgressDialog progressDialog;

    private Product product;

    public RetrievePriceHistoryTask(MainActivity activity, Product product) {
        this.activity = activity;
        this.product = product;
        this.progressDialog = Tools.createServiceDialog(activity, "Yükleniyor...", false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = null;
        HttpEntity entity = null;
        HttpResponse response = null;
        try {

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, Constants.SERVICE_CONNECTION_TIMEOUT);
            HttpClient client = new DefaultHttpClient(httpParams);

            String url = Constants.getUrlForPriceHistory(product.getId());

            HttpGet getRequest = new HttpGet(url);
            response = client.execute(getRequest);

            entity = response.getEntity();
            if (entity != null) {
                result = Tools.convertInputStreamToString(entity.getContent());
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        try {
            JSONArray jsonArray = new JSONArray(result);
            activity.openPriceHistoryScreen(jsonArray, product);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
