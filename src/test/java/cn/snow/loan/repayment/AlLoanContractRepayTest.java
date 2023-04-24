package cn.snow.loan.repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.dao.DbHandler;
import cn.snow.loan.dao.model.TRepayHistory;
import cn.snow.loan.plan.al.prepare.AlLoanRate;
import cn.snow.loan.plan.funding.prepare.LoanAmount;
import cn.snow.loan.plan.funding.prepare.LoanRate;
import cn.snow.loan.plan.funding.prepare.LoanTerm;

public class AlLoanContractRepayTest {

    Logger log = LoggerFactory.getLogger(AlLoanContractRepayTest.class);

    AlLoanContractRepay tested;

    @Before
    public void setUp() throws Exception {
        DbHandler.INIT.initMySqlConnectionFactory();

        tested = new AlLoanContractRepay();
    }

    @Test
    public void testJsonInMySql() throws SQLException {
        log.info("sdfsdfsdf{}", "asdfa");
        TRepayHistory h = new TRepayHistory();
        h.setAlContractNo("qwrqrqwrwq");
        h.setRepayType(1);
        h.setAmount(new BigDecimal("0"));
        h.setRepayDate(LocalDateTime.now());
        h.setPairDetail("{\"sdf\":\"123\"}");
        h.setComments("");
        TRepayHistory.create(h, true);
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
        contract.setFirstRepayDate(LocalDate.of(2022, 1, 1));

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
        List l = tested.preRepayTrail("ALF888439983062600", LocalDateTime.of(2022, 3, 15, 11, 12));
        l.stream().forEach(System.out::println);
    }

    @Test
    public void repay() {
        tested.repay("ALF886477923969800",
                LocalDateTime.of(2022, 3, 9, 11, 12),
                new BigDecimal("0"));
    }

    @Test
    public void termCompensation() {
        tested.termCompensation("ALF888439983062600",
                LoanTerm.monthTerm(2),
                LocalDateTime.of(2022, 2, 14, 11, 12));
    }

    @Test
    public void loanCompensation() {
        tested.loanCompensation("ALF888439983062600", LocalDateTime.of(2022, 3, 14, 11, 12));
    }
}