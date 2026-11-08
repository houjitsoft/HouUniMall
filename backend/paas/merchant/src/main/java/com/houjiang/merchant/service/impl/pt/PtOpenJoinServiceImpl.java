package com.houjiang.merchant.service.impl.pt;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.pt.PtOpenJoin;
import com.houjiang.merchant.common.mapper.pt.PtOpenJoinMapper;
import com.houjiang.merchant.service.mall.IMallOrderGoodsService;
import com.houjiang.merchant.service.mall.IMallOrderService;
import com.houjiang.merchant.service.pt.IPtOpenJoinService;
import com.houjiang.merchant.service.user.IUserService;
import com.houjiang.merchant.vo.pt.PtOpenJoinListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 拼团活动实现类
 * @author Amos
 */
@Service
public class PtOpenJoinServiceImpl implements IPtOpenJoinService {
    @Resource
    IUserService iUserService;
    @Resource
    IMallOrderGoodsService iMallOrderGoodsService;
    @Resource
    IMallOrderService iMallOrderService;
    @Resource
    PtOpenJoinMapper ptOpenJoinMapper;

    @Override
    public List<PtOpenJoinListedVo> getJoinList(Integer ptOpenId) {
        List<PtOpenJoin> list = ptOpenJoinMapper.selectList(new QueryWrapper<PtOpenJoin>().eq("pt_open_id", ptOpenId).eq("delete_time", 0));
        List<PtOpenJoinListedVo> ret =  new ArrayList<>();
        if (list.size() > 0) {
            for (PtOpenJoin item : list) {
                PtOpenJoinListedVo vo = new PtOpenJoinListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setUser(iUserService.detail(item.getUserId()));
                vo.setOrderGoods(iMallOrderGoodsService.getSimpleById(item.getOrderGoodsId()));
                vo.setOrderInfo(iMallOrderService.detail(item.getOrderId()));
                ret.add(vo);
            }
        }
        return ret;
    }
}
