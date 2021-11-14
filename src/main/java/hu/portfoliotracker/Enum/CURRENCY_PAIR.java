package hu.portfoliotracker.Enum;

public enum CURRENCY_PAIR {

    ADABUSD(2),
    BTCBUSD(65000),
    BNBBUSD(640),
    ETHBUSD(4600),
    XLMBUSD(0.3767),
    XRPBUSD(0.19);

    private double numVal;

    CURRENCY_PAIR(double numVal){
        this.numVal = numVal;
    }

    public double getNumVal() {
        return this.numVal;
    }

}
