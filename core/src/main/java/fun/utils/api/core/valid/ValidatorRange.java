package fun.utils.api.core.valid;

import fun.utils.api.core.exception.ApiException;
import fun.utils.api.core.util.ClassUtils;
import org.apache.commons.validator.GenericValidator;

import java.util.Date;

// @Range,parameters:{min:18,max:80},message:"年龄在18到80之间"
// @Range,parameters:{min:"2017-03-01",max:"2018-03-01"},message:"时间在2017年3月1日开始一年内"
public class ValidatorRange extends AbstractValidator {

    public ValidatorRange() {
        this.message = "值范围应在${min|default:'更小'}到${max|default:'更大'}之间";
    }

    @Override
    public boolean isValid(Object value) throws Exception {

        boolean isValid = false;
        Class<?> vClass = value.getClass();

        if (vClass.isAssignableFrom(Integer.class)) {
            int min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Integer.class) : Integer.MIN_VALUE;
            int max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Integer.class) : Integer.MAX_VALUE;
            int intValue = ((Integer) value).intValue();
            isValid = GenericValidator.isInRange(intValue, min, max);
        }
        else if (vClass.isAssignableFrom(Long.class)) {
            long min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Long.class) : Long.MIN_VALUE;
            long max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Long.class) : Long.MAX_VALUE;
            long longValue = ((Long) value).longValue();
            isValid = GenericValidator.isInRange(longValue, min, max);
        }
        else if (vClass.isAssignableFrom(Double.class)) {
            double min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Double.class) : Double.MIN_VALUE;
            double max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Double.class) : Double.MAX_VALUE;
            double doubleValue = ((Double) value).doubleValue();
            isValid = GenericValidator.isInRange(doubleValue, min, max);
        }
        else if (vClass.isAssignableFrom(Float.class)) {
            float min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Float.class) : Float.MIN_VALUE;
            float max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Float.class) : Float.MAX_VALUE;
            float floatValue = ((Float) value).floatValue();
            isValid = GenericValidator.isInRange(floatValue, min, max);
        }
        else if (vClass.isAssignableFrom(Date.class)) {
            long min = parameters.containsKey("min") ? ClassUtils.castValue(parameters.get("min"), Date.class).getTime() : Long.MIN_VALUE;
            long max = parameters.containsKey("max") ? ClassUtils.castValue(parameters.get("max"), Date.class).getTime() : Long.MAX_VALUE;
            long longValue =((Date) value).getTime();
            isValid = GenericValidator.isInRange(longValue, min, max);
        }
        else {
            throw new Exception(vClass.getSimpleName() + "数据不适用@Range验证");
        }
        return isValid;
    }

}
