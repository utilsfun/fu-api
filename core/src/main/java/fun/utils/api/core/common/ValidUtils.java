package fun.utils.api.core.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.valid.AbstractValidator;
import fun.utils.api.core.valid.CustomValidatorCreator;
import fun.utils.api.core.valid.DefaultValidatorCreator;
import fun.utils.api.core.valid.ValidatorCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;

import java.text.MessageFormat;
import java.util.List;


@Slf4j
public class ValidUtils {


    private final static DefaultValidatorCreator defaultValidatorCreator = new DefaultValidatorCreator();
    private final static CustomValidatorCreator customValidatorCreator = new CustomValidatorCreator();


    public static void validateValue(String name, Object value, String validType, JSONObject data, String message) throws ApiException {
        validateValue(null, name, value, validType, data, message);
    }

    public static void validateValue(WebApplicationContext webApplicationContext, String name, Object value, String validType, JSONObject data, String message) throws ApiException {

        AbstractValidator validator = null;

        if (validType.matches("@:[\\w.]+")) {
            //自定义Class验证器 如: @:com.my.test.DemoValidator
            try {
                validator = customValidatorCreator.create(validType);
            } catch (Exception e) {
                log.warn(validType + "自定义Class验证器加载错误", e);
                throw ApiException.parameterValidException(MessageFormat.format("参数{0},自定义Class验证器加载错误,{1}", name, e.getMessage()));
            }
        }
        else if (validType.matches("@[A-Z]\\w+")) {
            //内置验证器 如: @Max
            String className = validType.replaceFirst("@", "fun.utils.api.core.valid.Validator");
            try {
                validator = defaultValidatorCreator.create(validType);
            } catch (Exception e) {
                log.warn(validType + "内置验证器加载错误", e);
                throw ApiException.parameterValidException(MessageFormat.format("参数{0},内置验证器加载错误,{1}", name, e.getMessage()));
            }
        }
        else if (validType.matches("@(\\w+)\\((.+)\\)")) {
            //SpringBean验证器 如: @"my-validator-demo"
            List<String> strings = DataUtils.extractGroups(validType, "@(\\w+)\\((.+)\\)");
            String beanName = strings.get(1);
            String vName = strings.get(2);
            try {
                ValidatorCreator validatorCreator = webApplicationContext.getBean(beanName, ValidatorCreator.class);
                validator = validatorCreator.create(JSON.parse(vName).toString());
            } catch (Exception e) {
                log.warn(validType + "SpringBean验证器加载错误", e);
                throw ApiException.parameterValidException(MessageFormat.format("参数{0},SpringBean验证器加载错误,{1}", name, e.getMessage()));
            }
        }
        else {
            //不可识别
            throw ApiException.parameterValidException(MessageFormat.format("参数{0}值验证出错,不可识别的验证方式:{1}", name, validType));
        }

        try {
            validator.withParameters(data).withMessage(message).validate(value);
        } catch (Exception e) {
            throw ApiException.parameterValidException(MessageFormat.format("参数{0}值验证出错,{1}", name, e.getMessage()));
        }

    }

}
