package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.ClassUtils;
import fun.utils.common.DataUtils;
import lombok.Getter;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefNodeConvert implements NodeConvert {

    public final static String REF_REGEX = "^(\\(@(\\w+)(:'(.+)')?\\))?(.+)$";
    public final static String BESIEGE_REGEX = "^@\\{(((?!@\\{).)*)\\}$";

    private final static int REP_IDX = 5;
    private final static int CAST_IDX = 2;
    private final static int FORMAT_IDX = 4;

    public final static String PRE_TAG = "@ref>";

    @Getter
    private List<String> expressions = new ArrayList<>();

    @Getter
    private String typeCast = null;

    @Getter
    private String typeFormat = null;


    private JSON data;

    private JSONObject node;

    private VarThree varThree;


    public RefNodeConvert(String refExpression) {

        String string = refExpression;

        if (StringUtils.startsWithIgnoreCase(refExpression, PRE_TAG)) {
            string = StringUtils.substringAfter(refExpression, PRE_TAG);
        }
        else if (refExpression.matches(BESIEGE_REGEX)) {
            string = DataUtils.extractStr(refExpression, BESIEGE_REGEX);
        }

        List<String> groupValues = DataUtils.extractGroups(refExpression, REF_REGEX);
        expressions = Arrays.asList(groupValues.get(REP_IDX).split("||"));
        typeCast = groupValues.get(CAST_IDX);
        typeFormat = groupValues.get(FORMAT_IDX);
    }


    public RefNodeConvert(List<String> valueExpressions, String typeCast, String typeFormat) {
        this.expressions = valueExpressions;
        this.typeCast = typeCast;
        this.typeFormat = typeFormat;
    }


    private Object extractValue() {
        Object result = null;
        for (String subExpression : expressions) {
            result = extractValue(subExpression);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private Object extractValue(String expression) {

        if (DataUtils.isBesieged(expression, "'", "'")) {
            return JSON.parse(DataUtils.extractBesieged(expression, "'", "'"));
        }
        else if (StringUtils.startsWith(expression, ":")) {
            return varThree.eval(expression);
        }
        else {
            return JSONPath.eval(data, expression);
        }

    }

    private Object castValue(Object value) {

        if (StringUtils.isBlank(typeCast)) {
            return value;
        }

        Object result = ClassUtils.castValue(value, typeCast);

        if (StringUtils.isNotBlank(typeFormat)) {
            //** updating *************************

            //字符串format
            result = MessageFormat.format(typeFormat, result);
        }

        return result;
    }

    @Override
    public Object convert(JSON data, JSONObject node, VarThree varThree) {

        this.data = data;
        this.node = node;
        this.varThree = varThree;

        Object result = extractValue();
        return castValue(result);
    }

}
