package com.houjiang.commons.util;


import com.houjiang.commons.vo.order.WapShippingQueryResultVO;

import java.util.*;

/**
 * @author Amos
 */
public class ListSort {

	/**
	 * 按照List<Map<String,Object>>里面map的某个value重新封装成多个不同的list, 原始数据类型List<Map
	 * <String,Object>>, 转换后数据类型Map<String,List<Map<String,Object>>>
	 *
	 * @param list
	 * @param oneMapKey
	 * @return
	 */
	public static Map<String, Object> change(List<Map<String, Object>> list, String oneMapKey) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Set<Object> setTmp = new HashSet<Object>();
		for (Map<String, Object> tmp : list) {
			setTmp.add(tmp.get(oneMapKey));
		}
		Iterator<Object> it = setTmp.iterator();
		while (it.hasNext()) {
			String oneSetTmpStr = (String) it.next();
			List<Map<String, Object>> oneSetTmpList = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> tmp : list) {
				String oneMapValueStr = (String) tmp.get(oneMapKey);
				if (oneMapValueStr.equals(oneSetTmpStr)) {
					oneSetTmpList.add(tmp);
				}
			}
			resultMap.put(oneSetTmpStr, oneSetTmpList);
		}
		return resultMap;
	}

	/**
	 * 按照List<Map<String,Object>>里面map的某个value重新封装成多个不同的list, 原始数据类型List<Map
	 * <String,Object>>, 转换后数据类型Map<String,List<Map<String,Object>>>
	 *
	 * @param inList
	 * @param oneMapKey
	 * @return
	 */
	private static List<Map<String, Object>> change2(List<Map<String, Object>> inList, String oneMapKey,
			List<Map<String, Object>> outList) {
		// 1.将某个key的值存在set中
		Set<Object> setTmp = new HashSet<Object>();
		for (Map<String, Object> tmp : inList) {
			setTmp.add(tmp.get(oneMapKey));
		}
		// 2.遍历set
		Iterator<Object> it = setTmp.iterator();
		while (it.hasNext()) {
			String oneMapValueStr = "";
			String oneSetTmpStr = (String) it.next();
			Map<String, Object> oneSetTmpMap = new HashMap<String, Object>();
			List<Map<String, Object>> oneSetTmpList = new ArrayList<Map<String, Object>>();

			for (Map<String, Object> tmp : inList) {
				oneMapValueStr = (String) tmp.get(oneMapKey);
				if (oneSetTmpStr.equals(oneMapValueStr)) {
					oneSetTmpMap.put("text", oneSetTmpStr);
					oneSetTmpList.add(tmp);
				}
			}
			oneSetTmpMap.put("array", oneSetTmpList);
			outList.add(oneSetTmpMap);
		}
		return outList;
	}

	public static Map<Integer, List<WapShippingQueryResultVO>> byTransportAreaIdGroup(
			// map是用来接收分好的组的
			List<WapShippingQueryResultVO> goodsBaseList) {

		Map<Integer, List<WapShippingQueryResultVO>> map = new HashMap<Integer, List<WapShippingQueryResultVO>>();
		if (null == goodsBaseList) {
			return null;
		}
		Integer key;
		List<WapShippingQueryResultVO> listTmp;
		for (WapShippingQueryResultVO goodsBase : goodsBaseList) {
			// 按这个属性分组，map的Key
			key = goodsBase.getTransportTemplate();
			listTmp = map.get(key);
			if (null == listTmp) {
				listTmp = new ArrayList<>();
			}
			listTmp.add(goodsBase);
			map.put(key, listTmp);
		}
		return map;
	}
	public static Map<Integer, List<WapShippingQueryResultVO>> byInternationalTransportAreaIdGroup(
			// map是用来接收分好的组的
			List<WapShippingQueryResultVO> goodsBaseList) {

		Map<Integer, List<WapShippingQueryResultVO>> map = new HashMap<Integer, List<WapShippingQueryResultVO>>();
		if (null == goodsBaseList) {
			return null;
		}
		Integer key;
		List<WapShippingQueryResultVO> listTmp;
		for (WapShippingQueryResultVO goodsBase : goodsBaseList) {
			// 按这个属性分组，map的Key
			key = goodsBase.getInternationalTransportTemplate();
			listTmp = map.get(key);
			if (null == listTmp) {
				listTmp = new ArrayList<>();
			}
			listTmp.add(goodsBase);
			map.put(key, listTmp);
		}
		return map;
	}


}
