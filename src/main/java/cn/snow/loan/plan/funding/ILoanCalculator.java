package cn.snow.loan.plan.funding;

import java.math.BigDecimal;

import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

/**
 * Design it on 1/14/16.
 */
public interface ILoanCalculator {

    /**
     * 贷款计算
     *
     * @param totalLoanMoney 总贷款额
     * @param totalMonth 还款月数
     * @param loanRate 贷款利率
     * @param rateType 可选择年利率或月利率
     * @return
     */
    Loan calLoan(BigDecimal totalLoanMoney, int totalMonth, double loanRate, int rateType);

    default Loan repaymentPlan(LoanAmount loanAmount, LoanTerm loanTerm, LoanRate loanRate){
        return calLoan(loanAmount.getLoanAmountYuan(), loanTerm.getTerm(), loanRate.getYearRateBeforePercent().doubleValue(), LoanRate.RATE_TYPE_YEAR);
    }

}
