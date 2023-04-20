package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public interface ILoanContractRepay {

    Result initRepayPlan(ILoanContract contract);

    Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount);

    Result termCompensation(String contractNo, LoanTerm term, LocalDateTime compensationDateTime);

    Result loanCompensation(String contractNo, LocalDateTime compensationDateTime);
}
