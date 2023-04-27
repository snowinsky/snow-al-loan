package cn.snow.loan.plan;


import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.plan.funding.ACMLoanCalculator;
import cn.snow.loan.plan.funding.ACPIMLoanCalculator;
import cn.snow.loan.plan.funding.ILoanCalculator;
import cn.snow.loan.plan.funding.Loan;
import cn.snow.loan.plan.funding.LoanAmount;
import cn.snow.loan.plan.funding.LoanRate;
import cn.snow.loan.plan.funding.LoanTerm;

/**
 * Design it on 1/14/16.
 */
@SuppressWarnings("all")
public class LoanCalculatorTest {

    Logger log = LoggerFactory.getLogger(getClass());

    private int totalMonth;
    private BigDecimal totalMoney;
    private double rate;


    @Before
    public void setUp() {
        totalMonth = 12;//十二期，一个月一期
        totalMoney = new BigDecimal("120000");//总房款
        rate = 4;//利率，比如8.2的意思就是8.2%的意思
    }

    public static void main(String[] args) {
        LoanCalculatorTest t = new LoanCalculatorTest();
        t.setUp();
        t.testACMCalculate();
        t.testACPIMCalculate1();
    }

    @Test
    public void testACPIMCalculate1() {
        ILoanCalculator calculator = new ACPIMLoanCalculator();
        Loan loan = calculator.repaymentPlan(
                LoanAmount.valueOf(totalMoney),
                LoanTerm.monthTerm(totalMonth),
                LoanRate.yearRate(rate));
        log.info("{}", loan);
    }

    @Test
    public void testACMCalculate() {
        ILoanCalculator calculator = new ACMLoanCalculator();
        Loan loan = calculator.repaymentPlan(
                LoanAmount.valueOf(totalMoney),
                LoanTerm.monthTerm(totalMonth),
                LoanRate.yearRate(rate));
        log.info("{}", loan);
    }

}
