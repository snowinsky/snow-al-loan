package cn.snow.loan.repayment;

import java.math.BigDecimal;

import org.junit.Test;

public class BalanceMgmtTest {

    @Test
    public void consumeBalance() {
        BigDecimal b = new BigDecimal("100");
        BalanceMgmt bm = new BalanceMgmt(b);
        for (int i = 0; i < 5; i++) {
            System.out.println(bm.consumeBalance(new BigDecimal("30"), "asdfa", "asdf", 1));
            System.out.println(b);
        }
    }
}