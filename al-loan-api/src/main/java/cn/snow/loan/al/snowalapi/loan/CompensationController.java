package cn.snow.loan.al.snowalapi.loan;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.snow.loan.al.snowalapi.config.log.aop.annotation.PrintInAndOutLog;
import cn.snow.loan.al.snowalapi.config.version.ApiVersion;
import cn.snow.loan.al.snowalapi.loan.entity.InAlLoanParam;
import cn.snow.loan.al.snowalapi.loan.entity.InAlLoanParamWithTerm;
import cn.snow.loan.al.snowalapi.loan.entity.ResponseData;
import cn.snow.loan.plan.funding.LoanTerm;
import cn.snow.loan.repayment.AlLoanContractRepay;
import cn.snow.loan.repayment.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * 代偿，包括当期代偿和整笔代偿
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/v1.0/compensation")
public class CompensationController {

    private static final AlLoanContractRepay CONTRACT_REPAY = new AlLoanContractRepay();

    @PostMapping(value = "/term", produces = MediaType.APPLICATION_JSON_VALUE)
    @PrintInAndOutLog
    public ResponseData<Result> termCompensation(@RequestBody @Validated InAlLoanParamWithTerm paramWithTerm) {
        return ResponseData.success(CONTRACT_REPAY.termCompensation(paramWithTerm.getContractNo(), LoanTerm.monthTerm(paramWithTerm.getTerm()), paramWithTerm.getOperationDateTime()));
    }

    @PostMapping(value = "/loan", produces = MediaType.APPLICATION_JSON_VALUE)
    @PrintInAndOutLog
    public ResponseData<Result> loanCompensation(@RequestBody @Validated InAlLoanParam param) {
        return ResponseData.success(CONTRACT_REPAY.loanCompensation(param.getContractNo(), param.getOperationDateTime()));
    }
}
