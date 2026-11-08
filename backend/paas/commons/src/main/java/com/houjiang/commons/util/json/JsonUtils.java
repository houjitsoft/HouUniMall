package com.houjiang.commons.util.json;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public class JsonUtils {

	private static ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJson6(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {
			Object value = ((Map.Entry) map).getValue();
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<ParseJson> parseJsonDouble2(String str) {

		Map maps = (Map) JSON.parse(str);
		List<ParseJson> list = new ArrayList<ParseJson>();
		for (Object map : maps.entrySet()) {

			Object value = ((Map.Entry) map).getValue();
			Map maps2 = (Map) JSON.parse(JsonUtils.toString(value));
			for (Object map2 : maps2.entrySet()) {
				String value2 = (String) ((Map.Entry) map2).getValue();
				Object key = ((Map.Entry) map2).getKey();
				ParseJson parseJson = new ParseJson();
				parseJson.setSpecId(key);
				parseJson.setSpecValue(value2);
				list.add(parseJson);
			}

		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJsonDouble3(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {

			Object value = ((Map.Entry) map).getValue();
			Map maps2 = (Map) JSON.parse(JsonUtils.toString(value));
			for (Object map2 : maps2.entrySet()) {
				Object value2 = ((Map.Entry) map2).getValue();
				// Object key = ((Map.Entry) map2).getKey();
				list.add(value2);
			}

		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJsonDouble4(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {

			Object value = ((Map.Entry) map).getValue();
			Map maps2 = (Map) JSON.parse(JsonUtils.toString(value));
			for (Object map2 : maps2.entrySet()) {
				// Object value2 = ((Map.Entry) map2).getValue();
				Object key = ((Map.Entry) map2).getKey();
				list.add(key);
			}

		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJsonDouble5(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {

			Object value = ((Map.Entry) map).getValue();
			Map maps2 = (Map) JSON.parse(JsonUtils.toString(value));
			for (Object map2 : maps2.entrySet()) {
				Object value2 = ((Map.Entry) map2).getValue();
				// Object key = ((Map.Entry) map2).getKey();
				list.add(value2);
			}

		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<ParseJson> parseJson2(String str) {

		Map maps = (Map) JSON.parse(str);
		List<ParseJson> list = new ArrayList<ParseJson>();
		for (Object map : maps.entrySet()) {
			Object key = ((Map.Entry) map).getKey();
			Object value = ((Map.Entry) map).getValue();
			ParseJson parseJson = new ParseJson();
			parseJson.setSpecId(key);
			parseJson.setSpecValue(value);
			list.add(parseJson);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJson3(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {
			Object key = ((Map.Entry) map).getKey();
			list.add(key);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJson(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {
			Object value = ((Map.Entry) map).getValue();
			list.add(value);
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	public static Integer parseJsonGet(String str) {
		String goodsId = null;
		Map maps = (Map) JSON.parse(str);
		JsonObject bean = null;
		for (Object map : maps.entrySet()) {
			Object value = ((Map.Entry) map).getValue();
			goodsId = String.valueOf(value);
			bean = JsonUtils.toBean(goodsId, JsonObject.class);
			// list.add(value);
		}
		return Integer.parseInt(bean.getGoods_id());
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> parseJsonDouble(String str) {

		Map maps = (Map) JSON.parse(str);
		List<Object> list = new ArrayList<Object>();
		for (Object map : maps.entrySet()) {

			Object value = ((Map.Entry) map).getValue();
			Map maps2 = (Map) JSON.parse(JsonUtils.toString(value));
			for (Object map2 : maps2.entrySet()) {
				String value2 = (String) ((Map.Entry) map2).getValue();
				list.add(value2);
			}

		}
		return list;
	}

	/**
	 * Object转json字符串
	 * 
	 * @param obj
	 * @param <T>
	 * @return
	 */
	public static <T> String toString(T obj) {
		if (obj == null) {
			return null;
		}
		try {
			return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 数组的json对象转化为数组
	 * 
	 * @param str
	 * @return
	 */
	public static List<Integer> StringToList(String str) {
		List<Integer> list = new ArrayList<>();
		if (null != str && !str.equals("")) {
			String[] split = str.substring(1, str.length() - 1).split(",");
			if (split.length > 0 && !split[0].equals("")) {
				for (String string : split) {
					list.add(Integer.parseInt(string));
				}
			}
		}

		return list;
	}

	public static List<String> StringToList4(String str) {
		List<String> list = new ArrayList<>();
		if (null != str && !str.equals("")) {
			String[] split = str.substring(1, str.length() - 1).split(",");
			if (split.length > 0 && !split[0].equals("")) {
				for (String string : split) {
					list.add(string.substring(1, string.length() - 1));
				}
			}
		}
		return list;
	}

	public static List<Integer> StringToList2(String str) {

		String[] split = str.split(",");
		List<Integer> list = new ArrayList<>();
		for (String string : split) {
			list.add(Integer.parseInt(string.trim()));
		}
		return list;
	}

	public static List<String> StringToList3(String str) {

		String[] split = str.split(",");
		List<String> list = new ArrayList<>();
		for (String string : split) {
			list.add(string.trim());
		}
		return list;
	}

	public static <T> T toBean(String str, Class<T> cls) {
		if (null == str) {
			return null;
		}
		System.out.println(" ");
		System.out.println(" "+str);
		try {
			System.out.println(objectMapper.readValue(str, cls));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println(" ");
		try {
			return objectMapper.readValue(str, cls);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * string转object 用于转为集合对象
	 *
	 * @param str
	 *            json字符串
	 * @param collectionClass
	 *            被转集合class
	 * @param elementClasses
	 *            被转集合中对象类型class
	 * @param <T>
	 * @return
	 */
	public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses) {
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
		try {
			return objectMapper.readValue(str, javaType);
		} catch (IOException e) {
			return null;
		}

	}

	/**
	 * string转object
	 * @param str json字符串
	 * @param clazz 被转对象class
	 * @param <T>
	 * @return
	 */
	public static <T> T string2Obj(String str,Class<T> clazz){
		if (StringUtils.isEmpty(str) || clazz == null){
			return null;
		}
		try {
			return clazz.equals(String.class)? (T) str :objectMapper.readValue(str,clazz);
		} catch (IOException e) {
			System.out.println("Parse String to Object error");
			e.printStackTrace();
			return null;
		}
	}

}
