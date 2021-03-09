package fun.utils.api.core.valid;

import fun.utils.common.DataUtils;
import fun.utils.api.core.common.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
            log.debug(MessageFormat.format("validate fail : {0}", value));
            throw ApiException.parameterValidException(getFormatMessage());
        }else{
            log.trace(MessageFormat.format("validate success : {0}", value));
        }
    }

    public String getFormatMessage() {
        return DataUtils.stringFormat(this.message,this.parameters);
    }

}
