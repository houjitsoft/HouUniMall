package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseSensitiveClassificationEntity;
import com.houjiang.base.entity.BaseSensitiveEntity;
import com.houjiang.base.feign.GoodsClient;
import com.houjiang.base.mapper.BaseSensitiveClassificationMapper;
import com.houjiang.base.mapper.BaseSensitiveMapper;
import com.houjiang.base.service.BaseSensitiveService;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.sensitive.BaseSensitiveDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.vo.sensitive.BaseSensitiveVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BaseSensitiveServiceImpl
 * @Description  敏感词词库
 * @Author Amos
 * @Date 2024-01-30 16:45:08
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseSensitiveServiceImpl extends ServiceImpl<BaseSensitiveMapper, BaseSensitiveEntity>
        implements BaseSensitiveService {
    @Value("${ES_WORDS_PATH}")
    private String ES_WORDS_PATH ;

    private static String ES_WORDS_FILE_PATH;

    @PostConstruct
    public  String getPcIndexConfig2() {
        return ES_WORDS_FILE_PATH = this.ES_WORDS_PATH+"/EsWords/words.dic";
    }
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BaseSensitiveClassificationMapper baseSensitiveClassificationMapper;

    /**
     * 查询敏感配置信息
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 查询结果，包含分页信息和敏感配置列表
     */
    @Override
    public ResponseResult querySensitiveConfig(BaseSensitiveDTO param) {
        // 初始化分页对象
        // 分页查询
        Page<BaseSensitiveVO> page = new Page<>(param.getPage(), param.getLimit());
        // 执行查询
        IPage<BaseSensitiveVO> result = this.baseMapper.querySensitive(page,param);
        List<BaseSensitiveVO> records = result.getRecords();
        // 如果查询结果不为空，则将启用状态的敏感词添加到ES远程词库
        //将敏感词写入 es远程词库
        if(null != records && records.size() > 0){
            List<String> fieldValuesList = records.stream()
                    .filter(BaseSensitiveVO -> CommonType.smsEnableDisableStatus.STARTUSING.getCode().equals(BaseSensitiveVO.getSensitiveState()))
                    .map(BaseSensitiveVO::getSensitiveName)
                    .collect(Collectors.toList());
            addSensitiveWords(fieldValuesList);
        }
        // 返回查询结果
        return ResponseResult.success(PageUtils.getPage(result));
    }
    public void synSensitiveConfig() {
        List<String> strings = this.baseMapper.queryAllSensitive();
        if(null != strings &&  strings.size() > 0){
            addSensitiveWords(strings);
        }
    }
    /**
     * 添加或更新敏感配置
     *
     * @param param 添加或更新参数，包含敏感配置信息
     * @return 添加或更新结果，成功或失败
     */
    @Override
    public ResponseResult add(BaseSensitiveDTO param) {
        BaseSensitiveEntity entity = new BaseSensitiveEntity();
        BeanUtils.copyProperties(param, entity);
        Boolean flag;
        // 判断是否为新增，如果是新增，则设置添加时间
        if(null == param.getId()){
            entity.setSensitiveAddTime(new Date());
            flag = this.baseMapper.insert(entity) > 0;
        }else{
            flag = this.baseMapper.updateById(entity) > 0;
        }
        // 如果敏感状态为启用，则将敏感词添加到ES远程词库，并发送添加敏感词的请求给goods服务
        if(CommonType.smsEnableDisableStatus.STARTUSING.getCode().equals(entity.getSensitiveState())){
            List<BaseSensitiveVO> list = new ArrayList<>();
            BaseSensitiveVO baseSensitiveVO = new BaseSensitiveVO();
            // 数据转换
            BeanUtils.copyProperties(entity, baseSensitiveVO);
            // 查询分类名称和状态
            BaseSensitiveClassificationEntity baseSensitiveClassificationEntity =  baseSensitiveClassificationMapper.selectOne(new LambdaQueryWrapper<BaseSensitiveClassificationEntity>()
                    .eq(BaseSensitiveClassificationEntity::getClassificationId, entity.getSensitiveClassificationId()));
            baseSensitiveVO.setClassificationName(baseSensitiveClassificationEntity.getClassificationName());
            baseSensitiveVO.setClassificationState(baseSensitiveClassificationEntity.getClassificationState());
            list.add(baseSensitiveVO);
            goodsClient.addSensitiveList(list);
            //将敏感词写入 es远程词库
            List<String> sensitive = new ArrayList<>();
            sensitive.add(entity.getSensitiveName());
            addSensitiveWords(sensitive);
        }else{
            // 如果敏感状态为停用，则从ES远程词库中删除敏感词，并发送删除敏感词的请求给goods服务
            //数据从es中删除
            List<Integer> list = new ArrayList<>();
            list.add(entity.getId());
            goodsClient.deletSensitiveList(list);
            //将敏感词从 es远程词库中删除
            List<String> sensitive = new ArrayList<>();
            sensitive.add(entity.getSensitiveName());
            deleteSensitiveWords(sensitive);
        }
        // 根据操作结果返回成功或失败的响应
        return flag ? ResponseResult.success() : ResponseResult.fail();
    }
    /**
     * 删除敏感词
     * @param param 包含待删除敏感词ID的参数对象
     * @return 操作结果的响应对象
     */
    @Override
    public ResponseResult delete(BaseSensitiveDTO param) {
        // 如果ID列表不为空且不为空集合
        if(null != param.getIdList() && param.getIdList().size() > 0){
            // 根据ID列表查询对应的敏感词
            //查询要删除的敏感词
            List<String> sensitive = this.baseMapper.querySensitiveByIdList(param.getIdList());
            // 从ES远程词库中删除敏感词
            //将敏感词从 es远程词库中删除
            deleteSensitiveWords(sensitive);

            // 在数据库中批量删除敏感词
            this.baseMapper.deleteBatchIds(param.getIdList());

            // 从ES中删除敏感词
            //数据从es中删除
            goodsClient.deletSensitiveList(param.getIdList());
        }else{
            // 如果ID列表为空或为null，处理单个ID的删除情况
            //查询要删除的敏感词
            List<Integer> idList = new ArrayList<>();
            idList.add(param.getId());
            // 根据ID查询对应的敏感词
            List<String> sensitive = this.baseMapper.querySensitiveByIdList(idList);
            // 从ES远程词库中删除敏感词
            //将敏感词从 es远程词库中删除
            deleteSensitiveWords(sensitive);

            // 在数据库中删除单个敏感词
            this.baseMapper.deleteById(param.getId());

            // 从ES中删除敏感词
            //数据从es中删除
            List<Integer> list = new ArrayList<>();
            list.add(param.getId());
            goodsClient.deletSensitiveList(list);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 添加敏感词列表
     * @param param 包含敏感词信息的参数对象
     * @return 操作结果的响应对象
     */
    @Override
    public ResponseResult addList(BaseSensitiveDTO param) {
        BaseSensitiveEntity entity = new BaseSensitiveEntity();
        List<String> sensitive = new ArrayList<>();
        // 将参数对象的属性复制到实体对象
        BeanUtils.copyProperties(param, entity);
        // 设置敏感词的添加时间
        entity.setSensitiveAddTime(new Date());
        // 如果存在敏感词名称字符串
        if(null != param.getSensitiveNameStr()){
            // 将敏感词名称字符串按逗号分割
            //根据逗号分割 敏感词
            String[] result = param.getSensitiveNameStr().split(",");
            // 遍历分割后的敏感词数组，逐个插入数据库
            for (String s : result) {
                entity.setSensitiveName(s);
                this.baseMapper.insert(entity);
                // 如果敏感词状态为启用，则添加到ES远程词库
                if(CommonType.smsEnableDisableStatus.STARTUSING.getCode().equals(entity.getSensitiveState())){
                    // 数据添加进es
                    List<BaseSensitiveVO> list = new ArrayList<>();
                    BaseSensitiveVO baseSensitiveVO = new BaseSensitiveVO();
                    // 根据分类ID查询分类名称和状态
                    BaseSensitiveClassificationEntity baseSensitiveClassificationEntity =  baseSensitiveClassificationMapper.selectOne(new LambdaQueryWrapper<BaseSensitiveClassificationEntity>()
                            .eq(BaseSensitiveClassificationEntity::getClassificationId, entity.getSensitiveClassificationId()));
                    baseSensitiveVO.setClassificationName(baseSensitiveClassificationEntity.getClassificationName());
                    baseSensitiveVO.setClassificationState(baseSensitiveClassificationEntity.getClassificationState());
                    // 将实体对象的属性复制到VO对象
                    BeanUtils.copyProperties(entity, baseSensitiveVO);
                    list.add(baseSensitiveVO);
                    // 添加敏感词到ES远程词库
                    goodsClient.addSensitiveList(list);
                    // 将敏感词添加到集合中，用于后续写入ES远程词库
                    sensitive.add(entity.getSensitiveName());
                }
                // 重置ID，以便下一次插入
                //id 置空 用于下次添加
                entity.setId(null);
            }
            // 将敏感词写入ES远程词库
            //将敏感词写入 es远程词库
            addSensitiveWords(sensitive);
        }
        // 返回操作成功的响应结果
        return ResponseResult.success();
    }

    /**
     * 查询所有敏感词
     * @return 敏感词列表
     */
    @Override
    public List<BaseSensitiveVO> selectAllSensitive() {
        // 查询所有敏感词
        List<BaseSensitiveVO> result = this.baseMapper.selectAllSensitive();
        // 返回敏感词列表
        return result;
    }


    /**
     * 判断敏感词库是否存在 不存在就创建
     */
    public void createFileIfNotExists(String filePath) {
        // 创建一个File对象
        File file = new File(filePath);

        // 检查文件所在的目录是否存在
        File parentDir = file.getParentFile();

        // 如果父目录不存在，则创建
        if (!parentDir.exists()) {
            // 使用mkdirs()方法创建多级目录，如果只需要创建单级目录，可以使用mkdir()
            if (!parentDir.mkdirs()) {
                System.err.println("无法创建父目录: " + parentDir.getAbsolutePath());
                return;
            }
        }
        // 检查文件本身是否存在
        if (!file.exists()) {
            try {
                // 创建文件
                if (file.createNewFile()) {
                    System.out.println("文件 [" + filePath + "] 及其所在路径已成功创建。");
                } else {
                    System.out.println("无法创建文件，可能由于某种原因文件已存在或其他权限问题。");
                }
            } catch (IOException e) {
                System.err.println("创建文件 [" + filePath + "] 时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("文件 [" + filePath + "] 及其所在路径已经存在。");
        }
    }

    /**
     * 将敏感词添加到 es远程词库中
     */
    public void addSensitiveWords(List<String> sensitive){
        //判断敏感词库是否存在 不存在就创建
        createFileIfNotExists(ES_WORDS_FILE_PATH);
        try {
            if(null != sensitive && sensitive.size() > 0){
                HashSet<String>  sensitiveSet = new HashSet<>();
                sensitiveSet.addAll(sensitive);
                // 读取原文件内容
                List<String> lines = Files.readAllLines(Paths.get(ES_WORDS_FILE_PATH), StandardCharsets.UTF_8);
                if(lines != null && lines.size() > 0){
                    sensitiveSet.addAll(lines);
                }
                // 敏感词写入文件
                writeSensitiveToFile(sensitiveSet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 将敏感词从 es远程词库中删除
     */
    public void deleteSensitiveWords(List<String> sensitive){
        try {
            // 创建一个File对象
            File file = new File(ES_WORDS_FILE_PATH);
            // 检查文件是否存在
            if(file.exists()){
                // 读取输入文件
                List<String> lines = Files.readAllLines(Paths.get(ES_WORDS_FILE_PATH), StandardCharsets.UTF_8);
                if(null != lines && lines.size() > 0 ){
                    // 创建一个新的列表存储不包含目标字符的行
                    HashSet<String> newLines = new HashSet<>();
                    for (String line : lines) {
                        for (String s : sensitive) {
                            // 检查是否包含字符
                            if (!line.equals(s)) {
                                // 不包含则添加到新列表
                                newLines.add(line);
                            }
                        }
                    }
                    if(newLines != null && newLines.size() > 0){
                        // 敏感词写入文件
                        writeSensitiveToFile(newLines);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将敏感词写入文件
     * @param sensitiveList
     */
    public void writeSensitiveToFile(HashSet<String> sensitiveList){
        try {
            //清空文件内容
            Path fileToClear = Paths.get(ES_WORDS_FILE_PATH);
            Files.write(fileToClear, "".getBytes());

            //判断敏感词库是否存在 不存在就创建
            createFileIfNotExists(ES_WORDS_FILE_PATH);
            FileWriter writer = new FileWriter(ES_WORDS_FILE_PATH,true);
            BufferedWriter bw = new BufferedWriter(writer);
            for (String s : sensitiveList) {
                bw.write(s);
                // 添加一个换行符，如果不希望换行，可以省略这行
                bw.newLine();
                // 确保缓冲区的内容被立即写入到文件
                bw.flush();
            }
            System.out.println("字符串已成功写入文件: " + ES_WORDS_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
