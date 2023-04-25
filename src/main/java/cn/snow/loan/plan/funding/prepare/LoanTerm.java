package cn.snow.loan.plan.funding.prepare;

import java.util.StringJoiner;

public class LoanTerm {

    private final int term;
    private final int termType;

    private LoanTerm(){
        throw new UnsupportedOperationException("");
    }

    private LoanTerm(int term) {
        this.term = term;
        termType = TERM_TYPE_MONTH;
    }

    public static LoanTerm monthTerm(int term) {
        return new LoanTerm(term);
    }

    public int getTerm() {
        return term;
    }

    public int getTermType() {
        return termType;
    }

    public static final int TERM_TYPE_MONTH = 100;
    public static final int TERM_TYPE_YEAR = 101;
    public static final int TERM_TYPE_WEEK = 102;

    @Override
    public String toString() {
        return new StringJoiner(", ", LoanTerm.class.getSimpleName() + "[", "]")
                .add("term=" + term)
                .toString();
    }
}
