package cn.snow.loan.al.snowalapi.config.version;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

public class HeaderApiVersionCondition implements RequestCondition<HeaderApiVersionCondition> {

    private static final String X_VERSION = "X-VERSION";
    private final String version;

    public HeaderApiVersionCondition(String version) {
        this.version = version;
    }

    @Override
    public HeaderApiVersionCondition combine(HeaderApiVersionCondition other) {
        // 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
        return new HeaderApiVersionCondition(other.getApiVersion());
    }

    @Override
    public HeaderApiVersionCondition getMatchingCondition(HttpServletRequest httpServletRequest) {
        String headerVersion = httpServletRequest.getHeader(X_VERSION);
        if (Objects.equals(version, headerVersion)) {
            return this;
        }
        return null;
    }

    @Override
    public int compareTo(HeaderApiVersionCondition apiVersionCondition, HttpServletRequest httpServletRequest) {
        return 0;
    }

    public String getApiVersion() {
        return version;
    }

}