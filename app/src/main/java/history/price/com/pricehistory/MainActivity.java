package history.price.com.pricehistory;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import history.price.com.pricehistory.adapter.ProductListAdapter;
import history.price.com.pricehistory.entity.Product;
import history.price.com.pricehistory.http.RetrievePriceHistoryTask;
import history.price.com.pricehistory.http.SearchTask;


public class MainActivity extends PriceHistoryOpenerActivity {

    private List<Product> originalProductList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();

    private InterstitialAd interstitialAd;

    private static int numberOfSearch = 0;

    private Order order = Order.LOWEST_FIRST;
    private String selectedStore = "Tüm Mağazalar";
    private String selectedCategory = "Tüm Kategoriler";

    private TextView noResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(myToolbar);

        noResultTextView = (TextView) findViewById(R.id.noResultText);

        initBannerAd();
        initInterstitialAd();
        requestNewInterstitial();

        EditText searchText = (EditText) findViewById(R.id.searchText);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    search();
                }
                return false;
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("MyBoolean", true);
        super.onSaveInstanceState(savedInstanceState);
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
        search();
    }

    private void search() {
        String searchString = ((EditText) findViewById(R.id.searchText)).getText().toString();

        if (!isSearchStringValid(searchString)) {
            Toast.makeText(this, "Arama yapmak için en az üç karakter giriniz.", Toast.LENGTH_LONG).show();
            return;
        }

        numberOfSearch++;

        if (numberOfSearch > 0 && numberOfSearch % 5 == 0) {
            if (interstitialAd.isLoaded()) {
                isIntersititialAdOpened = true;
                interstitialAd.show();
            } else {
                requestNewInterstitial();
            }
        }

        SearchTask searchTask = new SearchTask(this, searchString);
        searchTask.execute();
    }

    private boolean isSearchStringValid(String searchString) {
        String[] searchWords = searchString.split(" ");

        int numberOfWords = searchWords.length;

        for (String searchWord : searchWords) {
            if (searchWord.trim().equals("")) {
                numberOfWords--;
            }
        }

        return numberOfWords != 0 && (numberOfWords != 1 || searchWords[0].length() > 2);
    }

    public void updateProductList(JSONArray resultArray) {

        try {
            originalProductList = parseProductListJSON(resultArray);
            initProductList();
            hideKeyboard();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private List<Product> parseProductListJSON(JSONArray jsonArray) throws JSONException {

        productList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray jsonObject = jsonArray.getJSONArray(i);

            Product parsedProduct = new Product();

            try {
                parsedProduct.setId(jsonObject.getInt(0));
                parsedProduct.setName(jsonObject.getString(1));
                parsedProduct.setLink(jsonObject.getString(2));
                parsedProduct.setPrice(new BigDecimal(jsonObject.getInt(3)));
                parsedProduct.setStoreId(jsonObject.getInt(4));
                parsedProduct.setCategoryId(jsonObject.getInt(5));

                productList.add(parsedProduct);

            } catch (JSONException e) {/*do nothing*/ }
        }

        return productList;
    }

    public void initProductList() {
        noResultTextView.setVisibility(View.INVISIBLE);
        productList = copyOriginalList();
        filterProductsByStore();
        filterProductsByCategory();
        sortProductList();

        if (productList.size() == 0) {
            noResultTextView.setVisibility(View.VISIBLE);
        }

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

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    private void sortProductList() {
        final int sortCoef;

        if (order == Order.HIGHEST_FIRST) {
            sortCoef = -1;
        } else {
            sortCoef = 1;
        }

        Collections.sort(productList, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return sortCoef * (lhs.getPrice().intValue() - rhs.getPrice().intValue());
            }
        });
    }

    public void onSortButtonClick(View view) {
        Button sortButton = (Button) findViewById(R.id.sortButton);

        if (order == Order.LOWEST_FIRST) {
            order = Order.HIGHEST_FIRST;

            sortButton.setText("Artan");
            sortButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up, 0);
        } else {
            order = Order.LOWEST_FIRST;

            sortButton.setText("Azalan");
            sortButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down, 0);
        }

        initProductList();
    }

    public void onStoreButtonClick(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.store_button_menu, popup.getMenu());
        popup.getMenu().clear();

        List<String> storeOptionList = new ArrayList<>();
        storeOptionList.add("Tüm Mağazalar");
        storeOptionList.addAll(WelcomeActivity.storeIdMap.keySet());
        for (final String store : storeOptionList) {
            popup.getMenu().add(store).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    selectedStore = item.getTitle().toString();
                    Button storeButton = (Button) findViewById(R.id.storeButton);
                    storeButton.setText(selectedStore);
                    initProductList();
                    return true;
                }
            });
        }
        popup.show();
    }

    public void onCategoryButtonClick(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.category_button_menu, popup.getMenu());
        popup.getMenu().clear();

        List<String> categoryOptionList = new ArrayList<>();
        categoryOptionList.addAll(WelcomeActivity.categoryIdMap.keySet());
        Collections.sort(categoryOptionList);
        categoryOptionList.add(0, "Tüm Kategoriler");

        for (final String category : categoryOptionList) {
            popup.getMenu().add(category).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    selectedCategory = item.getTitle().toString();
                    Button categoryButton = (Button) findViewById(R.id.categoryButton);
                    categoryButton.setText(selectedCategory);
                    initProductList();
                    return true;
                }
            });
        }
        popup.show();
    }

    private void filterProductsByStore() {
        if (selectedStore.equals("Tüm Mağazalar")) {
            return;
        }

        int storeId = WelcomeActivity.storeIdMap.get(selectedStore);

        Iterator<Product> iterator = productList.iterator();
        while (iterator.hasNext()) {
            Product p = iterator.next();
            if (p.getStoreId() != storeId) {
                iterator.remove();
            }
        }

    }

    private void filterProductsByCategory() {
        if (selectedCategory.equals("Tüm Kategoriler")) {
            return;
        }

        int categoryId = WelcomeActivity.categoryIdMap.get(selectedCategory);

        Iterator<Product> iterator = productList.iterator();

        while (iterator.hasNext()) {
            Product p = iterator.next();
            if (p.getCategoryId() != categoryId) {
                iterator.remove();
            }
        }
    }

    private List<Product> copyOriginalList() {
        ArrayList<Product> copyList = new ArrayList<>();

        for (Product p : originalProductList) {
            copyList.add(p);
        }

        return copyList;
    }

    enum Order {
        HIGHEST_FIRST("Fiyat azalan"),
        LOWEST_FIRST("Fiyat artan");

        private String text;

        Order(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static Order getOrder(String text) {
            if (HIGHEST_FIRST.getText().equals(text)) {
                return HIGHEST_FIRST;
            } else if (LOWEST_FIRST.getText().equals(text)) {
                return LOWEST_FIRST;
            }
            throw new RuntimeException();
        }
    }

}
