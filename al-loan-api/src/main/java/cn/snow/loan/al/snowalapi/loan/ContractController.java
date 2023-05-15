package cn.snow.loan.al.snowalapi.loan;

import java.math.BigDecimal;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.snow.loan.al.snowalapi.config.log.aop.annotation.PrintInAndOutLog;
import cn.snow.loan.al.snowalapi.config.version.ApiVersion;
import cn.snow.loan.al.snowalapi.loan.entity.InAlLoanContract;
import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.plan.al.AlLoanRate;
import cn.snow.loan.plan.funding.LoanAmount;
import cn.snow.loan.plan.funding.LoanRate;
import cn.snow.loan.plan.funding.LoanTerm;
import cn.snow.loan.repayment.AlLoanContractRepay;
import cn.snow.loan.repayment.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * 合同
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/{apiVersion}/contract")
public class ContractController {

    private static final AlLoanContractRepay CONTRACT_REPAY = new AlLoanContractRepay();

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @PrintInAndOutLog
    public Result createNewContract(@RequestBody @Validated InAlLoanContract alLoanContractIn) {
        FundingLoanContract contract = new FundingLoanContract(
                LoanAmount.valueOf(BigDecimal.valueOf(alLoanContractIn.getLoanAmount())),
                LoanTerm.monthTerm(alLoanContractIn.getLoanTerm()),
                LoanRate.yearRateBeforePercent(alLoanContractIn.getFundingYearRate()),
                LoanRate.yearRateBeforePercent(alLoanContractIn.getOverdueFeeYearRate()));
        contract.contractNo(alLoanContractIn.getContractNo());
        contract.setRepayDay(alLoanContractIn.getRepayMonthDay());
        contract.setDayOfGrace(alLoanContractIn.getDayOfGrace());
        contract.setFirstRepayDate(alLoanContractIn.getFirstRepayDate());

        AlLoanRate alLoanRate = new AlLoanRate(LoanRate.yearRateBeforePercent(alLoanContractIn.getAlFundingYearRate()));
        alLoanRate.setBreachFeeRate(LoanRate.dayRateBeforePercent(alLoanContractIn.getBreachFeeDayRate()));
        alLoanRate.setTermLateFeeRate(LoanRate.dayRateBeforePercent(alLoanContractIn.getTermLateFeeDayRate()));
        alLoanRate.setLoanLateFeeRate(LoanRate.dayRateBeforePercent(alLoanContractIn.getLoanLateFeeDayRate()));

        AlLoanContract alContract = new AlLoanContract(contract, alLoanRate);
        alContract.dayOfCompensation(alLoanContractIn.getDayOfCompensation());
        alContract.contractNo("F" + contract.contractNo());

        return CONTRACT_REPAY.initRepayPlan(alContract);
    }
}
