package com.houjiang.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.constant.RedisConstants;
import com.houjiang.commons.dto.goods.WebGoodsCatQueryDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.excel.Constants;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.commons.util.querypage.Query;
import com.houjiang.commons.vo.base.BaseSysConfigsVO;
import com.houjiang.commons.vo.base.PlatformoperationVO;
import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.goodscat.GoodsCatSearchChildVO;
import com.houjiang.commons.vo.goodscat.WebCatMenuVO;
import com.houjiang.commons.vo.shop.ApplyCatVO;
import com.houjiang.commons.vo.shop.ShopGoodsCatVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.goods.dto.ManageItemCatQueryDTO;
import com.houjiang.goods.dto.PlatFormGoodsCatsDTO;
import com.houjiang.goods.dto.ShopClassBindQueryDTO;
import com.houjiang.goods.entity.GoodsAttributeContentEntity;
import com.houjiang.goods.entity.GoodsAttributeEntity;
import com.houjiang.goods.entity.GoodsCatEntity;
// import com.houjiang.goods.feign.BusinessServiceClient; // TODO: business 服务已删除
import com.houjiang.goods.feign.MerchantServiceClient;
import com.houjiang.goods.mapper.*;
import com.houjiang.goods.service.GoodsCatService;
import com.houjiang.goods.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsCatServiceImpl
 * @Description 店铺商品分类
 * @Author Amos
 * @Date 2023/6/6 15:40
 * @Version 1.0
 * @author Amos
 */
