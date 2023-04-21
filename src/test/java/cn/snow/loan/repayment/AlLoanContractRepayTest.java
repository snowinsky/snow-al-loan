package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.dao.DbHandler;
import cn.snow.loan.plan.al.prepare.AlLoanRate;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContractRepayTest {

    AlLoanContractRepay tested;

    @Before
    public void setUp() throws Exception {
        DbHandler.INIT.initMySqlConnectionFactory();

        tested = new AlLoanContractRepay(new FundingLoanContractRepay());
    }

    @Test
    public void initRepayPlan() {
        FundingLoanContract contract = new FundingLoanContract(LoanAmount.valueOf(new BigDecimal("12000")),
                LoanTerm.monthTerm(12),
                LoanRate.yearRateBeforePercent(8.2),
                LoanRate.yearRateBeforePercent(8.2));
        contract.contractNo("F" + System.nanoTime());
        contract.setRepayDay(4);
        contract.setDayOfGrace(3);
        contract.setFirstRepayDate(LocalDate.of(2022,1,1));

        AlLoanRate alLoanRate = new AlLoanRate(LoanRate.yearRateBeforePercent(23.9));
        alLoanRate.setBreachFeeRate(LoanRate.dayRateBeforePercent(0.062));
        alLoanRate.setTermLateFeeRate(LoanRate.dayRateBeforePercent(0.097));
        alLoanRate.setLoanLateFeeRate(LoanRate.dayRateBeforePercent(0.097));

        AlLoanContract alContract = new AlLoanContract(contract, alLoanRate);
        alContract.dayOfCompensation(10);
        alContract.contractNo("AL" + contract.contractNo());

        tested.initRepayPlan(alContract);
    }

    @Test
    public void preRepayTrail() {
        List l = tested.preRepayTrail("ALF716351448965700", LocalDateTime.of(2022, 3, 9, 11, 12));
        l.stream().forEach(System.out::println);
    }

    @Test
    public void repay() {
    }

    @Test
    public void termCompensation() {
    }

    @Test
    public void loanCompensation() {
    }
}