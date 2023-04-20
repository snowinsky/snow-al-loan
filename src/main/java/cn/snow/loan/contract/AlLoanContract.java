package cn.snow.loan.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.al.prepare.AlLoanRate;
import cn.snow.loan.plan.funding.ILoanCalculator;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContract implements ILoanContract {

    private final FundingLoanContract loanContract;
    private final AlLoanRate alLoanRate;

    private AlLoanContract() {
        throw new UnsupportedOperationException("");
    }

    public AlLoanContract(FundingLoanContract loanContract, AlLoanRate alLoanRate) {
        this.loanContract = loanContract;
        this.alLoanRate = alLoanRate;
    }


    @Override
    public String contractNo() {
        return null;
    }

    @Override
    public Loan repayPlanTrial() {
        Loan bankLoan = loanContract.repayPlanTrial();
        Loan alLoan = loanContract.getLoanPlanCalculator().repaymentPlan(getLoanAmount(), getLoanTerm(), getLoanRate());

        List<LoanPerTerm> alLoanPerTermList = alLoan.getAllLoans();
        List<GuaranteeFeePerTerm> alGFeePerTermList = new ArrayList<>();
        for (int i = 0; i < alLoan.getTotalMonth(); i++) {
            LoanPerTerm alLoanPerTerm = alLoanPerTermList.get(i);
            LoanPerTerm bankLoanPerTerm = bankLoan.getAllLoans().get(i);
            alLoanPerTerm.setPayPrincipal(bankLoanPerTerm.getPayPrincipal());
            alLoanPerTerm.setInterest(bankLoanPerTerm.getInterest());
            alLoanPerTerm.setRemainPrincipal(bankLoanPerTerm.getRemainPrincipal());

            GuaranteeFeePerTerm gfeePerTerm = new GuaranteeFeePerTerm();
            gfeePerTerm.setMonth(alLoanPerTerm.getMonth());
            gfeePerTerm.setGuaranteeFee(alLoanPerTerm.getRepayment().subtract(bankLoanPerTerm.getRepayment()));
            alGFeePerTermList.add(gfeePerTerm);
        }

        AlLoan outLoan = new AlLoan();
        outLoan.setTotalGuaranteeFee(alGFeePerTermList.stream().map(GuaranteeFeePerTerm::getGuaranteeFee).reduce(BigDecimal.ZERO, BigDecimal::add));
        outLoan.setAllGuaranteeFees(alGFeePerTermList);
        outLoan.setTotalLoanMoney(alLoan.getTotalLoanMoney());
        outLoan.setTotalMonth(alLoan.getTotalMonth());
        outLoan.setLoanRate(alLoan.getLoanRate());
        outLoan.setTotalInterest(bankLoan.getTotalInterest());
        outLoan.setTotalRepayment(alLoan.getTotalRepayment());
        outLoan.setFirstRepayment(alLoan.getFirstRepayment());
        outLoan.setAvgRepayment(alLoan.getAvgRepayment());
        outLoan.setAllLoans(alLoanPerTermList);

        return outLoan;
    }

    @Override
    public ILoanCalculator getLoanPlanCalculator() {
        return loanContract.getLoanPlanCalculator();
    }

    @Override
    public LoanAmount getLoanAmount() {
        return loanContract.getLoanAmount();
    }

    @Override
    public LoanRate getLoanRate() {
        return this.alLoanRate.getYearRate();
    }

    @Override
    public LoanTerm getLoanTerm() {
        return loanContract.getLoanTerm();
    }

    @Override
    public int repayDay() {
        return loanContract.repayDay() - 1;
    }

    @Override
    public LocalDate firstRepayDate() {
        return loanContract.firstRepayDate().minusDays(1);
    }

    @Override
    public int dayOfGrace() {
        return loanContract.dayOfGrace();
    }

    @Override
    public int dayOfCompensation() {
        return loanContract.dayOfCompensation();
    }
}