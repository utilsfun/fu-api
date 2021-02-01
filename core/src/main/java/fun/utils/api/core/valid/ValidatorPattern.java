package fun.utils.api.core.valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;

// @Pattern,parameters:{regex:"\\d+"},message:"请输入纯数字"
public class ValidatorPattern extends AbstractValidator {

    public ValidatorPattern() {
        this.message = "应为正确的表达式格式";
    }

    @Override
    public boolean isValid(Object value) throws Exception {

        if (parameters == null || parameters.containsKey("regex")){
            throw new Exception("参数验证@Pattern配置不正确");
        }
        String regex = parameters.get("regex").toString();

        return GenericValidator.matchRegexp(String.valueOf(value), regex);

    }

}
