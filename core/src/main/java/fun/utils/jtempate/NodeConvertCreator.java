package fun.utils.jtempate;

import fun.utils.common.DataUtils;
import org.apache.commons.lang3.StringUtils;

public class NodeConvertCreator{

    public static NodeConvert create(String value) {

        NodeConvert result = null;


        // @ref:[(@cast-type[:cast-format])]ref-expr1[||ref-expr2]
        // @ref:wife.name
        // @ref:(@string:'名字:{}')wife.name
        // @ref:(@string:'名字:{}')wife.nickname||wife.nickname||'无名氏'
        // @ref:(@string:'yyyy年MM月dd日')wife.birthday


        // @ref:[(@cast-type[:cast-format])]ref-expr1[||ref-expr2]
        // @wife.name@ @wife.name;  @(wife.name); @{wife.name}  @<%=wife.name%>

        // @data('wife.name'); @wife.name;  @(wife.name); @{wife.name}  @<%=wife.name%>

        // @(s){data('wife.name') + s};  // @{data('wife.name')};

        // @<%=data('wife.name')%>

        // @ref:(@string:'名字:{}')wife.name
        // @ref:(@string:'名字:{}')wife.nickname||wife.nickname||'无名氏'
        // @ref:(@string:'yyyy年MM月dd日')wife.birthday

        if (StringUtils.startsWithIgnoreCase(value,RefNodeConvert.PRE_TAG)){
            result = new RefNodeConvert(value);

        } if (value.matches(RefNodeConvert.BESIEGE_REGEX)){
            result = new RefNodeConvert(value);

        } if (value.matches(RefNodeConvert.BESIEGE_REGEX)){
            result = new RefNodeConvert(value);
        }



        return result;
    }
}
