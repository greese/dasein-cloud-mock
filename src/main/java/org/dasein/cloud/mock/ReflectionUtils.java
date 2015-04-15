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
import java.lang.reflect.Type;
import java.util.*;

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
            if (source == null) {
                return null;
            } else {
                return (T) source;
            }
        }
    }

    public static Iterable<Object> convertToList(Object source, Type type) {

        List<Object> result = new ArrayList<Object>();
        if(source instanceof Iterable) {
            Iterator iterator = ((Iterable)source).iterator();
            while(iterator.hasNext()) {
                result.add(convert(type, iterator.next()));
            }
        } else {
            result.add(convert(type, source));
        }
        return result;
    }

    public static Map<Object, Object> convertToMap(Object obj, Type keyType, Type valType) {
        Map<Object, Object> result = new LinkedHashMap<Object, Object>();
        if(obj instanceof Map) {
            Set<Map.Entry<Object, Object>> entries = ((Map) obj).entrySet();
            for (Map.Entry entry : entries) {
                result.put(convert(keyType, entry.getKey()), convert(valType, entry.getValue()));
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

    public static Object convert(Type type, Object value){
        Object result = null;

        if(type instanceof Class)
            result = convertToSingleType(value, (Class)type);
        else if(type instanceof ParameterizedType){
            ParameterizedType pType = (ParameterizedType) type;
            Class clz = (Class) pType.getRawType();

            if(clz.isAssignableFrom(List.class)) { //Map is not Iterable
                Type elementType = pType.getActualTypeArguments()[0];
                result = convertToList(value, elementType);
            } else if(clz.isAssignableFrom(Map.class)) {
                Type keyType = pType.getActualTypeArguments()[0];
                Type valueType = pType.getActualTypeArguments()[1];
                result = convertToMap(value, keyType, valueType);
            }
        }
        return result;
    }

    public static void setField(Object targetObj, Field targetField, Object value) {
        Object result = convert(targetField.getGenericType(), value);

        targetField.setAccessible(true);
        try {
            targetField.set(targetObj, result);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException(illegalAccessException);
        }
    }
}
