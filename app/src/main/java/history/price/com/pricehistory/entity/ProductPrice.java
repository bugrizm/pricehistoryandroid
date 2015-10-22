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
