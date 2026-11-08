package com.houjiang.commons.util.excel;

import com.google.common.collect.Lists;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.goods.StorageVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.STRING;

/**
 * @author Amos
 */
public class ExcelsUtil {

    private static final int MIN_ROW = 1;

    /**
     * 普通excel
     *
     * @param title
     * @param maps
     * @param mergeIndex
     * @return
     */
    public static String createExcel(String[] title,
                                     Map<String/* sheet名 */, List<Map<String/* 对应title的值 */, String>>> maps, int[] mergeIndex) {
        if (title.length == 0) {
            return null;
        }
        /* 初始化excel模板 */
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = null;

        int n = 0;
        /* 循环sheet页 */
        for (Map.Entry<String, List<Map<String/* 对应title的值 */, String>>> entry : maps.entrySet()) {
            /* 实例化sheet对象并且设置sheet名称，book对象 */
            try {
                sheet = workbook.createSheet();
                workbook.setSheetName(n, entry.getKey());
                workbook.setSelectedTab(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            /* 初始化head，填值标题行（第一行） */
            Row row0 = sheet.createRow(0);
            for (int i = 0; i < title.length; i++) {
                /* 创建单元格，指定类型 */
                Cell cell_1 = row0.createCell(i, STRING);
                cell_1.setCellValue(title[i]);
            }
            /* 得到当前sheet下的数据集合 */
            List<Map<String/* 对应title的值 */, String>> list = entry.getValue();
            /* 遍历该数据集合 */
            List<PoiModel> poiModels = Lists.newArrayList();
            if (null != workbook) {
                Iterator iterator = list.iterator();
                int index = 1;/* 这里1是从excel的第二行开始，第一行已经塞入标题了 */
                int key = 1;
                while (iterator.hasNext()) {
                    Row row = sheet.createRow(index);
                    /* 取得当前这行的map，该map中以key，value的形式存着这一行值 */
                    Map<String, String> map = (Map<String, String>) iterator.next();
                    /* 循环列数，给当前行塞值 */
                    for (int i = 0; i < title.length; i++) {
                        String old = "";
                        /* old存的是上一行统一位置的单元的值，第一行是最上一行了，所以从第二行开始记 */
                        if (index > 1) {
                            old = poiModels.get(i) == null ? "" : poiModels.get(i).getContent();
                        }
                        /* 循环需要合并的列 */
                        for (int k : mergeIndex) {
                            if (index == 1) {
                                /* 记录第一行的开始行和开始列 */
                                PoiModel poiModel = new PoiModel();
                                poiModel.setOldContent(map.get(title[i]));
                                poiModel.setContent(map.get(title[i]));
                                poiModel.setRowIndex(1);
                                poiModel.setCellIndex(i);
                                poiModels.add(poiModel);
                                break;
                            } else if (i > 0 && k == i) {/* 这边i>0也是因为第一列已经是最前一列了，只能从第二列开始 */
                                /* 当前同一列的内容与上一行同一列不同时，把那以上的合并, 或者在当前元素一样的情况下，前一列的元素并不一样，这种情况也合并 */
                                /*
/**
                                 * 如果不需要考虑当前行与上一行内容相同，但是它们的前一列内容不一样则不合并的情况，把下面条件中||poiModels.get(i).getContent()
                                 * .equals(map.get(title[i])) && !poiModels.get(i -
                                 * 1).getOldContent().equals(map.get(title[i-1]))去掉就行
                                 */
                                if (!poiModels.get(i).getContent().equals(map.get(title[i]))) {
                                    /* 当前行的当前列与上一行的当前列的内容不一致时，则把当前行以上的合并 */
                                    if (sheet.getRow(poiModels.get(i).getRowIndex()).getCell(0).getStringCellValue().equals(sheet.getRow(index - 1).getCell(0).getStringCellValue())) {
                                        setMergeCell(sheet, poiModels.get(i).getRowIndex()/* 从第二行开始 */, index - 1/* 到第几行 */,
                                                poiModels.get(i).getCellIndex()/* 从某一列开始 */,
                                                poiModels.get(i).getCellIndex()/* 到第几列 */);
                                    }

                                    /* 重新记录该列的内容为当前内容，行标记改为当前行标记，列标记则为当前列 */
                                    poiModels.get(i).setContent(map.get(title[i]));
                                    poiModels.get(i).setRowIndex(index);
                                    poiModels.get(i).setCellIndex(i);
                                }

                            }
                            /* 处理第一列的情况 */
                            if (k == i && i == 0
                                    && !poiModels.get(i).getContent().equals(map.get(title[i]))) {
                                /* 当前行的当前列与上一行的当前列的内容不一致时，则把当前行以上的合并 */
                                if (sheet.getRow(poiModels.get(i).getRowIndex()).getCell(0).getStringCellValue().equals(sheet.getRow(index - 1).getCell(0).getStringCellValue())) {
                                    setMergeCell(sheet, poiModels.get(i).getRowIndex()/* 从第二行开始 */, index - 1/* 到第几行 */,
                                            poiModels.get(i).getCellIndex()/* 从某一列开始 */,
                                            poiModels.get(i).getCellIndex()/* 到第几列 */);
                                }

                                /* 重新记录该列的内容为当前内容，行标记改为当前行标记 */
                                poiModels.get(i).setContent(map.get(title[i]).trim());
                                poiModels.get(i).setRowIndex(index);
                                poiModels.get(i).setCellIndex(i);
                            }

                            /* 最后一行没有后续的行与之比较，所有当到最后一行时则直接合并对应列的相同内容 */
                            if (k == i && index == list.size()) {
                                if (index > 2 && sheet.getRow(index - 2).getCell(0).getStringCellValue().equals(sheet.getRow(index - 1).getCell(0).getStringCellValue())) {
                                    setMergeCell(sheet, poiModels.get(i).getRowIndex()/* 从第二行开始 */, index/* 到第几行 */,
                                            poiModels.get(i).getCellIndex()/* 从某一列开始 */,
                                            poiModels.get(i).getCellIndex()/* 到第几列 */);
                                }
                            }
                        }

                        Cell cell = row.createCell(i, STRING);
                        cell.setCellValue(map.get(title[i]));
                        /* 在每一个单元格处理完成后，把这个单元格内容设置为old内容 */
                        poiModels.get(i).setOldContent(old);
                    }
                    index++;
                }
            }
            n++;
        }
        /* 生成临时文件 */
        String fileName = String.valueOf(System.currentTimeMillis() / 1000);
        return getFileBase64URLString(workbook, fileName);
    }

    /**
     * 设置合并单元格
     *
     * @param sheet            sheet页对象
     * @param startRowIndex    开始行号
     * @param endRowIndex      结束行号
     * @param startColumnIndex 开始列号
     * @param endColumnIndex   结束列号
     */
    public static void setMergeCell(Sheet sheet, int startRowIndex, int endRowIndex, int startColumnIndex, int endColumnIndex) {
        //合并单元格区域只有一个单元格时，不合并
        if (endRowIndex == startRowIndex && endColumnIndex == startColumnIndex) {
            return;
        }
        //添加合并单元格区域
        CellRangeAddress cellRangeAddress = new CellRangeAddress(startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
        sheet.addMergedRegionUnsafe(cellRangeAddress);
    }

    /**
     * 读取excel
     *
     * @param file
     * @return
     */
    public static ResponseResult readExcel(MultipartFile file) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            Workbook wookbook = new HSSFWorkbook(file.getInputStream());
            // 设置计算函数
            FormulaEvaluator formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) wookbook);
            Sheet sheet = wookbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                Map<String, Object> map = new HashMap<>();
                if (null == row || row.getPhysicalNumberOfCells() == 0) {
                    continue;
                }
                Cell orderId = row.getCell(1);
                if (null != orderId) {
                    map.put("orderId", getValue(orderId));
                }
                Cell goodsId = row.getCell(17);
                if (null != goodsId) {
                    map.put("goodsId", getValue(goodsId));
                }
                Cell goodsNum = row.getCell(12);
                if (null != goodsNum) {
                    map.put("goodsNum", getValue(goodsNum));
                }
                Cell orderShippingExpressId = row.getCell(9);
                if (null != orderShippingExpressId) {
                    map.put("orderShippingExpressId", getValue(orderShippingExpressId));
                }
                Cell orderShippingCode = row.getCell(11);
                if (null != orderShippingCode) {
                    map.put("orderShippingCode", getValue(orderShippingCode));
                }
                Cell orderSellerName = row.getCell(19);
                if (null != orderSellerName) {
                    map.put("orderSellerName", getValue(orderSellerName));
                }
                Cell orderSellerAddress = row.getCell(20);
                if (null != orderSellerAddress) {
                    map.put("orderSellerAddress", getValue(orderSellerAddress));
                }
                Cell orderSellerContact = row.getCell(21);
                if (null != orderSellerContact) {
                    map.put("orderSellerContact", getValue(orderSellerContact));
                }
                Cell shippingAddressId = row.getCell(22);
                if (null != shippingAddressId) {
                    map.put("shippingAddressId", getValue(shippingAddressId));
                }
                Cell storageName = row.getCell(23);
                if (null != storageName) {
                    map.put("storageName", getValue(storageName));
                }
                Cell storageId = row.getCell(24);
                if (null != storageId) {
                    map.put("storageId", getValue(storageId));
                }
                list.add(map);
            }
            Integer index = 0;
            for (Map<String, Object> objectMap : list) {
                if (objectMap == null) {
                    return ResponseResult.fail();
                }
                if (!result.isEmpty() && !result.get(index).get("orderShippingCode").equals(objectMap.get("orderShippingCode"))
                        && objectMap.get("orderId").equals(result.get(index).get("orderId"))) {
                    return ResponseResult.fail(0, "同订单编号下物流单号只能相同");
                }
                List<Integer> goodsIds = new ArrayList<>();
                List<Integer> goodsNums = new ArrayList<>();
                if (!result.isEmpty() && objectMap.get("orderId").equals(result.get(index).get("orderId"))) {
                    goodsIds = (List<Integer>) result.get(index).get("goodsIds");
                    goodsIds.add(Integer.parseInt(objectMap.get("goodsId").toString()));
                    goodsNums = (List<Integer>) result.get(index).get("goodsNums");
                    goodsNums.add(Integer.parseInt(objectMap.get("goodsNum").toString()));
                } else {
                    goodsIds.add(Integer.parseInt(objectMap.get("goodsId").toString()));
                    goodsNums.add(Integer.parseInt(objectMap.get("goodsNum").toString()));
                }
                objectMap.put("goodsIds", goodsIds);
                objectMap.put("goodsNums", goodsNums);
                if (result.isEmpty()) {
                    result.add(objectMap);
                } else if (!objectMap.get("orderId").equals(result.get(index).get("orderId"))) {
                    result.add(objectMap);
                    index++;
                } else {
                    result.set(index, objectMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "请上传正确表格！");
        }
        return ResponseResult.success(result);
    }

