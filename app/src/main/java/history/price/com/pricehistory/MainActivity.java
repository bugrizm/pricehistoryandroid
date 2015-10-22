package history.price.com.pricehistory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import history.price.com.pricehistory.adapter.ProductListAdapter;
import history.price.com.pricehistory.entity.Product;
import history.price.com.pricehistory.entity.ProductPrice;
import history.price.com.pricehistory.http.RetrievePriceHistoryTask;
import history.price.com.pricehistory.http.SearchTask;


public class MainActivity extends ActionBarActivity {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private List<Product> productList;

    private InterstitialAd interstitialAd;

    private static int numberOfSearch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBannerAd();
        initInterstitialAd();
    }

    private void initBannerAd() {
        AdView mAdView = (AdView) findViewById(R.id.mainAdView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("60E86BD611C3E5168BF85F7F6B8A3649").build();
        mAdView.loadAd(adRequest);
    }

    private void initInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ad_main_interstitial));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("60E86BD611C3E5168BF85F7F6B8A3649").build();
        interstitialAd.loadAd(adRequest);
    }

    public void onSearchButtonClick(View view) {
        numberOfSearch++;

        if(numberOfSearch%5 == 2) {
            if(interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                requestNewInterstitial();
            }
        }

        String searchString = ((EditText)findViewById(R.id.searchText)).getText().toString();

        SearchTask searchTask = new SearchTask(this, searchString);
        searchTask.execute();
    }

    public void updateProductList(JSONArray resultArray) {

        try {
            parseProductListJSON(resultArray);
            initProductList();
            hideKeyboard();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<Product> parseProductListJSON(JSONArray jsonArray) throws JSONException {

        productList = new ArrayList<>();

        for(int i=0; i<jsonArray.length(); i++) {
            JSONArray jsonObject = jsonArray.getJSONArray(i);

            Product parsedProduct = new Product();

            try {
                parsedProduct.setId(jsonObject.getInt(0));
                parsedProduct.setName(jsonObject.getString(1));
                parsedProduct.setLink(jsonObject.getString(2));
                parsedProduct.setPrice(new BigDecimal(parsePrice(jsonObject.getInt(3))));
                parsedProduct.setStoreId(jsonObject.getInt(4));

                productList.add(parsedProduct);

            } catch(JSONException e) {/*do nothing*/ }
        }

        return productList;
    }

    private double parsePrice(int priceInt) {
        int price1 = priceInt/100;
        int price2 = priceInt - (price1*100);
        return price1 + (price2/100);
    }

    public void initProductList() {
        final ListView productListView = (ListView) findViewById(R.id.searchResultListView);
        ProductListAdapter adapter = ProductListAdapter.newInstance(getLayoutInflater(), productList);
        productListView.setAdapter(adapter);

        productListView.invalidate();

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Product product = (Product) productListView.getItemAtPosition(position);

                RetrievePriceHistoryTask retrievePriceHistoryTask = new RetrievePriceHistoryTask(MainActivity.this, product);
                retrievePriceHistoryTask.execute();
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();

        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void openPriceHistoryScreen(JSONArray resultArray, Product product) {
        try {
            Intent priceHistoryIntent = new Intent(this, PriceHistoryActivity.class);
            priceHistoryIntent.putExtra(PriceHistoryActivity.PRICE_HISTORY_LIST_EXTRA, (Serializable) parseProductPriceListJSON(resultArray));
            priceHistoryIntent.putExtra(PriceHistoryActivity.PRODUCT_EXTRA, (Serializable) product);
            startActivity(priceHistoryIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<ProductPrice> parseProductPriceListJSON(JSONArray jsonArray) throws JSONException {

        List<ProductPrice> productPriceList = new ArrayList<>();

        for(int i=0; i<jsonArray.length(); i++) {
            JSONArray jsonObject = jsonArray.getJSONArray(i);

            ProductPrice parsedProductPrice = new ProductPrice();

            try {
                parsedProductPrice.setDate(parseDate(jsonObject.getString(0)));
                parsedProductPrice.setPrice(new BigDecimal(parsePrice(jsonObject.getInt(1))));

                productPriceList.add(parsedProductPrice);

            } catch(Exception e) {/*do nothing*/ }
        }

        return productPriceList;
    }

    private Date parseDate(String date) throws ParseException {
        return format.parse(date);
    }

}
