package history.price.com.pricehistory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import history.price.com.pricehistory.R;
import history.price.com.pricehistory.WelcomeActivity;
import history.price.com.pricehistory.entity.Product;

/**
 * Created by bugri_000 on 4/7/2015.
 */
public class ProductListAdapter extends BaseAdapter {

    private List<Product> productList;
    private LayoutInflater inflater;

    public static ProductListAdapter newInstance(LayoutInflater inflater, List<Product> productList) {
        ProductListAdapter adapter = new ProductListAdapter();
        adapter.inflater = inflater;
        adapter.productList = productList;
        return adapter;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return productList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_list_item_wrapper, parent, false);
        }

        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.wrapped_item);

        TextView productNameText = (TextView) layout.findViewById(R.id.productNameText);
        TextView productStoreText = (TextView) layout.findViewById(R.id.storeText);
        TextView productPriceText = (TextView) layout.findViewById(R.id.productPriceText);

        Product product = productList.get(position);

        productNameText.setText(product.getName());
        productStoreText.setText(getStoreName(product.getStoreId()));
        productPriceText.setText(product.getPriceStringWithoutKurus());

        return convertView;
    }

    private String getStoreName(int storeId) {
        return WelcomeActivity.storeNameMap.get(storeId);
    }

}
