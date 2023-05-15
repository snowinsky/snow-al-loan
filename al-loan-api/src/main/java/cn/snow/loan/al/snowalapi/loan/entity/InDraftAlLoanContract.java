package cn.snow.loan.al.snowalapi.loan.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuppressWarnings("all")
public class InDraftAlLoanContract {

    private double loanAmount;
    private double fundingYearRate;
    //private double overdueFeeYearRate;
    private int loanTerm;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="Asia/Shanghai")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate firstRepayDate;
    //private int dayOfGrace;

    private double alFundingYearRate;
    //private double breachFeeDayRate;
    //private double termLateFeeDayRate;
    //private double loanLateFeeDayRate;
    //private int dayOfCompensation;
}
