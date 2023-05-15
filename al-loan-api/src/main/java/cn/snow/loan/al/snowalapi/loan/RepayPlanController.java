package cn.snow.loan.al.snowalapi.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.snow.loan.al.snowalapi.config.log.aop.annotation.PrintInAndOutLog;
import cn.snow.loan.al.snowalapi.config.version.ApiVersion;
import cn.snow.loan.al.snowalapi.loan.entity.InDraftAlLoanContract;
import cn.snow.loan.al.snowalapi.loan.entity.OutDraftAlLoanRepayPlan;
import cn.snow.loan.al.snowalapi.loan.entity.ResponseData;
import cn.snow.loan.al.snowalapi.loan.entity.TermAlLoanRepayPlan;
import cn.snow.loan.contract.AlLoanContract;
import cn.snow.loan.contract.FundingLoanContract;
import cn.snow.loan.plan.al.AlLoan;
import cn.snow.loan.plan.al.AlLoanRate;
import cn.snow.loan.plan.al.GuaranteeFeePerTerm;
import cn.snow.loan.plan.funding.LoanAmount;
import cn.snow.loan.plan.funding.LoanPerTerm;
import cn.snow.loan.plan.funding.LoanRate;
import cn.snow.loan.plan.funding.LoanTerm;
import cn.snow.loan.repayment.AlLoanContractRepay;
import lombok.extern.slf4j.Slf4j;

/**
 * 还款计划
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/{apiVersion}/repay-plan")
public class RepayPlanController {

    private static final AlLoanContractRepay CONTRACT_REPAY = new AlLoanContractRepay();

    @PostMapping(value = "/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    @PrintInAndOutLog
    @ApiVersion(value = "1.2")
    public ResponseData<OutDraftAlLoanRepayPlan> draftReplayPlan(@RequestBody @Validated InDraftAlLoanContract alLoanContractIn) {
        FundingLoanContract contract = new FundingLoanContract(
                LoanAmount.valueOf(BigDecimal.valueOf(alLoanContractIn.getLoanAmount())),
                LoanTerm.monthTerm(alLoanContractIn.getLoanTerm()),
                LoanRate.yearRateBeforePercent(alLoanContractIn.getFundingYearRate()),
                LoanRate.yearRateBeforePercent(alLoanContractIn.getFundingYearRate()));
        contract.setFirstRepayDate(alLoanContractIn.getFirstRepayDate());

        AlLoanRate alLoanRate = new AlLoanRate(LoanRate.yearRateBeforePercent(alLoanContractIn.getAlFundingYearRate()));

        AlLoanContract alContract = new AlLoanContract(contract, alLoanRate);

        AlLoan alLoan = (AlLoan) alContract.repayPlanTrial();

        OutDraftAlLoanRepayPlan draftAlLoanRepayPlan = new OutDraftAlLoanRepayPlan();
        draftAlLoanRepayPlan.setTermAlLoanRepayPlanList(convertAlLoan(alLoan, alLoanContractIn.getFirstRepayDate()));


        return ResponseData.success(draftAlLoanRepayPlan);
    }

    private List<TermAlLoanRepayPlan> convertAlLoan(AlLoan alLoan, LocalDate firstRepayDate) {

        List<TermAlLoanRepayPlan> l = new ArrayList<>();

        List<LoanPerTerm> allLoans = alLoan.getAllLoans();
        List<GuaranteeFeePerTerm> allGuaranteeFeeL = alLoan.getAllGuaranteeFees();
        int allLoansLength = allLoans.size();
        for (int i = 0; i < allLoansLength; i++) {
            LoanPerTerm loanPerTerm = allLoans.get(i);
            GuaranteeFeePerTerm guaranteeFeePerTerm = allGuaranteeFeeL.get(i);

            TermAlLoanRepayPlan trp = new TermAlLoanRepayPlan();
            //"期数",
            trp.setTerm(loanPerTerm.getMonth());
            //"到期日",
            trp.setRepayDate(firstRepayDate.plusMonths(i));
            //"剩余本金",
            trp.setRemainPrincipal(loanPerTerm.getRemainPrincipal());
            //"期供",
            trp.setRepayment(loanPerTerm.getRepayment());
            //"本金",
            trp.setPayPrincipal(loanPerTerm.getPayPrincipal());
            //"利息",
            trp.setInterest(loanPerTerm.getInterest());
            //"担保费"
            trp.setGuaranteeFee(guaranteeFeePerTerm.getGuaranteeFee());

            l.add(trp);
        }
        return l;
    }


}
