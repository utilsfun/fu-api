package fun.utils.api.core.valid;

import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.exception.ApiException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractValidator {

    protected Map<String,Object> parameters = new HashMap<>();
    protected String message = "应为正确的格式";


    public AbstractValidator withParameters(Map<String,Object> parameters){
        this.parameters = DataUtils.getEmptyIfNull(parameters);
        return this;
    }

    public AbstractValidator withMessage(String message){
        this.message = StringUtils.defaultIfBlank(message,this.message);
        return this;
    }

    public abstract boolean isValid(Object value) throws Exception;

    public void validate(Object value) throws Exception {
        if (!isValid(value)){
            throw ApiException.parameterValidException(getFormatMessage());
        }
    }

    public String getFormatMessage() {
        return DataUtils.stringFormat(this.message,this.parameters);
    }

}
