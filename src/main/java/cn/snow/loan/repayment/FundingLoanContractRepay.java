package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.braisdom.objsql.Databases;
import com.googlecode.aviator.AviatorEvaluator;

import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TFundingLoanContract;
import cn.snow.loan.dao.model.TFundingLoanRepayPlan;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.LoanTerm;

@SuppressWarnings("all")
public class FundingLoanContractRepay implements ILoanContractRepay {
    @Override
    public Result initRepayPlan(ILoanContract contract) {
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                FundingLoanContract fundingLoanContract = (FundingLoanContract) contract;
                TFundingLoanContract tFundingLoanContract = new TFundingLoanContract();
                //tFundingLoanContract.setId(System.nanoTime());
                tFundingLoanContract.setContractNo(fundingLoanContract.contractNo());
                tFundingLoanContract.setYearRate(fundingLoanContract.getLoanRate().getYearRateBeforePercent());
                tFundingLoanContract.setOverdueFeeRate(fundingLoanContract.overdueFeeRate().getDayRateBeforePercent());
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
            throw new IllegalStateException("new contract fail", e);
        }
    }

    @Override
    public Result setOverdueFlag(String contractNo, LocalDateTime checkDateTime) {
        throw new UnsupportedOperationException("no overdue flag");
    }

    @Override
    public List<TFundingLoanRepayPlan> preRepayTrail(String contractNo, LocalDateTime repayDateTime) {

        List<TFundingLoanRepayPlan> ss = Collections.emptyList();
        try {
            ss = TFundingLoanRepayPlan.query("contract_no = ?", contractNo);
        } catch (SQLException throwables) {
            throw new IllegalStateException("query by contractNo fail", throwables);
        }
        return ss.stream().filter(a -> repayDateTime.isAfter(a.getRepayDate())).peek(b -> {
            //当天不算,所以要减掉一天。计算罚息天数，得从上次还款日开始计算。
            long daysOfOverdueFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
            //超过了宽限期，且需计算罚息的天数大于0时才开始计算增量罚息
            if (repayDateTime.isAfter(b.getGraceDate()) && daysOfOverdueFee > 0) {

                Map<String, Object> env = new HashMap<>();
                env.put("principal", b.getPrincipal());
                env.put("overdueFeeRate", b.getOverdueFeeRate());
                env.put("daysOfOverdue", daysOfOverdueFee);
                env.put("overdueFee", b.getOverdueFee());

                b.setOverdueFee((BigDecimal) AviatorEvaluator.execute("overdueFee+(principal*overdueFeeRate/100*daysOfOverdue)", env));
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        try {
            List<TFundingLoanRepayPlan> ss = preRepayTrail(contractNo, repayDateTime);
            BigDecimal preBalance = repayAmount;
            for (TFundingLoanRepayPlan p : ss) {
                preBalance = consumeRepayAmountPerTermReturnBalance(repayDateTime, preBalance, p);
                //付款余额用完就直接跳出循环，不再继续平下一期的账了
                if (preBalance == null) {
                    break;
                }
            }
        } catch (SQLException throwables) {
            throw new IllegalStateException("repay fail", throwables);
        }
        return Result.success("repay success");
    }

    private BigDecimal consumeRepayAmountPerTermReturnBalance(LocalDateTime repayDateTime, BigDecimal repayAmountBalance, TFundingLoanRepayPlan p) throws SQLException {
        p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
        //1.罚息
        BigDecimal overdueFee = p.getOverdueFee();
        repayAmountBalance = repayAmountBalance.subtract(overdueFee);
        if (repayAmountBalance.compareTo(BigDecimal.ZERO) >= 0) {
            p.setOverdueFee(BigDecimal.ZERO);
        } else {
            p.setOverdueFee(new BigDecimal("-1").multiply(repayAmountBalance));
            TFundingLoanRepayPlan.update(p.getId(), p, true);
            return null;
        }
        //2.利息
        BigDecimal interest = p.getInterest();
        repayAmountBalance = repayAmountBalance.subtract(interest);
        if (repayAmountBalance.compareTo(BigDecimal.ZERO) >= 0) {
            p.setInterest(BigDecimal.ZERO);
        } else {
            p.setInterest(new BigDecimal("-1").multiply(repayAmountBalance));
            TFundingLoanRepayPlan.update(p.getId(), p, true);
            return null;
        }
        //3.本金
        BigDecimal principal = p.getPrincipal();
        repayAmountBalance = repayAmountBalance.subtract(principal);
        if (repayAmountBalance.compareTo(BigDecimal.ZERO) >= 0) {
            p.setPrincipal(BigDecimal.ZERO);
        } else {
            p.setPrincipal(new BigDecimal("-1").multiply(repayAmountBalance));
            TFundingLoanRepayPlan.update(p.getId(), p, true);
            return null;
        }
        TFundingLoanRepayPlan.update(p.getId(), p, true);
        return repayAmountBalance;
    }

    @Override
    public Result termCompensation(String contractNo, LoanTerm term, LocalDateTime compensationDateTime) {
        throw new UnsupportedOperationException("funding loan cannot support this operation");
    }

    @Override
    public Result loanCompensation(String contractNo, LocalDateTime compensationDateTime) {
        throw new UnsupportedOperationException("funding loan cannot support this operation");
    }
}