    /**
     * 获取数据类型
     *
     * @param xSSFCell
     * @return
     */
    public static String getValue(Cell xSSFCell) {
        if (null == xSSFCell) {
            return "";
        }
        String cellValue = "";
        // 以下是判断数据的类型
        //HSSFCell.CELL_TYPE_NUMERIC:
        switch (xSSFCell.getCellType()) {
            case NUMERIC:
                xSSFCell.setCellType(STRING);
                cellValue = xSSFCell.getStringCellValue();
                break;
            // 字符串
            case STRING:
                cellValue = xSSFCell.getStringCellValue();
                break;
            // Boolean
            case BOOLEAN:
                cellValue = xSSFCell.getBooleanCellValue() + "";
                break;
            // 公式
            case FORMULA:
                cellValue = xSSFCell.getCellFormula() + "";
                break;
            // 空值
            case BLANK:
            // 故障
            case ERROR:
            default:
                cellValue = "";
                break;
        }
        return cellValue;
    }

    /**
     * 获取Excel单元格的值，并将其转换为字符串形式。
     *
     * @param xSSFCell Excel中的单元格对象。
     * @return 单元格的值，如果单元格为null，则返回空字符串。
     */
    public static String getValue(Sheet sheet, List<CellRangeAddress> mergedRegions, Cell xSSFCell) {
        // 检查单元格是否为null，如果是，则直接返回空字符串
        if (null == xSSFCell) {
            return "";
        }
        // 检查单元格是否属于合并区域
        for (CellRangeAddress region : mergedRegions) {
            if (region.isInRange(xSSFCell.getRowIndex(), xSSFCell.getColumnIndex())) {
                // 如果属于合并区域，获取左上角单元格的值
                Row firstRow = sheet.getRow(region.getFirstRow());
                Cell firstCell = firstRow.getCell(region.getFirstColumn());
                // 使用工具类获取值
                return ExcelsUtil.getValue(firstCell);
            }
        }
        // 如果不属于合并区域，直接获取单元格的值
        return ExcelsUtil.getValue(xSSFCell);
    }

