package cn.snow.loan.contract;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

import cn.snow.loan.plan.funding.ACPIMLoanCalculator;
import cn.snow.loan.plan.funding.ILoanCalculator;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class FundingLoanContract implements ILoanContract {

    private static final ILoanCalculator LOAN_PLAN_CAL = new ACPIMLoanCalculator();

    private final LoanAmount loanAmount;
    private final LoanRate loanRate;
    private final LoanTerm loanTerm;
    private final LoanRate overdueFeeRate;

    private int repayDay;
    private LocalDate firstRepayDate;
    private int dayOfGrace;

    private FundingLoanContract() {
        throw new UnsupportedOperationException("");
    }

    public FundingLoanContract(LoanAmount loanAmount, LoanTerm loanTerm, LoanRate loanRate, LoanRate overdueFeeRate) {
        this.loanAmount = loanAmount;
        this.loanRate = loanRate;
        this.loanTerm = loanTerm;
        this.overdueFeeRate = overdueFeeRate;
        repayDay = 3;
        dayOfGrace = 5;
        firstRepayDate = LocalDate.of(2022, 1, repayDay());
    }

    private String contractNo;

    @Override
    public String contractNo() {
        return contractNo;
    }

    @Override
    public void contractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    @Override
    public Loan repayPlanTrial() {
        return LOAN_PLAN_CAL.repaymentPlan(loanAmount, loanTerm, loanRate);
    }

    @Override
    public ILoanCalculator getLoanPlanCalculator() {
        return LOAN_PLAN_CAL;
    }

    @Override
    public LoanAmount getLoanAmount() {
        return loanAmount;
    }

    @Override
    public LoanRate getLoanRate() {
        return loanRate;
    }

    @Override
    public LoanTerm getLoanTerm() {
        return loanTerm;
    }

    @Override
    public int repayDay() {
        return repayDay;
    }

    @Override
    public LocalDate firstRepayDate() {
        return firstRepayDate.with(ChronoField.DAY_OF_MONTH, repayDay);
    }

    @Override
    public int dayOfGrace() {
        return dayOfGrace;
    }


    public void setRepayDay(int repayDay) {
        this.repayDay = repayDay;
    }

    public void setFirstRepayDate(LocalDate firstRepayDate) {
        this.firstRepayDate = firstRepayDate;
    }

    public void setDayOfGrace(int dayOfGrace) {
        this.dayOfGrace = dayOfGrace;
    }

    public LoanRate overdueFeeRate(){
        return overdueFeeRate;
    }

}
