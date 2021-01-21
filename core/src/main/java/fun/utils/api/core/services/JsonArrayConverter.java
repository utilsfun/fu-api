package fun.utils.api.core.services;
/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Converts a comma-delimited String to an Array.
 * Only matches if String.class can be converted to the target array element type.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public class JsonArrayConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public JsonArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, List.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {

        TypeDescriptor sourceElementType = sourceType;
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();

        if (targetElementType == null) {
            // yes
            return true;
        }

        if (sourceElementType == null) {
            // maybe
            return true;
        }
        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            // yes
            return true;
        }
        if (ClassUtils.isAssignable(sourceElementType.getType(), targetElementType.getType())) {
            // maybe
            return true;
        }
        // no
        return false;

    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String string = (String) source;
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();


        JSON json = null;
        try{
            json = (JSON) JSON.parse(string);
        } catch (Exception e) {
        }

        if (json == null){
            String[] fields = StringUtils.split(string,",");
            Object target = Array.newInstance(targetElementType.getType(), fields.length);
            for (int i = 0; i < fields.length; i++) {
                String sourceElement = fields[i];
                //Object targetElement = this.conversionService.convert(sourceElement.trim(), sourceType, targetElementType);
                Object targetElement =TypeUtils.castToJavaBean(sourceElement.trim(), targetElementType.getType());
                Array.set(target, i, targetElement);
            }

            if (targetType.isCollection()){
                return Arrays.asList(target);
            }else{
                return target;
            }

        }else if (json instanceof JSONArray){

            JSONArray jsonArray = (JSONArray)json;

            Object target = Array.newInstance(targetElementType.getType(), jsonArray.size());



            for (int i = 0; i < jsonArray.size(); i++) {
                Object targetElement =TypeUtils.castToJavaBean(jsonArray.get(i), targetElementType.getType());
                Array.set(target, i, targetElement);
            }

            if (targetType.isCollection()){
                return Arrays.asList((Object[])target);
            }else{
                return (Object[])target;
            }


        }if (json instanceof JSONObject){

            return Arrays.asList(JSON.toJavaObject(json,targetElementType.getType()));

        }else{

            return convert(json.toString(), sourceType, targetType);

        }

    }

}
