package cn.snow.loan.plan.funding;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Design it on 1/14/16.
 */
public class LoanPerTerm {

    /**
     * 第几期
     */
    private int month;
    /**
     * 具体的还款日
     */
    private LocalDate repaymentDate;
    /**
     * 该期还款总额
     */
    private BigDecimal repayment;
    /**
     * 该月还款本金
     */
    private BigDecimal payPrincipal;
    /**
     * 该月还款利息
     */
    private BigDecimal interest;
    /**
     * 剩余还款总金额
     */
    private BigDecimal remainTotal;
    /**
     * 剩余还款本金
     */
    private BigDecimal remainPrincipal;

    /**
     * 第几年
     */
    private int year;
    /**
     * 第几年的第几个月
     */
    private int monthInYear;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getRepayment() {
        return repayment;
    }

    public void setRepayment(BigDecimal repayment) {
        this.repayment = repayment;
    }

    public BigDecimal getPayPrincipal() {
        return payPrincipal;
    }

    public void setPayPrincipal(BigDecimal payPrincipal) {
        this.payPrincipal = payPrincipal;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getRemainTotal() {
        return remainTotal;
    }

    public void setRemainTotal(BigDecimal remainTotal) {
        this.remainTotal = remainTotal;
    }

    public BigDecimal getRemainPrincipal() {
        return remainPrincipal;
    }

    public void setRemainPrincipal(BigDecimal remainPrincipal) {
        this.remainPrincipal = remainPrincipal;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonthInYear() {
        return monthInYear;
    }

    public void setMonthInYear(int monthInYear) {
        this.monthInYear = monthInYear;
    }

    public LocalDate getRepaymentDate() {
        return repaymentDate;
    }

    public void setRepaymentDate(LocalDate repaymentDate) {
        this.repaymentDate = repaymentDate;
    }
}
