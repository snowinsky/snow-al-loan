package cn.snow.loan.al.snowalapi.loan.entity;

import lombok.Data;

@Data
public class InAlLoanParamWithAmount extends InAlLoanParam {
    private double amount;
}
