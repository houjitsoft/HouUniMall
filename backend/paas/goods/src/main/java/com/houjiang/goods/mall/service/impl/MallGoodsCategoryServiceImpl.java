package com.houjiang.goods.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.goods.mall.dto.MallGoodsCategoryDTO;
import com.houjiang.goods.mall.entity.MallGoodsCategory;
import com.houjiang.goods.mall.mapper.MallGoodsCategoryMapper;
import com.houjiang.goods.mall.service.MallGoodsCategoryService;
import com.houjiang.goods.mall.vo.MallGoodsCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类 Service 实现（树形）
 * @author Amos
 */
@Service
public class MallGoodsCategoryServiceImpl implements MallGoodsCategoryService {

    @Autowired
    private MallGoodsCategoryMapper mapper;

    @Override
    public List<MallGoodsCategoryVO> list(String name) {
        LambdaQueryWrapper<MallGoodsCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(MallGoodsCategory::getDeleteTime, 0);
        if (name != null && !name.isEmpty()) qw.like(MallGoodsCategory::getName, name);
        qw.orderByAsc(MallGoodsCategory::getSort).orderByAsc(MallGoodsCategory::getId);
        List<MallGoodsCategory> all = mapper.selectList(qw);
        return buildTree(all);
    }

    @Override
    public List<MallGoodsCategoryVO> otherList() {
        LambdaQueryWrapper<MallGoodsCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(MallGoodsCategory::getDeleteTime, 0);
        qw.eq(MallGoodsCategory::getIsShow, 1);
        qw.orderByAsc(MallGoodsCategory::getSort).orderByAsc(MallGoodsCategory::getId);
        List<MallGoodsCategory> all = mapper.selectList(qw);
        return all.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public MallGoodsCategoryVO detail(Integer id) {
        MallGoodsCategory e = mapper.selectById(id);
        if (e == null) throw new RuntimeException("分类不存在");
        return toVO(e);
    }

    @Override
    public Integer add(MallGoodsCategoryDTO dto) {
        int now = (int) (System.currentTimeMillis() / 1000);
        MallGoodsCategory e = new MallGoodsCategory();
        BeanUtils.copyProperties(dto, e, "id");
        if (e.getPid() == null) e.setPid(0);
        if (e.getLevel() == null) e.setLevel(e.getPid() == 0 ? 1 : 2);
        if (e.getIsShow() == null) e.setIsShow(1);
        if (e.getSort() == null) e.setSort(0);
        e.setCreateTime(now);
        e.setUpdateTime(now);
        mapper.insert(e);
        return e.getId();
    }

    @Override
    public void edit(MallGoodsCategoryDTO dto) {
        MallGoodsCategory e = mapper.selectById(dto.getId());
        if (e == null) throw new RuntimeException("分类不存在");
        BeanUtils.copyProperties(dto, e);
        e.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        mapper.updateById(e);
    }

    @Override
    public void del(Integer id) {
        int now = (int) (System.currentTimeMillis() / 1000);
        // 同时把子节点也软删
        List<MallGoodsCategory> children = mapper.selectList(
                new LambdaQueryWrapper<MallGoodsCategory>().eq(MallGoodsCategory::getPid, id));
        mapper.update(null, new LambdaUpdateWrapper<MallGoodsCategory>()
                .eq(MallGoodsCategory::getId, id)
                .set(MallGoodsCategory::getDeleteTime, now));
        for (MallGoodsCategory c : children) {
            mapper.update(null, new LambdaUpdateWrapper<MallGoodsCategory>()
                    .eq(MallGoodsCategory::getId, c.getId())
                    .set(MallGoodsCategory::getDeleteTime, now));
        }
    }

    @Override
    public void changeStatus(Integer id) {
        MallGoodsCategory e = mapper.selectById(id);
        if (e == null) return;
        mapper.update(null, new LambdaUpdateWrapper<MallGoodsCategory>()
                .eq(MallGoodsCategory::getId, id)
                .set(MallGoodsCategory::getIsShow, e.getIsShow() != null && e.getIsShow() == 1 ? 0 : 1)
                .set(MallGoodsCategory::getUpdateTime, (int) (System.currentTimeMillis() / 1000)));
    }

    /* ----------------- helper ----------------- */

    private MallGoodsCategoryVO toVO(MallGoodsCategory e) {
        MallGoodsCategoryVO vo = new MallGoodsCategoryVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    private List<MallGoodsCategoryVO> buildTree(List<MallGoodsCategory> all) {
        Map<Integer, MallGoodsCategoryVO> map = new HashMap<>();
        for (MallGoodsCategory e : all) {
            map.put(e.getId(), toVO(e));
        }
        List<MallGoodsCategoryVO> roots = new ArrayList<>();
        for (MallGoodsCategory e : all) {
            MallGoodsCategoryVO node = map.get(e.getId());
            Integer pid = e.getPid() == null ? 0 : e.getPid();
            if (pid == 0 || !map.containsKey(pid)) {
                roots.add(node);
            } else {
                MallGoodsCategoryVO parent = map.get(pid);
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}
