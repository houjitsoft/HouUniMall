package com.houjiang.commons.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.houjiang.commons.util.excel.ExcelsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * springboot 导入导出excel
 * 
 * @author Amos
 *
 */
public class EasyPoiUtils {

	public static String exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName,
			boolean isCreateHeader, HttpServletResponse response) {
		ExportParams exportParams = new ExportParams(title, sheetName);
		exportParams.setCreateHeadRows(isCreateHeader);
		return defaultExport(list, pojoClass, fileName, response, exportParams);

	}

	private static String defaultExport(List<?> list, Class<?> pojoClass, String fileName, HttpServletResponse response,
			ExportParams exportParams) {

		Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);

		return downLoadExcel(fileName, response, workbook);
	}

	private static String downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {

		return ExcelsUtil.getFileBase64URLString(workbook, fileName);
	}

	public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
		if (StringUtils.isBlank(filePath)) {
			return null;
		}
		ImportParams params = new ImportParams();
		params.setTitleRows(titleRows);
		params.setHeadRows(headerRows);

		List<T> list = null;
		try {
			list = ExcelImportUtil.importExcel(new File(filePath), pojoClass, params);
		} catch (Exception e) {
		}
		return list;
	}

}
