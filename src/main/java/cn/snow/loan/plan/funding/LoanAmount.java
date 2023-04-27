package cn.snow.loan.plan.funding;

import java.math.BigDecimal;

public class LoanAmount {

    private final BigDecimal loanAmountYuan;

    private LoanAmount(){
        throw new UnsupportedOperationException("Please use valueOf");
    }

    private LoanAmount(BigDecimal loanAmount) {
        loanAmountYuan = loanAmount;
    }

    /**
     * @param totalAmount
     * @param downPaymentPercent 贷款首付比例，最小0，最大1
     */
    public LoanAmount(BigDecimal totalAmount, double downPaymentPercent) {
        loanAmountYuan = totalAmount.multiply(BigDecimal.valueOf(1 - downPaymentPercent)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static LoanAmount valueOf(BigDecimal loanAmount){
        return new LoanAmount(loanAmount);
    }

    public BigDecimal getLoanAmountYuan() {
        return loanAmountYuan;
    }
}