    /**
     * 判断字符串是否为URL
     *
     * @param urls 用户头像key
     * @return true:是URL、false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        if (StringUtils.isNotEmpty(urls)) {
            if (urls.startsWith("http://") || urls.startsWith("https://")) {
                isurl = true;
            }
        }
        return isurl;
    }


    public static boolean isHttpUrl(Cell cell) {
        if (null != cell) {
            String path = getValue(cell);
            if (StringUtils.isNotEmpty(path)) {
                return isHttpUrl(path);
            }
        }
        return true;

    }


    /**
     * 返回根据文件生成的url地址
     *
     * @param workbook
     * @return
     */
    public static String getFileBase64URLString(Workbook workbook, String fileName) {
        FileOutputStream out = null;
        String url = null;
        try {
            File tempFile = File.createTempFile(fileName, ".xlsx");
            out = new FileOutputStream(tempFile);
            workbook.write(out);
            url = Base64Utils.fileToBase64(tempFile);
            System.out.println(url);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return url;
        }
    }


    /**
     * 检查Excel 主图
     *
     * @param sheet
     * @return
     */
    public static List<String> checkMainImages(Sheet sheet) {
        int rows = sheet.getPhysicalNumberOfRows();
        Map<String, String> mainGoodsImages = new HashMap<String, String>();
        // 主图重复，后 存在codeNumber
        List<String> errorMainImages = new ArrayList<String>();
        // 解析的编码
        List<String> codeNumbers = new ArrayList<String>();
        // 检查主图的唯一性
        for (int i = 1; i < rows; i++) {
            Row row = sheet.getRow(i);
            if (null != row && row.getPhysicalNumberOfCells() > 0) {
                Cell goodsImagesCell1 = row.getCell(17);
                Cell codeNumberCell = row.getCell(0);
                if (null != goodsImagesCell1 && null != codeNumberCell) {
                    String codeNumber = getValue(codeNumberCell);
                    String mainGoodsImage = getValue(goodsImagesCell1);
                    // 编码不存在，说明是第一次解析到
                    if (!codeNumbers.contains(codeNumber)) {
                        codeNumbers.add(codeNumber);
                        if (StringUtils.isNotEmpty(mainGoodsImage)) {
                            // 主图名字已经存在
                            if (mainGoodsImages.containsKey(mainGoodsImage)) {
                                errorMainImages.add(codeNumber);
                                errorMainImages.add(mainGoodsImages.get(mainGoodsImage));
                            } else {
                                mainGoodsImages.put(mainGoodsImage, codeNumber);
                            }
                        }
                    }
                }
            }
        }
        return errorMainImages;
    }

