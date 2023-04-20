package cn.snow.loan.plan.funding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.snow.loan.plan.funding.prepare.LoanRate;

/**
 * 等额本金还款法
 * 计算公式 月还款额=贷款本金/还款月数+(贷款本金-已还本金累计额)*月利率
 * Design it on 1/23/16.
 */
public class ACMLoanCalculator implements ILoanCalculator {

    @Override
    public Loan calLoan(BigDecimal totalLoanMoney, int totalMonth, double loanRate, int rateType) {
        Loan loan = new Loan();
        //月利率=年利率除以12
        BigDecimal loanRateMonth = rateType == LoanRate.RATE_TYPE_YEAR ? BigDecimal.valueOf(loanRate / 100 / 12) : BigDecimal.valueOf(loanRate / 100);
        loan.setTotalMonth(totalMonth);
        loan.setTotalLoanMoney(totalLoanMoney);
        //每月还款本金数是固定的，贷款总额/贷款期数
        BigDecimal payPrincipal = totalLoanMoney.divide(new BigDecimal(totalMonth), 2, BigDecimal.ROUND_HALF_UP);
        //累积所还本金
        BigDecimal totalPayedPrincipal = BigDecimal.ZERO;
        //累积利息
        BigDecimal totalInterest = BigDecimal.ZERO;
        //累积已还款总数
        BigDecimal totalRepayment = BigDecimal.ZERO;
        List<LoanPerTerm> loanPerTermList = new ArrayList<>();
        int year = 0;
        int monthInYear = 0;
        for (int i = 0; i < totalMonth; i++) {
            LoanPerTerm loanPerTerm = new LoanPerTerm();
            loanPerTerm.setMonth(i + 1);
            loanPerTerm.setYear(year + 1);
            loanPerTerm.setMonthInYear(++monthInYear);
            if ((i + 1) % 12 == 0) {
                year++;
                monthInYear = 0;
            }
            totalPayedPrincipal = totalPayedPrincipal.add(payPrincipal);
            loanPerTerm.setPayPrincipal(payPrincipal);
            BigDecimal interest = totalLoanMoney.subtract(totalPayedPrincipal).multiply(loanRateMonth).setScale(2, BigDecimal.ROUND_HALF_UP);
            loanPerTerm.setInterest(interest);
            totalInterest = totalInterest.add(interest);
            loanPerTerm.setRepayment(payPrincipal.add(interest));
            if (i == 0) {
                loan.setFirstRepayment(loanPerTerm.getRepayment());
            }
            totalRepayment = totalRepayment.add(loanPerTerm.getRepayment());
            loanPerTerm.setRemainPrincipal(totalLoanMoney.subtract(totalPayedPrincipal));
            loanPerTermList.add(loanPerTerm);
        }
        loan.setTotalRepayment(totalRepayment);
        loan.setAvgRepayment(totalRepayment.divide(new BigDecimal(totalMonth), 2, BigDecimal.ROUND_HALF_UP));
        loan.setTotalInterest(totalInterest);
        BigDecimal totalPayedRepayment = BigDecimal.ZERO;
        for (LoanPerTerm loanPerTerm : loanPerTermList) {
            totalPayedRepayment = totalPayedRepayment.add(loanPerTerm.getRepayment());
            loanPerTerm.setRemainTotal(totalRepayment.subtract(totalPayedRepayment));
        }
        loan.setAllLoans(loanPerTermList);
        return loan;
    }

}
