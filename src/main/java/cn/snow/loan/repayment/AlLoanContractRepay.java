package cn.snow.loan.repayment;

import java.math.BigDecimal;
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.braisdom.objsql.Databases;
import com.googlecode.aviator.AviatorEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.contract.ILoanContract;
import cn.snow.loan.dao.model.TAlLoanContract;
import cn.snow.loan.dao.model.TAlLoanRepayPlan;
import cn.snow.loan.dao.model.TAlLoanTrxHistory;
import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.prepare.LoanTerm;
import cn.snow.loan.repayment.BalanceMgmt.ConsumeResult;
import cn.snow.loan.repayment.aviator.plugin.RateMultiplyFunction;
import cn.snow.loan.repayment.aviator.plugin.ScaleAmountFunction;
import cn.snow.loan.utils.JsonUtil;

@SuppressWarnings("all")
public class AlLoanContractRepay implements ILoanContractRepay {

    private static final Logger log = LoggerFactory.getLogger(AlLoanContractRepay.class);

    public AlLoanContractRepay() {
        AviatorEvaluator.addFunction(new RateMultiplyFunction());
        AviatorEvaluator.addFunction(new ScaleAmountFunction());
    }

    /**
     * 初始化合同和还款计划
     *
     * @param contract
     * @return
     */
    @Override
    public Result initRepayPlan(ILoanContract contract) {
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                AlLoanContract alLoanContract = (AlLoanContract) contract;
                FundingLoanContract fundingLoanContract = alLoanContract.getFundingLoanContract();
                //创建合同
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

                //创建执行计划
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

    /**
     * 设定合同逾期标识
     *
     * @param contractNo
     * @param checkDateTime
     * @return
     */
    @Override
    public Result setOverdueFlag(String contractNo, LocalDateTime checkDateTime) {
        List<TAlLoanRepayPlan> ss = queryAllRepayPlanByContractNo(contractNo);

        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {

                ObjectNode objectNode = JsonUtil.createNewObjectNode();
                objectNode.put("contractNo", contractNo);
                objectNode.put("trxDateTime", checkDateTime.toString());
                ArrayNode arrayNode = objectNode.putArray("trxPerTerm");

                for (TAlLoanRepayPlan s : ss) {
                    //过了宽限期且还没有还清本期欠款的，设定本期为逾期
                    if (checkDateTime.isAfter(s.getGraceDate()) && s.getOverdueFlag() == 0 && s.getPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                        ObjectNode subObj = JsonUtil.createNewObjectNode();
                        subObj.put("trxType", "changeOverdueFlag");
                        subObj.put("loanTerm", s.getLoanTerm());
                        subObj.putPOJO("beforeTrx", JsonUtil.toJson(s));
                        s.setOverdueFlag(1);
                        log.info("contractNo={} set overdueFlag=1 at {}", contractNo, checkDateTime);
                        subObj.putPOJO("afterTrx", JsonUtil.toJson(s));
                        arrayNode.add(subObj);
                        TAlLoanRepayPlan.update(s.getId(), s, true);
                    }
                }

                if (!arrayNode.isEmpty()) {
                    TAlLoanTrxHistory h = new TAlLoanTrxHistory();
                    h.setAlContractNo(contractNo);
                    h.setTrxType(1);
                    h.setAmount(BigDecimal.ZERO);
                    h.setTrxDateTime(checkDateTime);
                    h.setTrxDetail(objectNode.toPrettyString());
                    h.setTrxTypeInfo("客户逾期");
                    TAlLoanTrxHistory.create(h, true);
                }

                return Result.success("set overdue flag complete");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("set overdue flag fail");
        }
    }

    /**
     * 获取当前所有的还款计划
     *
     * @param contractNo
     * @return
     */
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

    /**
     * 还款试算，计算当下需要还多少钱
     *
     * @param contractNo
     * @param repayDateTime
     * @return
     */
    @Override
    public List<TAlLoanRepayPlan> preRepayTrail(String contractNo, LocalDateTime repayDateTime) {
        List<TAlLoanRepayPlan> l = preRepayTrailCore(contractNo, repayDateTime);
        if (l.isEmpty()) {
            log.warn("contractNo={} notfound", contractNo);
            throw new IllegalStateException("cannot found repay plan in the contract=" + contractNo);
        }
        return l;
    }

    /**
     * 还款试算核心功能
     *
     * @param contractNo
     * @param repayDateTime
     * @return
     */
    private List<TAlLoanRepayPlan> preRepayTrailCore(String contractNo, LocalDateTime repayDateTime) {
        //设定逾期标志
        setOverdueFlag(contractNo, repayDateTime);
        //查出数据库中当前还款状况
        List<TAlLoanRepayPlan> ss = queryAllRepayPlanByContractNo(contractNo);
        //如果已经整笔代偿了，则只需要计算贷款滞纳金
        if (isWholeLoanCompensation(ss)) {
            return ss.stream().peek(b -> {
                //计算整笔贷款的贷款滞纳金
                if (b.getCompLoanDate() != null) {
                    long daysOfLoanLateFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
                    if (repayDateTime.isAfter(b.getCompLoanDate()) && daysOfLoanLateFee > 0) {
                        Map<String, Object> env = new HashMap<>();
                        env.put("compPrincipal", b.getCompPrincipal());
                        env.put("compInterest", b.getCompInterest());
                        env.put("compOverdueFee", b.getCompOverdueFee());
                        env.put("compTermLateFee", b.getCompTermLateFee());
                        env.put("compGuaranteeFee", b.getCompGuaranteeFee());
                        env.put("compBreachFee", b.getCompBreachFee());

                        BigDecimal compAmt = (BigDecimal) AviatorEvaluator.compile("scale(compPrincipal+compInterest+compOverdueFee+compTermLateFee+compGuaranteeFee+compBreachFee)", true).execute(env);

                        env.put("compAmt", compAmt);
                        log.info("{}-{}:试算贷款滞纳金母金额={} by {}", contractNo, b.getLoanTerm(), compAmt, env);
                        env.put("daysOfLoanLateFee", daysOfLoanLateFee);
                        env.put("loanLateFeeRate", b.getLoanLateFeeRate());
                        env.put("loanLateFee", b.getLoanLateFee());

                        b.setCompAmt(compAmt);
                        BigDecimal currLoanLateFee = (BigDecimal) AviatorEvaluator.compile("scale(loanLateFee+(multiplyRate(compAmt, loanLateFeeRate/100)*daysOfLoanLateFee))", true).execute(env);
                        b.setLoanLateFee(currLoanLateFee);
                        log.info("{}-{}:试算贷款滞纳金={} by {}", contractNo, b.getLoanTerm(), b.getLoanLateFee(), env);
                    }
                }
            })//过滤掉已经正常还清的期次
                    .filter(a -> a.getCompPrincipal().compareTo(BigDecimal.ZERO) != 0 || a.getPrincipal().compareTo(BigDecimal.ZERO) != 0)
                    //按期次顺序排序
                    .sorted(Comparator.comparing(TAlLoanRepayPlan::getLoanTerm))
                    .collect(Collectors.toList());
        }
        //没整笔代偿时，需要计算每一期的实际应付款项
        return ss.stream().filter(a -> repayDateTime.isAfter(a.getRepayDate())).peek(b -> {
            //当期代偿之后才开始计算计算期款滞纳金
            if (b.getCompTermDate() != null) {
                long daysOfTermLateFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
                if (repayDateTime.isAfter(b.getCompTermDate()) && daysOfTermLateFee > 0 && b.getCompPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> env = new HashMap<>();
                    env.put("compPrincipal", b.getCompPrincipal());
                    env.put("daysOfTermLateFee", daysOfTermLateFee);
                    env.put("termLateFeeRate", b.getTermLateFeeRate());
                    env.put("termLateFee", b.getTermLateFee());
                    BigDecimal currTermLateFee = (BigDecimal) AviatorEvaluator.compile("scale(termLateFee+(multiplyRate(compPrincipal, termLateFeeRate/100)*daysOfTermLateFee))", true).execute(env);
                    b.setTermLateFee(currTermLateFee);
                    log.info("{}-{}:试算期款滞纳金={} by {}", contractNo, b.getLoanTerm(), b.getTermLateFee(), env);
                }
            } else {
                //计算罚息和违约金，都是宽限期内不算，超出宽限期直接从还款日开始算
                long daysOfOverdueFee = Duration.between(b.getLastRepayDate(), repayDateTime).toDays();
                //只有当剩余未还本金大于0的时候才会计算罚息和违约金
                if (b.getPrincipal().compareTo(BigDecimal.ZERO) > 0 && repayDateTime.isAfter(b.getRepayDate()) && daysOfOverdueFee > 0) {

                    Map<String, Object> env = new HashMap<>();
                    env.put("principal", b.getPrincipal());
                    env.put("daysOfOverdue", daysOfOverdueFee);
                    env.put("overdueFeeRate", b.getOverdueFeeRate());
                    env.put("breachFeeRate", b.getBreachFeeRate());
                    env.put("breachFee", b.getBreachFee());
                    env.put("overdueFee", b.getOverdueFee());

                    b.setOverdueFee(getOverdueFeeBalance(env));
                    log.info("{}-{}:试算罚息={} by {}", contractNo, b.getLoanTerm(), b.getOverdueFee(), env);
                    BigDecimal currBreachFee = (BigDecimal) AviatorEvaluator.compile("scale(breachFee+(multiplyRate(principal, breachFeeRate/100)*daysOfOverdue))", true).execute(env);
                    b.setBreachFee(currBreachFee);
                    log.info("{}-{}:试算违约金={} by {}", contractNo, b.getLoanTerm(), b.getBreachFee(), env);
                }
            }

        })//过滤掉已经正常还清的期次
                .filter(a -> a.getCompPrincipal().compareTo(BigDecimal.ZERO) != 0 || a.getPrincipal().compareTo(BigDecimal.ZERO) != 0)
                //按期次顺序排序
                .sorted(Comparator.comparing(TAlLoanRepayPlan::getLoanTerm))
                .collect(Collectors.toList());
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
        return (BigDecimal) AviatorEvaluator.compile("scale(overdueFee+multiplyRate(principal, overdueFeeRate/100)*daysOfOverdue)", true).execute(env);
    }

    @Override
    public Result repay(String contractNo, LocalDateTime repayDateTime, BigDecimal repayAmount) {
        log.info("###############客户还款，合同号={}，金额={}，时间={}", contractNo, repayAmount, repayDateTime);
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, repayDateTime);
                BalanceMgmt bm = new BalanceMgmt(repayAmount);
                for (TAlLoanRepayPlan p : ss) {
                    if (consumeRepayAmountPerTerm(repayDateTime, bm, p)) {
                        break;
                    }
                }
                TAlLoanTrxHistory h = new TAlLoanTrxHistory();
                h.setAlContractNo(contractNo);
                h.setTrxType(2);
                h.setAmount(repayAmount);
                h.setTrxDateTime(repayDateTime);
                h.setTrxDetail(bm.balanceConsumeLogs());
                h.setTrxTypeInfo("客户还款");
                TAlLoanTrxHistory.create(h, true);

                if (bm.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    log.error("客户溢缴款{}元，合同号{}，总还款额={}", bm.getBalance(), contractNo, repayAmount);
                    throw new IllegalStateException("客户溢缴款，无法配账。。。。");
                }
                return Result.success("repay success");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("repay fail", e);
        }
    }

    private boolean consumeRepayAmountPerTermInGraceDate(LocalDateTime repayDateTime, BalanceMgmt preBalance, TAlLoanRepayPlan p) throws SQLException {
        p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
        ConsumeResult cr;
        //p.getOverdueFee(); 罚息
        /*log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "罚息");
        ConsumeResult cr = preBalance.consumeBalance(p.getOverdueFee());
        p.setOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }*/
        //p.getInterest(); 利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "利息");
        cr = preBalance.consumeBalance(p.getInterest(), "利息", p.getContractNo(), p.getLoanTerm());
        p.setInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getPrincipal(); 本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "本金");
        cr = preBalance.consumeBalance(p.getPrincipal(), "本金", p.getContractNo(), p.getLoanTerm());
        p.setPrincipal(cr.getBalance());
        if (p.getPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("{}-{}:客户宽限期内还款[{}]结清，免[罚息]{}和[违约金]{}。", p.getContractNo(), p.getLoanTerm(), "本金", p.getOverdueFee(), p.getBreachFee());
            p.setOverdueFee(BigDecimal.ZERO);
            p.setBreachFee(BigDecimal.ZERO);
        }
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getLoanLateFee(); 贷款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "贷款滞纳金");
        cr = preBalance.consumeBalance(p.getLoanLateFee(), "贷款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setLoanLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getTermLateFee(); 期款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "期款滞纳金");
        cr = preBalance.consumeBalance(p.getTermLateFee(), "期款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompTermLateFee(); 代偿期款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿期款滞纳金");
        cr = preBalance.consumeBalance(p.getCompTermLateFee(), "代偿期款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setCompTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompOverdueFee(); 代偿罚息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿罚息");
        cr = preBalance.consumeBalance(p.getCompOverdueFee(), "代偿罚息", p.getContractNo(), p.getLoanTerm());
        p.setCompOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompInterest(); 代偿利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿利息");
        cr = preBalance.consumeBalance(p.getCompInterest(), "代偿利息", p.getContractNo(), p.getLoanTerm());
        p.setCompInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getBreachFee(); 违约金
        /*log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "违约金");
        cr = preBalance.consumeBalance(p.getBreachFee());
        p.setBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }*/
        //p.getGuaranteeFee(); 担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "担保费");
        cr = preBalance.consumeBalance(p.getGuaranteeFee(), "担保费", p.getContractNo(), p.getLoanTerm());
        p.setGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompBreachFee(); 代偿违约金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿违约金");
        cr = preBalance.consumeBalance(p.getCompBreachFee(), "代偿违约金", p.getContractNo(), p.getLoanTerm());
        p.setCompBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompGuaranteeFee(); 代偿担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿担保费");
        cr = preBalance.consumeBalance(p.getCompGuaranteeFee(), "代偿担保费", p.getContractNo(), p.getLoanTerm());
        p.setCompGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompPrincipal(); 代偿本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿本金");
        cr = preBalance.consumeBalance(p.getCompPrincipal(), "代偿本金", p.getContractNo(), p.getLoanTerm());
        p.setCompPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        TAlLoanRepayPlan.update(p.getId(), p, true);
        return false;
    }

    private boolean consumeRepayAmountPerTermOutGraceDate(LocalDateTime repayDateTime, BalanceMgmt preBalance, TAlLoanRepayPlan p) throws SQLException {
        p.setLastRepayDate(repayDateTime.truncatedTo(ChronoUnit.DAYS));
        //p.getOverdueFee(); 罚息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "罚息");
        ConsumeResult cr = preBalance.consumeBalance(p.getOverdueFee(), "罚息", p.getContractNo(), p.getLoanTerm());
        p.setOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getInterest(); 利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "利息");
        cr = preBalance.consumeBalance(p.getInterest(), "利息", p.getContractNo(), p.getLoanTerm());
        p.setInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getPrincipal(); 本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "本金");
        cr = preBalance.consumeBalance(p.getPrincipal(), "本金", p.getContractNo(), p.getLoanTerm());
        p.setPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getLoanLateFee(); 贷款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "贷款滞纳金");
        cr = preBalance.consumeBalance(p.getLoanLateFee(), "贷款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setLoanLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getTermLateFee(); 期款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "期款滞纳金");
        cr = preBalance.consumeBalance(p.getTermLateFee(), "期款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompTermLateFee(); 代偿期款滞纳金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿期款滞纳金");
        cr = preBalance.consumeBalance(p.getCompTermLateFee(), "代偿期款滞纳金", p.getContractNo(), p.getLoanTerm());
        p.setCompTermLateFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompOverdueFee(); 代偿罚息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿罚息");
        cr = preBalance.consumeBalance(p.getCompOverdueFee(), "代偿罚息", p.getContractNo(), p.getLoanTerm());
        p.setCompOverdueFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompInterest(); 代偿利息
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿利息");
        cr = preBalance.consumeBalance(p.getCompInterest(), "代偿利息", p.getContractNo(), p.getLoanTerm());
        p.setCompInterest(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getBreachFee(); 违约金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "违约金");
        cr = preBalance.consumeBalance(p.getBreachFee(), "违约金", p.getContractNo(), p.getLoanTerm());
        p.setBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getGuaranteeFee(); 担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "担保费");
        cr = preBalance.consumeBalance(p.getGuaranteeFee(), "担保费", p.getContractNo(), p.getLoanTerm());
        p.setGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompBreachFee(); 代偿违约金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿违约金");
        cr = preBalance.consumeBalance(p.getCompBreachFee(), "代偿违约金", p.getContractNo(), p.getLoanTerm());
        p.setCompBreachFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompGuaranteeFee(); 代偿担保费
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿担保费");
        cr = preBalance.consumeBalance(p.getCompGuaranteeFee(), "代偿担保费", p.getContractNo(), p.getLoanTerm());
        p.setCompGuaranteeFee(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        //p.getCompPrincipal(); 代偿本金
        log.info("{}-{}:客户还款[{}]开始", p.getContractNo(), p.getLoanTerm(), "代偿本金");
        cr = preBalance.consumeBalance(p.getCompPrincipal(), "代偿本金", p.getContractNo(), p.getLoanTerm());
        p.setCompPrincipal(cr.getBalance());
        if (cr.insufficient()) {
            TAlLoanRepayPlan.update(p.getId(), p, true);
            return true;
        }
        TAlLoanRepayPlan.update(p.getId(), p, true);
        return false;
    }

    private boolean consumeRepayAmountPerTerm(LocalDateTime repayDateTime, BalanceMgmt preBalance, TAlLoanRepayPlan p) throws SQLException {
        if (repayDateTime.isAfter(p.getGraceDate())) {
            return consumeRepayAmountPerTermOutGraceDate(repayDateTime, preBalance, p);
        }
        return consumeRepayAmountPerTermInGraceDate(repayDateTime, preBalance, p);
    }

    @Override
    public Result termCompensation(String contractNo, LoanTerm term, LocalDateTime compensationDateTime) {
        log.info("###############当期代偿，合同号={}，期次={}，时间={}", contractNo, term, compensationDateTime);
        List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, compensationDateTime);
        TAlLoanRepayPlan s = ss.stream().filter(a -> a.getLoanTerm().equals(term.getTerm())).findFirst().orElse(null);
        if (s == null) {
            return Result.fail("term not found");
        }
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                String beforChange = JsonUtil.toJson(s);
                s.setLoanTermStatus("t");
                s.setLastRepayDate(compensationDateTime.truncatedTo(ChronoUnit.DAYS));
                s.setCompTermDate(compensationDateTime);
                s.setCompPrincipal(s.getPrincipal());
                s.setCompInterest(s.getInterest());
                s.setCompOverdueFee(s.getOverdueFee());
                s.setPrincipal(BigDecimal.ZERO);
                s.setInterest(BigDecimal.ZERO);
                s.setOverdueFee(BigDecimal.ZERO);
                TAlLoanRepayPlan.update(s.getId(), s, true);

                BigDecimal compTermTotalAmt = s.getCompOverdueFee().add(s.getCompInterest()).add(s.getCompPrincipal());

                TAlLoanTrxHistory h = new TAlLoanTrxHistory();
                h.setAlContractNo(contractNo);
                h.setTrxType(3);
                h.setAmount(compTermTotalAmt);
                h.setTrxDateTime(compensationDateTime);
                ObjectNode on = JsonUtil.createNewObjectNode();
                on.put("loan_term", s.getLoanTerm());
                on.put("comp_term_amt", compTermTotalAmt);
                ObjectNode onDetail = JsonUtil.createNewObjectNode();
                onDetail.put("overdue_fee", s.getCompOverdueFee());
                onDetail.put("interest", s.getCompInterest());
                onDetail.put("principal", s.getCompPrincipal());
                onDetail.put("beforeTrx", beforChange);
                onDetail.put("afterTrx", JsonUtil.toJson(s));
                on.put("com_term_amt_details", onDetail);
                h.setTrxDetail(on.toPrettyString());
                h.setTrxTypeInfo("当期代偿");
                TAlLoanTrxHistory.create(h, true);

                log.info("{}-{}:当期代偿,代偿罚{}，息{}，本{}", contractNo, term.getTerm(), s.getCompOverdueFee(), s.getCompInterest(), s.getCompPrincipal());
                return Result.success("loan term compensation success");

            });
        } catch (SQLException e) {
            throw new IllegalStateException("loan term compensation fail", e);
        }
    }

    @Override
    public Result loanCompensation(String contractNo, LocalDateTime compensationDateTime) {
        log.info("###############整笔代偿，合同号={}，时间={}", contractNo, compensationDateTime);
        try {
            return Databases.executeTransactionally((connection, sqlExecutor) -> {
                List<TAlLoanRepayPlan> allSs = queryAllRepayPlanByContractNo(contractNo);
                if (isWholeLoanCompensation(allSs)) {
                    log.warn("该合同已经做过整笔代偿了，不必重复进行整笔代偿，一个合同只能做一次整笔代偿。");
                    return Result.success("whole loan compensation already complete");
                }
                List<TAlLoanRepayPlan> ss = preRepayTrail(contractNo, compensationDateTime);
                List<TAlLoanRepayPlan> mergeSs = mergeAlLoanRepayPlan(allSs, ss);

                ArrayNode logArray = JsonUtil.createNewJsonArray();

                for (TAlLoanRepayPlan s : mergeSs) {

                    ObjectNode logArrayElement = JsonUtil.createNewObjectNode();

                    if (s.getPrincipal().compareTo(BigDecimal.ZERO) == 0 && s.getCompPrincipal().compareTo(BigDecimal.ZERO) == 0) {
                        logArrayElement.put("beforeTrx", JsonUtil.toJson(s));
                        s.setLoanTermStatus("l");
                        s.setCompLoanDate(compensationDateTime);
                        log.info("{}-{}:整笔代偿-已结清期次, 无需任何操作", contractNo, s.getLoanTerm());
                        TAlLoanRepayPlan.update(s.getId(), s, true);

                        logArrayElement.put("afterTrx", JsonUtil.toJson(s));
                        logArrayElement.put("contract_no", contractNo);
                        logArrayElement.put("loan_term", s.getLoanTerm());
                        logArrayElement.put("term_state", "整笔代偿-已结清期次");
                        logArrayElement.put("term_operation", "无需任何操作");

                        logArray.add(logArrayElement);
                        continue;
                    }
                    s.setLastRepayDate(compensationDateTime.truncatedTo(ChronoUnit.DAYS));
                    String loanTermStatus = s.getLoanTermStatus();
                    long daysOfLoanCompensation = Duration.between(
                            LocalDateTime.of(compensationDateTime.toLocalDate().withDayOfMonth(s.getRepayDate().getDayOfMonth()), LocalTime.MIN),
                            compensationDateTime
                    ).toDays();

                    if (compensationDateTime.isBefore(s.getRepayDate())) {
                        logArrayElement.put("beforeTrx", JsonUtil.toJson(s));
                        s.setLoanTermStatus("l");
                        s.setCompLoanDate(compensationDateTime);
                        s.setCompPrincipal(s.getPrincipal());
                        Map<String, Object> env = new HashMap<>();
                        env.put("principal", s.getPrincipal());
                        env.put("daysOfInterest", daysOfLoanCompensation);
                        env.put("overdueFeeRate", s.getOverdueFeeRate());
                        BigDecimal interest = (BigDecimal) AviatorEvaluator.compile("scale(principal*overdueFeeRate/100*daysOfInterest)", true).execute(env);
                        s.setCompInterest(interest);
                        s.setCompOverdueFee(s.getOverdueFee());
                        s.setCompGuaranteeFee(s.getGuaranteeFee());

                        log.info("{}-{}:整笔代偿-未到期期次, 代偿本金{}，计提利息{}，担保费{}",
                                contractNo, s.getLoanTerm(), s.getCompPrincipal(), s.getCompInterest(), s.getCompGuaranteeFee());

                        s.setPrincipal(BigDecimal.ZERO);
                        s.setInterest(BigDecimal.ZERO);
                        s.setOverdueFee(BigDecimal.ZERO);
                        s.setGuaranteeFee(BigDecimal.ZERO);
                        TAlLoanRepayPlan.update(s.getId(), s, true);

                        logArrayElement.put("afterTrx", JsonUtil.toJson(s));
                        logArrayElement.put("contract_no", contractNo);
                        logArrayElement.put("loan_term", s.getLoanTerm());
                        logArrayElement.put("term_state", "整笔代偿-已结清期次");
                        logArrayElement.put("term_operation", "无需任何操作");

                        logArray.add(logArrayElement);
                        continue;
                    }
                    switch (loanTermStatus) {
                        case "n":
                            logArrayElement.put("beforeTrx", JsonUtil.toJson(s));
                            s.setLoanTermStatus("l");
                            s.setCompLoanDate(compensationDateTime);

                            s.setCompPrincipal(s.getPrincipal());
                            s.setCompInterest(s.getInterest());
                            s.setCompOverdueFee(s.getOverdueFee());
                            s.setCompBreachFee(s.getBreachFee());
                            s.setCompGuaranteeFee(s.getGuaranteeFee());

                            log.info("{}-{}:整笔代偿-到期未代偿期次, 代偿本金{}，利息{}，罚息{}，违约金{}，担保费{}",
                                    contractNo, s.getLoanTerm(), s.getCompPrincipal(), s.getCompInterest(), s.getCompOverdueFee(), s.getCompBreachFee(), s.getCompGuaranteeFee());

                            s.setPrincipal(BigDecimal.ZERO);
                            s.setInterest(BigDecimal.ZERO);
                            s.setOverdueFee(BigDecimal.ZERO);
                            s.setBreachFee(BigDecimal.ZERO);
                            s.setGuaranteeFee(BigDecimal.ZERO);
                            TAlLoanRepayPlan.update(s.getId(), s, true);

                            logArrayElement.put("afterTrx", JsonUtil.toJson(s));
                            logArrayElement.put("contract_no", contractNo);
                            logArrayElement.put("loan_term", s.getLoanTerm());
                            logArrayElement.put("term_state", "整笔代偿-到期未代偿期次");
                            logArrayElement.put("term_operation", "代偿本金{}，利息{}，罚息{}，违约金{}，担保费{}");
                            logArrayElement.put("comp_principal", s.getCompPrincipal());
                            logArrayElement.put("comp_interest", s.getCompInterest());
                            logArrayElement.put("comp_overdue_fee", s.getCompOverdueFee());
                            logArrayElement.put("comp_breach_fee", s.getCompBreachFee());
                            logArrayElement.put("comp_guarantee_fee", s.getCompGuaranteeFee());

                            logArray.add(logArrayElement);
                            break;
                        case "t":
                            logArrayElement.put("beforeTrx", JsonUtil.toJson(s));
                            s.setLoanTermStatus("l");
                            s.setCompLoanDate(compensationDateTime);

                            s.setCompGuaranteeFee(s.getGuaranteeFee());
                            s.setCompBreachFee(s.getBreachFee());
                            s.setCompTermLateFee(s.getTermLateFee());
                            log.info("{}-{}:整笔代偿-到期已代偿期次, 代偿期款滞纳金{}，违约金{}，担保费{}", contractNo, s.getLoanTerm(), s.getCompTermLateFee(), s.getCompBreachFee(), s.getCompGuaranteeFee());
                            s.setGuaranteeFee(BigDecimal.ZERO);
                            s.setBreachFee(BigDecimal.ZERO);
                            s.setTermLateFee(BigDecimal.ZERO);
                            TAlLoanRepayPlan.update(s.getId(), s, true);

                            logArrayElement.put("afterTrx", JsonUtil.toJson(s));
                            logArrayElement.put("contract_no", contractNo);
                            logArrayElement.put("loan_term", s.getLoanTerm());
                            logArrayElement.put("term_state", "整笔代偿-到期已代偿期次");
                            logArrayElement.put("term_operation", "代偿期款滞纳金{}，违约金{}，担保费{}");
                            logArrayElement.put("comp_term_late_fee", s.getCompTermLateFee());
                            logArrayElement.put("comp_breach_fee", s.getCompBreachFee());
                            logArrayElement.put("comp_guarantee_fee", s.getCompGuaranteeFee());

                            logArray.add(logArrayElement);
                            break;
                        case "l":
                            break;
                        default:
                            throw new UnsupportedOperationException("cannot support the loan term status:" + loanTermStatus);
                    }
                }
                TAlLoanTrxHistory h = new TAlLoanTrxHistory();
                h.setAlContractNo(contractNo);
                h.setTrxType(4);
                h.setAmount(new BigDecimal("0"));
                h.setTrxDateTime(compensationDateTime);
                h.setTrxDetail(logArray.toPrettyString());
                h.setTrxTypeInfo("整笔代偿");
                TAlLoanTrxHistory.create(h, true);

                return Result.success("whole loan compensation complete");
            });
        } catch (SQLException e) {
            throw new IllegalStateException("whole loan compensation fail", e);
        }
    }

    private List<TAlLoanRepayPlan> mergeAlLoanRepayPlan(List<TAlLoanRepayPlan> allRepayPlans, List<TAlLoanRepayPlan> repayRepayPlans) {
        List<TAlLoanRepayPlan> l = new ArrayList<>(allRepayPlans);
        for (int i = 0; i < l.size(); i++) {
            Integer term = l.get(i).getLoanTerm();
            TAlLoanRepayPlan repayedTerm = null;
            for (TAlLoanRepayPlan r : repayRepayPlans) {
                if (r.getLoanTerm().equals(term)) {
                    repayedTerm = r;
                    break;
                }
            }
            if (null != repayedTerm) {
                l.set(i, repayedTerm);
            }
        }
        return l;
    }
}