@Service
public class GoodsCatServiceImpl extends ServiceImpl<GoodsCatMapper, GoodsCatEntity>
        implements GoodsCatService {

    @Autowired
    private GoodsCatMapper goodsCatMapper;

    @Autowired
    private GoodsBaseMapper goodBaseMapper;

    @Autowired
    private GoodsAttributeMapper goodsAttributeMapper;

    @Autowired
    private GoodsAttributeContentMapper goodsAttributeContentMapper;

    @Autowired
    private GoodsBrandMapper goodsBrandMapper;

    @Autowired
    private MerchantServiceClient merchantServiceClient;

    /**
     * 查询所有商品分类，包括一级、二级和三级分类。
     * 该方法首先从数据库中获取所有分类，然后经过处理，去除没有三级分类的一二级分类，
     * 并进一步筛选出所有拥有子分类的分类。最后，将处理后的分类列表存储到缓存中，
     * 以提高后续查询效率，并返回处理后的分类列表。
     *
     * @return 包含所有商品分类的列表，每个分类都包括其所有的子分类。
     */
    @Override
    public ResponseResult queryAll() {
        // 查询所有分类
        List<ManageShopCatMenuVO> rootMenus = this.baseMapper.selectAll();
        
        // 构建树形结构
        List<ManageShopCatMenuVO> menuLists = getMenuList3(rootMenus);
        
        // 直接返回所有分类(包括一级、二级、三级),不过滤
        // 前端会根据需要显示对应层级
        
        // 将分类列表存储到缓存中
        RedisUtil.set(Constant.CACHE_GOODS_CAT_MANAGE_ALL, JsonUtils.toString(menuLists), RedisUtil.EXRP_MONTH);
        
        // 返回处理后的分类列表
        return ResponseResult.success(menuLists);
    }

    @Override
    public Integer queryParentCatId(ShopClassBindQueryDTO param) {
        return goodsCatMapper.queryParentCatId(param);
    }

    @Override
    public Integer queryChildCount(Integer param) {
        return goodsCatMapper.queryChildCount(param);
    }

    /**
     * 查询所有商品分类。
     * 优先从Redis缓存中获取，如果缓存中不存在，则从数据库中查询并存入缓存。
     *
     * @param shopId 店铺ID，用于特定店铺的商品分类查询，本方法中未使用。
     * @return 返回所有商品分类的结果，包含在ResponseResult对象中。
     */
    @Override
    public ResponseResult queryAllCat(Integer shopId) {
        // 初始化用于存储分类菜单的列表
        List<ManageShopCatMenuVO> menuLists = new ArrayList<ManageShopCatMenuVO>();
        // 尝试从Redis缓存中获取所有商品分类的数据
        // 从缓存中查找
        String str = RedisUtil.get(Constants.CACHE_GOODS_CAT_MANAGE_ALL);

        // 如果缓存中存在数据且不为空，则直接返回缓存数据
        if (null != str && !str.equals("")) {
            List<ManageShopCatMenuVO> redisResult = JsonUtils.string2Obj(str, List.class, ManageShopCatMenuVO.class);
            return ResponseResult.success(redisResult);
        }
        // 从数据库中查询所有商品分类
        List<ManageShopCatMenuVO> rootMenus = goodsCatMapper.selectAllCat2();
        // 处理分类数据，得到最终的菜单列表
        menuLists = getMenuList3(rootMenus);
        // 将处理后的菜单列表存入Redis缓存
        // 存入缓存
        RedisUtil.set(Constants.CACHE_GOODS_CAT_MANAGE_ALL, JsonUtils.toString(menuLists), RedisUtil.EXRP_MONTH);
        // 返回处理后的菜单列表
        return ResponseResult.success(menuLists);
    }


    /**
     * 获取一级和二级菜单列表
     * 该方法通过遍历给定的菜单列表，筛选出一级菜单，并为每个一级菜单添加其二级子菜单。
     * 一级菜单是指父级菜单ID为0的菜单，二级菜单是指直接属于某一级菜单的子菜单。
     *
     * @param rootMenus 菜单列表，包含所有级别的菜单
     * @return 包含一级及其二级子菜单的列表
     */
    public List<ManageShopCatMenuVO> getMenuList3(List<ManageShopCatMenuVO> rootMenus) {
        // 初始化一个列表用于存储筛选出的一级菜单
        List<ManageShopCatMenuVO> list = new ArrayList<ManageShopCatMenuVO>();

        // 遍历rootMenus，寻找一级菜单
        for (int i = 0; i < rootMenus.size(); i++) {
            ManageShopCatMenuVO menu = rootMenus.get(i);
            // 一级菜单的父级菜单ID为0
            // 一级菜单 没有父节点
            if (menu.getCatParentId() == 0) {
                list.add(menu);
            }
        }

        // 为每个一级菜单添加其二级子菜单
        /**
         * 利用递归找出所有子菜单
         */
        for (ManageShopCatMenuVO menu : list) {
            // 通过递归调用获取当前菜单的所有二级子菜单，并设置到当前菜单对象中
            menu.setChildMenu(getChild3(menu.getCatId(), rootMenus));
        }
        return list;
    }

    /**
     * 根据父菜单ID获取子菜单列表
     * 该方法通过递归方式查找并构建菜单树，直到找到所有子菜单或无子菜单为止。
     *
     * @param parentId 父菜单的ID，用于查找其子菜单
     * @param rootMenu 所有菜单的列表，作为查找的源数据
     * @param refundRatio 退款比例，用于为菜单设置退款比例，如果菜单未设置则使用传入的值
     * @return 子菜单列表，如果找不到子菜单则返回null
     */
    private List<ManageShopCatMenuVO> getChild3(Integer parentId, List<ManageShopCatMenuVO> rootMenu, BigDecimal refundRatio) {
        // 初始化子菜单列表
        List<ManageShopCatMenuVO> childList = new ArrayList<>();

        // 遍历所有菜单，查找父菜单ID匹配的菜单项
        for (ManageShopCatMenuVO menu : rootMenu) {
            // 检查菜单是否有父菜单ID且与指定的父菜单ID匹配
            if (!StringUtils.isEmpty(menu.getCatParentId()) && menu.getCatParentId().equals(parentId)) {
                // 如果菜单未设置退款比例，则使用传入的退款比例
                if (menu.getRefundRatio() == null) {
                    menu.setRefundRatio(refundRatio);
                }
                // 将匹配的菜单项添加到子菜单列表
                childList.add(menu);
            }
        }

        // 如果子菜单列表为空，表示没有找到子菜单，递归结束，返回null
        if (childList.size() == 0) {
            return null;
        }

        // 对子菜单列表中的每个菜单项，递归调用本方法查找其子菜单
        for (ManageShopCatMenuVO menu : childList) {
            // 递归获取子菜单，并设置到菜单项中
            menu.setChildGoodsCatList(getChild3(menu.getCatId(), rootMenu, menu.getRefundRatio()));
        }

        // 返回构建好的子菜单列表
        return childList;
    }


    /**
     * 根据父菜单ID查找子菜单
     * 该方法通过递归方式查找指定父菜单ID的所有子菜单，并构建菜单树结构。
     *
     * @param parentId 父菜单的ID，用于查找其子菜单
     * @param rootMenu 所有菜单的列表，作为查找的根菜单集合
     * @return 返回找到的子菜单列表，如果无子菜单则返回null
     */
    /**
     * 找出子菜单
     *
     * @param parentId
     * @param rootMenu
     * @return
     */
    private List<ManageShopCatMenuVO> getChild3(Integer parentId, List<ManageShopCatMenuVO> rootMenu) {
        // 初始化子菜单列表
        // 子菜单
        List<ManageShopCatMenuVO> childList = new ArrayList<ManageShopCatMenuVO>();

        // 遍历根菜单列表，查找父菜单ID匹配的菜单项
        for (ManageShopCatMenuVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (!StringUtils.isEmpty(menu.getCatParentId())) {
                if (menu.getCatParentId().equals(parentId)) {
                    childList.add(menu);
                }
            }
        }

        // 如果子菜单列表为空，表示未找到子菜单，递归结束，返回null
        // 递归退出条件
        if (childList.size() == 0) {
            return null;
        }

        // 对找到的子菜单列表，递归调用本方法，查找其子菜单，构建菜单树结构
        // 把子菜单的子菜单再循环一遍
        for (ManageShopCatMenuVO menu : childList) {
            // 递归
            menu.setChildMenu(getChild3(menu.getCatId(), rootMenu));
        }

        // 返回找到的子菜单列表
        return childList;
    }


    @Override
    public ResponseResult queryItemCat(ManageItemCatQueryDTO param) {
        List<ManageCatVO> goodsCats = new ArrayList<ManageCatVO>();

        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());
        Integer catParentId = param.getCatParentId();
        goodsCats = goodsCatMapper.selectByCatParentId(catParentId, userInfo.getShopType());

        return ResponseResult.success(goodsCats);
    }


    /**
     * 根据入会类别查询分类信息。
     * @param param 查询条件，包含分类级别和分类父ID等信息。
     * @return 分类结果，如果用户信息无效或未找到相关分类，则返回空或错误信息。
     */
    @Override
    public ResponseResult queryByRuzhu(ManageItemCatQueryDTO param){

        // 从Redis中根据token获取用户信息字符串
        String userInfoStr = RedisUtil.get(RedisConstants.USER_TOKEN_USER + param.getToken());
        // 解析token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(param.getToken());

        // 检查用户信息是否有效
        if (null == userInfoStr || userInfoStr.equals("")) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.TOKEN_IS_INVALID.getMsg());
        }
        if(null != userInfo){

            // 获取入会分类级别和商家ID
            Integer catLevel = param.getCatLevel();
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            // 根据分类级别查询不同的分类信息
            // 查询一级分类
            if(1 == catLevel){
                // 查询商家所有入会的一级分类
                // 查询商家入职的分类
                List<Integer> ruZhuCatids = goodsCatMapper.queryRuZhuCat(shopId);
                Set<Integer> catResults = new HashSet<>();
                // 递归查询所有一级分类的父分类
                if(null != ruZhuCatids && ruZhuCatids.size()> 0){
                    for (Integer catid : ruZhuCatids) {
                        this.queryParentCat(catid,catResults);
                    }
                }
                // 根据查询结果返回相应的分类信息
                if(null == catResults || ruZhuCatids.size() ==  0){
                    return ResponseResult.success();
                }else{
                    List<ManageCatVO> manageCatParent = goodsCatMapper.queryByCatIds2(catResults);
                    return ResponseResult.success(manageCatParent);
                }
                // 查询二级分类
            }else if(2 == catLevel){
                // 查询商家所有入会的一级分类
                // 查询商家入职的分类
                List<Integer> ruZhuCatids = goodsCatMapper.queryRuZhuCat(shopId);
                // 根据父分类ID查询二级分类
                // 根据一级分类id 查询所有二级分类
                List<Integer> catids = goodsCatMapper.queryByPranent(param.getCatParentId());
                // 查询商家入会分类的父分类与目标二级分类的交集，即符合条件的二级分类
                if(null != ruZhuCatids && ruZhuCatids.size() > 0 && null != catids && catids .size() > 0 ){
                    // 根据入职的分类id查询入职分类的上级id
                    List<Integer> ruZhuCatidPranent = goodsCatMapper.queryRuZhuPranentCat(ruZhuCatids);
                    // 两个分类取交集
                    List<Integer>  jiaoList = new ArrayList<>(ruZhuCatidPranent);
                    jiaoList.retainAll(catids);
                    if(null != jiaoList && jiaoList.size() > 0 ){
                        List<ManageCatVO> manageCatParent = goodsCatMapper.queryByCatIds(jiaoList);
                        return ResponseResult.success(manageCatParent);
                    }else{
                        return ResponseResult.success();
                    }
                }
                // 查询三级分类
            }else if(3 == catLevel){
                // 查询商家所有入会的一级分类
                // 查询商家入职的分类
                List<Integer> ruZhuCatids = goodsCatMapper.queryRuZhuCat(shopId);
                // 根据父分类ID查询三级分类，同时包含父分类ID自身
                // 根据前端传入的二级类目查询三级
                List<Integer> catids = goodsCatMapper.queryByPranent(param.getCatParentId());
                catids.add(param.getCatParentId());
                // 查询商家入会分类与目标三级分类的交集，即符合条件的三级分类
                if(null != ruZhuCatids && ruZhuCatids.size() > 0 && null != catids && catids .size() > 0 ){
                    // 两个分类取交集
                    List<Integer>  jiaoList = new ArrayList<>(ruZhuCatids);
                    jiaoList.retainAll(catids);
                    if(null != jiaoList && jiaoList.size() > 0 ){
                        List<ManageCatVO> manageCatParent = goodsCatMapper.queryByCatIds(jiaoList);
                        return ResponseResult.success(manageCatParent);
                    }else{
                        return ResponseResult.success();
                    }
                }
            }

        }else{
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(),BizCodeEnume.TOKEN_IS_NULL.getMsg());
        }

        return ResponseResult.success();
    }

    /**
     * 根据传入的条件查询平台商品分类。
     *
     * @param dto 查询条件DTO，包含分类展示状态、父分类ID、分类ID、分页信息等。
     * @return 返回查询结果，包含商品分类列表和总数。
     */
    @Override
    public ResponseResult queryGoodsCats(PlatFormGoodsCatsDTO dto) {
        // 初始化查询结果列表
        List<ManageShopCatMenuVO> results = null;

        // 初始化根分类列表
        List<ManageShopCatMenuVO> rootMenus = null;
        // 构建查询参数
        Map<String, Object> param = new HashMap<>();
        // 设置查询参数：分类展示状态和父分类ID
        param.put("catShowStatus", dto.getCatShowStatus());
        param.put("catParentId", dto.getCatParentId());
        // 根据参数查询平台商品分类
        rootMenus = goodsCatMapper.selectPlatFormCats(param);
        // 查询符合条件的分类总数
        Integer totalCount = goodsCatMapper.selectPlatFormCatsCount(param);
        // 根据根分类列表和分类ID获取完整的分类列表
        results = getMenuList3(rootMenus, dto.getCatId());

        // 如果传入了分页信息
        if (null != dto.getLimit() && null != dto.getPage()) {
            // 如果查询结果不为空
            if (null != results) {
                // 计算分页后的查询结果
                int toIndex;
                if (dto.getLimit() * dto.getPage() < results.size()) {
                    toIndex = dto.getLimit() * dto.getPage();
                } else {
                    toIndex = results.size();
                }
                results = results.subList((dto.getLimit() - 1) * dto.getPage(), toIndex);
            }

            // 构建返回的数据包，包含查询结果和总数
            HashMap<String, Object> map = new HashMap<>();
            map.put("data", results);
            map.put("totalCount", totalCount);
            // 返回分页后的查询结果
            return ResponseResult.success(map);
        }

        // 如果没有分页信息，直接返回查询结果
        return ResponseResult.success(results);
    }


    /**
     * 根据条件查询商品分类信息。
     *
     * @param map 查询条件，以键值对形式传递，可以包含多个条件。
     * @return 返回查询结果的响应对象，其中包含分页信息和查询到的商品分类数据。
     */
    @Override
    public ResponseResult goodscat(Map<String,Object> map) {
        // 通过基础Mapper接口的getGoodscatByPage方法，根据查询条件获取分页后的商品分类数据
        IPage<GoodsCatEntity> page=this.baseMapper.getGoodscatByPage(new Query<GoodsCatEntity>().getPage(map),
                map);

        // 将查询结果包装成响应结果对象返回，其中包含分页信息
        return ResponseResult.success(PageUtils.getPage(page));
    }

    /**
     * 查询非积分商品分类
     * @param map 包含查询条件的映射表，可能包含分类展示状态、分页信息等
     * @return 返回查询结果的响应对象
     */
    @Override
    public ResponseResult queryNotIntegral(Map<String, Object> map) {
        // 初始化结果列表
        List<PlatFormGoodsCatsVO> results = null;

        // 初始化根分类列表
        List<PlatFormGoodsCatsVO> rootMenus = null;
        // 获取店铺分类列表
        List<Integer> catList = this.baseMapper.getShopCats(map);
        // 设置分类展示状态为显示
        map.put("catShowStatus",1);
        // 根据展示状态查询平台非积分商品分类
        rootMenus = goodsCatMapper.selectPlatFormCatsNotIntegral(Integer.valueOf(map.get("catShowStatus").toString()));
        // 查询满足条件的分类总数
        Integer totalCount = goodsCatMapper.selectPlatFormCatsCountNotIntegral(Integer.valueOf(map.get("catShowStatus").toString()));

        // 处理分类菜单列表
        results = getMenuList5(rootMenus,catList);
        // 移除不符合条件的分类
        // 三级分类为空时删除二级分类，二级分类为空时删除一级分类
        results = removeCat(results);

        // 如果存在分页信息且分页有效，则进行分页处理
        if(map.get("limit") != null && map.get("page") != null && Integer.valueOf(map.get("limit").toString()) <= totalCount){
            // 获取每页记录数和当前页数
            int limt=Integer.valueOf(map.get("limit").toString());
            int page= Integer.valueOf(map.get("page").toString());
            // 计算查询起始位置
            map.put("limit",(limt - 1) * page);
            // 根据总数调整最后一页的页码，以确保分页逻辑正确
            if(totalCount < limt+page) {
                map.put("page",page-((limt+page)-totalCount));
            }else if(totalCount == limt+page){
                map.put("page",page-1);
            }else{
                map.put("page",page);
            }
            // 对结果进行分页
            results = results.subList(limt,limt+page);
            // 构建包含分页结果和总数的响应对象
            HashMap<String,Object> result = new HashMap<>();
            result.put("data",results);
            result.put("totalCount",totalCount);
            // 返回分页处理后的响应对象
            return ResponseResult.success(result);
        }
        // 如果没有分页或分页无效，则直接返回所有结果
        return ResponseResult.success(results);
    }

    @Override
    public ResponseResult queryByGoodsCatId(Integer catId, Integer goodsType, String token) {
        // 获取分类id
        GoodsCatSearchChildVO goodsCatSearchChild = null;
        List<WebCatMenuVO> allMenu = null;
        List<Integer> catIds = null;
        if (null != catId) {

            // 从缓存中获取
            String cacheCatIds = RedisUtil.get(Constant.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId);
            // 查询所有下级分类
            allMenu = this.baseMapper.selectAllCat();
            if (null != cacheCatIds && !cacheCatIds.equals("")) {
                goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
                catIds = goodsCatSearchChild.getCatIds();
            } else {
                allMenu = this.baseMapper.selectAllCat();
                catIds = getMenuList(allMenu, catId);
                goodsCatSearchChild = new GoodsCatSearchChildVO();
                goodsCatSearchChild.setAllMenu(allMenu);
                goodsCatSearchChild.setCatIds(catIds);
                RedisUtil.set(Constant.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId,
                        JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
            }
            catIds.add(catId);
        }

        List<GoodsIdsQueryVO> results = goodBaseMapper.selectByCatIds(catIds,goodsType);
        UserInfoVO userInfo = null;
        if(!StringUtils.isEmpty(token)) {
            userInfo=ParseTokenUtils.getUserInfoByToken(token);
        }
        if(null != userInfo && null != goodsType && 2 == goodsType){
            // 获取所有商品店铺信息
            List<Integer> goodsShopIdList = results.stream().map(GoodsIdsQueryVO::getShopId).distinct().collect(Collectors.toList());
            // 查询登录用户已申请经销商成功的店铺id
            List<BaseDealerApplyInfoVO> dealerApplyInfoList =  this.goodsCatMapper.queryDealerApplyListByShop(userInfo.getShopId(),goodsShopIdList);
            if(!CollectionUtils.isEmpty(dealerApplyInfoList)) {
                goodsShopIdList = dealerApplyInfoList.stream().map(BaseDealerApplyInfoVO::getSupplierShopId).distinct().collect(Collectors.toList());
                for (GoodsIdsQueryVO goods : results) {
                    // 商品无需申请经销商
                    if (null != goods.getSellCheck() && goods.getSellCheck().intValue() == 2) {
                        goods.setOperateType(goods.getSellDeliver());
                    } else {
                        if (!goodsShopIdList.contains(goods.getShopId())) {
                            goods.setOperateType(CommonType.goodsOperateType.APPLY.getCode());
                        } else {
                            goods.setOperateType(goods.getSellDeliver());
                        }
                    }
                }
            }else{
                for (GoodsIdsQueryVO goods : results) {
                    if(null != goods.getSellCheck()) {
                        if (goods.getSellCheck().intValue() == 1) {
                            goods.setOperateType (CommonType.goodsOperateType.APPLY.getCode());
                        } else {
                            goods.setOperateType (goods.getSellDeliver());
                        }
                    }
                }
            }
        }
        return ResponseResult.success(results);



    }

    /**
     * 根据商品分类ID查询商品分类信息。
     *
     * @param catId 商品分类ID
     * @return 查询结果，包含商品分类信息
     */
    @Override
    public ResponseResult queryCatByCatId(Integer catId) {
        // 使用LambdaQueryWrapper查询指定ID且展示状态为是的商品分类
        GoodsCatEntity currentCat = this.baseMapper.selectOne(new LambdaQueryWrapper<GoodsCatEntity>()
                .eq(GoodsCatEntity::getCatId, catId)
                .eq(GoodsCatEntity::getCatShowStatus, CommonType.commonStatus.YES.getCode()));
        return ResponseResult.success(currentCat);
    }

    /**
     * 查询商品分类列表。
     *
     * @param map 包含查询条件的Map对象，可包含展示状态和父分类ID
     * @return 查询结果，包含商品分类列表
     */
    @Override
    public ResponseResult queryCatList(Map<String, Object> map) {
        LambdaQueryWrapper<GoodsCatEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 根据传入的map中的showStatus条件进行查询
        if (map.get("showStatus") != null && !"null".equals(map.get("showStatus"))) {
            queryWrapper.eq(GoodsCatEntity::getCatShowStatus, map.get("showStatus"));
        }
        // 根据传入的map中的catParentId条件进行查询
        if (map.get("catParentId") != null && !"null".equals(map.get("catParentId"))) {
            queryWrapper.eq(GoodsCatEntity::getCatParentId, map.get("catParentId"));
        }
        // 执行查询并返回结果
        List<GoodsCatEntity> list = this.baseMapper.selectList(queryWrapper);
        return ResponseResult.success(list);
    }

    /**
     * 根据父分类ID查询子分类名称列表。
     *
     * @param productClassId 父分类ID。
     * @return 子分类名称列表。
     */
    @Override
    public List<String> querySonCatByParentId(Integer productClassId) {
        // 通过父分类ID查询子分类名称列表
        List<String> result = this.baseMapper.querySonCatByParentId(productClassId);
        return result;
    }

    /**
     * 根据父分类ID查询积分商品分类。
     *
     * @param map 包含token和catParentId的Map对象。
     * @return 查询结果。
     */
    @Override
    public ResponseResult queryIntegralItemCat(Map<String, Object> map) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(map.get("token").toString());
        if (null != userInfo) {
            // 将map中的catParentId转换为Integer类型
            Integer catParentId = Integer.valueOf(map.get("catParentId").toString());
            // 根据父分类ID查询积分商品分类
            // 从数据库查询分类
            List<GoodsCatEntity> goodsCats = this.baseMapper.queryIntegralItemCat(catParentId);

            // 返回查询成功的响应结果
            return ResponseResult.success(goodsCats);
        }
        // 如果用户信息为空，返回token非法的响应结果
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }



    /**
     * 获取指定商品分类及其所有子分类的ID列表。
     * 获取商品分类所有的子分类
     * @param allMenu 所有商品分类的列表。
     * @param catId 指定的商品分类ID。
     * @return 指定商品分类及其所有子分类的ID列表。
     */
    public List<Integer> getMenuList(List<WebCatMenuVO> allMenu, Integer catId) {
        // 初始化用于存储所有子分类ID的列表
        List<Integer> allChildCatIds = Lists.newArrayList();

        // 检查输入的分类列表是否为空
        if (!CollectionUtils.isEmpty(allMenu)) {
            // 遍历所有分类
            for (WebCatMenuVO catMenu : allMenu) {
                // 当找到指定ID的分类时
                if (catMenu.getCatId().equals(catId)) {
                    // 将该分类ID添加到结果列表中
                    allChildCatIds.add(catId);
                    // 递归获取该分类的所有子分类ID
                    getChild(allMenu, catMenu.getCatId(), allChildCatIds);
                }

            }
        }
        // 返回所有子分类ID的列表
        return allChildCatIds;

    }


    /**
     * 根据父级分类ID查询所有子级分类
     * 该方法主要用于处理菜单列表，将一级菜单与二级菜单进行分离，并为一级菜单添加对应的子菜单列表。
     *
     * @param rootMenus 原始菜单列表，包含所有级别菜单。
     * @return 返回处理后的菜单列表，仅包含一级菜单及各自对应的子菜单列表。
     */
    public List<ManageShopCatMenuVO> getMenuList(List<ManageShopCatMenuVO> rootMenus) {
        List<ManageShopCatMenuVO> childMenu = Lists.newArrayList();
        // 先找到所有的一级菜单
        List<ManageShopCatMenuVO> parentMenu = Lists.newArrayList();
        for (ManageShopCatMenuVO menu : rootMenus) {
            if (menu.getCatParentId().equals(0)) {
                parentMenu.add(menu);
            }else {
                childMenu.add(menu);
            }
        }

        for (ManageShopCatMenuVO goodsCatsMenuList : parentMenu) {
            List<ManageShopCatMenuVO> child1 = getChild(goodsCatsMenuList.getCatId(), childMenu, goodsCatsMenuList.getGroupId());
            goodsCatsMenuList.setChildGoodsCatList(child1);
        }
        return parentMenu;
    }



    /**
     * 根据父级ID获取子菜单列表。
     * 该方法通过递归方式构建菜单的层级关系，每个菜单项可能包含子菜单项。
     *
     * @param parentId 父菜单的ID，用于查找其子菜单。
     * @param rootMenu 所有菜单项的列表，作为查找子菜单的源数据。
     * @return 返回一个包含子菜单的列表。如果没有子菜单，则返回null。
     */
    private List<ManageShopCatMenuVO> getChild(Integer parentId, List<ManageShopCatMenuVO> rootMenu, Integer groupId) {
        // 初始化一个空列表，用于存放找到的子菜单项
        List<ManageShopCatMenuVO> childList = new ArrayList<ManageShopCatMenuVO>();

        // 遍历根菜单列表，查找父ID匹配的菜单项
        for (ManageShopCatMenuVO menu : rootMenu) {
            // 检查菜单的父ID是否为空，以及是否等于指定的父ID
            if (!StringUtils.isEmpty(menu.getCatParentId().toString())) {
                if (menu.getCatParentId().equals(parentId)) {
                    // 如果匹配，则将该菜单项添加到子菜单列表中
                    childList.add(menu);
                    if(menu.getGroupId() == null){
                        menu.setGroupId(groupId);
                    }
                }
            }
        }

        // 如果子菜单列表为空，则表示没有找到子菜单，返回null 递归退出条件
        if (childList.isEmpty()) {
            return null;
        }

        // 遍历子菜单列表，对每个菜单项递归调用本方法，查找其子菜单
        for (ManageShopCatMenuVO menu : childList) {
            // 递归
            menu.setChildGoodsCatList(getChild(menu.getCatId(), rootMenu, groupId));
        }

        // 返回包含子菜单的列表
        return childList;
    }




    /**
     * 找出子菜单
     *
     * @param parentId
     * @param allMenu
     * @return
     */
    private List<Integer> getChild(List<WebCatMenuVO> allMenu, Integer parentId, List<Integer> allChildCatIds) {

        List<Integer> childList = Lists.newArrayList();
        for (WebCatMenuVO catMenu : allMenu) {
            if (catMenu.getCatParentId().equals(parentId)) {
                allChildCatIds.add(catMenu.getCatId());
                childList.add(catMenu.getCatId());
                getChild(allMenu, catMenu.getCatId(), allChildCatIds);
            }
        }

        if (CollectionUtils.isEmpty(childList)) {
            return null;
        }

        return allChildCatIds;
    }

    /** 单三级分类为空时删除二级分类，二级分类为空时删除一级分类 */
    private List<PlatFormGoodsCatsVO> removeCat(List<PlatFormGoodsCatsVO> data){
        if (data != null && data.size()>0){
            Iterator<PlatFormGoodsCatsVO> iterator = data.iterator();
            while(iterator.hasNext()){
                PlatFormGoodsCatsVO platFormGoodsCatsResult = iterator.next();
                List<PlatFormGoodsCatsVO> catList = platFormGoodsCatsResult.getChildGoodsCatList();
                if (catList != null && catList.size()>0) {
                    Iterator<PlatFormGoodsCatsVO> iterator1 = catList.iterator();
                    while(iterator1.hasNext()){
                        PlatFormGoodsCatsVO next = iterator1.next();
                        List<PlatFormGoodsCatsVO> childGoodsCatList = next.getChildGoodsCatList();
                        if (childGoodsCatList == null || !(childGoodsCatList.size()>0)){
                            iterator1.remove();
                        }
                    }
                }  if (catList == null || !(catList.size()>0)){
                    iterator.remove();
                }
            }
        }


        return data;
    }

    public List<PlatFormGoodsCatsVO> getMenuList5(List<PlatFormGoodsCatsVO> rootMenus, List<Integer> catList) {
        List<PlatFormGoodsCatsVO> childMenu = Lists.newArrayList();
        for (PlatFormGoodsCatsVO menu : rootMenus) {

            if (menu.getCatParentId().equals(0)) {
                childMenu.add(menu);
            }
        }
        rootMenus.removeAll(childMenu);

        for (PlatFormGoodsCatsVO goodsCatsMenuList : childMenu) {
            List<PlatFormGoodsCatsVO> child1 = getChild5(goodsCatsMenuList.getCatId(),rootMenus,catList);
            goodsCatsMenuList.setChildGoodsCatList(child1);
        }

        return childMenu;
    }


    private List<PlatFormGoodsCatsVO> getChild5(Integer parentId, List<PlatFormGoodsCatsVO> rootMenu, List<Integer> catList) {
        List<PlatFormGoodsCatsVO> childList = new ArrayList<PlatFormGoodsCatsVO>();

        for (PlatFormGoodsCatsVO menu : rootMenu) {
            // 遍历所有节点，将父菜单id与传过来的id比较
            if (!StringUtils.isEmpty(menu.getCatParentId())) {
                if (menu.getCatParentId().equals(parentId)) {
                    if(!catList.contains(menu.getCatId())) {
                        childList.add(menu);
                    }
                }
            }
        }

        // 递归退出条件
        if (childList.size() == 0) {
            return null;
        }

        // 把子菜单的子菜单再循环一遍
        for (PlatFormGoodsCatsVO menu : childList) {
            // 递归
            menu.setChildGoodsCatList(getChild5(menu.getCatId(), rootMenu,catList));
        }

        return childList;
    }

    public Set<Integer> queryParentCat(Integer catid, Set<Integer> catResults) {
        //查询当前分类的信息
        ManageCatVO manageCatResult = goodsCatMapper.queryByCatId2(catid);
        if (null != manageCatResult && 0 == manageCatResult.getCatParentId()) {
            catResults.add(manageCatResult.getCatId());
            return catResults;
        }
        //查询当前分类的上级
        ManageCatVO manageCatParent = goodsCatMapper.queryByCatId2(manageCatResult.getCatParentId());
        if (null != manageCatParent && 0 == manageCatResult.getCatParentId()) {
            catResults.add(manageCatResult.getCatId());
            return catResults;
        } else if (manageCatParent == null){
            return catResults;
        } else {
            return queryParentCat(manageCatParent.getCatId(), catResults);
        }
    }

    @Override
    public ResponseResult queryCatListFilter(Map<String, Object> param) {
        if (param.get("shopId") != null && param.get("shopId") != ""){
            //查看店铺类型
            Integer shopType = goodsCatMapper.selectShopType((Integer) param.get("shopId"));
            if (shopType == 3){
                return ResponseResult.success();
            }
        }
        //catid
        Map<String,Object> map = new HashMap<>();
        // 查询所有下级分类
        Integer catId = null;
        if(null != param.get("catId") && !StringUtils.isEmpty(param.get("catId")) ){
            catId = Integer.parseInt(param.get("catId").toString());
        }
        List<WebCatMenuVO> nextCatIds  = goodsCatMapper.selectByCatParentId2(catId);
        List<Integer> list = new ArrayList<>();
        if (nextCatIds != null && !nextCatIds.isEmpty()){
            for (WebCatMenuVO nextCatId : nextCatIds) {
                List<WebCatMenuVO> nextChinldCatIds  = goodsCatMapper.selectByCatParentId2(nextCatId.getCatId());
                nextCatId.setAllChildCats(nextChinldCatIds);
                list.add(nextCatId.getCatId());
            }
        }
        List<WebCatMenuVO> webCatMenus = new ArrayList<>();

        if (!list.isEmpty()){
            webCatMenus = goodsCatMapper.selectByCatParentId3(list);
        }
        //商品属性查询
        LinkedHashSet<Map<String, Object>> goodsAttributes = null;
        if (webCatMenus == null || webCatMenus.isEmpty()){
            webCatMenus = new ArrayList<>();
            WebCatMenuVO webCatMenu = new WebCatMenuVO();
            webCatMenu.setCatId(catId);
            webCatMenus.add(webCatMenu);
        }

        goodsAttributes = goodsAttributeMapper.selectAttributes(webCatMenus);
        if(null != goodsAttributes && !goodsAttributes.isEmpty()) {
            //根据商品属性id查询属性内容
            for (Map<String,Object> goodsAttribute : goodsAttributes) {
                LinkedHashSet<Map<String,Object>> goodsAttributeContent =  goodsAttributeContentMapper.selectAttContents((Integer) goodsAttribute.get("id"));
                goodsAttribute.put("goodsAttributeContentResults",goodsAttributeContent);
            }
        }
        //品牌查询
        List<GoodsBrandCatParamVO> goodsBrandCatParam = goodsBrandMapper.selectBrandCats(webCatMenus);

        map.put("goodsBrandCatParam",goodsBrandCatParam);
        map.put("gdbGoodsAttParams",goodsAttributes);
        map.put("nextCatIds",nextCatIds);
        return ResponseResult.success(map);
    }

    @Override
    public ResponseResult queryIntegralItemCat(Integer catParentId) {
        List<GoodCatsResultVO> result = null;

        // 从数据库查询分类

        List<GoodsCatResultVO> goodsCats = goodsCatMapper.selectIntegralCatByParentId(catParentId);

        result = Lists.newArrayList();
        // 判断是否是二级分类，是获取三级分类
        if (catParentId != 0) {
            for (GoodsCatResultVO goodsCat : goodsCats) {
                GoodCatsResultVO cats = new GoodCatsResultVO();
                // 获取三级菜单
                List<GoodsCatResultVO> threeGoodsCats = goodsCatMapper.selectIntegralCat(goodsCat.getCatId());
                cats.setSecordCatId(goodsCat.getCatId());
                cats.setGdsCats(threeGoodsCats);
                cats.setSecordCatName(goodsCat.getCatName());
                result.add(cats);

            }
        } else {
            GoodCatsResultVO cats = new GoodCatsResultVO();
            cats.setGdsCats(goodsCats);
            result.add(cats);
        }
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult checkCatName(Map<String, Object> map) {
        String catName = (String) map.get("catName");
        Integer count = Math.toIntExact(this.baseMapper.selectCount(new LambdaQueryWrapper<GoodsCatEntity>().eq(GoodsCatEntity::getCatName, catName)));
        return ResponseResult.success(count);
    }

    @Override
    public ResponseResult queryIntegralGoodsCatsAll(PageHelper param) {
        List<ManageShopCatMenuVO> results = null;
        List<ManageShopCatMenuVO> rootMenus = null;
        rootMenus = this.baseMapper.selectIntegralCats();
        Integer totalCount = this.baseMapper.selectIntegralCatsCount();
        //数据解析封装
        results = getMenuList(rootMenus);
        if (param.getPage() != null && param.getLimit() != null && param.getPage() < totalCount) {
            param.setCurrentPage((param.getPage() - 1) * param.getLimit());
            if (totalCount < param.getPage() + param.getLimit()) {
                param.setPageSize(param.getLimit() - ((param.getPage() + param.getLimit()) - totalCount));
            } else if (totalCount == param.getPage() + param.getLimit()) {
                param.setPageSize(param.getLimit() - 1);
            } else {
                param.setPageSize(param.getLimit());
            }
            results = results.subList(param.getPage(), param.getPage() + param.getLimit());
            HashMap<String, Object> map = new HashMap<>();
            map.put("data", results);
            map.put("totalCount", totalCount);
            return ResponseResult.success(map);
        }
        return ResponseResult.success(results);
    }

    @Override
    public ResponseResult addIntegralCat(PlatFormGoodsCatsDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            if (null != param.getCatId()) {
                //修改积分分类
                this.baseMapper.updateIntegralGoodsCat(param);
            } else {
                if (null == param.getCatLevel()) {
                    param.setCatLevel(CommonType.catStart.GRADE_ONE.getCode());
                }
                List<PlatFormGoodsCatsDTO> goodsCats = Lists.newArrayList();
                String[] catNames = param.getCatName().split(",");
                for (String catName : catNames) {
                    param.setCatName(catName);
                    goodsCats.add(param);
                }
                //判读当前添加的类型是否是顶级类型
                if (null == param.getCatParentId() || CommonType.catStart.GRADE_ZERO.getCode().equals(param.getCatParentId())) {
                    //是顶级类型 直接添加
                    this.baseMapper.addIntegralCatsBatch(goodsCats);
                } else {
                    //根据他的上级id 查询他的上级是否允许添加虚拟商品
                    PlatFormGoodsCatsVO platFormGoodsCatsResult = this.baseMapper.selectParentCat(param.getCatParentId());
                    if (platFormGoodsCatsResult.getCatLevel().equals(CommonType.catStart.GRADE_ZERO.getCode())
                            || platFormGoodsCatsResult.getCatLevel().equals(CommonType.catStart.GRADE_ONE.getCode())) {
                        param.setCatLevel(CommonType.catStart.GRADE_TWO.getCode());
                    } else if (platFormGoodsCatsResult.getCatLevel().equals(CommonType.catStart.GRADE_TWO.getCode())) {
                        param.setCatLevel(CommonType.catStart.GRADE_THREE.getCode());
                    }
                    this.baseMapper.addIntegralCatsBatch(goodsCats);
                }
            }
            // 删除分类的缓存
            RedisUtil.remove("GOODS_INTEGRAL_CAT");
            return ResponseResult.success();
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult deleteIntegralCat(PlatFormGoodsCatsDTO param) {
        ResponseResult rr = deleteGoodsCat(param);
        if (rr.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            return rr;
        }
        // 删除积分分类的缓存
        RedisUtil.remove("GOODS_INTEGRAL_CAT");
        return ResponseResult.success();
    }

    @Override
    public ResponseResult deleteGoodsCat(PlatFormGoodsCatsDTO param) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(param.getToken());
        if (null != userInfo) {
            List<Integer> levelList = new ArrayList<>();
            //根据catId获取分类信息
            GoodsCatEntity cat = this.baseMapper.selectById(param.getCatId());
            if (!cat.getCatLevel().equals(CommonType.catStart.GRADE_THREE.getCode())) {
                //查询子类catId
                levelList = this.baseMapper.selectCatLevel(param.getCatId());
                //如果是1级分类循环查找3级分类id
                if (cat.getCatLevel().equals(CommonType.catStart.GRADE_ZERO.getCode())
                        || cat.getCatLevel().equals(CommonType.catStart.GRADE_ONE.getCode())) {
                    List<Integer> levelIds = new ArrayList<>(levelList);
                    for (Integer integer : levelIds) {
                        List<Integer> list = this.baseMapper.selectCatLevel(integer);
                        if (list.size() != 0) {
                            levelList.addAll(list);
                        }
                    }
                }
            }
            levelList.add(param.getCatId());
            Integer goodsCount = this.baseMapper.selectGoodsCountByCatIds(levelList);
//            //查询当前分类是否 是商家的经营类目
            levelList = levelList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (goodsCount == 0) {
                for (Integer catId : levelList) {
                    //删除分类
                    this.baseMapper.deleteById(catId);
                    //删除绑定的入驻分类
                    merchantServiceClient.deleteBindGoodsCat(catId);
                    merchantServiceClient.deleteClassBindTmp(catId);
                }
                // 删除分类的缓存
                RedisUtil.remove("GOODS_CAT");
                RedisUtil.fuzzyDeletion(RedisPrefixUtils.goodsClassify);
                RedisUtil.remove(Constant.CACHE_GOODS_CAT_MANAGE_ALL);
                return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
            }
            return ResponseResult.fail(BizCodeEnume.ELETE_GOODS_CAT_ERROP);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    @Override
    public ResponseResult addGoodsCat(PlatFormGoodsCatsDTO param) {
        if (null == param.getCatCommission()) {
            param.setCatCommission(new BigDecimal(0));
        }
        if (null == param.getBabyCommission()) {
            param.setBabyCommission(new BigDecimal(0));
        }
        if (param.getCatDisplayorder() >255 || param.getCatDisplayorder() <0){
            return ResponseResult.fail(BizCodeEnume.SORTING_VALUE_YOU_ENTERED_IS_OUT_OF_RANGE);
        }
        if (null != param.getCatId()) {
            //修改商品分类
            goodsCatMapper.updateGoodsCat(param);
        } else {
            if (null == param.getCatLevel()) {
                param.setCatLevel(0);
            }
            //判断当前分类是否是一级分类
            if(null == param.getCatParentId() || 0 == param.getCatParentId()){
                //是一级分类 直接添加
                goodsCatMapper.addPlatFormCatsBatch2(param);
            }else {
                //添加分了信息
                goodsCatMapper.addPlatFormCatsBatch2(param);
            }
        }
        // 删除分类的缓存
        RedisUtil.remove("JAVA_GOODS_CAT_WAP_GOODS_QUERY");
        RedisUtil.remove("GOODS_CAT");
        RedisUtil.remove(Constant.CACHE_GOODS_CAT_MANAGE_ALL);
        return ResponseResult.success(BizCodeEnume.OPERATION_SUCCESSFUL);
    }
    /**
     * 处理分类属性名  属性值
     * @param param
     */
    public void goodsCatAttribute(PlatFormGoodsCatsDTO param){
        //前端传入 分类属性
        List<GoodsAttributeEntity> goodsAttributeParamList = param.getGoodsAttriParam();
        //查询当前分类 已经添加的分类属性
        List<GoodsAttributeEntity> existGoodsAttribute =goodsAttributeMapper.selectAttribute(param.getCatId());
        if(null != existGoodsAttribute && existGoodsAttribute.size() > 0 ){
            for(GoodsAttributeEntity goodsAtt:existGoodsAttribute){
                //查询当前分类属性值
                List<GoodsAttributeContentEntity> attributeContent = goodsAttributeContentMapper.queryAttributeContentList(goodsAtt.getId());
                goodsAtt.setAttrContentList(attributeContent);
            }
        }

        if((goodsAttributeParamList != null && goodsAttributeParamList.size() > 0)
                && (existGoodsAttribute != null && existGoodsAttribute.size() > 0 )){
            // 前端传入 和 已经添加的分类属性都不为空
            //需要添加的分类属性名
            List<GoodsAttributeEntity> addGoodsAttribute = new ArrayList<>();
            //需要删除的分类属性名
            List<GoodsAttributeEntity> deleteGoodsAttribute = new ArrayList<>();
            //需要修改属性值 数据
            List<GoodsAttributeEntity> updateGoodsAttribute = new ArrayList<>();

            // 解析处要删除的分类属性名 和 对应的值
            for (GoodsAttributeEntity goodsAttributeParam : existGoodsAttribute) {
                if(!goodsAttributeParamList.stream().anyMatch(m -> m.getCommodityAttribute().equals(goodsAttributeParam.getCommodityAttribute()))){
                    deleteGoodsAttribute.add(goodsAttributeParam);
                }
            }
            //解析出要添加的分类属性 和 对应的值
            for (GoodsAttributeEntity goodsAttributeParam : goodsAttributeParamList) {
                if(!existGoodsAttribute.stream().anyMatch(m -> m.getCommodityAttribute().equals(goodsAttributeParam.getCommodityAttribute()))){
                    addGoodsAttribute.add(goodsAttributeParam);
                }else{
                    updateGoodsAttribute.add(goodsAttributeParam);
                }
            }
            //解析出 分类属性名称不变  属性值需要修改
            if(null != updateGoodsAttribute && updateGoodsAttribute.size() > 0){
                for (GoodsAttributeEntity goodsAttributeParam : updateGoodsAttribute) {
                    for (GoodsAttributeEntity existAttributeParam : existGoodsAttribute) {
                        //属性名相同时 判断属性值 是否需要添加或删除
                        if(goodsAttributeParam.getCommodityAttribute().equals(existAttributeParam.getCommodityAttribute())){
                            //前端传入的属性值
                            List<String> attributeContent = goodsAttributeParam.getAttributeContent();
                            //已经添加属性值
                            List<GoodsAttributeContentEntity>  existAttributeContent = existAttributeParam.getAttrContentList();
                            for (String s : attributeContent) {
                                //当前端传入的不在  已经添加的里面 就是添加
                                if(!existAttributeContent.stream().anyMatch(m -> m.getAttributeContent().equals(s))){
                                    GoodsAttributeEntity goodsParam = new GoodsAttributeEntity();
                                    goodsParam.setAttributeContentId(goodsAttributeParam.getId());
                                    goodsParam.setAttriContent(s);
                                    //存入属性内容
                                    goodsAttributeContentMapper.insertContent(goodsParam);
                                }
                            }
                            for (GoodsAttributeContentEntity s : existAttributeContent) {
                                //当已经添加的不在 前端传入的 就是删除
                                if(!attributeContent.contains(s.getAttributeContent())){
                                    goodsAttributeContentMapper.deleteContent(s.getId());
                                }
                            }

                        }
                    }
                }
            }

            //删除对应属性名 属性值
            if(null != deleteGoodsAttribute && deleteGoodsAttribute.size() > 0 ){
                for(GoodsAttributeEntity goodsAtt:deleteGoodsAttribute){
                    goodsAttributeContentMapper.deleteContent(goodsAtt.getId());
                    //删除分类属性名
                    goodsAttributeMapper.deleteAttris(goodsAtt.getId());
                }
            }
            // 添加对应属性名 属性值
            if(null != addGoodsAttribute && addGoodsAttribute.size() > 0 ){
                for (GoodsAttributeEntity goodsParam : addGoodsAttribute) {
                    goodsParam.setCatId(param.getCatId());
                    //存入属性名
                    goodsAttributeMapper.insertAttribute(goodsParam);
                    if (goodsParam.getAttributeContent().size() > 0) {
                        List<String> attCons = goodsParam.getAttributeContent();
                        for (String attCon : attCons) {
                            goodsParam.setAttributeContentId(goodsParam.getId());
                            goodsParam.setAttriContent(attCon);
                            //存入属性内容
                            goodsAttributeContentMapper.insertContent(goodsParam);
                        }
                    }
                }
            }
        }else if((goodsAttributeParamList != null && goodsAttributeParamList.size() > 0)
                && (existGoodsAttribute == null || existGoodsAttribute.size() == 0 )){
            // 前端传入的分类属性都不为空 和 已经添加的分类属性为空 直接添加
            if (null != goodsAttributeParamList &&goodsAttributeParamList.size() > 0) {
                for (GoodsAttributeEntity goodsParam : goodsAttributeParamList) {
                    goodsParam.setCatId(param.getCatId());
                    //存入属性名
                    goodsAttributeMapper.insertAttribute(goodsParam);
                    if (goodsParam.getAttributeContent().size() > 0) {
                        List<String> attCons = goodsParam.getAttributeContent();
                        for (String attCon : attCons) {
                            goodsParam.setAttributeContentId(goodsParam.getId());
                            goodsParam.setAttriContent(attCon);
                            //存入属性内容
                            goodsAttributeContentMapper.insertContent(goodsParam);
                        }
                    }
                }
            }
        }else if((goodsAttributeParamList == null || goodsAttributeParamList.size() == 0)
                && (existGoodsAttribute != null && existGoodsAttribute.size() >0 )){
            // 前端传入的分类属性都为空   即删除当前分类的所有属性 已经添加的分类属性不为空
            for(GoodsAttributeEntity goodsAtt:existGoodsAttribute){
                goodsAttributeContentMapper.deleteAttriContents(goodsAtt.getId());
                //删除分类属性名
                goodsAttributeMapper.deleteAttris(goodsAtt.getId());
            }
        }
    }
    @Override
    public ResponseResult queryGoodsCatsAll(PageHelper pageHelper) {
        UserInfoVO userInfo = ParseTokenUtils.getPlatformUser(pageHelper.getToken());
        if (null != userInfo) {
            List<ManageShopCatMenuVO> results = null;
            List<ManageShopCatMenuVO> rootMenus = null;
            Map<String, Object> param=new HashMap<>();
            rootMenus = this.baseMapper.selectPlatFormCats(param);
            //数据解析封装
            results = getMenuList(rootMenus);
            Integer totalCount = results != null ? results.size() : 0;
            if(null != pageHelper.getPage() && null != pageHelper.getLimit()){
                if(null != results){
                    int toIndex = Math.min(pageHelper.getPage() * pageHelper.getLimit(), results.size());
                    results = results.subList((pageHelper.getPage() - 1) * pageHelper.getLimit(), toIndex);
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("data", results);
                map.put("totalCount", totalCount);
                return ResponseResult.success(map);
            }
            return ResponseResult.success(results);
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
    }

    /**
     * 根据分类ID查询虚拟商品分类信息。
     *
     * @param param 查询参数，包含分类ID。
     * @return 返回查询结果，包含虚拟商品分类信息。
     */
    @Override
    public ResponseResult queryVirtual(ManageCatResultVO param) {
        // 通过分类ID查询虚拟商品分类信息
        ManageCatResultVO goodsCats = this.baseMapper.queryVirtualByCatId(param.getCatId());
        // 返回查询结果
        return ResponseResult.success(goodsCats);
    }

    /**
     * 查询等级分类列表。
     *
     * @return 返回等级分类列表。
     */
    @Override
    public ResponseResult queryRankCat() {
        // 查询所有父级分类ID为0，且非积分商品的分类，按显示顺序和分类ID升序排列
        List<GoodsCatEntity> entities = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsCatEntity>()
                .eq(GoodsCatEntity::getCatParentId, 0)
                .eq(GoodsCatEntity::getIsIntegralGoods, 0)
                .orderByAsc(GoodsCatEntity::getCatDisplayorder,GoodsCatEntity::getCatId));
        // 返回查询结果
        return ResponseResult.success(entities);
    }

    @Override
    public ResponseResult queryNoCat(Map<String, Object> param) {
        if(param.get("token")!=null) {
            String token = String.valueOf(param.get("token"));
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            if(null == userInfo ){
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            List<ManageShopCatMenuVO> menuLists = new ArrayList<ManageShopCatMenuVO>();
            List<ManageShopCatMenuVO> returnmenuLists = new ArrayList<ManageShopCatMenuVO>();

            List<ManageShopCatMenuVO> rootMenus = this.baseMapper.selectAllCat1(shopId, String.valueOf(param.get("catName")));

            menuLists =getMenuList3(rootMenus);

            // 使用Stream过滤删除二级菜单下三级菜单为空的情况
            returnmenuLists = menuLists.stream()
                    .map(menu ->  {
                        if (menu.getChildMenu() != null) {
                            List<ManageShopCatMenuVO> updatedSubMenus = menu.getChildMenu().stream()
                                    .filter(subMenu -> subMenu.getChildMenu() != null && !subMenu.getChildMenu().isEmpty())
                                    .collect(Collectors.toList());
                            menu.setChildMenu(updatedSubMenus);
                        }
                        return menu;
                    })
                    .filter(x -> x.getChildMenu() != null && !x.getChildMenu().isEmpty())
                    .collect(Collectors.toList());

            return ResponseResult.success(returnmenuLists);
        }else {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
    }
    /**
     * 根据传入的参数申请分类绑定。
     * @param param 一个Map对象，包含申请分类所需的各项参数，如token、batchId、catId等。
     * @return 返回一个ResponseResult对象，表示申请结果，成功或失败。
     */
    @Override
    public ResponseResult applyCat(Map<String, Object> param) {
        // 检查token是否为空，为空则返回失败结果
        if(param.get("token")!=null) {
            // 从参数中提取token和batchId
            String token = String.valueOf(param.get("token"));
            String batchId = String.valueOf(param.get("batchId"));

            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 如果用户信息为空，则返回失败结果，表示token无效
            if(null == userInfo ){
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            // 获取用户所属店铺的ID
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            // 如果batchId为空，则生成一个新的batchId
            if (StringUtils.isEmpty(batchId)) {
                // 生成batchId的格式为日期加5位随机数
                StringBuffer id = new StringBuffer();
                DateFormat dFormat3 = new SimpleDateFormat("yyyyMMdd");

                String format = dFormat3.format(new Date());

                Random random = new Random();
                String verificationCode = String.valueOf(random.nextInt(9) + 1);
                for (int i = 0; i < 5; i++) {
                    verificationCode += random.nextInt(10);
                }
                id.append(format).append(verificationCode);
                param.put("batchId",id.toString());
            }

            // 初始化标记，用于后续判断操作是新增还是修改
            Integer integer = 0;

            // 将shopId和起始页数start添加到参数中
            param.put("shopId",shopId);
            param.put("start",1);

            // 提取catId列表，并遍历该列表
            List<Integer> catId = (List) param.get("catId");
            ArrayList<Object> list = new ArrayList<>();
            for (Integer o : catId) {
                HashMap<Object, Object> hashMap = new HashMap<>();
                // 根据catId查询分类名称
                List<String> querycatname = this.baseMapper.queryCatName(o);
                // 将查询结果转换为JSON字符串
                String s = JSON.toJSONString(querycatname);

                hashMap.put("catId",o);
                hashMap.put("catName",s);

                list.add(hashMap);
            }
            // 将分类信息列表替换原来的catId参数
            param.put("catId",list);

            // 如果batchId为空，则为新增操作；否则先删除旧记录，再插入新记录，视为修改操作
            if (StringUtils.isEmpty(batchId)) {
                // 新增操作
                integer = this.baseMapper.insertclassbind(param);
            }else{
                // 修改操作
                this.baseMapper.deleteclassbind(param);

                integer = this.baseMapper.insertclassbind(param);
            }

            // 如果操作影响的行数大于0，则返回成功结果；否则返回失败结果
            if (integer > 0){
                return ResponseResult.success("申请成功");
            }
            return ResponseResult.fail(0, "申请失败");
        }else {
            // 如果token为空，则返回失败结果，表示token缺失
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
    }

    /**
     * 根据传入的参数查询申请的分类信息。
     *
     * @param param 包含查询所需各种参数的Map，如token、batchId、limit等。
     * @return 返回查询结果的ResponseResult对象。
     */
    @Override
    public ResponseResult qureyApplyCat(Map<String, Object> param) {
        // 检查token是否存在
        if (param.get("token") != null) {
            // 从参数中提取token和batchId
            String token = String.valueOf(param.get("token"));
            // 通过token获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            // 如果用户信息为空，则返回token无效的错误信息
            if (null == userInfo) {
                return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
            }
            // 从用户信息中获取店铺ID
            Integer pageSize = (Integer) param.get("limit");
            Integer shopId = Integer.valueOf(userInfo.getShopId());

            // 将店铺ID加入查询参数
            param.put("shopId", shopId);

            // 初始化分类查询结果列表
            // 查询入驻的分类
            List<ApplyCatVO> list = new ArrayList<>();
            // 执行分类查询
            List<ApplyCatVO> queryJoinCat = this.baseMapper.queryJoinCat(param);
            // 如果查询结果不为空，处理查询结果
            if (!CollectionUtils.isEmpty(queryJoinCat)) {
                ApplyCatVO catDTO = new ApplyCatVO();
                catDTO.setBatchId("0");
                catDTO.setStart(2);
                // 遍历查询结果，设置分类名称等信息
                for (ApplyCatVO catEntity : queryJoinCat) {
                    List<String> catNameList = this.baseMapper.querySonCatByParentId(catEntity.getCatId());
                    String catName = JSON.toJSONString(catNameList);
                    catEntity.setCatName(catName);
                    catEntity.setCatNames(catNameList);
                }
                catDTO.setCatEntities(queryJoinCat);
                list.add(catDTO);
            }

            // 执行分页查询
            IPage<ApplyCatVO> page = this.baseMapper.qureyApplyCat(new Query<ApplyCatVO>().getPage(param), param);

            // 如果分页查询结果不为空，处理分页查询结果
            if (!CollectionUtils.isEmpty(page.getRecords())) {
                // 遍历分页查询结果，查询每个分类的子分类信息
                for (ApplyCatVO applyCatEntity : page.getRecords()) {
                    param.put("batchId", applyCatEntity.getBatchId());
                    List<ApplyCatVO> querycat = goodsCatMapper.querycat(param);
                    // 如果子分类查询结果不为空，处理子分类信息
                    if (!CollectionUtils.isEmpty(querycat)) {
                        for (ApplyCatVO catEntity : querycat) {
                            String catName = catEntity.getCatName();
                            List<String> strings = JSON.parseArray(catName, String.class);
                            catEntity.setCatNames(strings);
                        }
                    }
                    applyCatEntity.setCatEntities(querycat);
                }
            } else {
                // 如果分页查询结果为空，设置为空列表
                page.setRecords(new ArrayList<>());
            }
            // 将之前查询的分类信息合并到分页查询结果中
            page.getRecords().addAll(list);
            // 设置分页信息
            // 设置分页
            PageUtils result = PageUtils.getPage(page);
            result.setTotalCount(page.getRecords().size());
            Integer totalPage = (result.getTotalCount() % pageSize) == 0 ? result.getTotalCount() / pageSize : result.getTotalCount() / pageSize + 1;
            result.setTotalPage(totalPage);
            // 返回查询结果
            return ResponseResult.success(result);

        } else {
            // 如果token不存在，返回token为空的错误信息
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_NULL);
        }
    }

    /**
     * 查询平台店铺分类信息。
     * 根据传入的参数查询平台的店铺分类，包括根分类和子分类，并根据分页参数进行分页处理。
     *
     * @param param 一个Map对象，包含查询所需的参数，如分类展示状态(catShowStatus)、分类ID(catId)、分页信息(limit和page)等。
     * @return 返回查询结果的ResponseResult对象，包含分类列表和总数信息。
     */
    @Override
    public ResponseResult queryCat(Map<String, Object> param) {
        // 初始化查询结果列表
        List<ManageShopCatMenuVO> results = null;

        // 查询根分类信息
        List<ManageShopCatMenuVO> rootMenus = null;
        rootMenus = this.baseMapper.selectPlatFormCats2(Integer.valueOf(param.get("catShowStatus").toString()));

        // 查询分类总数
        Integer totalCount =  this.baseMapper.selectPlatFormCatsCount2(Integer.valueOf(param.get("catShowStatus").toString()));

        // 根据根分类和分类ID查询具体的分类列表
        results = getMenuList3(rootMenus,Integer.valueOf(param.get("catId").toString()));

        // 如果传入了分页参数，则进行分页处理
        if(null != param.get("limit") && null != param.get("page")){
            Integer limit=Integer.valueOf(param.get("limit").toString());
            Integer page=Integer.valueOf(param.get("page").toString());
            // 根据分页参数对查询结果进行截取
            if(null != results){
                int toIndex;
                if (limit * page < results.size()) {
                    toIndex =limit * page;
                } else {
                    toIndex = results.size();
                }
                results = results.subList((limit - 1) * page, toIndex);
            }

            // 构建包含分页信息的返回结果
            HashMap<String,Object> map = new HashMap<>();
            map.put("data",results);
            map.put("totalCount",totalCount);
            return ResponseResult.success(map);
        }

        // 如果没有分页参数，直接返回查询结果
        return  ResponseResult.success(results);
    }

    /**
     * 根据分类ID查询商品分类信息。
     *
     * @param map 包含查询参数的映射表，其中catId表示父分类ID。
     * @return 返回查询结果的ResponseResult对象，包含商品分类实体的列表。
     *
     * 此方法通过调用baseMapper的selectList方法，使用LambdaQueryWrapper构建查询条件，
     * 查询出父分类ID等于传入参数catId且是否为积分商品为否的商品分类实体列表。
     * 最后，将查询结果封装进ResponseResult对象中并返回。
     */
    @Override
    public ResponseResult queryGoodsCatByCatId(Map<String, Object> map) {
        // 根据传入的catId查询不为积分商品的商品分类
        List<GoodsCatEntity> catMap = this.baseMapper.selectList(new LambdaQueryWrapper<GoodsCatEntity>()
                .eq(GoodsCatEntity::getCatShowStatus, CommonType.commonStatus.YES.getCode())
                .eq(GoodsCatEntity::getCatParentId, map.get("catId").toString())
                .eq(GoodsCatEntity::getIsIntegralGoods, 0));
        // 返回查询结果的成功响应
        return ResponseResult.success(catMap);
    }

    /**
     * 根据分类ID查询分类列表。
     * 该方法首先尝试从Redis缓存中获取分类ID列表，如果缓存不存在或过期，则从数据库中查询所有分类菜单，
     * 并根据当前分类ID生成分类ID列表，同时将结果缓存起来，以提高后续查询效率。
     * 最后，将当前分类ID添加到列表中，以确保包含当前分类。
     *
     * @param catId 分类ID，用于查询特定分类下的子分类列表。
     * @return 返回分类ID列表的JSON字符串，如果列表为空，则返回空JSON字符串。
     */
    @Override
    public ResponseResult queryFxCatList(Integer catId) {
        // 用于存储分类ID列表
        List<Integer> catIds;
        // 用于存储所有分类菜单的视图对象
        List<WebCatMenuVO> allMenu;
        // 用于缓存分类查询结果的视图对象
        GoodsCatSearchChildVO goodsCatSearchChild;
        // 尝试从Redis缓存中获取分类ID列表
        // 从缓存中获取
        String cacheCatIds = RedisUtil.get(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId);
        if (null != cacheCatIds && !cacheCatIds.equals("")) {
            // 如果缓存存在，则反序列化缓存数据
            goodsCatSearchChild = JsonUtils.string2Obj(cacheCatIds, GoodsCatSearchChildVO.class);
            catIds = goodsCatSearchChild.getCatIds();
        } else {
            // 如果缓存不存在，则从数据库中查询所有分类菜单
            allMenu = this.baseMapper.selectAllCat();
            // 根据当前分类ID和所有分类菜单，生成分类ID列表
            catIds = getMenuList(allMenu, catId);
            // 初始化缓存数据对象
            goodsCatSearchChild = new GoodsCatSearchChildVO();
            // 设置缓存数据对象的属性
            goodsCatSearchChild.setAllMenu(allMenu);
            goodsCatSearchChild.setCatIds(catIds);
            // 将分类查询结果缓存到Redis中，过期时间为1个月
            RedisUtil.set(Constants.CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX + "_" + catId, JsonUtils.toString(goodsCatSearchChild), RedisUtil.EXRP_MONTH);
        }
        // 如果分类ID列表不为空，则将当前分类ID添加到列表中
        if (!CollectionUtils.isEmpty(catIds)) {
            catIds.add(catId);
        }
        // 返回分类ID列表的JSON字符串
        return ResponseResult.success(JSON.toJSONString(catIds));
    }

    /**
     * 根据分类ID获取菜单列表的第三种实现方式。
     * 该方法主要用于处理分类菜单的显示逻辑，根据传入的分类ID和根菜单列表，
     * 构建出一个包含指定父分类ID的子菜单列表。
     * 同时，还会根据配置信息，为菜单项设置退款比例等信息。
     *
     * @param rootMenus 根菜单列表，包含所有一级菜单项。
     * @param catId 分类ID，用于筛选出指定父分类ID的子菜单。
     * @return 返回一个列表，包含所有指定父分类ID的子菜单项。
     */
    public List<ManageShopCatMenuVO> getMenuList3(List<ManageShopCatMenuVO> rootMenus,Integer catId) {
        // 查询平台默认质保金配置
        //查询平台默认质保金
        List<BaseSysConfigsVO> pos =
                this.baseMapper.selectByConfigType("retention_money_default_limit");
        // 获取默认质保金金额
        String amount = null;
        if (pos != null && pos.size() > 0){
            amount = pos.get(0).getConfigValue();
        }

        // 查询平台操作规格配置
        List<BaseSysConfigsVO> pos1 =
                this.baseMapper.selectByConfigType("platform_operation_specifications");
        // 解析平台操作规格配置，用于后续设置菜单的退款比例
        PlatformoperationVO platformoperationEntity = new PlatformoperationVO();
        if (pos != null && pos1.size() > 0){
            String configValue = pos1.get(0).getConfigValue();
            platformoperationEntity = JSONObject.parseObject(configValue, PlatformoperationVO.class);
        }

        // 初始化子菜单列表
        List<ManageShopCatMenuVO> childMenu = Lists.newArrayList();
        // 根据catId是否存在，执行不同的逻辑
        if (catId != null) {
            // 如果catId存在，只选择父分类ID为0且ID等于catId的菜单项
            for (ManageShopCatMenuVO menu : rootMenus) {
                if (menu.getCatParentId().equals(0) && menu.getCatId().equals(catId)) {
                    childMenu.add(menu);
                }
            }
        }else{
            // 如果catId不存在，遍历所有菜单项，设置退款比例，并选择父分类ID为0的菜单项
            for (ManageShopCatMenuVO menu : rootMenus) {
                // 设置菜单的退款比例
                if (menu.getRefundRatio() == null){
                    menu.setRefundRatio(new BigDecimal(amount));
                }
                // 设置菜单的退款比例
                if (menu.getRefundProportion() == null){
                    menu.setRefundProportion(platformoperationEntity.getUniversalRatio());
                }
                if (menu.getCatParentId().equals(0)) {
                    childMenu.add(menu);
                }
            }
        }
        // 从根菜单列表中移除已选中的子菜单，避免重复显示
        rootMenus.removeAll(childMenu);

        // 遍历子菜单列表，为每个菜单项加载其子菜单
        for (ManageShopCatMenuVO goodsCatsMenuList : childMenu) {
            // 递归获取子菜单项
            List<ManageShopCatMenuVO> child1 = getChild3(goodsCatsMenuList.getCatId(),rootMenus,goodsCatsMenuList.getRefundRatio());
            goodsCatsMenuList.setChildGoodsCatList(child1);
        }

        // 返回处理后的子菜单列表
        return childMenu;

    }

    /**
     * 根据分类id查询该分类下所有下级分类id
     * @param catId 分类id
     * @return 所有下级分类id
     */
    @Override
    public List<Integer> queryAllSubordinateCatIdByCatId(Integer catId) {
        List<Integer> catIds = new ArrayList<>(baseMapper.selectList(new LambdaQueryWrapper<GoodsCatEntity>()
                        .select(GoodsCatEntity::getCatId).eq(GoodsCatEntity::getCatParentId, catId))
                .stream().map(GoodsCatEntity::getCatId).toList());
        List<Integer> catIdList = new ArrayList<>(catIds);
        if(!catIds.isEmpty()){
            for (Integer id : catIds) {
                catIdList.addAll(queryAllSubordinateCatIdByCatId(id));
            }
            return catIdList;
        }
        return Collections.singletonList(catId);
    }

    /**
     * 根据分类id查询该分类下所有上级分类id
     * @param catId 分类id
     * @return 所有上级分类id
     */
    @Override
    public List<GoodsCatResultVO> selectNavSaleCategories(Integer catId,Integer integralGoods) {
        if(catId == null){
            return Collections.emptyList();
        }
        GoodsCatEntity cat = this.getById(catId);
        if(cat.getCatLevel() == 1){
            GoodsCatResultVO vo = new GoodsCatResultVO();
            BeanUtils.copyProperties(cat,vo);
            List<SecordNameListVO> levelOneCategory = baseMapper.selectLevelOneCategory(integralGoods);
            vo.setSecordNameList(levelOneCategory);
            return Collections.singletonList(vo);
        }
        List<GoodsCatResultVO> list = baseMapper.selectNavSaleCategories(cat.getCatId());
        List<GoodsCatResultVO> result = new ArrayList<>(list);
        for (GoodsCatResultVO vo : list) {
            List<GoodsCatResultVO> catResult = selectNavSaleCategories(vo.getCatParentId(),integralGoods);
            result.addAll(catResult);
        }
        result = result.stream().sorted(Comparator.comparing(GoodsCatResultVO::getCatLevel)).toList();
        return result;
    }

    /**
     * 根据分类id集合查询上级分类集合
     * @param catIdList 分类id集合
     * @return 上级分类集合
     */
    @Override
    public List<GoodsCatResultVO> selectLineSaleCategoriesByThreeCat(List<Integer> catIdList) {
        List<GoodsCatEntity> list = baseMapper.selectList(new LambdaQueryWrapper<GoodsCatEntity>()
                .in(GoodsCatEntity::getCatId, catIdList));
        List<GoodsCatEntity> distinctList = list.stream()
                .collect(Collectors.toMap(
                        // 根据 catParentId 去重
                        GoodsCatEntity::getCatParentId,
                        // 保留原始对象
                        Function.identity(),
                        // 如果 catParentId 相同，保留第一个
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
        List<GoodsCatResultVO> result = new ArrayList<>();
        for (GoodsCatEntity vo : distinctList) {
            GoodsCatEntity parent = this.getById(vo.getCatParentId());
            GoodsCatResultVO catVO = new GoodsCatResultVO();
            BeanUtils.copyProperties(parent,catVO);
            List<SecordNameListVO> cats = baseMapper.selectLineSaleCategoriesByThreeCat(vo.getCatParentId());
            catVO.setSecordNameList(cats);
            result.add(catVO);
        }
        return result;
    }

    /**
     * 根据分类ID查询线性销售分类列表。
     *
     * @param catId 分类ID，用于指定查询的分类。
     * @return 返回一个包含商品分类结果的列表，类型为List<GoodsCatResultVO>。
     */
    @Override
    public List<GoodsCatResultVO> selectLineSaleCategoriesByCatId(Integer catId) {
        List<GoodsCatResultVO> catIds = baseMapper.selectLineSaleCategoriesByCatId(catId);
        return catIds;
    }

    /**
     * 根据参数查询商品分类信息。
     *
     * @param param 查询参数，封装了查询条件的对象，类型为WebGoodsCatQueryDTO。
     * @return 返回一个包含查询结果的ResponseResult对象，结果为商品分类列表。
     */
    @Override
    public ResponseResult selectByParam(WebGoodsCatQueryDTO param) {
        // 调用baseMapper根据参数查询商品分类
        List<GoodsCatVO> goodsCatResults = this.baseMapper.selectByParam(param);
        // 返回查询结果
        return ResponseResult.success(goodsCatResults);
    }

    /**
     * 根据分类ID查询店铺分类菜单信息。
     *
     * @param catId 分类ID，用于指定查询的分类。
     * @return 返回一个包含店铺分类菜单信息的对象，类型为ManageShopCatMenuVO。
     */
    @Override
    public ManageShopCatMenuVO selectByCatId2(Integer catId) {
        ManageShopCatMenuVO vo = this.baseMapper.selectByCatId2(catId);
        return vo;
    }

    /**
     * 根据分类ID查询佣金率信息。
     *
     * @param catId 分类ID，用于指定查询的分类。
     * @return 返回一个包含佣金率信息的对象，类型为CommissionRateBeanVO。
     */
    @Override
    public CommissionRateBeanVO selectCommissionRate(Integer catId) {
        CommissionRateBeanVO vo = this.baseMapper.selectCommissionRate(catId);
        return vo;
    }

    /**
     * 查询所有三级分类信息。
     *
     * @return 返回一个包含所有三级分类信息的列表，类型为List<ShopGoodsCatVO>。
     */
    @Override
    public List<ShopGoodsCatVO> selectAllCat3() {
        List<ShopGoodsCatVO> list = this.baseMapper.selectAllCat3();
        return list;
    }

    /**
     * 根据主键查询商品分类信息。
     *
     * @param catId 分类ID，作为主键用于查询。
     * @return 返回一个包含商品分类信息的对象，类型为GdbMerchantGoodsCatVO。
     */
    @Override
    public GdbMerchantGoodsCatVO selectGoodsCatByPrimaryKey(Integer catId) {
        GdbMerchantGoodsCatVO vo = this.baseMapper.selectGoodsCatByPrimaryKey(catId);
        return vo;
    }

    /**
     * 根据分类ID查询分类名称列表。
     *
     * @param catId 分类ID，用于指定查询的分类。
     * @return 返回一个包含分类名称的列表，类型为List<String>。
     */
    @Override
    public List<String> queryCatName(Integer catId) {
        List<String> list = this.baseMapper.queryCatName(catId);
        return list;
    }

    /**
     * 根据分类ID查询分类名称字符串。
     *
     * @param catId 分类ID，用于指定查询的分类。
     * @return 返回一个包含分类名称的字符串。
     */
    @Override
    public String selectByCatNameString(Integer catId) {
        String catName = this.baseMapper.selectByCatNameString(catId);
        return catName;
    }

    /**
     * 查询所有二级分类信息。
     *
     * @return 返回一个包含所有二级分类信息的列表，类型为List<WebCatMenuVO>。
     */
    @Override
    public List<WebCatMenuVO> selectAllCat2() {
        List<WebCatMenuVO> list = this.baseMapper.selectAllCatFeign();
        return list;
    }

    /**
     * 根据父分类ID查询二级分类名称列表。
     *
     * @param catId 父分类ID，用于指定查询的父分类。
     * @return 返回一个包含二级分类名称列表的对象，类型为List<SecordNameListVO>。
     */
    @Override
    public List<SecordNameListVO> selectByCatParentId(Integer catId) {
        List<SecordNameListVO> list = this.baseMapper.selectByCatParentIdFeign(catId);
        return list;
    }

}
