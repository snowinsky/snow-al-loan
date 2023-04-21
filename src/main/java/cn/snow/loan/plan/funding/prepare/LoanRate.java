package cn.snow.loan.plan.funding.prepare;

import java.math.BigDecimal;

public class LoanRate {

    public static final int RATE_TYPE_YEAR = 10;


    private final BigDecimal yearRateBeforePercent;
    private final BigDecimal monthRateBeforePercent;
    private final BigDecimal dayRateBeforePercent;

    /**
     * @param yearRateBeforePercent 8.2 利率，比如8.2的意思就是8.2%的意思
     * @param rateDiscount          1 利率优惠，1表示基准利率，1.20就是上浮20%， 0.8就是下浮20%
     */
    private LoanRate(double yearRateBeforePercent, double rateDiscount) {
        this.yearRateBeforePercent = BigDecimal.valueOf(yearRateBeforePercent).multiply(BigDecimal.valueOf(rateDiscount));
        monthRateBeforePercent = this.yearRateBeforePercent.divide(new BigDecimal("12"), 10, BigDecimal.ROUND_HALF_UP);
        dayRateBeforePercent = this.yearRateBeforePercent.divide(new BigDecimal("360"), 10, BigDecimal.ROUND_HALF_UP);
    }

    private LoanRate(double yearRateBeforePercent) {
        this(yearRateBeforePercent, 1);
    }

    public static LoanRate yearRateBeforePercent(double rate) {
        return new LoanRate(rate);
    }

    public static LoanRate dayRateBeforePercent(double rate) {
        return new LoanRate(BigDecimal.valueOf(rate).multiply(new BigDecimal("360")).doubleValue());
    }

    public static LoanRate yearRate(double rate) {
        return new LoanRate(BigDecimal.valueOf(rate).multiply(new BigDecimal("100")).doubleValue());
    }

    public static LoanRate dayRate(double rate) {
        return new LoanRate(BigDecimal.valueOf(rate).multiply(new BigDecimal("100")).multiply(new BigDecimal("360")).doubleValue());
    }

    public BigDecimal getYearRateBeforePercent() {
        return yearRateBeforePercent;
    }

    public BigDecimal getMonthRateBeforePercent() {
        return monthRateBeforePercent;
    }

    public BigDecimal getDayRateBeforePercent() {
        return dayRateBeforePercent;
    }

    public BigDecimal getYearRate() {
        return yearRateBeforePercent.divide(new BigDecimal("100"), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getMonthRate() {
        return monthRateBeforePercent.divide(new BigDecimal("100"), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getDayRate() {
        return dayRateBeforePercent.divide(new BigDecimal("100"), 10, BigDecimal.ROUND_HALF_UP);
    }
}
