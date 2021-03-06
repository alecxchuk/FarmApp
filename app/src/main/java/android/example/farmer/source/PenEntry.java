package android.example.farmer.source;

public class PenEntry {

    //private int id;
    private String penName;
    private String penBreed;
    private String stockDate;



    private String birdSource;
    private int age;
    private int number;
    private int losses;


    private int no_of_chicks;
    private int no_of_hens;
    private int no_of_cocks;


    public PenEntry(int no_of_chicks, int no_of_hens, int no_of_cocks) {
        this.no_of_chicks = no_of_chicks;
        this.no_of_hens = no_of_hens;
        this.no_of_cocks = no_of_cocks;
    }

    /**
     * This constructor is for adding a new entry to the database. Id is autogenerated so is not needed
     * in this constructor
     *
     * @param penName
     * @param penBreed
     * @param stockDate
     * @param age
     */
    public PenEntry(String penName, String penBreed, String birdSource, String stockDate, int age, int number, int losses,
                    int no_of_chicks, int no_of_hens, int no_of_cocks) {
        this.penName = penName;
        this.penBreed = penBreed;
        this.birdSource=birdSource;
        this.stockDate = stockDate;
        this.age = age;
        this.number = number;
        this.losses = losses;
        //this.no_of_chicks = no_of_chicks;
        //this.no_of_hens = no_of_hens;
        //this.no_of_cocks = no_of_cocks;
    }

    public PenEntry() {
    }




    /*public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }*/

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public String getPenBreed() {
        return penBreed;
    }

    public void setPenBreed(String penBreed) {
        this.penBreed = penBreed;
    }

    public String getBirdSource() {
        return birdSource;
    }

    public void setBirdSource(String birdSource) {
        this.birdSource = birdSource;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int thirdProduction) {
        this.losses = losses;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStockDate() {
        return stockDate;
    }

    public void setStockDate(String date) {
        this.stockDate = date;
    }

    public int getNo_of_chicks() {
        return no_of_chicks;
    }

    public int getNo_of_hens() {
        return no_of_hens;
    }

    public int getNo_of_cocks() {
        return no_of_cocks;
    }

}
