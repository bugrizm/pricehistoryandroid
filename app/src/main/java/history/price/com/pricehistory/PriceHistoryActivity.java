package history.price.com.pricehistory;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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


public class PriceHistoryActivity extends ActionBarActivity {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public static final String PRICE_HISTORY_LIST_EXTRA = "PRICE_HISTORY_LIST_EXTRA";
    public static final String PRODUCT_EXTRA = "PRODUCT_EXTRA";

    private List<ProductPrice> productPriceList;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);

        productPriceList = (List<ProductPrice>) getIntent().getSerializableExtra(PRICE_HISTORY_LIST_EXTRA);
        product = (Product) getIntent().getSerializableExtra(PRODUCT_EXTRA);

        initBannerAd();
        initProductName();
        initProductPriceGraph();
        initProductPriceList();
    }

    private void initBannerAd() {
        AdView mAdView = (AdView) findViewById(R.id.historyAdView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("60E86BD611C3E5168BF85F7F6B8A3649").build();
        mAdView.loadAd(adRequest);
    }

    private void initProductName() {
        ((TextView)findViewById(R.id.historyProductNameText)).setText(product.getName());

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

        List<Entry> yVals = new ArrayList<>();
        List<String> xVals = new ArrayList<>();

        float maxValue = 0;
        float minValue = Float.MAX_VALUE;

        for(int i=0; i<productPriceList.size(); i++) {
            ProductPrice pp = productPriceList.get(i);
            float price = pp.getPrice().floatValue();

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
            float price = pp.getPrice().floatValue();

            yVals.add(new Entry(price - minValue, i));
            xVals.add(i, format.format(pp.getDate()));
        }

        final float finalMinValue = minValue;

        LineDataSet dataSet = new LineDataSet(yVals, "");
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(R.color.black);
        dataSet.setValueTextSize(11);
        dataSet.setValueTextColor(R.color.green_button);
        dataSet.setLineWidth(1.5f);
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
