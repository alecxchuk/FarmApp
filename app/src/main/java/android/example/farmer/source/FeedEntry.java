package android.example.farmer.source;

public class FeedEntry {

    private String feedBrand;
    private String status;



    private String penName;
    private String feedDate;
    private int quantity, feedType;



    public FeedEntry(String penName,String feedDate,String feedBrand, int feedType, String status, int quantity) {
        this.penName=penName;
        this.feedDate = feedDate;
        this.feedBrand = feedBrand;
        this.feedType = feedType;
        this.status = status;
        this.quantity = quantity;

    }



    public FeedEntry(){}

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFeedBrand() {
        return feedBrand;
    }

    public void setFeedBrand(String feedBrand) {
        this.feedBrand = feedBrand;
    }

    public int getFeedType() {
        return feedType;
    }

    public void setFeedType(int feedType) {
        this.feedType = feedType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedDate() {
        return feedDate;
    }

    public void setFeedDate(String feedDate) {
        this.feedDate = feedDate;
    }


}
