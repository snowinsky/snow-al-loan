package cn.snow.loan.plan.funding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.snow.loan.plan.funding.prepare.LoanRate;

/**
 * 等额本息还款法
 * 等额本息计算公式：每月还款额=贷款本金×[月利率×(1+月利率)^还款月数]÷{[(1+月利率)^还款月数]-1}
 * ^表示n次方
 * Design it on 1/23/16.
 */
public class ACPIMLoanCalculator implements ILoanCalculator {

    @Override
    public Loan calLoan(BigDecimal totalLoanMoney, int totalMonth, double loanRate, int rateType) {
        Loan loan = new Loan();
        //月利率，就是年利率除以12
        BigDecimal loanRateMonth = LoanRate.yearRateBeforePercent(loanRate).getMonthRate();
        //获取公式中的(1+月利率)^还款月数
        BigDecimal factor = BigDecimal.valueOf(1 + loanRateMonth.doubleValue()).pow(totalMonth);
        //月平均还款额
        BigDecimal avgRepayment = totalLoanMoney.multiply(loanRateMonth).multiply(factor).divide(factor.subtract(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
        //年利率
        loan.setLoanRate(loanRate);
        //总贷款金额
        loan.setTotalLoanMoney(totalLoanMoney);
        //还款期数
        loan.setTotalMonth(totalMonth);
        //每月还款金额
        loan.setAvgRepayment(avgRepayment);
        //总还款额
        loan.setTotalRepayment(avgRepayment.multiply(new BigDecimal(totalMonth)));
        //首笔还款额
        loan.setFirstRepayment(avgRepayment);

        BigDecimal totalPayedPrincipal = BigDecimal.ZERO;//累积所还本金
        BigDecimal totalInterest = BigDecimal.ZERO; //总利息
        BigDecimal totalRepayment = BigDecimal.ZERO; // 已还款总数
        List<LoanPerTerm> loanPerTermList = new ArrayList<>();
        int year = 0;
        int monthInYear = 0;
        for (int i = 0; i < totalMonth; i++) {
            LoanPerTerm loanPerTerm = new LoanPerTerm();
            //上期结余贷款本金=贷款总金额-已经还了的本金总金额
            BigDecimal remainPrincipal = totalLoanMoney.subtract(totalPayedPrincipal);
            //当期利息=剩余本金总金额*月利率
            BigDecimal interest = remainPrincipal.multiply(loanRateMonth).setScale(2, BigDecimal.ROUND_HALF_UP);
            //总利息逐期汇总
            totalInterest = totalInterest.add(interest);
            //当期本金=月平均还款-当期利息
            BigDecimal principal = loan.getAvgRepayment().subtract(interest);
            //总本金逐期汇总
            totalPayedPrincipal = totalPayedPrincipal.add(principal);
            //期数
            loanPerTerm.setMonth(i + 1);
            //第几年
            loanPerTerm.setYear(year + 1);
            //某一年的第几月
            loanPerTerm.setMonthInYear(++monthInYear);
            if ((i + 1) % 12 == 0) {
                year++;
                monthInYear = 0;
            }
            //当月利息
            loanPerTerm.setInterest(interest);
            //当月本金
            loanPerTerm.setPayPrincipal(principal);
            //当月月供 当月需还款总额
            loanPerTerm.setRepayment(loan.getAvgRepayment());
            //总还款额汇总
            totalRepayment = totalRepayment.add(loanPerTerm.getRepayment());
            //剩余本金
            loanPerTerm.setRemainPrincipal(remainPrincipal);
            //剩余需还款=总还款金额-已还款额
            loanPerTerm.setRemainTotal(loan.getTotalRepayment().subtract(totalRepayment));
            //当月还款计划
            loanPerTermList.add(loanPerTerm);
        }
        //还款总利息
        loan.setTotalInterest(totalInterest);
        loan.setAllLoans(loanPerTermList);
        return loan;
    }

}
