package cn.snow.loan.al.snowalapi.config.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

public class PathApiVersionCondition implements RequestCondition<PathApiVersionCondition> {

    private static final Pattern VERSION_PREFIX_PATTERN = Pattern.compile("v(\\d+\\.\\d+)");

    private final String version;

    public PathApiVersionCondition(String version) {
        this.version = version;
    }

    @Override
    public PathApiVersionCondition combine(PathApiVersionCondition other) {
        // 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
        return new PathApiVersionCondition(other.getApiVersion());
    }


    @Override
    public PathApiVersionCondition getMatchingCondition(HttpServletRequest httpServletRequest) {
        Matcher m = VERSION_PREFIX_PATTERN.matcher(httpServletRequest.getRequestURI());
        if (m.find()) {
            String pathVersion = m.group(1);
            // 这个方法是精确匹配, 就是请求消息中的版本是3.0，就只能调用版本号是3.0的接口
            if (Objects.equals(pathVersion, version)) {
                return this;
            }
            // 该方法是只要大于等于最低接口version即匹配成功，需要和compareTo()配合， 就是请求消息中的版本号是3.0，则可以调用<=3.0的最大的版本号的
            // 举例：定义有1.0/1.1接口，访问1.2，则实际访问的是1.1，如果从小开始那么排序反转即可
//            if(Float.parseFloat(pathVersion)>=Float.parseFloat(version)){
//                return this;
//            }

        }
        return null;
    }

    @Override
    public int compareTo(PathApiVersionCondition other, HttpServletRequest request) {
        return 0;
        // 优先匹配最新的版本号，和getMatchingCondition注释掉的代码同步使用
//        return other.getApiVersion().compareTo(this.version);
    }

    public String getApiVersion() {
        return version;
    }

}
