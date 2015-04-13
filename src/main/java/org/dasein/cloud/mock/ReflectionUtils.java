/*
 * *
 *  * Copyright (C) 2009-2015 Dell, Inc.
 *  * See annotations for authorship information
 *  *
 *  * ====================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * ====================================================================
 *
 */

package org.dasein.cloud.mock;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jeffrey Yan on 3/19/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public abstract class ReflectionUtils {
    public static Boolean convertToBoolean(Object source) {
        if (source instanceof Boolean) {
            return (Boolean) source;
        } else {
            return Boolean.valueOf(source.toString());
        }
    }

    public static String convertToString(Object source) {
        if (source == null) {
            return "";
        }
        return source.toString();
    }

    public static Enum convertToEnum(Object source, Class<Enum> targetClz) {
        if (source == null) {
            return null;
        }
        return Enum.valueOf(targetClz, source.toString());
    }

    public static <T> T convertToSingleType(Object source, Class<T> targetClz) {
        if (Boolean.class.isAssignableFrom(targetClz) || Boolean.TYPE.isAssignableFrom(targetClz)) {
            return (T) convertToBoolean(source);
        } else if (Enum.class.isAssignableFrom(targetClz)) {
            return (T) convertToEnum(source, (Class<Enum>) targetClz);
        } else if (String.class.isAssignableFrom(targetClz)) {
            return (T) convertToString(source);
        } else {
            return (T) source;
        }
    }

    public static <T> Iterable<T> convertToList(Object source, Class<T> targetElementClz) {
        List<T> result = new ArrayList<T>();
        if(source instanceof Iterable) {
            Iterator iterator = ((Iterable)source).iterator();
            while(iterator.hasNext()) {
                result.add(convertToSingleType(iterator.next(), targetElementClz));
            }
        } else {
            result.add(convertToSingleType(source, targetElementClz));
        }
        return result;
    }

    public static <K, V> Map<K, V> convertToMap(Object obj, Class<K> targetKeyClz, Class<V> targetValueClz) {
        Map<K, V> result = new LinkedHashMap<K, V>();
        if(obj instanceof Map) {
            Set<Map.Entry<K, V>> entries = ((Map) obj).entrySet();
            for (Map.Entry entry : entries) {
                result.put(convertToSingleType(entry.getKey(), targetKeyClz), convertToSingleType(entry.getValue(), targetValueClz));
            }
        } else {
            throw new IllegalArgumentException("Cannot convert " + obj.getClass() + " to java.util.Map");
        }
        return result;
    }

    public static Field getField(Class<?> targetClz, String name) {
        try {
            return targetClz.getDeclaredField(name);
        } catch (NoSuchFieldException ignore) {
            throw new IllegalArgumentException("No field " + name + " in class " + targetClz);
        }
    }

    public static void setField(Object targetObj, Field targetField, Object value) {
        Object result;

        Class<?> fieldClz = targetField.getType();
        if(fieldClz.isAssignableFrom(List.class)) { //Map is not Iterable
            ParameterizedType parameterizedType = (ParameterizedType) targetField.getGenericType();
            Class<?> element = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            result = convertToList(value, element);
        } else if(fieldClz.isAssignableFrom(Map.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) targetField.getGenericType();
            Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
            result = convertToMap(value, keyType, valueType);
        } else {
            result = convertToSingleType(value, fieldClz);
        }

        targetField.setAccessible(true);
        try {
            targetField.set(targetObj, result);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException(illegalAccessException);
        }
    }
}
