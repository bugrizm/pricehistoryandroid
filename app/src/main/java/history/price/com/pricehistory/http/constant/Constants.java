package history.price.com.pricehistory.http.constant;

/**
 * Created by taylan on 15.06.2014.
 */
public class Constants {

    // PARAMS
    public static final int SERVICE_CONNECTION_TIMEOUT = 10000;
    public static final int MAX_NUMBER_OF_CONNECTION_ATTEMPTS = 10;

    public static final String BASE_URL = "http://1-dot-pricehistory-1095.appspot.com/";

    public static final String getUrlForSearch(String searchString) {
        String[] keywords = searchString.split(" ");

        String url = BASE_URL + "search/";

        for (String keyword : keywords) {
            url += keyword.trim() + "+";
        }

        url = url.substring(0, url.length()-1);

        return url;
    }

    public static final String getUrlForPriceHistory(int productId) {
        return BASE_URL + "priceHistory/" + productId;
    }

}

