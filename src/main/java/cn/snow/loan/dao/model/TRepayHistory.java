package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.braisdom.objsql.annotations.DomainModel;

/**
 * t_repay_history
 * @author 
 */
@DomainModel(tableName = "t_repay_history")
public class TRepayHistory implements Serializable {
    private Long id;

    private String alContractNo;

    /**
     * 1.repay 2.term compensation 3 loan compensation
     */
    private Byte repayType;

    private BigDecimal amount;

    private LocalDateTime repayDate;

    private Object pairDetail;

    private String comments;

    private static final long serialVersionUID = 1L;
}