    /**
     * 校验 相同编码 shopId是否相同
     *
     * @param sheet
     * @return 返回错误的编码集合
     */
    public static List<String> checkShopId(Sheet sheet) {
        int rows = sheet.getPhysicalNumberOfRows();
        // 用来存储错误的shopId的编码
        List<String> errorShopIds = new ArrayList<String>();
        // 存储codeNumber 和 shopId 的对应关系
        Map<String, String> codeNumberShopIds = new HashMap<>();
        for (int index = 1; index < rows; index++) {
            Row row = sheet.getRow(index);
            if (null != row && row.getPhysicalNumberOfCells() > 0) {
                Cell shopIdCell = row.getCell(1);
                Cell codeNumberCell = row.getCell(0);
                if (null != shopIdCell && null != codeNumberCell) {
                    String codeNumber = getValue(codeNumberCell);
                    String shopId = getValue(shopIdCell);
                    if (StringUtils.isNotEmpty(shopId) && StringUtils.isNotEmpty(codeNumber)) {
                        if (codeNumberShopIds.containsKey(codeNumber)) {
                            String shopIdMap = codeNumberShopIds.get(codeNumber);
                            if (!shopIdMap.equals(shopId)) {
                                errorShopIds.add(codeNumber);
                            }
                        } else {
                            codeNumberShopIds.put(codeNumber, shopId);
                        }
                    }
                }
            }
        }
        return errorShopIds;
    }

