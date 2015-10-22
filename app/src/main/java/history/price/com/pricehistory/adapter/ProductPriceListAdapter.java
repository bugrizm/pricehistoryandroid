package history.price.com.pricehistory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import history.price.com.pricehistory.R;
import history.price.com.pricehistory.entity.ProductPrice;

/**
 * Created by bugri_000 on 4/7/2015.
 */
public class ProductPriceListAdapter extends BaseAdapter {

    private List<ProductPrice> productPriceList;
    private LayoutInflater inflater;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    public static ProductPriceListAdapter newInstance(LayoutInflater inflater, List<ProductPrice> productPriceList) {
        ProductPriceListAdapter adapter = new ProductPriceListAdapter();
        adapter.inflater = inflater;
        adapter.productPriceList = productPriceList;
        return adapter;
    }

    @Override
    public int getCount() {
        return productPriceList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return productPriceList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_price_list_item, parent, false);
        }

        TextView priceText = (TextView) convertView.findViewById(R.id.historyPriceText);
        TextView dateText = (TextView) convertView.findViewById(R.id.historyDateText);

        ProductPrice productPrice = productPriceList.get(position);

        priceText.setText(productPrice.getPrice() + " TL");
        dateText.setText(format.format(productPrice.getDate()));

        return convertView;
    }

}
