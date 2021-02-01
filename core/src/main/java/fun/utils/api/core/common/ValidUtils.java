package fun.utils.api.core.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.exception.ApiException;
import fun.utils.api.core.valid.AbstractValidator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;

import java.text.MessageFormat;
import java.util.Date;

@Data
public class ValidUtils {


    public static void validateValue(String name, Object value, String type, JSONObject data, String hint) throws ApiException {

        if (type.startsWith("@")){

            AbstractValidator validator = null;
            String className = "fun.utils.api.core.valid.Validator" + type.replaceFirst("@","");

            try {
                Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                validator = (AbstractValidator) validatorClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw ApiException.parameterValidException(MessageFormat.format("参数{0}值验证出错,{1}", name, e.getMessage()));
            } catch (Exception e) {
                throw ApiException.parameterValidException(MessageFormat.format("参数{0}值验证出错,{1}", name, e.getMessage()));
            }

            try {

                validator.withParameters(data).withMessage(hint).isValid(value);

            } catch (Exception e) {
                throw ApiException.parameterValidException(MessageFormat.format("参数{0}值验证出错,{1}", name, e.getMessage()));
            }

        }
        else {
            throw ApiException.parameterValidException("参数" + name + "值验证出错,不可识别的验证方式:" + type);
        }

    }

}
