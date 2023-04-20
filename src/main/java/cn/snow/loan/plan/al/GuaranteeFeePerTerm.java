package cn.snow.loan.plan.al;

import java.math.BigDecimal;

public class GuaranteeFeePerTerm {

    /**
     * 第几期
     */
    private int month;

    private BigDecimal guaranteeFee;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getGuaranteeFee() {
        return guaranteeFee;
    }

    public void setGuaranteeFee(BigDecimal guaranteeFee) {
        this.guaranteeFee = guaranteeFee;
    }
}
