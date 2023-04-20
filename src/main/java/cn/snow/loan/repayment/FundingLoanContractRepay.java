package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.github.braisdom.objsql.Databases;

import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TFundingLoanContract;
import cn.snow.loan.dao.model.TFundingLoanRepayPlan;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class FundingLoanContractRepay implements ILoanContractRepay {
    @Override
    public Result initRepayPlan(ILoanContract contract) {
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                FundingLoanContract fundingLoanContract = (FundingLoanContract) contract;
                TFundingLoanContract tFundingLoanContract = new TFundingLoanContract();
                //tFundingLoanContract.setId(System.nanoTime());
                tFundingLoanContract.setContractNo("F" + System.nanoTime());
                tFundingLoanContract.setYearRate(fundingLoanContract.getLoanRate().getYearRate());
                tFundingLoanContract.setOverdueFeeRate(fundingLoanContract.getLoanRate().getOverdueFeeDayRate());
                tFundingLoanContract.setRepayDay(fundingLoanContract.repayDay());
                tFundingLoanContract.setGraceDay(fundingLoanContract.dayOfGrace());
                tFundingLoanContract.setLoanTerm(fundingLoanContract.getLoanTerm().getTerm());
                tFundingLoanContract.setFirstRepayDate(fundingLoanContract.firstRepayDate().atStartOfDay());

                TFundingLoanContract.create(tFundingLoanContract, true);

                Loan loanPlan = fundingLoanContract.repayPlanTrial();
                for (int i = 0; i < contract.getLoanTerm().getTerm(); i++) {
                    LoanPerTerm loanPerTerm = loanPlan.getAllLoans().get(i);
                    TFundingLoanRepayPlan t = new TFundingLoanRepayPlan();
                    //t.setId(0L);
                    t.setContractNo(tFundingLoanContract.getContractNo());
                    t.setLoanTerm(loanPerTerm.getMonth());
                    t.setRepayDate(tFundingLoanContract.getFirstRepayDate().plusMonths(i));
                    t.setGraceDate(t.getRepayDate().plusDays(tFundingLoanContract.getGraceDay()));
                    t.setOverdueFeeRate(tFundingLoanContract.getOverdueFeeRate());
                    t.setPrincipal(loanPerTerm.getPayPrincipal());
                    t.setInterest(loanPerTerm.getInterest());
                    t.setOverdueFee(new BigDecimal("0"));
                    t.setLastRepayDate(t.getRepayDate());
                    TFundingLoanRepayPlan.create(t, true);
                }
                return Result.success("create contract success");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("", e);
        }
    }

    public List<TFundingLoanRepayPlan> preRepayTrail(String contractNo, LocalDateTime repayDateTime) throws SQLException {
        List<TFundingLoanRepayPlan> ss = TFundingLoanRepayPlan.query("contract_no = ?", contractNo);
        return ss.stream().filter(a -> repayDateTime.isAfter(a.getRepayDate())).map(b -> {
            //当天不算
            long daysOfOverdueFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays() - 1;
            if (repayDateTime.isAfter(b.getGraceDate()) && daysOfOverdueFee > 0) {
                b.setOverdueFee(b.getPrincipal()
                        .multiply(b.getOverdueFeeRate())
                        .multiply(BigDecimal.valueOf(daysOfOverdueFee)));
            }
            return b;
        }).collect(Collectors.toList());
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        try {
            List<TFundingLoanRepayPlan> ss = preRepayTrail(contractNo, repayDateTime);
            BigDecimal preBalance = repayAmount;
            for (TFundingLoanRepayPlan p : ss) {
                //1.罚息
                BigDecimal overdueFee = p.getOverdueFee();
                preBalance = preBalance.subtract(overdueFee);
                if (preBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    p.setOverdueFee(BigDecimal.ZERO);
                } else {
                    p.setOverdueFee(new BigDecimal("-1").multiply(preBalance));
                    p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
                    TFundingLoanRepayPlan.update(p.getId(), p, true);
                    break;
                }
                //2.利息
                BigDecimal interest = p.getInterest();
                preBalance = preBalance.subtract(interest);
                if (preBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    p.setInterest(BigDecimal.ZERO);
                } else {
                    p.setInterest(new BigDecimal("-1").multiply(preBalance));
                    p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
                    TFundingLoanRepayPlan.update(p.getId(), p, true);
                    break;
                }
                //3.本金
                BigDecimal principal = p.getPrincipal();
                preBalance = preBalance.subtract(principal);
                if (preBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    p.setPrincipal(BigDecimal.ZERO);
                } else {
                    p.setPrincipal(new BigDecimal("-1").multiply(preBalance));
                    p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
                    TFundingLoanRepayPlan.update(p.getId(), p, true);
                    break;
                }

                p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
                TFundingLoanRepayPlan.update(p.getId(), p, true);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Result.success("aaa");
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
