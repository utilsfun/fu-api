package fun.utils.api.core.valid;

import fun.utils.api.core.common.ClassUtils;

import java.util.List;

// @In,parameters:{values:[1,3,5,9]},message:"请在1,3,5,9中选择"
public class ValidatorIn extends AbstractValidator {

    public ValidatorIn() {
        this.message = "取值应在${values|json}之内";
    }

    @Override
    public boolean isValid(Object value) throws Exception {

        if (parameters == null || !parameters.containsKey("values")){
            throw new Exception("参数验证@In配置不正确");
        }

        Object values = parameters.get("values");

        List valueList = ClassUtils.castValue(values,List.class);

        if (valueList == null || valueList.size() == 0) {
            throw new Exception("参数验证@In配置不正确");
        }

        return valueList.contains(value);

    }

}
