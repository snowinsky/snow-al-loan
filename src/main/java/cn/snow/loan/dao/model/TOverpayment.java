package cn.snow.loan.dao.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.braisdom.objsql.annotations.Column;
import com.github.braisdom.objsql.annotations.DomainModel;
import com.github.braisdom.objsql.annotations.PrimaryKey;

/**
 * t_overpayment
 * @author 
 */
@SuppressWarnings("all")
@DomainModel(tableName = "t_overpayment")
public class TOverpayment implements Serializable {
    @PrimaryKey
    private Long id;

    private String alContractNo;

    private BigDecimal amount;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm",timezone = "Asia/Shanghai")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime trxDateTime;

    private static final long serialVersionUID = 1L;
}