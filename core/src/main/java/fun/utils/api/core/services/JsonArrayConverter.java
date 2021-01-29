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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Converts a comma-delimited String to an Array.
 * Only matches if String.class can be converted to the target array element type.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public class JsonArrayConverter implements GenericConverter {

    private final ConversionService defConversionService;
    public JsonArrayConverter(ConversionService defConversionService) {
        this.defConversionService = defConversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, List.class));
    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        String string = StringUtils.defaultString((String) source,"").trim();
        TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
        if (string.matches("\\[.+\\]")) {
            return JSON.parseArray((String) source, targetElementType.getType());
        } else if (string.matches("\\{.+\\}")) {
            return Arrays.asList(JSON.parseObject((String) source, targetElementType.getType()));
        } else {
            return defConversionService.convert(source, sourceType, targetType);
        }
    }

}
