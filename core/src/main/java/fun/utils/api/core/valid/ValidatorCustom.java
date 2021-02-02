package fun.utils.api.core.valid;

import fun.utils.api.core.exception.ApiException;
import fun.utils.api.core.util.ClassUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.List;

// @Custom,parameters:{validator:"my.test.ValidatorDemo",parameter1:"a"},message:"请符合测试规则demo"
// validator类应要继承AbstractValidator
@Slf4j
public class ValidatorCustom extends AbstractValidator {

    public ValidatorCustom() {
        this.message = "取值应符合自定义验证";
    }

    @Override
    public boolean isValid(Object value) throws Exception {

        if (parameters == null || !parameters.containsKey("validator")){
            throw new Exception("参数验证@Custom配置不正确");
        }

        AbstractValidator validator = null;

        try {

            Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(String.valueOf(parameters.get("validator")));
            validator =  (AbstractValidator) validatorClass.newInstance();

        } catch (Exception e) {
            log.warn("自定义验证器加载错误",e);
            throw e;
        }

        return validator.withParameters(parameters).withMessage(message).isValid(value);

    }

}
