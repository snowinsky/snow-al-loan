package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.util.StringJoiner;

public class BalanceMgmt {

    private BigDecimal balance;

    public BalanceMgmt(BigDecimal balance) {
        this.balance = balance;
    }

    public ConsumeResult consumeBalance(BigDecimal amt) {
        balance = balance.subtract(amt);
        if (balance.compareTo(BigDecimal.ZERO) >= 0) {
            return sufficient(BigDecimal.ZERO);
        } else {
            return insufficient(new BigDecimal("-1").multiply(balance));
        }
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
}
