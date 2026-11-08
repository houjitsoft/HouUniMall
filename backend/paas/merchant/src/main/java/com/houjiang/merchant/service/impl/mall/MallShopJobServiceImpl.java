package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.common.entity.mall.MallShopJob;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopJobMapper;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.service.mall.IMallShopAdminJobsService;
import com.houjiang.merchant.service.mall.IMallShopJobService;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.validate.mall.MallShopJobCreateValidate;
import com.houjiang.merchant.validate.mall.MallShopJobSearchValidate;
import com.houjiang.merchant.validate.mall.MallShopJobUpdateValidate;
import com.houjiang.merchant.vo.mall.MallShopJobDetailVo;
import com.houjiang.merchant.vo.mall.MallShopJobListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.houjiang.merchant.common.validation.Assert;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 商家岗位实现类
 * @author Amos
 */
@Service
public class MallShopJobServiceImpl implements IMallShopJobService {
        
    @Resource
    MallShopJobMapper mallShopJobMapper;
    @Resource
    IMallShopAdminJobsService iMallShopAdminJobsService;

    /**
     * 商家岗位列表
     *
     * @author Amos
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<MallShopJobListedVo>
     */
    @Override
    public PageResult<MallShopJobListedVo> list(PageValidate pageValidate, MallShopJobSearchValidate searchValidate) {
        Integer page  = pageValidate.getPage_no();
        Integer limit = pageValidate.getPage_size();

        QueryWrapper<MallShopJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0).orderByDesc(Arrays.asList("sort", "id"));

        mallShopJobMapper.setSearch(queryWrapper, searchValidate, new String[]{
            "=:shopId@shop_id:int",
            "like:code:str",
            "like:name:str",
            "=:sort:int",
            "=:remark:str",
            "=:status:int",
        });

        IPage<MallShopJob> iPage = mallShopJobMapper.selectPage(new Page<>(page, limit), queryWrapper);

        List<MallShopJobListedVo> list = new LinkedList<>();
        for(MallShopJob item : iPage.getRecords()) {
            MallShopJobListedVo vo = new MallShopJobListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setStatusDesc(this.getStatusDescAttr(item.getStatus()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    private String getStatusDescAttr(Integer status) {
        return YesNoEnum.getStatusDesc(status);
    }

    /**
     * 商家岗位详情
     *
     * @author Amos
     * @param id 主键参数
     * @return MallShopJob
     */
    @Override
    public MallShopJobDetailVo detail(Integer id) {
        MallShopJob model = mallShopJobMapper.selectOne(
                new QueryWrapper<MallShopJob>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在");

        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能查看其他店铺的岗位信息");
        }

        MallShopJobDetailVo vo = new MallShopJobDetailVo();
        BeanUtils.copyProperties(model, vo);
        return vo;
    }

    /**
     * 商家岗位新增
     *
     * @author Amos
     * @param createValidate 参数
     */
    @Override
    public void add(MallShopJobCreateValidate createValidate) {
        Assert.isNull(mallShopJobMapper.selectOne(new QueryWrapper<MallShopJob>()
                .select("id,code,name")
                .nested(
                        wq->wq.eq("code", createValidate.getCode())
                                .or()
                                .eq("name", createValidate.getName())
                )
                .eq("delete_time", 0).eq("shop_id", createValidate.getShopId())
                .last("limit 1")), "该岗位已存在");

        MallShopJob model = new MallShopJob();
        model.setCreateTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setShopId(createValidate.getShopId());
        model.setCode(createValidate.getCode());
        model.setName(createValidate.getName());
        model.setSort(createValidate.getSort());
        model.setRemark(createValidate.getRemark());
        model.setStatus(createValidate.getStatus());
        mallShopJobMapper.insert(model);
    }

    /**
     * 商家岗位编辑
     *
     * @author Amos
     * @param updateValidate 参数
     */
    @Override
    public void edit(MallShopJobUpdateValidate updateValidate) {
        MallShopJob model = mallShopJobMapper.selectOne(new QueryWrapper<MallShopJob>()
                .select(MallShopJob.class, info ->
                        !info.getColumn().equals("delete_time"))
                .eq("id", updateValidate.getId())
                .eq("delete_time", 0)
                .last("limit 1"));
        Assert.notNull(model, "岗位不存在");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能编辑其他店铺的岗位信息");
        }
        Assert.isNull(mallShopJobMapper.selectOne(new QueryWrapper<MallShopJob>()
                .select("id,code,name")
                .ne("id", updateValidate.getId())
                .nested(
                        wq->wq.eq("code", updateValidate.getCode())
                                .or()
                                .eq("name", updateValidate.getName())
                )
                .eq("delete_time", 0).eq("shop_id", model.getShopId())
                .last("limit 1")), "该岗位已存在");

        model.setId(updateValidate.getId());
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        model.setCode(updateValidate.getCode());
        model.setName(updateValidate.getName());
        model.setSort(updateValidate.getSort());
        model.setRemark(updateValidate.getRemark());
        model.setStatus(updateValidate.getStatus());
        mallShopJobMapper.updateById(model);
    }

    /**
     * 商家岗位删除
     *
     * @author Amos
     * @param id 主键ID
     */
    @Override
    public void del(Integer id) {
        MallShopJob model = mallShopJobMapper.selectOne(
                new QueryWrapper<MallShopJob>()
                    .eq("id", id).eq("delete_time", 0)
                    .last("limit 1"));

        Assert.notNull(model, "数据不存在!");
        if (model.getShopId().equals(MerchantThreadLocal.getShopId()) == false) {
            throw new OperateException("不能删除其他店铺的岗位信息");
        }

        Boolean adminUse = iMallShopAdminJobsService.shopAdminUse(id);

        Assert.isTrue(adminUse, "该岗位已被管理员使用,请先移除");

        model.setDeleteTime(System.currentTimeMillis() / 1000);
        model.setUpdateTime(System.currentTimeMillis() / 1000);
        mallShopJobMapper.updateById(model);
    }

    @Override
    public List<MallShopJobListedVo> otherLists(MallShopJobSearchValidate searchValidate) {
        QueryWrapper<MallShopJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_time", 0).orderByDesc(Arrays.asList("sort", "id"));

        mallShopJobMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "=:shopId@shop_id:int",
                "like:code:str",
                "like:name:str",
                "=:sort:int",
                "=:remark:str",
                "=:status:int",
        });
        List<MallShopJob> jobList = mallShopJobMapper.selectList(queryWrapper);
        List<MallShopJobListedVo> list = new LinkedList<>();
        for(MallShopJob item : jobList) {
            MallShopJobListedVo vo = new MallShopJobListedVo();
            BeanUtils.copyProperties(item, vo);
            vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
            vo.setStatusDesc(this.getStatusDescAttr(item.getStatus()));
            list.add(vo);
        }

        return list;
    }
}
