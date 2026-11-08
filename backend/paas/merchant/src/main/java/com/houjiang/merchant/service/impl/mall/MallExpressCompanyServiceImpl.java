package com.houjiang.merchant.service.impl.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.mall.MallExpressCompany;
import com.houjiang.merchant.common.mapper.mall.MallExpressCompanyMapper;
import com.houjiang.merchant.common.util.StringUtils;
import com.houjiang.merchant.common.util.TimeUtils;
import com.houjiang.merchant.common.util.UrlUtils;
import com.houjiang.merchant.service.mall.IMallExpressCompanyService;
import com.houjiang.merchant.vo.mall.MallExpressCompanyListedVo;
import com.houjiang.merchant.vo.mall.MallExpressCompanySingleDetailVo;
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
public class MallExpressCompanyServiceImpl implements IMallExpressCompanyService {

    @Resource
    MallExpressCompanyMapper mallExpressCompanyMapper;
    @Override
    public MallExpressCompanySingleDetailVo getSingleDetail(Integer id) {
        MallExpressCompany model = this.getById(id);
        if (StringUtils.isNull(model)) {
            return null;
        } else {
            MallExpressCompanySingleDetailVo vo = new MallExpressCompanySingleDetailVo();
            BeanUtils.copyProperties(model, vo);
            return vo;
        }
    }

    @Override
    public MallExpressCompany getById(Integer id) {
        return mallExpressCompanyMapper.selectOne(new QueryWrapper<MallExpressCompany>().eq("delete_time", 0).eq("id", id));
    }

    @Override
    public List<MallExpressCompanyListedVo> getLists() {
        List<MallExpressCompany> list = mallExpressCompanyMapper.selectList(new QueryWrapper<MallExpressCompany>().eq("delete_time", 0).orderByDesc("sort").orderByDesc("id"));
        List<MallExpressCompanyListedVo> ret = new ArrayList<MallExpressCompanyListedVo>();
        if (list.size() > 0) {
            for (MallExpressCompany item : list) {
                MallExpressCompanyListedVo vo = new MallExpressCompanyListedVo();
                BeanUtils.copyProperties(item, vo);
                vo.setImage(UrlUtils.toShopRelativeUrl(item.getImage()));
                vo.setCreateTime(TimeUtils.timestampToDate(item.getCreateTime()));
                vo.setUpdateTime(TimeUtils.timestampToDate(item.getUpdateTime()));
                ret.add(vo);
            }
        }
        return ret;
    }


}
