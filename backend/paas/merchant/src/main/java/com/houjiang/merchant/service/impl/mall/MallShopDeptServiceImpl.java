package com.houjiang.merchant.service.impl.mall;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallShopDept;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopDeptMapper;
import com.houjiang.merchant.common.util.ListUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAdminDeptService;
import com.houjiang.merchant.service.mall.IMallShopDeptService;
import com.houjiang.merchant.validate.mall.MallShopDeptCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptTopUpdateValidate;
import com.houjiang.merchant.validate.mall.MallShopDeptUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopDeptDetailVo;
import com.houjiang.merchant.vo.mall.MallShopDeptVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家部门实现类
 * @author Amos
 */
@Service
public class MallShopDeptServiceImpl implements IMallShopDeptService {
        
    @Resource
    MallShopDeptMapper mallShopDeptMapper;
    @Resource
    IMallShopAdminDeptService iMallShopAdminDeptService;

    /**
     * 商家部门列表
     *
     * @author Amos
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopDeptListedVo>
     */
    @Override
    public JSONArray list(MallShopDeptSearchValidate searchValidate) {
        QueryWrapper<MallShopDept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0).eq("shop_id", searchValidate.getShopId());
        queryWrapper.orderByDesc(Arrays.asList("sort", "id"));
        queryWrapper.select(MallShopDept.class, info ->
                !info.getColumn().equals("delete_time"));

        mallShopDeptMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:name:str",
                "=:status@status:int"
        });

        List<MallShopDept> systemAuthDeptList = mallShopDeptMapper.selectList(queryWrapper);

        List<MallShopDeptVo> list = new LinkedList<>();
        for (MallShopDept systemAuthDept : systemAuthDeptList) {
            MallShopDeptVo vo = new MallShopDeptVo();
            BeanUtils.copyProperties(systemAuthDept, vo);
            vo.setStatusDesc(systemAuthDept.getStatus() != null && systemAuthDept.getStatus().intValue() == 1 ? "正常" : "禁用");
            vo.setCreateTime(TimeUtils.timestampToDate(systemAuthDept.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(systemAuthDept.getUpdateTime()));
            list.add(vo);
        }

        JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(list));
        return ListUtils.listToTree(jsonArray, "id", "pid", "sub");
    }

    /**
     * 商家部门详情
     * @author Amos
     * @param id 主键参数
     * @return MallShopDept
     */
    @Override
    public MallShopDeptDetailVo detail(Integer id) {
        MallShopDept model = mallShopDeptMapper.selectOne(
                new QueryWrapper<MallShopDept>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的部门信息");
        }

        MallShopDeptDetailVo vo = new MallShopDeptDetailVo();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }

    /**
     * 商家部门新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(MallShopDeptCreateValidate createValidate) {
        if (createValidate.getPid().equals(0)) {
            MallShopDept systemAuthDept = mallShopDeptMapper.selectOne(
                    new QueryWrapper<MallShopDept>()
                            .select("id,pid,name")
                            .eq("pid", 0)
                            .isNull("delete_time")
                            .last("limit 1"));

            Assert.isNull(systemAuthDept, "顶级部门只允许有一个");
        }

        Assert.isNull(mallShopDeptMapper.selectOne(new QueryWrapper<MallShopDept>()
                .select("id,pid,name")
                .nested(
                        wq->wq.eq("name", createValidate.getName())
                )
                .eq("delete_time", 0).eq("shop_id", createValidate.getShopId())
                .last("limit 1")), "该部门已存在");
        MallShopDept model = new MallShopDept();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setName(createValidate.getName());
        model.setPid(createValidate.getPid());
        model.setStatus(createValidate.getStatus());
        model.setSort(createValidate.getSort());
        model.setContact(createValidate.getContact());
        model.setMobile(createValidate.getMobile());
        mallShopDeptMapper.insert(model);
    }

    /**
     * 商家部门编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(MallShopDeptUpdateValidate updateValidate) {
        MallShopDept model = mallShopDeptMapper.selectOne(
                new QueryWrapper<MallShopDept>()
                    .eq("id",  updateValidate.getId()).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        Assert.isFalse((model.getPid().equals(0) && updateValidate.getPid()>0), "顶级部门不能修改上级");
        Assert.isFalse(updateValidate.getId().equals(updateValidate.getPid()), "上级部门不能是自己");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的部门信息");
        }

        Assert.isNull(mallShopDeptMapper.selectOne(new QueryWrapper<MallShopDept>()
                .select("id,pid,name")
                .ne("id", updateValidate.getId())
                .nested(
                        wq->wq.eq("name", updateValidate.getName())
                )
                .eq("delete_time", 0).eq("shop_id", model.getShopId())
                .last("limit 1")), "该部门已存在");

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setPid(updateValidate.getPid());
        model.setStatus(updateValidate.getStatus());
        model.setSort(updateValidate.getSort());
        model.setContact(updateValidate.getContact());
        model.setMobile(updateValidate.getMobile());
        mallShopDeptMapper.updateById(model);
    }

    @Override
    public void topEdit(MallShopDeptTopUpdateValidate updateValidate) {
        MallShopDept model = mallShopDeptMapper.selectOne(
                new QueryWrapper<MallShopDept>()
                        .eq("id", updateValidate.getId()).eq("delete_time", 0)
                        .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (!model.getShopId().equals(MerchantThreadLocal.getShopId())) {
            throw new OperateException("不能编辑其他店铺的部门信息");
        }
        Assert.isTrue(model.getPid().equals(0), "仅顶级部门可调用此接口");

        Assert.isNull(mallShopDeptMapper.selectOne(new QueryWrapper<MallShopDept>()
                .select("id,pid,name")
                .ne("id", updateValidate.getId())
                .eq("name", updateValidate.getName())
                .eq("delete_time", 0).eq("shop_id", model.getShopId())
                .last("limit 1")), "该部门已存在");

        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setName(updateValidate.getName());
        model.setPid(0);
        model.setStatus(updateValidate.getStatus());
        model.setSort(updateValidate.getSort());
        model.setContact(updateValidate.getContact());
        model.setMobile(updateValidate.getMobile());
        mallShopDeptMapper.updateById(model);
    }

    /**
     * 商家部门删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallShopDept model = mallShopDeptMapper.selectOne(
                new QueryWrapper<MallShopDept>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        Assert.isFalse((model.getPid() == 0), "顶级部门不能删除");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的部门信息");
        }

        MallShopDept pModel = mallShopDeptMapper.selectOne(
                new QueryWrapper<MallShopDept>()
                        .select("id,pid,name")
                        .eq("pid", id)
                        .eq("delete_time", 0)
                        .last("limit 1"));

        Assert.isNull(pModel, "请先删除子级部门");

        Assert.isTrue(iMallShopAdminDeptService.deptAdminUse(model.getId()), "有管理员使用此部门，不能删除");

        mallShopDeptMapper.delete(new QueryWrapper<MallShopDept>().eq("id", id));
    }

}