    /**
     * 校验规格是否相同，不相同返回编码
     */
    public static List<String> checkSpec(Sheet sheet) {
        int rows = sheet.getPhysicalNumberOfRows();
        // 用来存储不相同的规格类型的编码
        List<String> errorSpeces = new ArrayList<String>();
        // 存储codeNumber 和 spec规格 的对应关系
        Map<String, String> codeNumberSpeces = new HashMap<>();
        for (int index = 1; index < rows; index++) {
            Row row = sheet.getRow(index);
            if (null != row && row.getPhysicalNumberOfCells() > 0) {
                Cell codeNumberCell = row.getCell(0);
                Cell specCell = row.getCell(13);
                if (null != codeNumberCell && null != specCell) {
                    String codeNumber = getValue(codeNumberCell);
                    String specStr = getValue(specCell);
                    if (StringUtils.isNotEmpty(specStr) && StringUtils.isNotEmpty(codeNumber)) {
                        if (codeNumberSpeces.containsKey(codeNumber)) {
                            String oldSpec = codeNumberSpeces.get(codeNumber);
                            if (!specStr.equals(oldSpec)) {
                                errorSpeces.add(codeNumber);
                            }
                        }
                    } else {
                        codeNumberSpeces.put(codeNumber, specStr);
                    }
                }
            }
        }
        return errorSpeces;
    }

    /**
     * 创建excel workboot
     *
     * @return
     */
    public static Workbook createWorkBook() {
        return new HSSFWorkbook();
    }

