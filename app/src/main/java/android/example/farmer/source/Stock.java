package android.example.farmer.source;

public class Stock {
    private String Date;
    private int chicks, hens, cocks, type;

    public void setpenStockDate(String date) {
        Date = date;
    }

    public void setChicks(int chicks) {
        this.chicks = chicks;
    }

    public void setHens(int hens) {
        this.hens = hens;
    }

    public void setCocks(int cocks) {
        this.cocks = cocks;
    }

    public void setStockType(int type) {
        this.type = type;
    }

    public String getPenStockDate() {
        return Date;
    }

    public int getChicks() {
        return chicks;
    }

    public int getHens() {
        return hens;
    }

    public int getCocks() {
        return cocks;
    }

    public int getStockType() {
        return type;
    }
}
