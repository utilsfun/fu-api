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
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Set;

public class JsonObjectConverter implements GenericConverter {

    private final ConversionService defConversionService;


    public JsonObjectConverter(ConversionService defConversionService) {
        this.defConversionService = defConversionService;
    }


    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Object.class));
    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        if (defConversionService.canConvert(sourceType, targetType)) {
            return defConversionService.convert(source, sourceType, targetType);
        } else {
            if (ParserConfig.getGlobalInstance().get(targetType.getType()) == null) {
                return JSON.parseObject(JSON.toJSONString(source), targetType.getType());
            } else {
                return TypeUtils.castToJavaBean(source, targetType.getType());
            }
        }
    }

}
