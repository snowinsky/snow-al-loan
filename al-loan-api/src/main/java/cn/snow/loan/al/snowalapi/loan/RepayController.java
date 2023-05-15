package cn.snow.loan.al.snowalapi.loan;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.snow.loan.al.snowalapi.config.version.ApiVersion;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户还款
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/v1.0/repay")
public class RepayController {
}
