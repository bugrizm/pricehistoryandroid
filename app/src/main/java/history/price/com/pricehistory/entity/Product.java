package history.price.com.pricehistory.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by bugri_000 on 7/10/2015.
 */
public class Product implements Serializable {

    private int id;
    private String name;
    private BigDecimal price;
    private int storeId;
    private String link;

    public Product() {}

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
