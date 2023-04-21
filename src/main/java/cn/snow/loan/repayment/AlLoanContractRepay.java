package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.github.braisdom.objsql.Databases;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TAlLoanContract;
import cn.snow.loan.dao.model.TAlLoanRepayPlan;
import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContractRepay implements ILoanContractRepay {

    private final FundingLoanContractRepay fundingLoanContractRepay;

    public AlLoanContractRepay(FundingLoanContractRepay fundingLoanContractRepay) {
        this.fundingLoanContractRepay = fundingLoanContractRepay;
    }

    @Override
    public Result initRepayPlan(ILoanContract contract) {
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                AlLoanContract alLoanContract = (AlLoanContract) contract;
                FundingLoanContract fundingLoanContract = alLoanContract.getFundingLoanContract();
                Result r = fundingLoanContractRepay.initRepayPlan(fundingLoanContract);
                if (!r.success()) {
                    throw new IllegalStateException("init funding loan contract fail");
                }
                TAlLoanContract tAlLoanContract = Optional.of(alLoanContract).map(a -> {
                    TAlLoanContract c = new TAlLoanContract();
                    c.setContractNo(alLoanContract.contractNo());
                    c.setFundingContractNo(fundingLoanContract.contractNo());
                    c.setYearRate(alLoanContract.getLoanRate().getYearRateBeforePercent());
                    c.setBreachFeeRate(alLoanContract.alLoanRate().getBreachFeeRate().getDayRateBeforePercent());
                    c.setTermLateFeeRate(alLoanContract.alLoanRate().getTermLateFeeRate().getDayRateBeforePercent());
                    c.setLoanLateFeeRate(alLoanContract.alLoanRate().getLoanLateFeeRate().getDayRateBeforePercent());
                    c.setRepayDay(alLoanContract.repayDay());
                    c.setGraceDay(alLoanContract.dayOfGrace());
                    c.setCompensationDay(alLoanContract.dayOfCompensation());
                    c.setLoanTerm(alLoanContract.getLoanTerm().getTerm());
                    c.setFirstRepayDate(alLoanContract.firstRepayDate().atStartOfDay());
                    c.setCompensationDate(null);
                    c.setLoanLateFee(BigDecimal.ZERO);
                    c.setLastRepayDate(c.getFirstRepayDate());
                    return c;
                }).orElseThrow(() -> new IllegalStateException("init al loan contract fail"));
                TAlLoanContract.create(tAlLoanContract, true);

                AlLoan alLoan = (AlLoan) alLoanContract.repayPlanTrial();
                List<LoanPerTerm> fundingLoanTerms = alLoan.getAllLoans();
                List<GuaranteeFeePerTerm> alLoanGuaranteeFeeTerms = alLoan.getAllGuaranteeFees();
                for (int i = 0; i < contract.getLoanTerm().getTerm(); i++) {
                    TAlLoanRepayPlan p = new TAlLoanRepayPlan();
                    p.setContractNo(alLoanContract.contractNo());
                    p.setLoanTerm(alLoanGuaranteeFeeTerms.get(i).getMonth());
                    p.setRepayDate(tAlLoanContract.getFirstRepayDate().plusMonths(i));
                    p.setGraceDate(p.getRepayDate().plusDays(tAlLoanContract.getGraceDay()));
                    p.setCompensationDate(p.getRepayDate().plusDays(tAlLoanContract.getCompensationDay()));
                    p.setBreachFeeRate(tAlLoanContract.getBreachFeeRate());
                    p.setTermLateFeeRate(tAlLoanContract.getTermLateFeeRate());
                    p.setLoanLateFeeRate(tAlLoanContract.getLoanLateFeeRate());
                    p.setPrincipal(fundingLoanTerms.get(i).getPayPrincipal());
                    p.setInterest(fundingLoanTerms.get(i).getInterest());
                    p.setOverdueFee(BigDecimal.ZERO);
                    p.setGuaranteeFee(alLoanGuaranteeFeeTerms.get(i).getGuaranteeFee());
                    p.setBreachFee(BigDecimal.ZERO);
                    p.setTermLateFee(BigDecimal.ZERO);
                    p.setLastRepayDate(p.getRepayDate());
                    p.setLoanTermStatus("n");
                    TAlLoanRepayPlan.create(p, true);
                }
                return Result.success("create new al contract success");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("new al contract fail", e);
        }
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
