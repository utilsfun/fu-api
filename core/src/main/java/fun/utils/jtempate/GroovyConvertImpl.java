package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.ClassUtils;

public class GroovyConvertImpl implements IConvert {


    @Override
    public JSON convert(JSON template, JSON data) {
        return null;
    }


    public Object convert(Object value, JSON data,JSONObject node,VarThree varThree) {

        if (value == null){
            return null;
        }
        else if (value instanceof String){
            String stringValue = (String)value;
            NodeConvert nodeConvert = NodeConvertCreator.create(stringValue);
            return nodeConvert.convert(data,node,varThree);
        }
        else if (value instanceof JSONObject){

            JSONObject target = new JSONObject();
            JSONObject objectValue = (JSONObject)value;
            objectValue.keySet().forEach(k-> {
                Object v = objectValue.get(k);
                Object v1 = convert(v, data,objectValue,varThree);
                String k1 = ClassUtils.castValue(convert(k,data,objectValue,varThree),String.class);
                target.put(k1,v1);
            });
            return target;

        }
        else if (value instanceof JSONArray){
            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray)value;
            arrayValue.forEach(v->{
                target.add(convert(v, data,node,varThree));
            });
            return  target;
        }
        else{
            return  value;
        }
    }

}
