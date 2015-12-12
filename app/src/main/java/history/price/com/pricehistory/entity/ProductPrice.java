package history.price.com.pricehistory.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by bugri_000 on 7/10/2015.
 */
public class ProductPrice implements Serializable {

    private Date date;
    private BigDecimal price;

    public String getPriceString() {
        int priceLira = price.intValue()/100;
        int priceKurus = price.intValue() - (priceLira*100);

        if(priceKurus < 10) {
            return priceLira + ",0" + priceKurus + " TL";
        }

        return priceLira + "," + priceKurus + " TL";
    }

    public String getPriceStringWithoutKurus() {
        int priceLira = price.intValue()/100;
        return priceLira + " TL";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
