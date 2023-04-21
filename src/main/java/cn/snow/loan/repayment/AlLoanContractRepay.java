package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContractRepay implements ILoanContractRepay{
    @Override
    public Result initRepayPlan(ILoanContract contract) {
        return null;
    }

    @Override
    public List<?> preRepayTrail(String contractNo, LocalDateTime repayDateTime) {
        return null;
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        return null;
    }

    @Override
    public Result termCompensation(String contractNo, LoanTerm term, LocalDateTime compensationDateTime) {
        return null;
    }

    @Override
    public Result loanCompensation(String contractNo, LocalDateTime compensationDateTime) {
        return null;
    }
}
