package com.houjiang.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.BaseDashboardPrefDTO;
import com.houjiang.base.dto.BaseDashboardPrefItemDTO;
import com.houjiang.base.dto.BaseDashboardPrefSectionDTO;
import com.houjiang.base.entity.BaseDashboardPrefEntity;
import com.houjiang.base.entity.BaseDashboardPrefItemEntity;
import com.houjiang.base.entity.BaseDashboardPrefSectionEntity;
import com.houjiang.base.mapper.BaseDashboardPrefItemMapper;
import com.houjiang.base.mapper.BaseDashboardPrefMapper;
import com.houjiang.base.mapper.BaseDashboardPrefSectionMapper;
import com.houjiang.base.service.BaseDashboardPrefService;
import com.houjiang.base.vo.BaseDashboardPrefVO;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard 用户偏好 ServiceImpl
 *
 * @author Amos
 * @since 2026-05-13
 */
@Service
public class BaseDashboardPrefServiceImpl
        extends ServiceImpl<BaseDashboardPrefMapper, BaseDashboardPrefEntity>
        implements BaseDashboardPrefService {

    /** 默认栅格列数 */
    private static final int DEFAULT_COLS = 12;
    /** 默认刷新间隔：0 关闭 */
    private static final int DEFAULT_REFRESH = 0;

    @Autowired
    private BaseDashboardPrefItemMapper itemMapper;

    @Autowired
    private BaseDashboardPrefSectionMapper sectionMapper;

    /** 区块 config 的 JSON 读写，与 Spring 共用一份 jackson 配置无强依赖，故本地 new */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 区块标识白名单，脏数据直接丢弃，避免前端渲染出未知区块 */
    private static final List<String> VALID_SECTION_IDS = Arrays.asList(
            "hero", "stats", "quick", "todo", "trend", "widgets");

    /**
     * 解析区块配置 JSON，失败返回空 Map（由前端补默认）
     */
    private Map<String, Object> parseSectionConfig(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, Map.class);
            return map == null ? new HashMap<>() : map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 序列化区块配置，失败返回空对象 JSON
     */
    private String writeSectionConfig(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(config);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public ResponseResult getPref(String token) {
        UserInfoVO user = parseUser(token);
        if (user == null) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }

        BaseDashboardPrefEntity entity = this.getOne(
                new QueryWrapper<BaseDashboardPrefEntity>().eq("user_id", user.getUserId()).last("LIMIT 1"));

        BaseDashboardPrefVO vo = new BaseDashboardPrefVO();
        if (entity == null) {
            vo.setUserId(user.getUserId());
            vo.setCols(DEFAULT_COLS);
            vo.setRefreshInterval(DEFAULT_REFRESH);
            vo.setVersion(1);
            vo.setItems(new ArrayList<>());
            vo.setSections(new ArrayList<>());
            return ResponseResult.success(vo);
        }
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setCols(entity.getCols() == null ? DEFAULT_COLS : entity.getCols());
        vo.setRefreshInterval(entity.getRefreshInterval() == null ? DEFAULT_REFRESH : entity.getRefreshInterval());
        vo.setVersion(entity.getVersion() == null ? 1 : entity.getVersion());

        List<BaseDashboardPrefItemEntity> items = itemMapper.selectList(
                new QueryWrapper<BaseDashboardPrefItemEntity>().eq("pref_id", entity.getId()).orderByAsc("sort"));
        List<BaseDashboardPrefVO.Item> itemVOs = new ArrayList<>();
        if (items != null) {
            for (BaseDashboardPrefItemEntity it : items) {
                BaseDashboardPrefVO.Item item = new BaseDashboardPrefVO.Item();
                item.setModuleId(it.getModuleId());
                item.setVisible(it.getVisible());
                item.setSort(it.getSort());
                item.setX(it.getX());
                item.setY(it.getY());
                item.setW(it.getW());
                item.setH(it.getH());
                itemVOs.add(item);
            }
        }
        vo.setItems(itemVOs);

        List<BaseDashboardPrefSectionEntity> sections = sectionMapper.selectList(
                new QueryWrapper<BaseDashboardPrefSectionEntity>().eq("pref_id", entity.getId()).orderByAsc("sort"));
        List<BaseDashboardPrefVO.Section> sectionVOs = new ArrayList<>();
        if (sections != null) {
            for (BaseDashboardPrefSectionEntity it : sections) {
                if (it == null || it.getSectionId() == null || !VALID_SECTION_IDS.contains(it.getSectionId())) {
                    continue;
                }
                BaseDashboardPrefVO.Section section = new BaseDashboardPrefVO.Section();
                section.setId(it.getSectionId());
                section.setVisible(it.getVisible());
                section.setSort(it.getSort());
                section.setConfig(parseSectionConfig(it.getConfigJson()));
                sectionVOs.add(section);
            }
        }
        vo.setSections(sectionVOs);
        return ResponseResult.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult savePref(String token, BaseDashboardPrefDTO dto) {
        UserInfoVO user = parseUser(token);
        if (user == null) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        if (dto == null) {
            return ResponseResult.fail(400, "参数不能为空");
        }

        BaseDashboardPrefEntity entity = this.getOne(
                new QueryWrapper<BaseDashboardPrefEntity>().eq("user_id", user.getUserId()).last("LIMIT 1"));

        Date now = new Date();
        if (entity == null) {
            entity = new BaseDashboardPrefEntity();
            entity.setUserId(user.getUserId());
            entity.setOrgId(user.getShopId());
            entity.setCols(dto.getCols() == null ? DEFAULT_COLS : dto.getCols());
            entity.setRefreshInterval(dto.getRefreshInterval() == null ? DEFAULT_REFRESH : dto.getRefreshInterval());
            entity.setVersion(1);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            this.save(entity);
        } else {
            if (dto.getCols() != null) {
                entity.setCols(dto.getCols());
            }
            if (dto.getRefreshInterval() != null) {
                entity.setRefreshInterval(dto.getRefreshInterval());
            }
            entity.setVersion((entity.getVersion() == null ? 1 : entity.getVersion()) + 1);
            entity.setUpdatedAt(now);
            this.updateById(entity);
        }

        // 全量覆盖明细
        itemMapper.delete(new QueryWrapper<BaseDashboardPrefItemEntity>().eq("pref_id", entity.getId()));
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            int sort = 0;
            for (BaseDashboardPrefItemDTO it : dto.getItems()) {
                if (it == null || it.getModuleId() == null || it.getModuleId().isEmpty()) {
                    continue;
                }
                BaseDashboardPrefItemEntity row = new BaseDashboardPrefItemEntity();
                row.setPrefId(entity.getId());
                row.setModuleId(it.getModuleId());
                row.setVisible(it.getVisible() == null ? 1 : it.getVisible());
                row.setSort(it.getSort() == null ? sort : it.getSort());
                row.setX(it.getX() == null ? 0 : it.getX());
                row.setY(it.getY() == null ? 0 : it.getY());
                row.setW(it.getW() == null ? 4 : it.getW());
                row.setH(it.getH() == null ? 1 : it.getH());
                itemMapper.insert(row);
                sort++;
            }
        }
        // 全量覆盖区块：老客户端不传 sections 时保持原样，避免把已有区块配置冲掉
        if (dto.getSections() != null) {
            sectionMapper.delete(new QueryWrapper<BaseDashboardPrefSectionEntity>().eq("pref_id", entity.getId()));
            int sectionSort = 0;
            for (BaseDashboardPrefSectionDTO it : dto.getSections()) {
                if (it == null || it.getId() == null || it.getId().isEmpty()
                        || !VALID_SECTION_IDS.contains(it.getId())) {
                    continue;
                }
                BaseDashboardPrefSectionEntity row = new BaseDashboardPrefSectionEntity();
                row.setPrefId(entity.getId());
                row.setSectionId(it.getId());
                row.setVisible(it.getVisible() == null ? 1 : it.getVisible());
                row.setSort(it.getSort() == null ? sectionSort : it.getSort());
                row.setConfigJson(writeSectionConfig(it.getConfig()));
                sectionMapper.insert(row);
                sectionSort++;
            }
        }
        return ResponseResult.success(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult resetPref(String token) {
        UserInfoVO user = parseUser(token);
        if (user == null) {
            return ResponseResult.fail(10086, "token失效，请重新获取！");
        }
        BaseDashboardPrefEntity entity = this.getOne(
                new QueryWrapper<BaseDashboardPrefEntity>().eq("user_id", user.getUserId()).last("LIMIT 1"));
        if (entity != null) {
            itemMapper.delete(new QueryWrapper<BaseDashboardPrefItemEntity>().eq("pref_id", entity.getId()));
            sectionMapper.delete(new QueryWrapper<BaseDashboardPrefSectionEntity>().eq("pref_id", entity.getId()));
            entity.setCols(DEFAULT_COLS);
            entity.setRefreshInterval(DEFAULT_REFRESH);
            entity.setVersion((entity.getVersion() == null ? 1 : entity.getVersion()) + 1);
            entity.setUpdatedAt(new Date());
            this.updateById(entity);
        }
        return ResponseResult.success(true);
    }

    /**
     * 解析 token 获取用户
     */
    private UserInfoVO parseUser(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            UserInfoVO user = ParseTokenUtils.getUserInfoByToken(token);
            if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
                return null;
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
