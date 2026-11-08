package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.admin.Admin;
import com.houjiang.merchant.common.entity.mall.MallExpressTpl;
import com.houjiang.merchant.common.entity.mall.MallOrderLog;
import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAdmin;
import com.houjiang.merchant.common.entity.user.User;
import com.houjiang.merchant.common.enums.OperatorEnum;
import com.houjiang.merchant.common.enums.OrderLogEnum;
import com.houjiang.merchant.common.mapper.admin.AdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallExpressTplMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopAdminMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.mapper.user.UserMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.service.mall.IMallExpressTplService;
import com.houjiang.merchant.service.mall.IMallOrderLogService;
import com.houjiang.merchant.vo.mall.MallExpressTplGoodsVo;
import com.houjiang.merchant.vo.mall.MallOrderLogListedVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 商家账号实现类
 * @author Amos
 */
@Service
public class MallOrderLogServiceImpl implements IMallOrderLogService {

    @Resource
    MallOrderLogMapper mallOrderLogMapper;
    @Resource
    AdminMapper adminMapper;
    @Resource
    MallShopMapper mallShopMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    MallShopAdminMapper mallShopAdminMapper;

    @Override
    public List<MallOrderLogListedVo> getListByOId(Integer orderId) {
        List<MallOrderLog> list = mallOrderLogMapper.selectList(new QueryWrapper<MallOrderLog>().eq("order_id", orderId).eq("delete_time", 0).orderByDesc("id"));
        List<MallOrderLogListedVo> retList = new ArrayList<MallOrderLogListedVo>();
        if (list.size() > 0) {
            for (MallOrderLog item : list) {
                MallOrderLogListedVo vo = new MallOrderLogListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setChannelText(OrderLogEnum.getMsgByCode(item.getChannel()));
                vo.setOperator(this.getOperatorAttr(item.getOperatorType(), item.getOperatorId()));
                retList.add(vo);
            }
        }
        return retList;
    }

    /**
     * 名称
     * @param operatorType
     * @return
     */
    @Override
    public String getOperatorAttr(Integer operatorType, Integer operatorId) {
        if (operatorType.equals(OperatorEnum.SYS.getCode())) {
            return OperatorEnum.SYS.getMsg();
        }
        if (operatorType.equals(OperatorEnum.ADMIN.getCode())) {
           MallShopAdmin admin = mallShopAdminMapper.selectOne(new QueryWrapper<MallShopAdmin>().eq("id", operatorId).eq("delete_time", 0));
           return StringUtils.isNull(admin) ? "" : admin.getName();
        }
        if (operatorType.equals(OperatorEnum.SHOP.getCode())) {
            MallShop mallshop = mallShopMapper.selectOne(new QueryWrapper<MallShop>().eq("id", operatorId).eq("delete_time", 0));
            return StringUtils.isNull(mallshop) ? "" : mallshop.getName();
        }
        if (operatorType.equals(OperatorEnum.USER.getCode())) {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", operatorId).isNull("delete_time"));
            return StringUtils.isNull(user) ? "" : user.getNickname();
        }

        return "";
    }
}
