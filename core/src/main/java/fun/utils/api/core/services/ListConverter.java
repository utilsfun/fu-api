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


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ListConverter implements GenericConverter {


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, List.class));
    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }

        String string = (String) source;

        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
        List target = new ArrayList();

        JSON json = null;
        try {
            json = (JSON) JSON.parse(string);
        } catch (Exception e) {
        }

        if (json == null) {

            String[] fields = StringUtils.split(string, ",");
            for (int i = 0; i < fields.length; i++) {
                String sourceElement = fields[i];
                Object targetElement = convert(sourceElement.trim(), sourceType, targetElementType);
                target.add(targetElement);
            }

        } else if (json instanceof JSONArray) {

            JSONArray jsonArray = (JSONArray) json;

            for (int i = 0; i < jsonArray.size(); i++) {
                Object targetElement = TypeUtils.castToJavaBean(jsonArray.get(i), targetElementType.getType());
                target.add(targetElement);
            }

        } else if (json instanceof JSONObject) {

            target.add(JSON.toJavaObject(json, targetElementType.getType()));

        } else {

            return convert(json.toString(), sourceType, targetType);

        }

        return targetType.isArray() ? target.toArray() : target;

    }

}


