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

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Jeffrey Yan on 3/19/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public abstract class ReflectionUtils {
    private static Boolean convertToBoolean(Object source) {
        if (source instanceof Boolean) {
            return (Boolean) source;
        } else {
            return Boolean.valueOf(source.toString());
        }
    }

    private static String convertToString(Object source) {
        if (source == null) {
            return "";
        }
        return source.toString();
    }

    private static Enum convertToEnum(Object source, Class<Enum> targetClz) {
        if (source == null) {
            return null;
        }
        return Enum.valueOf(targetClz, source.toString());
    }

    private static <T> T convertToSimpleType(Object source, Class<T> targetClz) {
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

    private static <T> T convertToComplexType(Object source, Class<T> targetClz) {
        if(source instanceof Map) {
            try {
                Constructor<T> constructor = null;

                Constructor<?>[] constructors = targetClz.getDeclaredConstructors();
                for (Constructor c : constructors) {
                    if (c.getParameterTypes().length == 0) {
                        constructor = c;
                        break;
                    }
                }
                if (constructor == null) {
                    throw new IllegalArgumentException(targetClz + "has no default constructor");
                }

                constructor.setAccessible(true);
                T result = constructor.newInstance(new Object[0]);

                for (Map.Entry<String, ?> configEntry : ((Map<String, ?>)source).entrySet()) {
                    Field field = getField(targetClz, configEntry.getKey());
                    setField(result, field, configEntry.getValue());
                }
                return result;
            } catch (InvocationTargetException invocationTargetException) {
                throw new IllegalArgumentException(invocationTargetException);
            } catch (InstantiationException instantiationException) {
                throw new IllegalArgumentException("Not able to instantiate " + targetClz + " instance");
            } catch (IllegalAccessException illegalAccessException) {
                throw new IllegalArgumentException("Has no access to " + targetClz + " default constructor");
            }
        } else {
            throw new IllegalArgumentException("Cannot convert " + source.getClass() + " to " + targetClz);
        }
    }

    private static List<Object> convertToList(Object source, Type type) {
        List<Object> result = new ArrayList<Object>();
        if(source instanceof Iterable) {
            Iterator iterator = ((Iterable)source).iterator();
            while(iterator.hasNext()) {
                result.add(convert(iterator.next(), type));
            }
        } else {
            result.add(convert(source, type));
        }
        return result;
    }

    private static Map<Object, Object> convertToMap(Object obj, Type keyType, Type valType) {
        Map<Object, Object> result = new LinkedHashMap<Object, Object>();
        if(obj instanceof Map) {
            Set<Map.Entry<Object, Object>> entries = ((Map) obj).entrySet();
            for (Map.Entry entry : entries) {
                result.put(convert(entry.getKey(), keyType), convert(entry.getValue(), valType));
            }
        } else {
            throw new IllegalArgumentException("Cannot convert " + obj.getClass() + " to java.util.Map");
        }
        return result;
    }

    public static Object convert(Object value, Type type){
        Object result = null;

        if(type instanceof Class){
            Class clz = (Class) type;
            if (!Enum.class.isAssignableFrom(clz) && clz.getCanonicalName().contains("org.dasein.cloud")) {
                result = convertToComplexType(value, clz);
            } else {
                result = convertToSimpleType(value, clz);
            }
        } else if(type instanceof ParameterizedType){
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
        } else {
            throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to " + type);
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
        Object result = convert(value, targetField.getGenericType());

        targetField.setAccessible(true);
        try {
            targetField.set(targetObj, result);
        } catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException(illegalAccessException);
        }
    }
}
