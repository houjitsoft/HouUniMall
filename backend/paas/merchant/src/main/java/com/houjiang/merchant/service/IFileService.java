package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.validate.file.FileCateValidate;
import com.houjiang.merchant.validate.file.FileSearchValidate;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.album.FileVo;
import com.houjiang.merchant.common.core.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口类
 */
public interface IFileService {

    /**
     * 文件列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 其他搜索参数
     * @return PageResult<AlbumVo>
     */
    PageResult<FileVo> fileList(PageValidate pageValidate, FileSearchValidate searchValidate);

    /**
     * 文件重命名
     *
     * @param id 文件ID
     * @param name 文件名称
     */
    void fileRename(Integer id, String name);

    /**
     * 文件移动
     *
     * @author Amos
     * @param ids 文件ID
     * @param cid 类目ID
     */
    void fileMove(List<Integer> ids, Integer cid);

    /**
     * 文件新增
     *
     * @author Amos
     * @param params 文件信息参数
     */
    Integer fileAdd(Map<String, String> params);

    /**
     * 文件删除
     *
     * @author Amos
     * @param ids 文件ID
     */
    void fileDel(List<Integer> ids);

    /**
     * 分类列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return JSONArray
     */
    JSONObject cateList(FileSearchValidate searchValidate);

    /**
     * 分类新增
     *
     * @author Amos
     * @param cateValidate 分类参数
     */
    void cateAdd(FileCateValidate cateValidate);

    /**
     * 分类编辑
     *
     * @author Amos
     * @param id 分类ID
     * @param name 分类名称
     */
    void cateRename(Integer id, String name);

    /**
     * 分类删除
     *
     * @author Amos
     * @param id 分类ID
     */
    void cateDel(Integer id);

}
