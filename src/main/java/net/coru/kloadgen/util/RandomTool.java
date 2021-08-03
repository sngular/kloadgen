/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.kloadgen.util;

import com.github.curiousoddman.rgxgen.RgxGen;
import net.coru.kloadgen.exception.KLoadGenException;
import net.coru.kloadgen.model.ConstraintTypeEnum;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public final class RandomTool {

    private static final Set<String> VALID_TYPES = SetUtils
            .hashSet("map", "enum", "string", "int", "long", "timestamp", "stringTimestamp", "short", "double", "longTimestamp", "uuid", "array",
                    "boolean", "bytes");

    private RandomTool() {
    }

    public static boolean isTypeValid(String type) {
        return VALID_TYPES.contains(type);
    }

    public static Map<String, Object> generateRandomMap(String fieldType, Integer mapSize, List<String> fieldValueList,
                                                        Map<ConstraintTypeEnum, String> constrains) {
        Map value = new HashMap(mapSize);
        switch (fieldType) {
            case "int-map":
                value.putAll(generateIntMap(mapSize, fieldValueList, mapSize, constrains));
                break;
            case "long-map":
                value.putAll(generateLongMap(mapSize, fieldValueList, mapSize, constrains));
                break;
            case "double-map":
                value.putAll(generateDoubleMap(mapSize, fieldValueList, mapSize, constrains));
                break;
            case "short-map":
                value.putAll(generateShortMap(mapSize, fieldValueList, mapSize, constrains));
                break;
            case "float-map":
                value.putAll(generateFloatMap(mapSize, fieldValueList, mapSize, constrains));
                break;
            case "uuid-map":
                value.putAll(generateUuidMap(mapSize, fieldValueList));
                break;
            case "boolean-map":
                value = generateBooleanMap(mapSize, fieldValueList);
                break;
            default:
                value.putAll(generateStringMap(mapSize, fieldValueList, mapSize, constrains));
                break;
        }
        return value;
    }

    public static List generateRandomArray(String fieldType, Integer valueLength, List<String> fieldValueList, Integer arraySize,
                                           Map<ConstraintTypeEnum, String> constrains) {
        List value;
        switch (fieldType) {
            case "int-array":
                value = generateIntArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "number-array":
                value = generateNumberArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "long-array":
                value = generateLongArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "double-array":
                value = generateDoubleArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "short-array":
                value = generateShortArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "float-array":
                value = generateFloatArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "string-array":
                value = generateStringArray(arraySize, valueLength, fieldValueList, constrains);
                break;
            case "uuid-array":
                value = generateUuidArray(arraySize, fieldValueList);
                break;
            case "boolean-array":
                value = generateBooleanArray(arraySize, fieldValueList);
                break;
            default:
                value = new ArrayList<>();
                break;
        }
        return value;
    }

    public static Object generateRandom(String fieldType, Integer valueLength, List<String> fieldValueList,
                                        Map<ConstraintTypeEnum, String> constrains) {
        Object value;
        switch (fieldType) {
            case "string":
                value = getStringValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "number":
                value = getNumberValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "int":
                value = getIntValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "long":
                value = getLongValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "short":
                value = getShortValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "double":
                value = getDoubleValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "float":
                value = getFloatValueOrRandom(valueLength, fieldValueList, constrains);
                break;
            case "bytes":
                value = getByteRandom(valueLength);
                break;
            case "timestamp":
            case "longTimestamp":
            case "stringTimestamp":
                value = getTimestampValueOrRandom(fieldType, fieldValueList);
                break;
            case "uuid":
                value = getUUIDValueOrRandom(fieldValueList);
                break;
            case "boolean":
                value = getBooleanValueOrRandom(fieldValueList);
                break;
            case "enum":
                value = getEnumValueOrRandom(fieldValueList);
                break;
            default:
                value = fieldType;
                break;
        }
        return value;
    }

    private static String getEnumValueOrRandom(List<String> fieldValueList) {
        String value;
        if (!fieldValueList.isEmpty()) {
            value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
        } else {
            throw new KLoadGenException("Wrong enums values, problem in the parsing process");
        }
        return value;
    }

    protected static Object castValue(Object value, String type) {
        Object castValue;
        switch (type) {
            case "int":
                castValue = Integer.valueOf(value.toString());
                break;
            case "double":
                castValue = Double.valueOf(value.toString());
                break;
            case "long":
                castValue = Long.valueOf(value.toString());
                break;
            case "float":
                castValue = Float.valueOf(value.toString());
                break;
            case "boolean":
                castValue = Boolean.valueOf(value.toString());
                break;
            default:
                castValue = value.toString();
                break;
        }
        return castValue;
    }

    private static List<Integer> generateIntArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                  Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Integer> intArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            intArray.add(getIntValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return intArray;
    }

    private static List<Number> generateNumberArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                    Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Number> intArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            intArray.add(getIntValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return intArray;
    }

    private static List<Long> generateLongArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Long> longArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            longArray.add(getLongValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return longArray;
    }

    private static List<Double> generateDoubleArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                    Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Double> doubleArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            doubleArray.add(getDoubleValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return doubleArray;
    }

    private static List<Short> generateShortArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                  Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Short> shortArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            shortArray.add(getShortValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return shortArray;
    }

    private static List<Float> generateFloatArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                  Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Float> floatArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            floatArray.add(getFloatValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return floatArray;
    }

    private static List<String> generateStringArray(Integer arraySize, Integer valueLength, List<String> fieldValueList,
                                                    Map<ConstraintTypeEnum, String> constrains) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<String> stringArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            stringArray.add(getStringValueOrRandom(valueLength, fieldValueList, constrains));
        }
        return stringArray;
    }

    private static List<UUID> generateUuidArray(Integer arraySize, List<String> fieldValueList) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<UUID> uuidArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            uuidArray.add(getUUIDValueOrRandom(fieldValueList));
        }
        return uuidArray;
    }

    private static List<Boolean> generateBooleanArray(Integer arraySize, List<String> fieldValueList) {
        int size = arraySize == 0 ? RandomUtils.nextInt(1, 5) : arraySize;
        List<Boolean> booleanArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            booleanArray.add(getBooleanValueOrRandom(fieldValueList));
        }
        return booleanArray;
    }

    private static Map<String, Integer> generateIntMap(Integer mapSize, List<String> fieldValueList, int valueLength,
                                                       Map<ConstraintTypeEnum, String> constrains) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Integer> intMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (intMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    intMap.put(tempValue[0], Integer.parseInt(tempValue[1]));
                } else {
                    intMap.put(tempValue[0], getIntValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (intMap.size() != mapSize) {
            for (int i = 0; i < Math.abs(intMap.size() - mapSize); i++) {
                intMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getIntValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return intMap;
    }

    private static Map<String, Long> generateLongMap(int mapSize, List<String> fieldValueList, int valueLength,
                                                     Map<ConstraintTypeEnum, String> constrains) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Long> longMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (longMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    longMap.put(tempValue[0], Long.parseLong(tempValue[1]));
                } else {
                    longMap.put(tempValue[0], getLongValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (longMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(longMap.size() - mapSize); i++) {
                longMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getLongValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return longMap;
    }

    private static Map<String, Double> generateDoubleMap(int mapSize, List<String> fieldValueList, int valueLength,
                                                         Map<ConstraintTypeEnum, String> constrains) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Double> doubleMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (doubleMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    doubleMap.put(tempValue[0], Double.parseDouble(tempValue[1]));
                } else {
                    doubleMap.put(tempValue[0], getDoubleValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (doubleMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(doubleMap.size() - mapSize); i++) {
                doubleMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getDoubleValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return doubleMap;
    }

    private static Map<String, Float> generateFloatMap(int mapSize, List<String> fieldValueList, int valueLength,
                                                       Map<ConstraintTypeEnum, String> constrains) {

        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Float> floatMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (floatMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    floatMap.put(tempValue[0], Float.parseFloat(tempValue[1]));
                } else {
                    floatMap.put(tempValue[0], getFloatValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (floatMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(floatMap.size() - mapSize); i++) {
                floatMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getFloatValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return floatMap;
    }

    private static Map<String, Short> generateShortMap(int mapSize, List<String> fieldValueList, int valueLength,
                                                       Map<ConstraintTypeEnum, String> constrains) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Short> shortMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (shortMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    shortMap.put(tempValue[0], Short.parseShort(tempValue[1]));
                } else {
                    shortMap.put(tempValue[0], getShortValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (shortMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(shortMap.size() - mapSize); i++) {
                shortMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getShortValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return shortMap;
    }

    private static Map<String, String> generateStringMap(int mapSize, List<String> fieldValueList, int valueLength,
                                                         Map<ConstraintTypeEnum, String> constrains) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, String> stringMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (stringMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    stringMap.put(tempValue[0], tempValue[1]);
                } else {
                    stringMap.put(tempValue[0], getStringValueOrRandom(valueLength, Collections.emptyList(), constrains));
                }
            }
        }
        if (stringMap.size() != mapSize) {
            //Math.abs(stringMap.size() - mapSize)
            for (int i = 0; i < mapSize; i++) {
                stringMap.put(getStringValueOrRandom(valueLength, Collections.emptyList(), constrains),
                        getStringValueOrRandom(valueLength, Collections.emptyList(), constrains));
            }
        }
        return stringMap;
    }

    private static Map<String, UUID> generateUuidMap(int mapSize, List<String> fieldValueList) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, UUID> uuidMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (uuidMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    uuidMap.put(tempValue[0], UUID.fromString(tempValue[1]));
                } else {
                    uuidMap.put(tempValue[0], getUUIDValueOrRandom(Collections.emptyList()));
                }
            }
        }
        if (uuidMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(uuidMap.size() - mapSize); i++) {
                uuidMap.put(getStringValueOrRandom(0, Collections.emptyList(), Collections.emptyMap()),
                        getUUIDValueOrRandom(Collections.emptyList()));
            }
        }
        return uuidMap;
    }

    private static Map<String, Boolean> generateBooleanMap(Integer mapSize, List<String> fieldValueList) {
        int size = mapSize > 0 ? mapSize : RandomUtils.nextInt(1, 5);
        Map<String, Boolean> booleanMap = new HashMap<>(size);
        if (!fieldValueList.isEmpty()) {
            while (booleanMap.size() < Math.min(size, fieldValueList.size())) {
                String[] tempValue = getMapEntryValue(fieldValueList);
                if (tempValue.length > 1) {
                    booleanMap.put(tempValue[0], Boolean.parseBoolean(tempValue[1]));
                } else {
                    booleanMap.put(tempValue[0], getBooleanValueOrRandom(Collections.emptyList()));
                }
            }
        }
        if (booleanMap.size() != mapSize) {
            for (int i = 0; i <= Math.abs(booleanMap.size() - mapSize); i++) {
                booleanMap.put(getStringValueOrRandom(0, Collections.emptyList(), Collections.emptyMap()),
                        getBooleanValueOrRandom(Collections.emptyList()));
            }
        }
        return booleanMap;
    }

    private static String[] getMapEntryValue(List<String> fieldValueList) {
        return fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim().split(":");
    }

    private static Integer getIntValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                               Map<ConstraintTypeEnum, String> constrains) {
        int value;
        if (!fieldValueList.isEmpty()) {
            value = Integer.parseInt(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        } else {
            Number minimum = calculateMinimum(valueLength, constrains);
            Number maximum = calculateMaximum(valueLength, constrains);
            if (Integer.MAX_VALUE < ((BigDecimal) maximum).longValueExact()) {
                maximum = Integer.MAX_VALUE;
            }
            if (Integer.MAX_VALUE < minimum.longValue()) {
                minimum = Integer.MIN_VALUE;
            }
            if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
                int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
                maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
                value = RandomUtils.nextInt(minimum.intValue(), maximum.intValue()) * multipleOf;
            } else {
                value = RandomUtils.nextInt(minimum.intValue(), maximum.intValue());
            }
        }
        return value;
    }

    private static Number getNumberValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                                 Map<ConstraintTypeEnum, String> constrains) {
        Number value;
        if (!fieldValueList.isEmpty()) {
            String chosenValue = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
            if (chosenValue.contains(".")) {
                value = Float.parseFloat(chosenValue);
            } else {
                value = Integer.parseInt(chosenValue);
            }
        } else {
            Number minimum = calculateMinimum(valueLength, constrains);
            Number maximum = calculateMaximum(valueLength, constrains);
            if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
                int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
                maximum = maximum.intValue() > multipleOf ? maximum.intValue() / multipleOf : maximum;
                value = RandomUtils.nextFloat(minimum.intValue(), maximum.intValue()) * multipleOf;
            } else {
                value = RandomUtils.nextFloat(minimum.intValue(), maximum.intValue());
            }
        }
        return value;
    }

    private static Long getLongValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                             Map<ConstraintTypeEnum, String> constrains) {
        long value;
        if (!fieldValueList.isEmpty()) {
            value = Long.parseLong(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        } else {
            value = getRandomLongValue(valueLength, constrains);
        }
        return value;
    }

    private static long getRandomLongValue(Integer valueLength, Map<ConstraintTypeEnum, String> constrains) {
        long value;
        Number minimum = calculateMinimum(valueLength, constrains);
        Number maximum = calculateMaximum(valueLength, constrains);
        if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
            int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
            maximum = maximum.longValue() > multipleOf ? maximum.longValue() / multipleOf : maximum;
            value = RandomUtils.nextLong(minimum.longValue(), maximum.longValue()) * multipleOf;
        } else {
            value = RandomUtils.nextLong(minimum.longValue(), maximum.longValue());
        }
        return value;
    }

    private static Double getDoubleValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                                 Map<ConstraintTypeEnum, String> constrains) {
        double value;
        if (!fieldValueList.isEmpty()) {
            value = Double.parseDouble(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        } else {
            long intPart = getRandomLongValue(valueLength / 2, constrains);
            long decPart = getRandomLongValue(valueLength / 2, constrains);
            value = Double.parseDouble(intPart + "." + decPart);
        }
        return value;
    }

    private static Float getFloatValueOrRandom(Integer valueLength, List<String> fieldValuesList,
                                               Map<ConstraintTypeEnum, String> constrains) {
        float value;
        if (!fieldValuesList.isEmpty()) {
            value = Float.parseFloat(fieldValuesList.get(RandomUtils.nextInt(0, fieldValuesList.size())).trim());
        } else {
            Long intPart = getRandomLongValue(valueLength / 2, constrains);
            Long decPart = getRandomLongValue(valueLength / 2, constrains);
            value = new BigDecimal(intPart + "." + decPart).floatValue();
        }
        return value;
    }

    private static Number calculateMaximum(int valueLength, Map<ConstraintTypeEnum, String> constrains) {
        Number maximum;
        if (constrains.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
            if (constrains.containsKey(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) {
                maximum = Integer.parseInt(constrains.get(ConstraintTypeEnum.EXCLUDED_MAXIMUM_VALUE)) - 1L;
            } else {
                maximum = Integer.parseInt(constrains.get(ConstraintTypeEnum.MAXIMUM_VALUE));
            }
        } else {
            maximum = new BigDecimal(StringUtils.rightPad("1", valueLength + 1, '0'));
        }
        return maximum;
    }

    private static Number calculateMinimum(int valueLength, Map<ConstraintTypeEnum, String> constrains) {
        Number minimum;
        if (constrains.containsKey(ConstraintTypeEnum.MINIMUM_VALUE)) {
            if (constrains.containsKey(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) {
                minimum = Integer.parseInt(constrains.get(ConstraintTypeEnum.EXCLUDED_MINIMUM_VALUE)) - 1;
            } else {
                minimum = Integer.parseInt(constrains.get(ConstraintTypeEnum.MINIMUM_VALUE));
            }
        } else {
            minimum = Long.parseLong(StringUtils.rightPad("1", valueLength, '0'));
        }
        return minimum;
    }

    private static ByteBuffer getByteRandom(Integer valueLength) {
        ByteBuffer value;
        if (valueLength == 0) {
            value = ByteBuffer.wrap(RandomUtils.nextBytes(4));
        } else {
            value = ByteBuffer.wrap(RandomUtils.nextBytes(valueLength));
        }
        return value;
    }

    private static String getStringValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                                 Map<ConstraintTypeEnum, String> constrains) {
        String value;
        if (!fieldValueList.isEmpty()) {
            value = fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim();
        } else {
            if (constrains.containsKey(ConstraintTypeEnum.REGEX)) {
                RgxGen rxGenerator = new RgxGen(constrains.get(ConstraintTypeEnum.REGEX));
                value = rxGenerator.generate();
                if (valueLength > 0 || constrains.containsKey(ConstraintTypeEnum.MAXIMUM_VALUE)) {
                    value = value.substring(0, getMaxLength(valueLength, constrains.get(ConstraintTypeEnum.MAXIMUM_VALUE)));
                }
            } else {
                value = RandomStringUtils.randomAlphabetic(valueLength == 0 ? RandomUtils.nextInt(1, 20) : valueLength);
            }
        }
        return value;
    }

    private static int getMaxLength(Integer valueLength, String maxValueStr) {
        int maxValue = Integer.parseInt(StringUtils.defaultIfEmpty(maxValueStr, "0"));
        if (valueLength > 0 && maxValue == 0) {
            maxValue = valueLength;
        }
        return maxValue;
    }

    private static Short getShortValueOrRandom(Integer valueLength, List<String> fieldValueList,
                                               Map<ConstraintTypeEnum, String> constrains) {
        short value;
        if (!fieldValueList.isEmpty()) {
            value = Short.parseShort(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        } else {
            if (valueLength < 5) {
                Number minimum = calculateMinimum(valueLength, constrains);
                Number maximum = calculateMaximum(valueLength, constrains);
                if (constrains.containsKey(ConstraintTypeEnum.MULTIPLE_OF)) {
                    int multipleOf = Integer.parseInt(constrains.get(ConstraintTypeEnum.MULTIPLE_OF));
                    maximum = maximum.shortValue() > multipleOf ? maximum.shortValue() / multipleOf : maximum;
                    value = (short) (RandomUtils.nextInt(minimum.shortValue(), maximum.shortValue()) * multipleOf);
                } else {
                    value = (short) RandomUtils.nextInt(minimum.shortValue(), maximum.shortValue());
                }
            } else {
                value = (short) RandomUtils.nextInt(1, 32767);
            }
        }
        return value;
    }

    private static int calculateSize(int valueLength) {
        return Math.max(valueLength, 0);
    }

    private static Object getTimestampValueOrRandom(String type, List<String> fieldValueList) {
        LocalDateTime value;
        if (!fieldValueList.isEmpty()) {
            value = LocalDateTime.parse(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        } else {
            value = LocalDateTime.now();
        }
        if ("longTimestamp".equalsIgnoreCase(type)) {
            return value.toInstant(ZoneOffset.UTC).toEpochMilli();
        } else if ("stringTimestamp".equalsIgnoreCase(type)) {
            return value.toString();
        }
        return value;
    }

    private static UUID getUUIDValueOrRandom(List<String> fieldValueList) {
        UUID value = UUID.randomUUID();
        if (!fieldValueList.isEmpty()) {
            value = UUID.fromString(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        }
        return value;
    }

    private static Boolean getBooleanValueOrRandom(List<String> fieldValueList) {
        boolean value = RandomUtils.nextBoolean();
        if (!fieldValueList.isEmpty()) {
            value = Boolean.parseBoolean(fieldValueList.get(RandomUtils.nextInt(0, fieldValueList.size())).trim());
        }
        return value;
    }

    public static Object generateSeq(String fieldName, String fieldType, List<String> fieldValueList, Map<String, Object> context) {

        return RandomTool.castValue(
                context.compute(fieldName, (fieldNameMap,
                                            seqObject) -> seqObject == null ? getSafeValue(fieldValueList) : ((Long) seqObject) + 1),
                fieldType);
    }

    private static Long getSafeValue(List<String> fieldValueList) {
        return fieldValueList.isEmpty() ? 1L : Long.parseLong(fieldValueList.get(0));
    }
}
