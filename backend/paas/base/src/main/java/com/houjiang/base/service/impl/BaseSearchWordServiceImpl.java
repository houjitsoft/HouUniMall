package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.entity.BaseSearchWordEntity;
import com.houjiang.base.mapper.BaseSearchWordMapper;
import com.houjiang.base.service.BaseSearchWordService;
import com.houjiang.commons.dto.goods.GoodsMerchantSearchWordDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 搜索热门词(BaseSearchWord)
 *
 * @since 2023-03-28 14:15:03
 * @author Amos
 */
@Service
public class BaseSearchWordServiceImpl extends ServiceImpl<BaseSearchWordMapper, BaseSearchWordEntity>
        implements BaseSearchWordService {

    @Autowired
    private BaseSearchWordMapper baseSearchWordMapper;

    /**
     * 根据通用名称查询商品商家搜索词。
     *
     * @param commonName 通用名称
     * @return 商品商家搜索词DTO
     */
    @Override
    public GoodsMerchantSearchWordDTO selectBySearchName(String commonName) {
        return baseSearchWordMapper.selectBySearchName(commonName);
    }

    /**
     * 根据主键选择性更新商品商家搜索词记录。
     *
     * @param record 商品商家搜索词DTO
     * @return 影响的行数
     */
    @Override
    public Integer updateByPrimaryKeySelective(GoodsMerchantSearchWordDTO record) {
        return baseSearchWordMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 选择性插入商品商家搜索词记录。
     *
     * @param record 商品商家搜索词DTO
     * @return 影响的行数
     */
    @Override
    public Integer insertSelective(GoodsMerchantSearchWordDTO record) {
        return baseSearchWordMapper.insertSelective(record);
    }

    /**
     * 根据查询条件查询商品商家搜索词。
     *
     * @param baseSearchWordDtoReq 查询条件DTO
     * @return 查询结果
     */
    @Override
    public ResponseResult querySearchWord(GoodsMerchantSearchWordDTO baseSearchWordDtoReq) {
        // 校验token
        // 校验token
        String token = baseSearchWordDtoReq.getToken();
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByTokenNoIsExist(token) == null ? ParseTokenUtils.getPlatformUser(token) : ParseTokenUtils.getUserInfoByToken(token);
        try {
            if (null != userInfo) {
                // 将查询条件DTO转换为Map
                // 将对象转换为map
                Map data = ParseMapUtils.beanToMap(baseSearchWordDtoReq);
                // 分页查询商品商家搜索词
                // 分页查询数据
                IPage<BaseSearchWordEntity> page = this.baseMapper.querySearchWord(new Query<BaseSearchWordEntity>().getPage(data), baseSearchWordDtoReq);

                return ResponseResult.success(PageUtils.getPage(page));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 查询所有关键词。
     *
     * @return 关键词列表
     */
    @Override
    public ResponseResult queryKeyWords() {
        List<String> keyWords = this.baseSearchWordMapper.queryKeyWords();
        return ResponseResult.success(keyWords);
    }
}