    /**
     * 创建sheet
     *
     * @param workbook
     * @param sheetName
     * @return
     */
    public static Sheet createSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        // 创建表头格式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置单元格格式
        DataFormat dataFormat = workbook.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("@"));
        return sheet;
    }

    /**
     * 单元格写入数据
     *
     * @param sheet
     * @param title
     * @param dataList
     */
    public static void writeSheet(Sheet sheet, String[] title, List<Map<String, String>> dataList) {
        // 写入表头
        writeTitle(sheet, title);
        // 0行已经被写了表头
        int startRowIndex = 1;
        for (Map<String, String> dataMapper : dataList) {
            Row row = sheet.createRow(startRowIndex);
            for (int titleSeq = 0; titleSeq < title.length; titleSeq++) {
                String rowValue = dataMapper.get(title[titleSeq]);
                Cell cell = row.createCell(titleSeq, CellType.STRING);
                cell.setCellValue(rowValue);
            }
            startRowIndex++;
        }
    }

    public static void writeTitle(Sheet sheet, String[] title) {
        // 写入表头
        Row titleRow = sheet.createRow(0);
        for (int titleSeq = 0; titleSeq < title.length; titleSeq++) {
            Cell titleCell = titleRow.createCell(titleSeq);
            titleCell.setCellValue(title[titleSeq]);
        }
    }

    /**
     * 设置一对一级联行
     *
     * @param workbook      工作单元
     * @param linkData      连接数据
     * @param hidTitle      映射sheet表头
     * @param isHidden      是否隐藏
     * @param targetSheet   目标sheet
     * @param targetDataCnt 目标sheet数据条数
     * @param parentCol     父Col字符
     * @param childCol      子col字符
     */
    public static void createLinkageValidation(
            Workbook workbook,
            List<List<StorageVO>> linkData,
            String[] hidTitle,
            boolean isHidden,
            Sheet targetSheet,
            int targetDataCnt,
            char parentCol,
            char childCol
    ) {
        int lastRowNum = targetSheet.getLastRowNum();
        for (Integer n = 0; n < lastRowNum; n++) {
            String nameSheet = "xshell" + n;
            // 创建隐藏sheet
            Sheet hidSheet = workbook.createSheet(nameSheet);
            workbook.setSheetHidden(workbook.getSheetIndex(hidSheet), isHidden);
            // 设置hidTitle
            Row hidTitleRow = hidSheet.createRow(0);
            for (int hidTitleSeq = 0; hidTitleSeq < hidTitle.length; hidTitleSeq++) {
                Cell hidTitleCell = hidTitleRow.createCell(hidTitleSeq);
                hidTitleCell.setCellValue(hidTitle[hidTitleSeq]);
            }
            // 遍历父col值
            int rowIndex = 1;
            for (StorageVO entry : linkData.get(n)) {
                Row row = hidSheet.createRow(rowIndex);
                row.createCell(0).setCellValue(entry.getStorageName());
                row.createCell(1).setCellValue(entry.getStorageId().toString());
                rowIndex++;
            }
            int b = n + 1;
            // 获取覆盖行
            Row targetRow = targetSheet.getRow(b);
            // 获取映射列
            Cell childCell = targetRow.getCell((int) childCol - 'A');
            if (childCell == null) {
                childCell = targetRow.createCell((int) childCol - 'A');
            }
            // 获取选择列
            String parentCell = String.valueOf(parentCol) + (b + 1);
            String formula = String.format(
                    "IF(ISNA(VLOOKUP(%s,%s!A:B,2,0)),\"\",VLOOKUP(%s,%s!A:B,2,0))",
                    parentCell, nameSheet, parentCell, nameSheet
            );
            childCell.setCellFormula(formula);

            // 设置下拉列表
            String nameName = parentCol + "xshell" + b + "_parent";
            Name name = workbook.createName();
            name.setNameName(nameName);
            // 这里跳过第一行 因为第一行为标题行
            // 因为第一行是标题 所以要多加一行
            name.setRefersToFormula(nameSheet + "!$A$2:$A$" + linkData.get(n).size() + 1);
            DVConstraint constraint = DVConstraint.createFormulaListConstraint(nameName);
            CellRangeAddressList regions = new CellRangeAddressList(
                    n, n + 1, (int) parentCol - 'A', (int) parentCol - 'A'
            );
            targetSheet.addValidationData(new HSSFDataValidation(regions, constraint));
        }
    }

    public static String getFileUrl(Workbook workbook) throws Exception {
        String fileName = String.valueOf(System.currentTimeMillis() / 1000);
        FileOutputStream out = null;
        String localPath = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile(fileName, ".xlsx");
            File pathurl = new File(ResourceUtils.getURL("classpath:").getPath());
            String path = pathurl + "/static/" + fileName + ".xlsx";
            localPath = path;
            /*
/**
             * localPath="e://test.csv";
             */
            out = new FileOutputStream(localPath);
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
