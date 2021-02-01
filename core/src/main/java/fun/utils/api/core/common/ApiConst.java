package fun.utils.api.core.common;

import java.util.ArrayList;
import java.util.List;

public class ApiConst {
    public final static List<String> PUBLIC_GROOVY_IMPORTS = new ArrayList<>();

    static {
        PUBLIC_GROOVY_IMPORTS.add("import com.alibaba.fastjson.*;");
        PUBLIC_GROOVY_IMPORTS.add("org.apache.commons.lang3.StringUtils;");
    }

}
