package com.houjiang.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.base.entity.BaseDistrictEntity;
import com.houjiang.base.mapper.BaseDistrictMapper;
import com.houjiang.base.service.BaseDistrictService;
import com.houjiang.base.vo.district.ShopAllBaseDistrict2VO;
import com.houjiang.base.vo.district.ShopAllBaseDistrict3VO;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.excel.Constants;
import com.houjiang.commons.vo.base.DealerSetVO;
import com.houjiang.commons.vo.shop.ShopAllBaseDistrictVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseDistrictServiceImpl
 * @Description 地区
 * @Author Amos
 * @Date 2023/6/14 15:02
 * @Version 1.0
 * @author Amos
 */
@Service
public class BaseDistrictServiceImpl extends ServiceImpl<BaseDistrictMapper, BaseDistrictEntity>
    implements BaseDistrictService{

    @org.springframework.beans.factory.annotation.Autowired
    private RedisCache redisCache;

    @Autowired
    private BaseDistrictMapper baseDistrictMapper;


    /**
     * 根据父区域ID查询不同的区域实体列表。
     * <p>
     * 该方法用于查询特定父区域ID下的所有区域实体。如果父区域ID为null，则查询所有根区域实体。
     * 主要用于前端展示区域选择列表，例如省、市、县的级联选择。
     *
     * @param districtParentId 父区域ID，用于限定查询的区域范围。如果为null，则查询所有根区域。
     * @return 返回包含所有符合条件的区域实体的列表。如果查询结果为空，则列表为空。
     */
    @Override
    public ResponseResult queryDistinct(Integer districtParentId) {
        // 使用LambdaQueryWrapper构造查询条件，根据父区域ID查询区域实体列表
        List<BaseDistrictEntity> districts = this.baseMapper.selectList(new LambdaQueryWrapper<BaseDistrictEntity>()
                .eq(null != districtParentId, BaseDistrictEntity::getDistrictParentId, districtParentId));
        // 返回查询结果作为成功响应
        return ResponseResult.success(districts);
    }

    /**
     * 根据省份ID查询对应的港口信息。
     * <p>
     * 该方法用于查询特定省份对应的出口港口信息。每个省份可能有多个出口港口，此方法返回这些港口的字符串组合。
     * 主要用于物流或运输系统中，根据发货地选择合适的出口港口。
     *
     * @param provinceId 省份ID，用于查询该省份对应的港口信息。
     * @return 返回该省份所有港口信息的字符串组合。如果查询结果为空，则返回空字符串。
     */
    @Override
    public String queryProvinceport(String provinceId) {
        // 直接调用baseDistrictMapper查询指定省份的港口信息
        return baseDistrictMapper.queryProvinceport(provinceId);
    }
    /**
     * 获取地址所有的父分类id
     *
     * @param allMenu
     * @param catId
     * @return
     */
    public List<Integer> getDistrictList(List<ShopAllBaseDistrictVO> allMenu, Integer catId) {
        List<Integer> allChildCatIds = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(allMenu)) {
            for (ShopAllBaseDistrictVO catMenu : allMenu) {
                if (catMenu.getDistrictId().equals(catId)) {
                    allChildCatIds.add(catMenu.getDistrictId());
                    if (!catMenu.getDistrictParentId().equals(0)) {
                        getDistrictChild3(allMenu, catMenu.getDistrictParentId(), allChildCatIds);
                    }
                    break;
                }

            }
        }
        return allChildCatIds;

    }

    /**
     * 找出子菜单
     *
     * @return
     */
    private List<Integer> getDistrictChild3(List<ShopAllBaseDistrictVO> allMenu, Integer parentId,
                                            List<Integer> allChildCatIds) {

        for (ShopAllBaseDistrictVO catMenu : allMenu) {
            if (catMenu.getDistrictId().equals(parentId)) {
                allChildCatIds.add(catMenu.getDistrictId());
                parentId = catMenu.getDistrictParentId();
                break;
            }
        }

        if (!parentId.equals(0)) {
            getDistrictChild3(allMenu, parentId, allChildCatIds);
        }

        return allChildCatIds;
    }

    @Override
    public ResponseResult getAddressString(Integer provinceId, Integer cityId, Integer districtId) {

        //省或直辖市名称
        String provinceName= this.baseMapper.selectById(String.valueOf(provinceId)).getDistrictName();
        //市或直辖市区名称
        String cityName = this.baseMapper.selectById(String.valueOf(cityId)).getDistrictName();
        //区或直辖市区街道名称
        String districtName = this.baseMapper.selectById(String.valueOf(districtId)).getDistrictName();
        String address=provinceName+cityName+districtName;
        address=address.replace("null","");
        return ResponseResult.success(address);
    }

    @Override
    public ResponseResult queryAllDistrict() {

        List<ShopAllBaseDistrictVO> districtList = null;

        // 从缓存中取
        String value2 = (String) redisCache.getCacheObject(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE2);
        if (null != value2 && !value2.equals("")) {
            districtList = JSON.parseArray(value2, ShopAllBaseDistrictVO.class);
        } else {
            // 从缓存中取
            List<ShopAllBaseDistrictVO> allMenu = null;
            String value = (String) redisCache.getCacheObject(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE);
            if (null != value && !value.equals("")) {
                allMenu = JSON.parseArray(value, ShopAllBaseDistrictVO.class);
            } else {
                allMenu = baseDistrictMapper.selectAllDistrict();
                // 设置缓存
                redisCache.setCacheObject(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE, JSON.toJSONString(allMenu));
                redisCache.expire(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
            }
            districtList = getDistrictList2(allMenu);
            // 设置缓存
            redisCache.setCacheObject(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE2, JSON.toJSONString(districtList));
            redisCache.expire(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE2, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
        }

        return ResponseResult.success(districtList);
    }

    @Override
    public List<String> selectByIdList(List<Integer> districtIds) {
        return baseDistrictMapper.selectByIdList(districtIds);
    }

    @Override
    public List<String> queryRegionName(Map<String, Integer> districtMap) {
        return this.baseMapper.queryRegionName(districtMap);
    }


    /**
     * 查询所有的区域
     *
     * @param rootMenus
     * @return
     */
    public List<ShopAllBaseDistrictVO> getDistrictList2(List<ShopAllBaseDistrictVO> rootMenus) {
        // 先找到所有的一级菜单
        List<ShopAllBaseDistrictVO> childMenu = Lists.newArrayList();
        for (ShopAllBaseDistrictVO menu : rootMenus) {

            if (menu.getDistrictParentId().equals(0)) {
                ShopAllBaseDistrictVO menu2 = new ShopAllBaseDistrictVO();
                menu2.setDistrictId(menu.getDistrictId());
                menu2.setDistrictName(menu.getDistrictName());
                menu2.setCheckedSubMenus(new ArrayList<>());
                menu2.setCheckAll(false);
                menu2.setIsIndeterminate(false);
                List<ShopAllBaseDistrictVO> child1 = getDistrictChild2(menu.getDistrictId(), rootMenus);
                menu2.setChildDistrict(child1);
                childMenu.add(menu2);
            }
        }
        return childMenu;

    }


    /**
     * 找出子菜单
     *
     * @param parentId
     * @param rootMenu
     * @return
     */
    private List<ShopAllBaseDistrictVO> getDistrictChild2(Integer parentId, List<ShopAllBaseDistrictVO> rootMenu) {
        // 子菜单
        List<ShopAllBaseDistrictVO> childList = new ArrayList<ShopAllBaseDistrictVO>();

        for (ShopAllBaseDistrictVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (!StringUtils.isEmpty(menu.getDistrictParentId())) {
                if (menu.getDistrictParentId().equals(parentId)) {
                    ShopAllBaseDistrictVO district2 = new ShopAllBaseDistrictVO();
                    district2.setDistrictId(menu.getDistrictId());
                    district2.setDistrictName(menu.getDistrictName());
                    childList.add(district2);
                }
            }
        }
        return childList;
    }

    @Override
    public String getAddressById(Integer districtId) {
        return baseMapper.queryProvincePort(String.valueOf(districtId));
    }

    @Override
    public ResponseResult queryAllDistrictList() {
        List<ShopAllBaseDistrictVO> allMenu = this.baseMapper.selectAllDistrict2();
        // 设置缓存
        redisCache.setCacheObject(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE, JSON.toJSONString(allMenu));
        redisCache.expire(Constants.CACHE_DISTRICTS_SHOP_ADDRESS_ALL_BASE, RedisUtil.EXRP_MONTH, java.util.concurrent.TimeUnit.SECONDS);
        // 将旧格式数据转换为新格式
        List<ShopAllBaseDistrict3VO> districtList = getDistrictList3(allMenu);
        // 返回查询结果
        return ResponseResult.success(districtList);
    }

    private List<ShopAllBaseDistrict3VO> getDistrictList3(List<ShopAllBaseDistrictVO> rootMenus) {
        // 初始化用于存储所有一级区域的列表
        // 先找到所有的一级菜单
        List<ShopAllBaseDistrict3VO> childMenu = Lists.newArrayList();
        // 遍历给定的一级地区菜单列表
        for (ShopAllBaseDistrictVO menu : rootMenus) {
            // 判断当前菜单是否为一级菜单（父ID为0）
            if (menu.getDistrictParentId().equals(0)) {
                // 创建一个新的区域对象，用于表示一级区域
                ShopAllBaseDistrict3VO menu2 = new ShopAllBaseDistrict3VO();
                // 设置区域的ID和名称
                menu2.setDistrictId(menu.getDistrictId());
                menu2.setDistrictName(menu.getDistrictName());
                // 初始化子菜单列表和复选框状态
                menu2.setCheckedSubMenus(new ArrayList<>());
                menu2.setCheckAll(false);
                menu2.setIsIndeterminate(false);
                // 获取当前区域的所有子区域，并设置到新区域对象中
                List<ShopAllBaseDistrict2VO> child1 = getDistrictChild4(menu.getDistrictId(), rootMenus);
                menu2.setChildDistrict(child1);
                // 将当前区域对象添加到结果列表中
                childMenu.add(menu2);
            }
        }
        // 返回处理后的区域列表
        return childMenu;
    }

    /**
     * 根据父级区域ID，从根菜单中筛选出子区域列表。
     * 此方法用于构建区域选择菜单的层级结构，从一级区域到更具体的子区域。
     *
     * @param parentId 父级区域的ID，用于查找其子区域。
     * @param rootMenu 根区域列表，包含所有区域信息。
     * @return 筛选后的子区域列表，仅包含指定父级区域的子区域。
     */
    private List<ShopAllBaseDistrict2VO> getDistrictChild4(Integer parentId, List<ShopAllBaseDistrictVO> rootMenu) {
        // 初始化子区域列表，用于存储找到的子区域。
        // 子菜单
        List<ShopAllBaseDistrict2VO> childList = new ArrayList<>();

        // 遍历根区域列表，查找匹配的子区域。
        for (ShopAllBaseDistrictVO menu : rootMenu) {
            // 检查区域是否有父级ID，排除顶级区域。
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (menu.getDistrictParentId() != null) {
                // 比较区域的父级ID与指定的父级ID是否匹配。
                if (menu.getDistrictParentId().equals(parentId)) {
                    // 匹配成功，创建子区域对象，并添加到子区域列表。
                    ShopAllBaseDistrict2VO district2 = new ShopAllBaseDistrict2VO();
                    district2.setDistrictId(menu.getDistrictId());
                    district2.setDistrictName(menu.getDistrictName());
                    district2.setDistrictIsLevel(menu.getDistrictIsLevel());
                    if (menu.getDistrictIsLevel() != 3) {
                        district2.setChildDistrict(getDistrictChild4(menu.getDistrictId(), rootMenu));
                    } else {
                        district2.setChildDistrict(null);
                    }
                    childList.add(district2);
                }
            }
        }
        // 返回找到的子区域列表。
        return childList;
    }

    /**
     * 根据地址名得到地址Id
     *
     * @param addressName 地址名
     * @return 地址Id
     */
    @Override
    public Integer getAddressIdByName(String addressName, Integer level) {
        return baseMapper.selectOne(new LambdaQueryWrapper<BaseDistrictEntity>()
                .eq(BaseDistrictEntity::getDistrictName, addressName)
                .eq(BaseDistrictEntity::getDistrictIsLevel, level)).getDistrictId();
    }

    @Cacheable(value = "district", key = "#root.methodName + #root.args")
    @Override
    public String queryDistrictCode(String districtId) {
        // 通过地区ID查询省份信息，这里使用了BaseMapper的查询方法。
        return this.baseMapper.queryDistrictCode(districtId);

    }

    @Override
    public List<ShopAllBaseDistrictVO> selectAllDistrict2() {
        List<ShopAllBaseDistrictVO> result = this.baseMapper.selectAllDistrict2();
        return result;
    }

    @Override
    public DealerSetVO queryDealerSet(String name) {
        DealerSetVO result = this.baseMapper.queryDealerSet(name);
        return result;
    }

}




