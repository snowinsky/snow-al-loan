package cn.snow.loan.dao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

@DomainModel
public class TAlLoanContract {
    @PrimaryKey
    private Long id;
    private String contractNo;
    private BigDecimal yearRate;
    private BigDecimal overdueFeeRate;
    private int repayDay;
    private int graceDay;
    private int compensationDay;
    private int loanTerm;
    private LocalDateTime firstRepayDate;
}
