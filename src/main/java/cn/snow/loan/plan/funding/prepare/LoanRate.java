package cn.snow.loan.plan.funding.prepare;

import java.math.BigDecimal;

public class LoanRate {

    public static final int RATE_TYPE_YEAR = 10;

    /**
     * 贷款利率，百分号前面的数，例如 8.2
     */
    private final double rate;
    private final BigDecimal overdueFeeDayRate;
    /**
     * 贷款利率类型，
     */
    private final int rateType;

    private LoanRate() {
        throw new UnsupportedOperationException("Please use LoanRate.yearRate(agr)");
    }

    /**
     * @param rate         8.2 利率，比如8.2的意思就是8.2%的意思
     * @param rateDiscount 1 利率优惠，1表示基准利率，1.20就是上浮20%， 0.8就是下浮20%
     * @param rateType     10
     */
    private LoanRate(double rate, double rateDiscount, int rateType, BigDecimal overdueFeeDayRate) {
        this.rate = rate * rateDiscount;
        this.rateType = rateType;
        this.overdueFeeDayRate = overdueFeeDayRate == null || overdueFeeDayRate.doubleValue() <= 0 ? getDayRate() : overdueFeeDayRate;
    }

    private LoanRate(double rate) {
        this(rate, 1, RATE_TYPE_YEAR, null);
    }

    public static LoanRate yearRate(double rate) {
        return new LoanRate(rate);
    }

    public static LoanRate dayRate(double rate) {
        return new LoanRate(BigDecimal.valueOf(rate).multiply(new BigDecimal("360")).doubleValue());
    }

    public static LoanRate yearRate(double rate, BigDecimal overdueFeeDayRate) {
        return new LoanRate(rate, 1, RATE_TYPE_YEAR, overdueFeeDayRate);
    }

    public static LoanRate dayRate(double rate, BigDecimal overdueFeeDayRate) {
        return new LoanRate(BigDecimal.valueOf(rate).multiply(new BigDecimal("360")).doubleValue(), 1, RATE_TYPE_YEAR, overdueFeeDayRate);
    }

    private static LoanRate yeatRateWithDiscount(double rate, double rateDiscount) {
        return new LoanRate(rate, rateDiscount, RATE_TYPE_YEAR, null);
    }

    public double getYearRateBeforePercent() {
        return rate;
    }

    public BigDecimal getYearRate() {
        return BigDecimal.valueOf(rate).divide(new BigDecimal("100"), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getMonthRate() {
        return getYearRate().divide(new BigDecimal("12"), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getDayRate() {
        return getYearRate().divide(new BigDecimal("360"), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getOverdueFeeDayRate() {
        return this.overdueFeeDayRate;
    }
}
