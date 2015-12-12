package history.price.com.pricehistory;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import history.price.com.pricehistory.adapter.ProductPriceListAdapter;
import history.price.com.pricehistory.entity.Product;
import history.price.com.pricehistory.entity.ProductPrice;
import history.price.com.pricehistory.utils.FavouritePreferences;


public class PriceHistoryActivity extends BaseActivity {

    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    public static final String PRICE_HISTORY_LIST_EXTRA = "PRICE_HISTORY_LIST_EXTRA";
    public static final String PRODUCT_EXTRA = "PRODUCT_EXTRA";

    private List<ProductPrice> productPriceList;
    private Product product;

    private FavouritePreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);

        preferences = new FavouritePreferences(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.priceHistoryToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productPriceList = (List<ProductPrice>) getIntent().getSerializableExtra(PRICE_HISTORY_LIST_EXTRA);
        product = (Product) getIntent().getSerializableExtra(PRODUCT_EXTRA);

        initBannerAd();
        initProductName();
        initProductPriceGraph();
        initProductPriceList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_price_history, menu);

        MenuItem menuItem = menu.findItem(R.id.mark_favourite_button);

        if(preferences.hasProduct(product.getId())) {
            menuItem.setIcon(R.drawable.favourite);
        } else {
            menuItem.setIcon(R.drawable.not_favourite);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        item.setIcon(R.drawable.favourite);
        int id = item.getItemId();

        if (id == R.id.mark_favourite_button) {

            if(preferences.hasProduct(product.getId())) {
                preferences.removeProduct(product.getId());
                item.setIcon(R.drawable.not_favourite);
            } else {
                preferences.addProduct(product.getId());
                item.setIcon(R.drawable.favourite);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initBannerAd() {
        AdView mAdView = (AdView) findViewById(R.id.historyAdView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("60E86BD611C3E5168BF85F7F6B8A3649").build();
        mAdView.loadAd(adRequest);
    }

    private void initProductName() {
        String storeName = WelcomeActivity.storeNameMap.get(product.getStoreId());
        int greenColor = getResources().getColor(R.color.dark_green_button);
        int grayColor = getResources().getColor(R.color.grey);

        getResources().getColor(R.color.dark_green_button);
        ((TextView) findViewById(R.id.historyProductNameText)).setText(product.getName());

        TextView productNameText = (TextView)findViewById(R.id.historyProductNameText);

        Spannable productNameWord = new SpannableString(product.getName());
        productNameWord.setSpan(new ForegroundColorSpan(greenColor), 0, productNameWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable storeNameWord = new SpannableString(" - " + storeName);
        storeNameWord.setSpan(new ForegroundColorSpan(grayColor), 0, storeNameWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        storeNameWord.setSpan(new RelativeSizeSpan(0.8f), 0, storeNameWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        productNameText.setText(productNameWord);
        productNameText.append(storeNameWord);

        TextView linkText = (TextView) findViewById(R.id.linkText);
        String linkHtmlString = "<a href='" + product.getLink() + "'> Ürüne Git </a>";
        linkText.setText(Html.fromHtml(linkHtmlString));
        linkText.setClickable(true);
        linkText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initProductPriceGraph() {
        sortProductPriceListAsc();

        ProductPrice currentPrice = new ProductPrice();
        currentPrice.setDate(Calendar.getInstance().getTime());
        currentPrice.setPrice(productPriceList.get(productPriceList.size() - 1).getPrice());
        productPriceList.add(currentPrice);

        LineChart chart = (LineChart) findViewById(R.id.priceHistoryGraph);
        chart.setTouchEnabled(false);

        List<Entry> yVals = new ArrayList<>();
        List<String> xVals = new ArrayList<>();

        float maxValue = 0;
        float minValue = Float.MAX_VALUE;

        for(int i=0; i<productPriceList.size(); i++) {
            ProductPrice pp = productPriceList.get(i);
            float price = pp.getPrice().floatValue() / 100;

            if(price > maxValue) {
                maxValue = price;
            }
            if(price < minValue) {
                minValue = price;
            }
        }

        minValue -= 50;

        for(int i=0; i<productPriceList.size(); i++) {
            ProductPrice pp = productPriceList.get(i);
            float price = pp.getPrice().floatValue() / 100;

            yVals.add(new Entry(price - minValue, i));
            xVals.add(i, format.format(pp.getDate()));
        }

        final float finalMinValue = minValue;

        LineDataSet dataSet = new LineDataSet(yVals, "");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(Color.rgb(0, 0, 200));
        dataSet.setValueTextSize(11);
        dataSet.setValueTextColor(R.color.green_button);
        dataSet.setLineWidth(3f);
        dataSet.setFillColor(Color.RED);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";//(int)(value + finalMinValue) + " TL";
            }
        });

        chart.setAutoScaleMinMaxEnabled(true);
        chart.getLegend().setEnabled(false);

        chart.getAxisLeft().setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return (int) (value + finalMinValue) + "";
            }
        });

        chart.getAxisRight().setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return "     ";
            }
        });

        chart.getAxisRight().setEnabled(false);

        List<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.setDescription("");
        chart.fitScreen();

        chart.invalidate();

    }

    private void sortProductPriceListAsc() {
        Collections.sort(productPriceList, new Comparator<ProductPrice>() {
            @Override
            public int compare(ProductPrice lhs, ProductPrice rhs) {
                return lhs.getDate().compareTo(rhs.getDate());
            }
        });
    }

    private void initProductPriceList() {
        sortProductPriceListDesc();

        final ListView productPriceListView = (ListView) findViewById(R.id.priceHistoryListView);
        ProductPriceListAdapter adapter = ProductPriceListAdapter.newInstance(getLayoutInflater(), productPriceList);
        productPriceListView.setAdapter(adapter);

        productPriceListView.invalidate();
    }

    private void sortProductPriceListDesc() {
        Collections.sort(productPriceList, new Comparator<ProductPrice>() {
            @Override
            public int compare(ProductPrice lhs, ProductPrice rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
    }

}
