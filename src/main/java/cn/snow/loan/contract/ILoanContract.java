package cn.snow.loan.contract;

import java.time.LocalDate;

import cn.snow.loan.plan.funding.ILoanCalculator;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public interface ILoanContract {

    String contractNo();

    void contractNo(String contractNo);

    Loan repayPlanTrial();

    ILoanCalculator getLoanPlanCalculator();

    LoanAmount getLoanAmount();

    LoanRate getLoanRate();

    LoanTerm getLoanTerm();

    int repayDay();

    LocalDate firstRepayDate();

    int dayOfGrace();

}
