package cn.snow.loan.plan.al;

import java.math.BigDecimal;
import java.util.List;

import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.LoanPerTerm;

public class AlLoan extends Loan {
    /**
     * 需还款总利息金额
     */
    private BigDecimal totalGuaranteeFee;

    private List<GuaranteeFeePerTerm> allGuaranteeFees;

    public BigDecimal getTotalGuaranteeFee() {
        return totalGuaranteeFee;
    }

    public void setTotalGuaranteeFee(BigDecimal totalGuaranteeFee) {
        this.totalGuaranteeFee = totalGuaranteeFee;
    }

    public List<GuaranteeFeePerTerm> getAllGuaranteeFees() {
        return allGuaranteeFees;
    }

    public void setAllGuaranteeFees(List<GuaranteeFeePerTerm> allGuaranteeFees) {
        this.allGuaranteeFees = allGuaranteeFees;
    }

    @Override
    public String toString() {
        StringBuilder allLoansStr = new StringBuilder();
        if (getAllLoans() != null) {
            List<LoanPerTerm> allLoans = getAllLoans();
            List<GuaranteeFeePerTerm> allGuaranteeFeeL = getAllGuaranteeFees();
            int allLoansLength = allLoans.size();
            for (int i = 0; i < allLoansLength; i++) {
                LoanPerTerm loanPerTerm = allLoans.get(i);
                GuaranteeFeePerTerm guaranteeFeePerTerm = allGuaranteeFeeL.get(i);

                String lbmStr = String.format("期数:%02d 第%02d年 第%02d月 月供:%-8s 本金:%-8s 利息:%-8s 担保费:%-8s 剩余贷款:%-8s 上期结余未还本金:%-8s",
                        loanPerTerm.getMonth(),
                        loanPerTerm.getYear(),
                        loanPerTerm.getMonthInYear(),
                        loanPerTerm.getRepayment(),
                        loanPerTerm.getPayPrincipal(),
                        loanPerTerm.getInterest(),
                        guaranteeFeePerTerm.getGuaranteeFee(),
                        loanPerTerm.getRemainTotal(),
                        loanPerTerm.getRemainPrincipal());
                if (allLoansStr.length() == 0) {
                    allLoansStr.append(lbmStr);
                } else {
                    allLoansStr.append("\n").append(lbmStr);
                }
            }
        }
        return "每月平均还款: " + getAvgRepayment() + "\t" +
                "总利息: " + getTotalInterest() + "\t" +
                "总担保费: " + getTotalGuaranteeFee() + "\t" +
                "还款总额：" + getTotalRepayment() + "\t" +
                "首月还款: " + getFirstRepayment() + "\t" +
                "\n" +
                allLoansStr;
    }
}
