package cn.snow.loan.plan.funding;

import java.math.BigDecimal;
import java.util.List;

/**
 * Design it on 1/14/16.
 */
public class Loan {

    /**
     * 贷款总金额
     */
    private BigDecimal totalLoanMoney;
    /**
     * 贷款期数
     */
    private int totalMonth;
    /**
     * 贷款年利率，8.2%=0.082，就是算出来的那个小数，不是百分号前面的数
     */
    private double loanRate;

    /**
     * 需还款总利息金额
     */
    private BigDecimal totalInterest;
    /**
     * 需还款总金额（包含本金加利息）
     */
    private BigDecimal totalRepayment;
    /**
     * 首期还款金额
     */
    private BigDecimal firstRepayment;
    /**
     * 月平均还款金额
     */
    private BigDecimal avgRepayment;

    /**
     * 所有月份的还款计划情况
     */
    private List<LoanPerTerm> allLoans;

    public BigDecimal getTotalLoanMoney() {
        return totalLoanMoney;
    }

    public void setTotalLoanMoney(BigDecimal totalLoanMoney) {
        this.totalLoanMoney = totalLoanMoney;
    }

    public int getTotalMonth() {
        return totalMonth;
    }

    public void setTotalMonth(int totalMonth) {
        this.totalMonth = totalMonth;
    }

    public double getLoanRate() {
        return loanRate;
    }

    public void setLoanRate(double loanRate) {
        this.loanRate = loanRate;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getTotalRepayment() {
        return totalRepayment;
    }

    public void setTotalRepayment(BigDecimal totalRepayment) {
        this.totalRepayment = totalRepayment;
    }

    public BigDecimal getFirstRepayment() {
        return firstRepayment;
    }

    public void setFirstRepayment(BigDecimal firstRepayment) {
        this.firstRepayment = firstRepayment;
    }

    public BigDecimal getAvgRepayment() {
        return avgRepayment;
    }

    public void setAvgRepayment(BigDecimal avgRepayment) {
        this.avgRepayment = avgRepayment;
    }

    public List<LoanPerTerm> getAllLoans() {
        return allLoans;
    }

    public void setAllLoans(List<LoanPerTerm> allLoans) {
        this.allLoans = allLoans;
    }

    @Override
    public String toString() {
        StringBuilder allLoansStr = new StringBuilder();
        if (allLoans != null) {
            for (LoanPerTerm loanPerTerm : allLoans) {
                String lbmStr = String.format("期数:%02d 第%02d年 第%02d月 月供:%-8s 本金:%-8s 利息:%-8s 剩余应还:%-8s 上期结余未还本金:%-8s",
                        loanPerTerm.getMonth(),
                        loanPerTerm.getYear(),
                        loanPerTerm.getMonthInYear(),
                        loanPerTerm.getRepayment(),
                        loanPerTerm.getPayPrincipal(),
                        loanPerTerm.getInterest(),
                        loanPerTerm.getRemainTotal(),
                        loanPerTerm.getRemainPrincipal());
                if (allLoansStr.length() == 0) {
                    allLoansStr.append(lbmStr);
                } else {
                    allLoansStr.append("\n").append(lbmStr);
                }
            }
        }
        return "每月还款: " + getAvgRepayment() + "\t" +
                "总利息: " + getTotalInterest() + "\t" +
                "总担保费: " + getTotalInterest() + "\t" +
                "还款总额：" + getTotalRepayment() + "\t" +
                "首月还款: " + getFirstRepayment() + "\t" +
                "\n" +
                allLoansStr;
    }

    public static BigDecimal calInterestPenalty(BigDecimal principalAmountNoRepay, BigDecimal dayRate) {
        return principalAmountNoRepay.multiply(dayRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
