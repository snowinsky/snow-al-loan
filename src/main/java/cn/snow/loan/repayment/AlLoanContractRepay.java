package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.braisdom.objsql.Databases;
import com.googlecode.aviator.AviatorEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TAlLoanContract;
import cn.snow.loan.dao.model.TAlLoanRepayPlan;
import cn.snow.loan.dao.model.TRepayHistory;
import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanTerm;
import cn.snow.loan.repayment.BalanceMgmt.ConsumeResult;
import cn.snow.loan.utils.JsonUtil;

public class AlLoanContractRepay implements ILoanContractRepay {

    private static final Logger log = LoggerFactory.getLogger(AlLoanContractRepay.class);

    @Override
    public Result initRepayPlan(ILoanContract contract) {
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                AlLoanContract alLoanContract = (AlLoanContract) contract;
                FundingLoanContract fundingLoanContract = alLoanContract.getFundingLoanContract();
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
                    p.setLoanTermStatus("n");
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
        List<TAlLoanRepayPlan> ss = queryAllRepayPlanByContractNo(contractNo);
        ss.stream().filter(a -> checkDateTime.isAfter(a.getGraceDate())).forEach(s -> {
            long durationDay = Duration.between(s.getGraceDate(), checkDateTime).toDays();
            //过了宽限期且还没有还清本期欠款的，设定本期为逾期
            if (s.getOverdueFlag() == 0 && durationDay > 0 && s.getPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                s.setOverdueFlag(1);
                log.info("contractNo={} set overdueFlag=1 at {}", contractNo, checkDateTime);
                try {
                    TAlLoanRepayPlan.update(s.getId(), s, true);
                } catch (SQLException e) {
                    throw new IllegalStateException("set overdue flag fail");
                }
            }
        });
        return Result.success("set overdue flag complete");
    }

    private List<TAlLoanRepayPlan> queryAllRepayPlanByContractNo(String contractNo) {
        try {
            List<TAlLoanRepayPlan> l = TAlLoanRepayPlan.query("contract_no = ?", contractNo);
            if (l == null || l.isEmpty()) {
                return Collections.emptyList();
            }
            return l.stream().sorted(Comparator.comparing(TAlLoanRepayPlan::getLoanTerm)).collect(Collectors.toList());
        } catch (SQLException e) {
            throw new IllegalStateException("query all replay plan by contractNo fail", e);
        }
    }

