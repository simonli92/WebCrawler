package io.CrawlerAtribute;

import java.io.Serializable;
import java.util.List;

public class item implements Serializable{
    private static final long serialVersionUID = 1L;
    public int itemID;
    public String keyWord;
    public List<String> keyWords;
    public String title;
    public double price;
    public String thumbnail;
    public String brand;
    public String detail_url;
    public String category;
}
