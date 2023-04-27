package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.utils.JsonUtil;

@SuppressWarnings("all")
public class BalanceMgmt {

    private static final Logger log = LoggerFactory.getLogger(BalanceMgmt.class);

    private BigDecimal balance;
    private final ArrayNode balanceConsumeLog;
    private final ObjectNode balanceConsumeLogRoot;

    public BalanceMgmt(BigDecimal balance) {
        this.balance = balance;
        balanceConsumeLogRoot = JsonUtil.createNewObjectNode();
        balanceConsumeLogRoot.put("repay_total_amt", balance);
        balanceConsumeLog = balanceConsumeLogRoot.putArray("repay_pair_detail_array");
    }

//    public ConsumeResult consumeBalance(BigDecimal amt, String amtTitle){
//        BigDecimal preBalance = new BigDecimal(balance.toString());
//        balance = balance.subtract(amt);
//        if (balance.compareTo(BigDecimal.ZERO) > 0) {
//            log.info("还款{}：preBalance={}, spendAmt={}, endBalance={}", amtTitle, preBalance, amt, balance);
//            ObjectNode consumeDetail = JsonUtil.createNewObjectNode();
//            consumeDetail.put("pair_item_title", amtTitle);
//            consumeDetail.put("pre_balance", preBalance);
//            consumeDetail.put("pair_amt", amt);
//            consumeDetail.put("end_balance", balance);
//            consumeDetail.put("pair_amt_balance", BigDecimal.ZERO);
//            balanceConsumeLog.add(consumeDetail);
//            return sufficient(BigDecimal.ZERO);
//        } else {
//            log.info("还款{}：preBalance={}, spendAmt={}, endBalance={}", amtTitle, preBalance, amt, 0);
//            BigDecimal pairAmtBalance = new BigDecimal("-1").multiply(balance);
//            ObjectNode consumeDetail = JsonUtil.createNewObjectNode();
//            consumeDetail.put("pair_item_title", amtTitle);
//            consumeDetail.put("pre_balance", preBalance);
//            consumeDetail.put("pair_amt", amt);
//            consumeDetail.put("end_balance", 0);
//            consumeDetail.put("pair_amt_balance", pairAmtBalance);
//            balanceConsumeLog.add(consumeDetail);
//            return insufficient(pairAmtBalance);
//        }
//    }

    public ConsumeResult consumeBalance(BigDecimal amt, String amtTitle, String contractNo, Integer loanTerm){
        BigDecimal preBalance = new BigDecimal(balance.toString());
        balance = balance.subtract(amt);
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            log.info("还款{}-term={}：preBalance={}, spendAmt={}, endBalance={}", amtTitle, loanTerm, preBalance, amt, balance);
            ObjectNode consumeDetail = JsonUtil.createNewObjectNode();
            consumeDetail.put("contract_no", contractNo);
            consumeDetail.put("loan_term", loanTerm);
            consumeDetail.put("pair_item_title", amtTitle);
            consumeDetail.put("pre_balance", preBalance);
            consumeDetail.put("pair_amt", amt);
            consumeDetail.put("end_balance", balance);
            consumeDetail.put("pair_amt_balance", BigDecimal.ZERO);
            balanceConsumeLog.add(consumeDetail);
            return sufficient(BigDecimal.ZERO);
        } else {
            log.info("还款{}-term={}：preBalance={}, spendAmt={}, endBalance={}", amtTitle, loanTerm, preBalance, amt, 0);
            BigDecimal pairAmtBalance = new BigDecimal("-1").multiply(balance);
            ObjectNode consumeDetail = JsonUtil.createNewObjectNode();
            consumeDetail.put("contract_no", contractNo);
            consumeDetail.put("loan_term", loanTerm);
            consumeDetail.put("pair_item_title", amtTitle);
            consumeDetail.put("pre_balance", preBalance);
            consumeDetail.put("pair_amt", amt);
            consumeDetail.put("end_balance", 0);
            consumeDetail.put("pair_amt_balance", pairAmtBalance);
            balanceConsumeLog.add(consumeDetail);
            return insufficient(pairAmtBalance);
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public static ConsumeResult sufficient(BigDecimal balance) {
        return new ConsumeResult(true, balance);
    }

    public static ConsumeResult insufficient(BigDecimal balance) {
        return new ConsumeResult(false, balance);
    }

    public static class ConsumeResult {

        private boolean sufficient;
        private BigDecimal balance;

        public ConsumeResult(boolean sufficient, BigDecimal balance) {
            this.sufficient = sufficient;
            this.balance = balance;
        }

        public boolean sufficient() {
            return sufficient;
        }

        public boolean insufficient(){
            return !sufficient;
        }

        public void setSufficient(boolean sufficient) {
            this.sufficient = sufficient;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ConsumeResult.class.getSimpleName() + "[", "]")
                    .add("sufficient=" + sufficient)
                    .add("balance=" + balance)
                    .toString();
        }
    }

    public ObjectNode getBalanceConsumeLogRoot() {
        return balanceConsumeLogRoot;
    }

    public String balanceConsumeLogs(){
        return balanceConsumeLogRoot.toPrettyString();
    }
}