    @Override
    public List<TAlLoanRepayPlan> preRepayTrail(String contractNo, LocalDateTime repayDateTime) {
        List<TAlLoanRepayPlan> l = preRepayTrailCore(contractNo, repayDateTime)
                .stream()
                //过滤掉已经正常还清的期次
                .filter(a -> a.getCompPrincipal().compareTo(BigDecimal.ZERO) != 0 || a.getPrincipal().compareTo(BigDecimal.ZERO) != 0)
                //按期次顺序排序
                .sorted(Comparator.comparing(TAlLoanRepayPlan::getLoanTerm))
                .collect(Collectors.toList());
        if (l.isEmpty()) {
            log.warn("contractNo={} notfound", contractNo);
            throw new IllegalStateException("cannot found repay plan in the contract=" + contractNo);
        }
        TRepayHistory h = new TRepayHistory();
        h.setAlContractNo(contractNo);
        h.setRepayType(1);
        h.setAmount(new BigDecimal("0"));
        h.setRepayDate(LocalDateTime.now());
        h.setPairDetail(JsonUtil.toJson(l));
        h.setComments("");
        try {
            TRepayHistory.create(h, true);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return l;
    }

    private List<TAlLoanRepayPlan> preRepayTrailCore(String contractNo, LocalDateTime repayDateTime) {
        setOverdueFlag(contractNo, repayDateTime);
        List<TAlLoanRepayPlan> ss = queryAllRepayPlanByContractNo(contractNo);
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

                        BigDecimal compAmt = (BigDecimal) AviatorEvaluator.compile("compPrincipal+compInterest+compOverdueFee+compTermLateFee+compGuaranteeFee+compBreachFee", true).execute(env);
                        compAmt = compAmt.setScale(2, RoundingMode.HALF_UP);

                        env.put("compAmt", compAmt);
                        log.info("{}-{}:试算贷款滞纳金母金额={} by {}", contractNo, b.getLoanTerm(), compAmt, env);
                        env.put("daysOfLoanLateFee", daysOfLoanLateFee);
                        env.put("loanLateFeeRate", b.getLoanLateFeeRate());
                        env.put("loanLateFee", b.getLoanLateFee());

                        b.setCompAmt(compAmt);
                        BigDecimal currLoanLateFee = (BigDecimal) AviatorEvaluator.compile("loanLateFee+(compAmt*loanLateFeeRate/100*daysOfLoanLateFee)", true).execute(env);
                        currLoanLateFee = currLoanLateFee.setScale(2, RoundingMode.HALF_UP);
                        b.setLoanLateFee(currLoanLateFee);
                        log.info("{}-{}:试算贷款滞纳金={} by {}", contractNo, b.getLoanTerm(), b.getLoanLateFee(), env);
                    }
                }
            }).collect(Collectors.toList());
        }
        return ss.stream().filter(a -> repayDateTime.isAfter(a.getRepayDate())).peek(b -> {
            //当期代偿之后才开始计算计算期款滞纳金
            if (b.getCompTermDate() != null) {
                long daysOfTermLateFee = Duration.between(b.getCompTermDate(), repayDateTime).toDays();
                if (repayDateTime.isAfter(b.getCompTermDate()) && daysOfTermLateFee > 0) {
                    Map<String, Object> env = new HashMap<>();
                    env.put("compPrincipal", b.getCompPrincipal());
                    env.put("daysOfTermLateFee", daysOfTermLateFee);
                    env.put("termLateFeeRate", b.getTermLateFeeRate());
                    env.put("termLateFee", b.getTermLateFee());
                    BigDecimal currTermLateFee = (BigDecimal) AviatorEvaluator.compile("termLateFee+(compPrincipal*termLateFeeRate/100*daysOfTermLateFee)", true).execute(env);
                    currTermLateFee = currTermLateFee.setScale(2, RoundingMode.HALF_UP);
                    b.setTermLateFee(currTermLateFee);
                    log.info("{}-{}:试算期款滞纳金={} by {}", contractNo, b.getLoanTerm(), b.getTermLateFee(), env);
                }
            } else {
                //计算罚息和违约金，都是宽限期内不算，超出宽限期直接从还款日开始算
                long daysOfOverdueFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
                //只有当剩余未还本金大于0的时候才会计算罚息和违约金
                if (b.getPrincipal().compareTo(BigDecimal.ZERO) > 0 && repayDateTime.isAfter(b.getGraceDate()) && daysOfOverdueFee > 0) {

                    Map<String, Object> env = new HashMap<>();
                    env.put("principal", b.getPrincipal());
                    env.put("daysOfOverdue", daysOfOverdueFee);
                    env.put("overdueFeeRate", b.getOverdueFeeRate());
                    env.put("breachFeeRate", b.getBreachFeeRate());
                    env.put("breachFee", b.getBreachFee());
                    env.put("overdueFee", b.getOverdueFee());

                    b.setOverdueFee(getOverdueFeeBalance(env));
                    log.info("{}-{}:试算罚息={} by {}", contractNo, b.getLoanTerm(), b.getOverdueFee(), env);
                    BigDecimal currBreachFee = (BigDecimal) AviatorEvaluator.compile("breachFee+(principal*breachFeeRate/100*daysOfOverdue)", true).execute(env);
                    currBreachFee = currBreachFee.setScale(2, RoundingMode.HALF_UP);
                    b.setBreachFee(currBreachFee);
                    log.info("{}-{}:试算违约金={} by {}", contractNo, b.getLoanTerm(), b.getBreachFee(), env);
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
        Object r = AviatorEvaluator.compile("overdueFee+principal*overdueFeeRate/100*daysOfOverdue", true).execute(env);
        return ((BigDecimal) r).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        log.info("客户还款，合同号={}，金额={}，时间={}", contractNo, repayAmount, repayDateTime);
        try {
            List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, repayDateTime);
            BalanceMgmt bm = new BalanceMgmt(repayAmount);
            for (TAlLoanRepayPlan p : ss) {
                if (consumeRepayAmountPerTermReturnBalance(repayDateTime, bm, p)) {
                    break;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("repay fail", e);
        }
        return Result.success("repay success");
    }


    private boolean consumeRepayAmountPerTermReturnBalance(LocalDateTime repayDateTime, BalanceMgmt preBalance, TAlLoanRepayPlan p) throws SQLException {
        p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
        //p.getOverdueFee(); 罚息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "罚息");
        ConsumeResult cr = preBalance.consumeBalance(p.getOverdueFee());
        p.setOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getInterest(); 利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "利息");
        cr = preBalance.consumeBalance(p.getInterest());
        p.setInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getPrincipal(); 本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "本金");
        cr = preBalance.consumeBalance(p.getPrincipal());
        p.setPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getLoanLateFee(); 贷款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "贷款滞纳金");
        cr = preBalance.consumeBalance(p.getLoanLateFee());
        p.setLoanLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getTermLateFee(); 期款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "期款滞纳金");
        cr = preBalance.consumeBalance(p.getTermLateFee());
        p.setTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompOverdueFee(); 代偿罚息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿罚息");
        cr = preBalance.consumeBalance(p.getCompOverdueFee());
        p.setCompOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompInterest(); 代偿利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿利息");
        cr = preBalance.consumeBalance(p.getCompInterest());
        p.setCompInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getBreachFee(); 违约金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "违约金");
        cr = preBalance.consumeBalance(p.getBreachFee());
        p.setBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getGuaranteeFee(); 担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "担保费");
        cr = preBalance.consumeBalance(p.getGuaranteeFee());
        p.setGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompBreachFee(); 代偿违约金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿违约金");
        cr = preBalance.consumeBalance(p.getCompBreachFee());
        p.setCompBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompGuaranteeFee(); 代偿担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿担保费");
        cr = preBalance.consumeBalance(p.getCompGuaranteeFee());
        p.setCompGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompPrincipal(); 代偿本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿本金");
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
        log.info("当期代偿，合同号={}，期次={}，时间={}", contractNo, term, compensationDateTime);
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
            log.info("{}-{}:当期代偿,代偿罚{}，息{}，本{}", contractNo, term.getTerm(), s.getCompOverdueFee(), s.getCompInterest(), s.getCompPrincipal());
            return Result.success("loan term compensation success");
        } catch (SQLException e) {
            throw new IllegalStateException("loan term compensation fail", e);
        }
    }

    @Override
    public Result loanCompensation(String contractNo, LocalDateTime compensationDateTime) {
        log.info("整笔代偿，合同号={}，时间={}", contractNo, compensationDateTime);
        try {
            List<TAlLoanRepayPlan> allSs = queryAllRepayPlanByContractNo(contractNo);
            if (isWholeLoanCompensation(allSs)) {
                log.warn("该合同已经做过整笔代偿了，不必重复进行整笔代偿，一个合同只能做一次整笔代偿。");
                return Result.success("whole loan compensation already complete");
            }
            List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, compensationDateTime);
            List<TAlLoanRepayPlan> mergeSs = mergeAlLoanRepayPlan(allSs, ss);
            for (TAlLoanRepayPlan s : mergeSs) {
                String loanTermStatus = s.getLoanTermStatus();
                long daysOfLoanCompensation = Duration.between(
                        LocalDateTime.of(compensationDateTime.toLocalDate().withDayOfMonth(s.getRepayDate().getDayOfMonth()), LocalTime.MIN),
                        compensationDateTime
                ).toDays();
                switch (loanTermStatus) {
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
                        Object currOverdueFee = AviatorEvaluator.compile("overdueFee+(overdueFeePrincipal*overdueFeeRate/100*daysOfTermLateFee)", true).execute(env);
                        BigDecimal currOverdueFeeDecimal = ((BigDecimal) currOverdueFee).setScale(2, RoundingMode.HALF_UP);
                        s.setCompOverdueFee(currOverdueFeeDecimal);
                        s.setCompBreachFee(s.getBreachFee());
                        s.setCompGuaranteeFee(s.getGuaranteeFee());

                        log.info("{}-{}:整笔代偿-未当期代偿期次, 代偿本金{}，利息{}，罚息{}，违约金{}，担保费{}",
                                contractNo, s.getLoanTerm(), s.getCompPrincipal(), s.getCompInterest(), s.getCompOverdueFee(), s.getCompBreachFee(), s.getCompGuaranteeFee());

                        s.setPrincipal(BigDecimal.ZERO);
                        s.setInterest(BigDecimal.ZERO);
                        s.setOverdueFee(BigDecimal.ZERO);
                        s.setBreachFee(BigDecimal.ZERO);
                        s.setGuaranteeFee(BigDecimal.ZERO);
                        TAlLoanRepayPlan.update(s.getId(), s, true);
                        break;
                    case "t":
                        s.setLoanTermStatus("l");
                        s.setCompLoanDate(compensationDateTime);

                        s.setCompGuaranteeFee(s.getGuaranteeFee());
                        s.setCompBreachFee(s.getBreachFee());
                        s.setCompTermLateFee(s.getTermLateFee());
                        log.info("{}-{}:整笔代偿-已当期代偿期次, 代偿期款滞纳金{}，违约金{}，担保费{}", contractNo, s.getLoanTerm(), s.getCompTermLateFee(), s.getCompBreachFee(), s.getCompGuaranteeFee());
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
