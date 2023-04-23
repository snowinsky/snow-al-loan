package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.braisdom.objsql.Databases;
import com.googlecode.aviator.AviatorEvaluator;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TAlLoanContract;
import cn.snow.loan.dao.model.TAlLoanRepayPlan;
import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanTerm;
import cn.snow.loan.repayment.BalanceMgmt.ConsumeResult;

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
                    c.setOverdueFeeRate(fundingLoanContract.overdueFeeRate().getDayRateBeforePercent());
                    c.setBreachFeeRate(alLoanContract.alLoanRate().getBreachFeeRate().getDayRateBeforePercent());
                    c.setTermLateFeeRate(alLoanContract.alLoanRate().getTermLateFeeRate().getDayRateBeforePercent());
                    c.setLoanLateFeeRate(alLoanContract.alLoanRate().getLoanLateFeeRate().getDayRateBeforePercent());
                    c.setRepayDay(alLoanContract.repayDay());
                    c.setGraceDay(alLoanContract.dayOfGrace());
                    c.setCompensationDay(alLoanContract.dayOfCompensation());
                    c.setLoanTerm(alLoanContract.getLoanTerm().getTerm());
                    c.setFirstRepayDate(alLoanContract.firstRepayDate().atStartOfDay());
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
                    p.setOverdueFeeRate(tAlLoanContract.getOverdueFeeRate());
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
                    p.setOverdueFlag(0);
                    p.setLoanTermStatus("u");
                    p.setCompPrincipal(BigDecimal.ZERO);
                    p.setCompInterest(BigDecimal.ZERO);
                    p.setCompOverdueFee(BigDecimal.ZERO);
                    p.setCompLoanDate(null);
                    p.setCompTermDate(null);
                    p.setCompTermLateFee(BigDecimal.ZERO);
                    p.setCompGuaranteeFee(BigDecimal.ZERO);
                    p.setCompBreachFee(BigDecimal.ZERO);
                    p.setCompAmt(BigDecimal.ZERO);
                    p.setLoanLateFee(BigDecimal.ZERO);
                    TAlLoanRepayPlan.create(p, true);
                }
                return Result.success("create new al contract success");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("new al contract fail", e);
        }
    }

    @Override
    public Result setOverdueFlag(String contractNo, LocalDateTime checkDateTime) {
        try {
            List<TAlLoanRepayPlan> ss = TAlLoanRepayPlan.query("contract_no = ?", contractNo);
            ss.stream().filter(a -> checkDateTime.isAfter(a.getRepayDate())).forEach(s -> {
                long durationDay = Duration.between(s.getRepayDate(), checkDateTime).toDays();
                if (s.getOverdueFlag() == 0 && durationDay > 0 && s.getPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                    s.setOverdueFlag(1);
                    try {
                        TAlLoanRepayPlan.update(s.getId(), s, true);
                    } catch (SQLException e) {
                        throw new IllegalStateException("set overdue flag fail");
                    }
                }
            });
            return Result.success("set overdue flag complete");
        } catch (SQLException e) {
            throw new IllegalStateException("query by contractNo fail", e);
        }
    }

    @Override
    public List<TAlLoanRepayPlan> preRepayTrail(String contractNo, LocalDateTime repayDateTime) {
        setOverdueFlag(contractNo, repayDateTime);
        List<TAlLoanRepayPlan> ss;
        try {
            ss = TAlLoanRepayPlan.query("contract_no = ?", contractNo);
        } catch (SQLException e) {
            throw new IllegalStateException("query by contractNo fail", e);
        }
        if (isWholeLoanCompensation(ss)) {
            return ss.stream().peek(b -> {
                //计算整笔贷款的贷款滞纳金
                if (b.getCompLoanDate() != null) {
                    long daysOfLoanLateFee = Duration.between(b.getCompLoanDate(), repayDateTime).toDays();
                    if (repayDateTime.isAfter(b.getCompLoanDate()) && daysOfLoanLateFee > 0) {
                        Map<String, Object> env = new HashMap<>();
                        env.put("compPrincipal", b.getCompPrincipal());
                        env.put("compInterest", b.getCompInterest());
                        env.put("compOverdueFee", b.getCompOverdueFee());
                        env.put("compTermLateFee", b.getTermLateFee());
                        env.put("compGuaranteeFee", b.getCompGuaranteeFee());
                        env.put("compBreachFee", b.getCompBreachFee());

                        BigDecimal compAmt = (BigDecimal) AviatorEvaluator.execute("compPrincipal+compInterest+compOverdueFee+compTermLateFee+compGuaranteeFee+compBreachFee", env);

                        env.put("compAmt", compAmt);
                        env.put("daysOfLoanLateFee", daysOfLoanLateFee);
                        env.put("loanLateFeeRate", b.getLoanLateFeeRate());
                        env.put("loanLateFee", b.getLoanLateFee());

                        b.setCompAmt(compAmt);
                        b.setLoanLateFee((BigDecimal) AviatorEvaluator.execute("loanLateFee+(compAmt*loanLateFeeRate/100*daysOfLoanLateFee)", env));
                    }
                }
            }).collect(Collectors.toList());
        }
        return ss.stream().filter(a -> repayDateTime.isAfter(a.getRepayDate())).peek(b -> {
            //计算期款滞纳金
            if (b.getCompTermDate() != null) {
                long daysOfTermLateFee = Duration.between(b.getCompTermDate(), repayDateTime).toDays();
                if (repayDateTime.isAfter(b.getCompTermDate()) && daysOfTermLateFee > 0) {
                    Map<String, Object> env = new HashMap<>();
                    env.put("compPrincipal", b.getCompPrincipal());
                    env.put("daysOfTermLateFee", daysOfTermLateFee);
                    env.put("termLateFeeRate", b.getTermLateFeeRate());
                    env.put("termLateFee", b.getTermLateFee());
                    b.setTermLateFee((BigDecimal) AviatorEvaluator.execute("termLateFee+(compPrincipal*termLateFeeRate/100*daysOfTermLateFee)", env));
                }
            } else {
                //计算罚息和违约金，都是宽限期内不算，超出宽限期直接从还款日开始算
                long daysOfOverdueFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
                if (repayDateTime.isAfter(b.getGraceDate()) && daysOfOverdueFee > 0) {

                    Map<String, Object> env = new HashMap<>();
                    env.put("principal", b.getPrincipal());
                    env.put("daysOfOverdue", daysOfOverdueFee);
                    env.put("overdueFeeRate", b.getOverdueFeeRate());
                    env.put("breachFeeRate", b.getBreachFeeRate());
                    env.put("breachFee", b.getBreachFee());
                    env.put("overdueFee", b.getOverdueFee());

                    b.setOverdueFee(getOverdueFeeBalance(env));
                    b.setBreachFee((BigDecimal) AviatorEvaluator.execute("breachFee+(principal*breachFeeRate/100*daysOfOverdue)", env));
                }
            }

        }).collect(Collectors.toList());
    }

    private boolean isWholeLoanCompensation(List<TAlLoanRepayPlan> ss) {
        return ss.stream().anyMatch(a -> a.getLoanTermStatus().equals("l"));
    }

    /**
     * 从银行获取未还罚息的余额
     * 当前实现方式为自己计算，好处就是比银行的速度快，因为银行每天上午8点才出最新的还款计划，影响多批次代扣效率
     *
     * @param env
     * @return
     */
    private BigDecimal getOverdueFeeBalance(Map<String, Object> env) {
        Object r = AviatorEvaluator.execute("overdueFee+scale(principal*overdueFeeRate/100*daysOfOverdue, 2, 4)", env);
        return (BigDecimal) r;
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        try {
            List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, repayDateTime);
            BalanceMgmt bm = new BalanceMgmt(repayAmount);
            for (TAlLoanRepayPlan p : ss) {
                if (consumeRepayAmountPerTermReturnBalance(repayDateTime, bm, p)) {
                    break;
                }
            }
        } catch (SQLException throwables) {
            throw new IllegalStateException("repay fail", throwables);
        }
        return Result.success("repay success");
    }


    private boolean consumeRepayAmountPerTermReturnBalance(LocalDateTime repayDateTime, BalanceMgmt preBalance, TAlLoanRepayPlan p) throws SQLException {
        p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
        //p.getOverdueFee(); 罚息
        ConsumeResult cr = preBalance.consumeBalance(p.getOverdueFee());
        p.setOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getInterest(); 利息
        cr = preBalance.consumeBalance(p.getInterest());
        p.setInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getPrincipal(); 本金
        cr = preBalance.consumeBalance(p.getPrincipal());
        p.setPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getLoanLateFee(); 贷款滞纳金
        cr = preBalance.consumeBalance(p.getLoanLateFee());
        p.setLoanLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getTermLateFee(); 期款滞纳金
        cr = preBalance.consumeBalance(p.getTermLateFee());
        p.setTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompOverdueFee(); 代偿罚息
        cr = preBalance.consumeBalance(p.getCompOverdueFee());
        p.setCompOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompInterest(); 代偿利息
        cr = preBalance.consumeBalance(p.getCompInterest());
        p.setCompInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getBreachFee(); 违约金
        cr = preBalance.consumeBalance(p.getBreachFee());
        p.setBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getGuaranteeFee(); 担保费
        cr = preBalance.consumeBalance(p.getGuaranteeFee());
        p.setGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompBreachFee(); 代偿违约金
        cr = preBalance.consumeBalance(p.getCompBreachFee());
        p.setCompBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompGuaranteeFee(); 代偿担保费
        cr = preBalance.consumeBalance(p.getCompGuaranteeFee());
        p.setCompGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompPrincipal(); 代偿本金
        cr = preBalance.consumeBalance(p.getCompPrincipal());
        p.setCompPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        TAlLoanRepayPlan.update(p.getId(), p, true);
        return false;
    }

    @Override
    public Result termCompensation(String contractNo, LoanTerm term, LocalDateTime compensationDateTime) {
        List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, compensationDateTime);
        TAlLoanRepayPlan s = ss.stream().filter(a -> a.getLoanTerm().equals(term.getTerm())).findFirst().orElse(null);
        if (s == null) {
            return Result.fail("term not found");
        }
        try {
            s.setLoanTermStatus("t");
            s.setCompTermDate(compensationDateTime);
            s.setCompPrincipal(s.getPrincipal());
            s.setCompInterest(s.getInterest());
            s.setCompOverdueFee(s.getOverdueFee());
            s.setPrincipal(BigDecimal.ZERO);
            s.setInterest(BigDecimal.ZERO);
            s.setOverdueFee(BigDecimal.ZERO);
            TAlLoanRepayPlan.update(s.getId(), s, true);
            return Result.success("loan term compensation success");
        } catch (SQLException e) {
            throw new IllegalStateException("loan term compensation fail", e);
        }
    }

    @Override
    public Result loanCompensation(String contractNo, LocalDateTime compensationDateTime) {
        try {
            List<TAlLoanRepayPlan> allSs = TAlLoanRepayPlan.query("contract_no = ?", contractNo);
            List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, compensationDateTime);
            List<TAlLoanRepayPlan> mergeSs = mergeAlLoanRepayPlan(allSs, ss);
            for (TAlLoanRepayPlan s : mergeSs) {
                String loanTermStatus = s.getLoanTermStatus();
                long daysOfLoanCompensation = Duration.between(
                        LocalDateTime.of(compensationDateTime.toLocalDate().withDayOfMonth(s.getRepayDate().getDayOfMonth()), LocalTime.MIN),
                        compensationDateTime
                ).toDays();
                switch (loanTermStatus) {
                    case "u":
                    case "n":
                        s.setLoanTermStatus("l");
                        s.setCompLoanDate(compensationDateTime);

                        s.setCompPrincipal(s.getPrincipal());
                        s.setCompInterest(s.getInterest());

                        Map<String, Object> env = new HashMap<>();
                        env.put("overdueFeePrincipal", s.getPrincipal());
                        env.put("daysOfTermLateFee", daysOfLoanCompensation);
                        env.put("overdueFeeRate", s.getOverdueFeeRate());
                        env.put("overdueFee", s.getOverdueFee());
                        s.setCompOverdueFee((BigDecimal) AviatorEvaluator.execute("overdueFee+(overdueFeePrincipal*overdueFeeRate/100*daysOfTermLateFee)", env));

                        s.setCompGuaranteeFee(s.getGuaranteeFee());
                        s.setPrincipal(BigDecimal.ZERO);
                        s.setInterest(BigDecimal.ZERO);
                        s.setOverdueFee(BigDecimal.ZERO);
                        s.setGuaranteeFee(BigDecimal.ZERO);
                        TAlLoanRepayPlan.update(s.getId(), s, true);
                        break;
                    case "c":
                        break;
                    case "t":
                        s.setLoanTermStatus("l");
                        s.setCompLoanDate(compensationDateTime);

                        s.setCompGuaranteeFee(s.getGuaranteeFee());
                        s.setCompBreachFee(s.getBreachFee());
                        s.setCompTermLateFee(s.getTermLateFee());
                        s.setGuaranteeFee(BigDecimal.ZERO);
                        s.setBreachFee(BigDecimal.ZERO);
                        s.setTermLateFee(BigDecimal.ZERO);
                        TAlLoanRepayPlan.update(s.getId(), s, true);
                        break;
                    case "l":
                        break;
                    default:
                        throw new UnsupportedOperationException("cannot support the loan term status:" + loanTermStatus);
                }
            }
            return Result.success("whole loan compensation complete");
        } catch (SQLException e) {
            throw new IllegalStateException("Whole contract compensation", e);
        }
    }

    private List<TAlLoanRepayPlan> mergeAlLoanRepayPlan(List<TAlLoanRepayPlan> allRepayPlans, List<TAlLoanRepayPlan> repayRepayPlans) {
        List<TAlLoanRepayPlan> l = new ArrayList<>(repayRepayPlans);
        l.addAll(allRepayPlans.subList(repayRepayPlans.size(), allRepayPlans.size()));
        return l;
    }
}
