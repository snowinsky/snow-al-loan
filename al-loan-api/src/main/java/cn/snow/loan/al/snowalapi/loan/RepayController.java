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
import cn.snow.loan.al.snowalapi.loan.entity.InAlLoanParamWithAmount;
import cn.snow.loan.al.snowalapi.loan.entity.ResponseData;
import cn.snow.loan.repayment.AlLoanContractRepay;
import cn.snow.loan.repayment.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户还款
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/v1.0/repay")
public class RepayController {

    private static final AlLoanContractRepay CONTRACT_REPAY = new AlLoanContractRepay();

    @PostMapping(value = "/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    @PrintInAndOutLog
    public ResponseData<Result> draftReplayPlan(@RequestBody @Validated InAlLoanParamWithAmount paramWithAmount) {
        return ResponseData.success(CONTRACT_REPAY.repay(paramWithAmount.getContractNo(), paramWithAmount.getOperationDateTime(), BigDecimal.valueOf(paramWithAmount.getAmount())));
    }
}
