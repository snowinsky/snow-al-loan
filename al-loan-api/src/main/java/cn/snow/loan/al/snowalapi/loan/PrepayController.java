package cn.snow.loan.al.snowalapi.loan;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.snow.loan.al.snowalapi.config.version.ApiVersion;
import lombok.extern.slf4j.Slf4j;

/**
 * 提前结清
 */
@RestController
@Slf4j
@ApiVersion
@RequestMapping("/{apiVersion}/prepay")
public class PrepayController {
}
