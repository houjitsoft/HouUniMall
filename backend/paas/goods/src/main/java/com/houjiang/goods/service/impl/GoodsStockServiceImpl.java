package com.houjiang.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.houjiang.commons.dto.goods.GoodsStockDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.exception.DefinedException;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.excel.ExcelsUtil;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.MerchantGoodsStockDTO;
import com.houjiang.goods.entity.GoodsBaseEntity;
import com.houjiang.goods.entity.GoodsStockEntity;
import com.houjiang.goods.mapper.GoodsBaseMapper;
import com.houjiang.goods.mapper.GoodsStockMapper;
import com.houjiang.goods.mapper.StorageGoodsBaseMapper;
import com.houjiang.goods.service.GoodsStockService;
import com.houjiang.goods.vo.MerchantGoodsStockVO;
import com.houjiang.goods.vo.StorageGoodsBaseVO;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class GoodsStockServiceImpl extends ServiceImpl<GoodsStockMapper, GoodsStockEntity>
        implements GoodsStockService {

    @Resource
    private StorageGoodsBaseMapper storageGoodsBaseMapper;

    @Resource
    private GoodsBaseMapper GoodsBaseMapper;

    /**
     * 查询商品库存分页方法
     * 该方法用于处理商户查询商品库存分页的请求它从数据库中获取与指定条件匹配的商品库存分页数据
     *
     * @param param 查询参数对象，包含分页信息和查询条件
     * @return 包含分页查询结果的响应对象
     */
    @Override
    public ResponseResult<PageUtils<MerchantGoodsStockVO>> queryGoodsStockPage(MerchantGoodsStockDTO param) {
        // 从令牌中解析用户信息，以确定当前操作的商户
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置店铺ID，以便后续查询特定店铺的商品库存
        param.setShopId(Integer.valueOf(ui.getShopId()));

        // 根据传入的分页参数创建Page对象
        Page<MerchantGoodsStockVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行分页查询操作，返回包含分页数据的IPage对象
        IPage<MerchantGoodsStockVO> result = baseMapper.queryGoodsStockPage(page, param);

        // 遍历查询结果，为每个商品项补充总库存信息
        for (MerchantGoodsStockVO vo : result.getRecords()) {
            // 通过商品ID查询总库存，并设置到商品对象中
            vo.setTotalStorageStock(storageGoodsBaseMapper.selectTotalStorageStock(vo.getGoodsId()));
        }

        // 返回查询结果的分页工具对象包装
        return ResponseResult.success(PageUtils.getPage(result));
    }

    /**
     * 分页查询商品库存信息并准备导出Excel
     *
     * @param param 查询参数对象，包含商品库存的查询条件和分页信息
     * @return 响应结果，包含导出Excel文件的URL
     */
    @Override
    public ResponseResult<String> queryPageExp(MerchantGoodsStockDTO param) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置当前用户店铺ID到查询参数中
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 定义导出表格的标题
        String[] title = {"商品名称", "商品id", "规格", "商品在售库存", "在仓库存", "库存预警", "更新时间", "创建时间"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();
        // 创建分页对象
        Page<MerchantGoodsStockVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行分页查询商品库存
        IPage<MerchantGoodsStockVO> result = baseMapper.queryGoodsStockPage(page, param);
        // 遍历查询结果，准备导出数据
        for (MerchantGoodsStockVO item : result.getRecords()) {
            HashMap<String, String> map = new HashMap<>();
            // 填充商品信息到Map中
            map.put("商品名称", item.getGoodsName());
            map.put("商品id", item.getGoodsId() + "");
            map.put("规格", item.getGoodsSpec());
            map.put("商品在售库存", item.getGoodsStock() + "");
            // 查询并在仓库存量并转换为字符串后添加到Map中
            map.put("在仓库存", storageGoodsBaseMapper.selectTotalStorageStock(item.getGoodsId()) + "");
            // 根据库存情况设置库存预警状态
            map.put("库存预警", item.getGoodsAlarm() > item.getGoodsStock() ? "库存不在" : "正常");
            // 格式化并添加更新时间和创建时间
            map.put("更新时间", DateUtils.formateForSdfCn(item.getUpdateTime()));
            map.put("创建时间", DateUtils.formateForSdfCn(item.getCreateTime()));
            // 将Map添加到数据列表中
            list.add(map);
        }
        // 构建并返回导出的Excel文件URL
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        return ResponseResult.success(url);
    }


    /**
     * 更新商品库存
     *
     * @param param 商品库存参数对象，包含需要更新的商品ID和库存数量
     * @return 更新操作的结果，成功或失败以及失败原因
     */
    @Override
    public ResponseResult updateGoodsStock(MerchantGoodsStockDTO param) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 查询商品的总库存量
        Integer totalStorageStock = storageGoodsBaseMapper.selectTotalStorageStock(param.getGoodsId());
        // 检查是否库存充足
        if (totalStorageStock < param.getGoodsStock()) {
            // 如果库存不足，返回失败结果
            return ResponseResult.fail(BizCodeEnume.GOODS_STOCK_NOT_ENOUGH);
        }
        // 更新商品库存
        int count = baseMapper.update(null, new LambdaUpdateWrapper<GoodsStockEntity>()
                // 设置新的库存值
                .set(GoodsStockEntity::getGoodsStock, param.getGoodsStock())
                // 更新时间设置为当前时间
                .set(GoodsStockEntity::getUpdateTime, DateUtils.getNowDate())
                // 更新操作员设置为当前用户
                .set(GoodsStockEntity::getUpdateUser, ui.getUserName())
                // 根据商品ID进行更新
                .eq(GoodsStockEntity::getGoodsId, param.getGoodsId()));
        // 根据更新影响的行数判断操作是否成功
        return count > 0 ? ResponseResult.success() : ResponseResult.fail();
    }


    /**
     * 下载商品库存模板
     *
     * @param param 商品库存参数
     * @return 返回导出结果的响应
     */
    @Override
    public ResponseResult<String> downloadGoodsStockTemplate(MerchantGoodsStockDTO param) {
        // 获取当前用户信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置店铺ID
        param.setShopId(Integer.valueOf(ui.getShopId()));

        // 定义导出表格的标题
        String[] title = {"商品id", "商品名称", "规格", "实际总库存", "*在售库存"};
        // 初始化数据列表
        List<Map<String, String>> list = Lists.newArrayList();

        // 创建分页对象
        Page<MerchantGoodsStockVO> page = new Page<>(param.getPage(), param.getLimit());
        // 查询商品库存分页数据
        IPage<MerchantGoodsStockVO> result = baseMapper.queryGoodsStockPage(page, param);

        // 遍历查询结果，组装数据
        for (MerchantGoodsStockVO item : result.getRecords()) {
            HashMap<String, String> map = new HashMap<>();
            // 填充商品信息到数据列表
            map.put("商品id", item.getGoodsId() + "");
            map.put("商品名称", item.getGoodsName());
            map.put("规格", item.getGoodsSpec());
            // 查询并设置商品的实际总库存
            map.put("实际总库存", storageGoodsBaseMapper.selectTotalStorageStock(item.getGoodsId()) + "");
            // 暂时不填在售库存
            map.put("*在售库存", "");
            // 将组装好的数据添加到列表
            list.add(map);
        }

        // 构建并返回导出的Excel文件URL
        Map<String, List<Map<String, String>>> map = Maps.newHashMap();
        map.put("数据", list);
        String url = ExcelsUtil.createExcel(title, map, new int[]{0});
        return ResponseResult.success(url);
    }


    /**
     * 导入商品库存模板
     *
     * @param multipartFile 用户上传的文件
     * @return 返回导入操作的结果
     */
    @Override
    public ResponseResult importGoodsStockTemplate(MultipartFile multipartFile,String token) {
        // 从token中获取店铺信息
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        // 获取当前用户对应的店铺ID
        Integer shopId = Integer.valueOf(ui.getShopId());
        // 处理文件相关逻辑
        File file = getFile(multipartFile);
        // 设置临时文件在程序退出时自动删除
        file.deleteOnExit();
        // 检查文件是否存在
        if (!file.exists()) {
            return ResponseResult.fail(BizCodeEnume.FILE_NOT_EXIST.getCode(), BizCodeEnume.FILE_NOT_EXIST.getMsg());
        }

        // 获取第一个sheet
        Sheet sheetAt = getSheet(file);

        // 获取行长度
        int length = sheetAt.getRow(0).getPhysicalNumberOfCells();
        // 初始化过滤数量
        int filterNum = 0;
        // 处理Excel中的数据行
        for (int i = 1; i < sheetAt.getPhysicalNumberOfRows(); i++) {
            Row row = sheetAt.getRow(i);
            MerchantGoodsStockDTO goods = new MerchantGoodsStockDTO();
            for (int index = 0; index < length; index++) {
                Cell cell = row.getCell(index);
                // 处理单元格数据
                if ("".equals(ExcelsUtil.getValue(cell))) {
                    continue;
                } else if (index == 0) {
                    goods.setGoodsId((int) Double.parseDouble(ExcelsUtil.getValue(cell)));
                } else if (index == 4) {
                    if (!StringUtils.isEmpty(ExcelsUtil.getValue(cell))) {
                        goods.setGoodsStock((int) Double.parseDouble(ExcelsUtil.getValue(cell)));
                    }
                }
                // 更新或过滤商品库存信息
                if (goods.getGoodsId() != null && goods.getGoodsStock() != null) {
                    int count = Math.toIntExact(GoodsBaseMapper.selectCount(new LambdaQueryWrapper<GoodsBaseEntity>()
                            .eq(GoodsBaseEntity::getGoodsId, goods.getGoodsId())
                            .eq(GoodsBaseEntity::getShopId, shopId)));
                    if (count == 1) {
                        baseMapper.update(null, new LambdaUpdateWrapper<GoodsStockEntity>()
                                .set(GoodsStockEntity::getGoodsStock, goods.getGoodsStock())
                                .eq(GoodsStockEntity::getGoodsId, goods.getGoodsId()));
                    } else {
                        filterNum++;
                    }
                }
            }
        }
        // 根据过滤情况返回处理结果
        return filterNum > 0 ? ResponseResult.success(1, "模板中存在不符合规则数据，已自动过滤.") : ResponseResult.success();
    }

    /**
     * 将上传的多部分文件转换为File对象
     *
     * @param multipartFile 通过表单上传的文件对象
     * @return 转换后的File对象，如果文件类型不支持或文件创建失败则抛出异常
     * @throws IllegalArgumentException 如果文件类型不是xls或xlsx
     * @throws RuntimeException         如果创建临时文件失败
     */
    private File getFile(MultipartFile multipartFile) {
        File file = null;
        // 获取上传文件的原始名称
        String[] filename = getFilename(multipartFile);
        // 检查文件类型是否为xls或xlsx
        if (!"xls".equals(filename[1]) && !"xlsx".equals(filename[1])) {
            throw new IllegalArgumentException("不支持的文件类型：" + filename[1]);
        }
        try {
            // 创建临时文件
            file = File.createTempFile(filename[0] + "导入在售库存", filename[1] + ".");
            // 将上传的文件转移至临时文件
            multipartFile.transferTo(file);
        } catch (IOException e) {
            // 处理文件创建失败的情况
            System.err.println("创建临时文件失败: " + e.getMessage());
            throw new RuntimeException("创建临时文件失败", e);
        }
        return file;
    }

    /**
     * 从multipartFile中获取文件名和扩展名
     *
     * @param multipartFile 上传的文件对象
     * @return 字符串数组，包含文件名和扩展名
     * @throws IllegalArgumentException 如果文件名为空或没有扩展名
     * @throws DefinedException         如果文件名不合法
     */
    private static String[] getFilename(MultipartFile multipartFile) {
        // 获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 检查文件是否为空
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 通过点分隔文件名获取扩展名
        String[] filename = originalFilename.split("\\.");
        // 检查文件扩展名
        if (filename.length < 2) {
            throw new IllegalArgumentException("文件必须具有扩展名");
        }
        // 验证文件名是否合法
        if (!FileUtils.isValidFilename(filename[0])) {
            throw new DefinedException("文件名不合法，无法继续处理");
        }
        return filename;
    }

    /**
     * 根据给定的文件获取Excel的第一个Sheet
     * 此方法主要负责打开Excel文件，验证其内容，并返回第一个Sheet
     * 它处理了文件不存在、格式错误和行数限制等问题
     *
     * @param file 待处理的Excel文件
     * @return Excel文件的第一个Sheet
     * @throws DefinedException 当文件不存在、读取文件时发生错误、
     *                          sheets页不存在、Excel格式错误或Excel行数超过限制时抛出
     */
    private Sheet getSheet(File file) {
        // 处理Excel文件
        Workbook sheets = null;
        try {
            // 验证文件路径的安全性
            Path filePath = Paths.get(file.getPath());
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new FileNotFoundException("文件不存在或不是常规文件: " + file.getPath());
            }
            // 使用 try-with-resources 确保流自动关闭
            try (InputStream inputStream = new FileInputStream(file.getPath())) {
                sheets = WorkbookFactory.create(inputStream);
            }
        } catch (FileNotFoundException e) {
            System.err.println("文件未找到: " + e.getMessage());
            throw new DefinedException("文件未找到: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("读取文件时发生错误: " + e.getMessage());
            throw new DefinedException("读取文件时发生错误: " + e.getMessage());
        }
        // 检查Workbook是否创建成功
        if (sheets == null) {
            throw new DefinedException("sheets页不存在！");
        }
        // 获取第一个sheet
        Sheet sheetAt = sheets.getSheetAt(0);
        // 检查Excel格式
        if (null == sheetAt.getRow(0) || null == sheetAt.getRow(1)) {
            throw new DefinedException(BizCodeEnume.DATA_FORMAT_WRONG);
        }
        // 获取有效行数
        int goodTotalNums = sheetAt.getPhysicalNumberOfRows() - 1;
        // 检查Excel行数限制
        if (goodTotalNums > 100) {
            throw new DefinedException(BizCodeEnume.EXCEL_MAX_ROWS);
        }
        if (goodTotalNums == 0) {
            throw new DefinedException(BizCodeEnume.EXCEL_NO_GOODS);
        }
        return sheetAt;
    }

    /**
     * 查询商品库存分布
     * 该方法用于查询特定商户的商品库存信息，包括总库存和分库存的分布情况
     * 通过解析令牌获取当前操作的商户信息，确保商品库存查询的准确性
     * 设置查询参数中的店铺ID，确保查询的是当前商户的商品库存
     * 调用基础映射器查询商品的基本库存信息
     * 通过商品ID查询总库存量，并将其设置到商品库存对象中
     * 查询商品的分库存列表，并将其设置到商品库存对象中
     * 返回成功响应结果，包含查询到的商品库存信息
     *
     * @param param 商品库存查询参数，包含商品ID等信息
     * @return 返回响应结果，包含商品库存分布信息
     */
    @Override
    public ResponseResult<MerchantGoodsStockVO> queryStockDistribution(MerchantGoodsStockDTO param) {
        // 从令牌中解析用户信息，以确定当前操作的商户
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(param.getToken());
        // 设置店铺ID，以便后续查询特定店铺的商品库存
        param.setShopId(Integer.valueOf(ui.getShopId()));
        // 查询商品的基本库存信息
        MerchantGoodsStockVO result = baseMapper.queryGoodsStock(param);
        // 通过商品ID查询总库存，并设置到商品对象中
        result.setTotalStorageStock(storageGoodsBaseMapper.selectTotalStorageStock(result.getGoodsId()));
        // 查询商品的分库存列表
        List<StorageGoodsBaseVO> list = storageGoodsBaseMapper.queryStorageGoodsList(param);
        // 将分库存列表设置到商品对象中
        result.setStorageGoodsList(list);
        // 返回成功响应结果，包含查询到的商品库存信息
        return ResponseResult.success(result);
    }

    /**
     * 添加商品库存数据
     * @param dto 商品库存数据
     * @return 操作结果
     */
    @Override
    public ResponseResult insertGoodsStock(GoodsStockDTO dto) {
        GoodsStockEntity goodsStockEntity = new GoodsStockEntity();
        BeanUtils.copyProperties(dto, goodsStockEntity);
        baseMapper.insert(goodsStockEntity);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult syncStock() {
        // 从外部系统获取商品库存信息列表
        List<Map<String, Object>> list = this.baseMapper.syncStock();
        // 遍历每个商品的库存信息
        for (Map<String, Object> map : list) {
            // 提取商品ID、通用ID、库存警戒线和库存数量
            String goodsId = map.get("goodsId").toString();
            String commonId = map.get("commonId").toString();
            String goodsAlarm = map.get("goodsAlarm").toString();
            String goodsStock = map.get("goodsStock").toString();

            // 根据商品ID和通用ID查询当前系统中的商品库存信息
            // 更新商品库存
            GoodsStockEntity stockEntity = this.getOne(new LambdaUpdateWrapper<GoodsStockEntity>()
                    .eq(GoodsStockEntity::getGoodsId, goodsId)
                    .eq(GoodsStockEntity::getCommonId, commonId));
            // 如果商品库存信息存在
            if (null != stockEntity) {
                // 更新商品库存和库存警戒线，并记录更新时间和更新人
                stockEntity.setGoodsStock(Integer.valueOf(goodsStock));
                stockEntity.setGoodsAlarm(Integer.valueOf(goodsAlarm));
                stockEntity.setUpdateUser("1");
                stockEntity.setUpdateTime(new Date());
                this.updateById(stockEntity);
            } else {
                // 如果商品库存信息不存在，则创建新的商品库存信息
                stockEntity = new GoodsStockEntity();
                // 设置商品库存、库存警戒线、通用ID和商品ID，并记录创建时间和创建人
                stockEntity.setGoodsStock(Integer.valueOf(goodsStock));
                stockEntity.setGoodsAlarm(Integer.valueOf(goodsAlarm));
                stockEntity.setCommonId(Integer.valueOf(commonId));
                stockEntity.setGoodsId(Integer.valueOf(goodsId));
                stockEntity.setCreateUser("1");
                stockEntity.setCreateTime(new Date());
                this.save(stockEntity);
            }
        }

        // 返回操作成功的结果
        return ResponseResult.success();
    }

    @Override
    public Integer selectGoodsStock(Integer goodsId) {
        Integer goodsStock = this.baseMapper.selectGoodsStock(goodsId);
        return goodsStock;
    }
}




