package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;

public interface IConvert {
    public JSON convert(JSON template, JSON data);
}
