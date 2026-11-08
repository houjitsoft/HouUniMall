package com.houjiang.commons.util.excel;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
public class ExcelUtil {

	private static Logger LG = LoggerFactory.getLogger(ExcelUtil.class);

	private Class<?> clazz;

	/**
	 * 构造函数，用于兼容现有控制器代码
	 */
	public <T> ExcelUtil(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * 根据annotation的seq排序后的栏位
	 *
	 * @param clazz
	 * @return
	 */
	private static List<FieldForSortting> sortFieldByAnno(Class<?> clazz) {
		Field[] fieldsArr = clazz.getDeclaredFields();
		List<FieldForSortting> fields = new ArrayList<>();
		List<FieldForSortting> annoNullFields = new ArrayList<>();
		for (Field field : fieldsArr) {
			ExcelCell ec = field.getAnnotation(ExcelCell.class);
			if (ec == null) {
				// 没有ExcelCell Annotation 视为不汇入
				continue;
			}
			int id = ec.index();
			fields.add(new FieldForSortting(field, id));
		}
		fields.addAll(annoNullFields);
		sortByProperties(fields, true, false, "index");
		return fields;
	}

	private static void sortByProperties(List<? extends Object> list, boolean isNullHigh, boolean isReversed,
			String... props) {
		if (CollectionUtils.isNotEmpty(list)) {
			Comparator<?> typeComp = ComparableComparator.getInstance();
			if (isNullHigh == true) {
				typeComp = ComparatorUtils.nullHighComparator(typeComp);
			} else {
				typeComp = ComparatorUtils.nullLowComparator(typeComp);
			}
			if (isReversed) {
				typeComp = ComparatorUtils.reversedComparator(typeComp);
			}

			List<Object> sortCols = new ArrayList<Object>();

			if (props != null) {
				for (String prop : props) {
					sortCols.add(new BeanComparator(prop, typeComp));
				}
			}
			if (sortCols.size() > 0) {
				Comparator<Object> sortChain = new ComparatorChain(sortCols);
				Collections.sort(list, sortChain);
			}
		}
	}

	/** 判断文件是否存在 */
	public static File judeFileExists(String path) {
		File file = new File(path);
		if (file.exists()) {
			System.out.println("file exists");
		} else {
			System.out.println("file not exists, create it ...");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/** 创建文件 */
	public static String exportExcel(Map<String, String> map, Collection<Object> dataset, String path, String fileName,
			HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=GBK");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
		/*
		 //
/**
		 * if (path == null || "".equals(path)) { path = "e://" + fileName; }
		 * 判断文件是否存在 File file = judeFileExists(path); OutputStream out = new
		 * FileOutputStream(file);
		 */

		File pathurl = new File(ResourceUtils.getURL("classpath:").getPath());

		if (path == null || "".equals(path)) {
			path = pathurl + "/static/" + fileName;
		}
		// 判断文件是否存在
		/* path="e://test.csv"; */
		File file = judeFileExists(path);

		OutputStream out = new FileOutputStream(file);

		// ServletOutputStream out=response.getOutputStream();

		ExcelUtil.exportExcel(map, dataset, out);
		out.close();
		
		 //return "http://47.105.158.195:81/mallplatformmanager/"+fileName;
	

		return Constants.PLATFORM_URL  + fileName;

	}

	/**
	 * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
	 * 用于单个sheet
	 *
	 * @param <T>
	 * @param headers
	 *            表格属性列名数组
	 * @param dataset
	 *            需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
	 *            javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
	 * @param out
	 *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
	 */
	public static <T> void exportExcel(Map<String, String> headers, Collection<T> dataset, OutputStream out) {
		exportExcel(headers, dataset, out, null);
	}

	/**
	 * 利用JAVA的反射机制，将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
	 * 用于单个sheet
	 *
	 * @param <T>
	 * @param headers
	 *            表格属性列名数组
	 * @param dataset
	 *            需要显示的数据集合,集合中一定要放置符合javabean风格的类的对象。此方法支持的
	 *            javabean属性的数据类型有基本数据类型及String,Date,String[],Double[]
	 * @param out
	 *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
	 * @param pattern
	 *            如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
	 */
	public static <T> void exportExcel(Map<String, String> headers, Collection<T> dataset, OutputStream out,
			String pattern) {
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet();

		write2Sheet(sheet, headers, dataset, pattern);
		try {
			workbook.write(out);
		} catch (IOException e) {
			LG.error(e.toString(), e);
		}
	}

	/**
	 * 每个sheet的写入
	 *
	 * @param sheet
	 *            页签
	 * @param headers
	 *            表头
	 * @param dataset
	 *            数据集合
	 * @param pattern
	 *            日期格式
	 */
	private static <T> void write2Sheet(HSSFSheet sheet, Map<String, String> headers, Collection<T> dataset,
			String pattern) {
		// 时间格式默认"yyyy-MM-dd"
		if (StringUtils.isEmpty(pattern)) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		// 标题行转中文
		Set<String> keys = headers.keySet();
		Iterator<String> it1 = keys.iterator();
		// 存放临时键变量
		String key = "";
		// 标题列数
		int c = 0;
		while (it1.hasNext()) {
			key = it1.next();
			if (headers.containsKey(key)) {
				HSSFCell cell = row.createCell(c);
				HSSFRichTextString text = new HSSFRichTextString(headers.get(key));
				cell.setCellValue(text);
				c++;
			}
		}

		// 遍历集合数据，产生数据行
		Iterator<T> it = dataset.iterator();
		int index = 0;
		while (it.hasNext()) {
			index++;
			row = sheet.createRow(index);
			T t = it.next();
			try {
				if (t instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) t;
					int cellNum = 0;
					// 遍历列名
					Iterator<String> it2 = keys.iterator();
					while (it2.hasNext()) {
						key = it2.next();
						if (!headers.containsKey(key)) {
							LG.error("Map 中 不存在 key [" + key + "]");
							continue;
						}
						Object value = map.get(key);
						HSSFCell cell = row.createCell(cellNum);

						cellNum = setCellValue(cell, value, pattern, cellNum, null, row);

						cellNum++;
					}
				} else {
					List<FieldForSortting> fields = sortFieldByAnno(t.getClass());
					int cellNum = 0;
					for (int i = 0; i < fields.size(); i++) {
						HSSFCell cell = row.createCell(cellNum);
						Field field = fields.get(i).getField();
						field.setAccessible(true);
						Object value = field.get(t);

						cellNum = setCellValue(cell, value, pattern, cellNum, field, row);

						cellNum++;
					}
				}
			} catch (Exception e) {
				LG.error(e.toString(), e);
			}
		}
		// 设定自动宽度
		for (int i = 0; i < headers.size(); i++) {
			sheet.autoSizeColumn(6);
		}
	}

	private static int setCellValue(HSSFCell cell, Object value, String pattern, int cellNum, Field field,
			HSSFRow row) {
		String textValue = null;
		if (value instanceof Integer) {
			int intValue = (Integer) value;
			cell.setCellValue(intValue);
		} else if (value instanceof Float) {
			float fValue = (Float) value;
			cell.setCellValue(fValue);
		} else if (value instanceof Double) {
			double dValue = (Double) value;
			cell.setCellValue(dValue);
		} else if (value instanceof Long) {
			long longValue = (Long) value;
			cell.setCellValue(longValue);
		} else if (value instanceof Boolean) {
			boolean bValue = (Boolean) value;
			cell.setCellValue(bValue);
		} else if (value instanceof Date) {
			Date date = (Date) value;
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			textValue = sdf.format(date);
		} else if (value instanceof String[]) {
			String[] strArr = (String[]) value;
			for (int j = 0; j < strArr.length; j++) {
				String str = strArr[j];
				cell.setCellValue(str);
				if (j != strArr.length - 1) {
					cellNum++;
					cell = row.createCell(cellNum);
				}
			}
		} else if (value instanceof Double[]) {
			Double[] douArr = (Double[]) value;
			for (int j = 0; j < douArr.length; j++) {
				Double val = douArr[j];
				// 值不为空则set Value
				if (val != null) {
					cell.setCellValue(val);
				}

				if (j != douArr.length - 1) {
					cellNum++;
					cell = row.createCell(cellNum);
				}
			}
		} else {
			// 其它数据类型都当作字符串简单处理
			String empty = StringUtils.EMPTY;
			if (field != null) {
				ExcelCell anno = field.getAnnotation(ExcelCell.class);
				if (anno != null) {
					empty = anno.defaultValue();
				}
			}
			textValue = value == null ? empty : value.toString();
		}
		if (textValue != null) {
			HSSFRichTextString richString = new HSSFRichTextString(textValue);
			cell.setCellValue(richString);
		}
		return cellNum;
	}

	/**
	 * 实例方法，用于导出Excel，兼容现有控制器代码
	 *
	 * @param response HttpServletResponse
	 * @param list 数据列表
	 * @param fileName 文件名
	 */
	public void exportExcel(jakarta.servlet.http.HttpServletResponse response, List<?> list, String fileName) {
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName + ".xls", "UTF-8"));

			// 创建表头映射（这里使用简单的方式，实际项目中应该从注解中获取）
			Map<String, String> headers = new HashMap<>();
			if (clazz != null) {
				List<FieldForSortting> fields = sortFieldByAnno(clazz);
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i).getField();
					ExcelCell annotation = field.getAnnotation(ExcelCell.class);
					if (annotation != null) {
						// 使用字段名作为表头
						headers.put(field.getName(), field.getName());
					} else {
						headers.put(field.getName(), field.getName());
					}
				}
			}

			// 调用静态方法导出
			exportExcel(headers, list, response.getOutputStream());
		} catch (Exception e) {
			LG.error("导出Excel失败", e);
		}
	}
}
