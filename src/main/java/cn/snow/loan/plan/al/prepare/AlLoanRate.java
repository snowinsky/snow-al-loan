package cn.snow.loan.plan.al.prepare;

import cn.snow.loan.plan.funding.prepare.LoanRate;

public class AlLoanRate {

    /**
     * 助贷贷款年利率
     */
    private final LoanRate yearRate;
    /**
     * 违约金日利率
     */
    private LoanRate breachFeeRate;
    /**
     * 期款滞纳金利率
     */
    private LoanRate termLateFeeRate;
    /**
     * 贷款滞纳金利率
     */
    private LoanRate loanLateFeeRate;

    public AlLoanRate(LoanRate yearRate) {
        this.yearRate = yearRate;
    }

    public LoanRate getYearRate() {
        return yearRate;
    }

    public LoanRate getBreachFeeRate() {
        return breachFeeRate;
    }

    public void setBreachFeeRate(LoanRate breachFeeRate) {
        this.breachFeeRate = breachFeeRate;
    }

    public LoanRate getTermLateFeeRate() {
        return termLateFeeRate;
    }

    public void setTermLateFeeRate(LoanRate termLateFeeRate) {
        this.termLateFeeRate = termLateFeeRate;
    }

    public LoanRate getLoanLateFeeRate() {
        return loanLateFeeRate;
    }

    public void setLoanLateFeeRate(LoanRate loanLateFeeRate) {
        this.loanLateFeeRate = loanLateFeeRate;
    }
}
