package com.houjiang.mall.service.album;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.mall.entity.album.MallAlbumFile;
import com.houjiang.mall.entity.album.MallAlbumFileCate;
import com.houjiang.mall.mapper.album.MallAlbumFileCateMapper;
import com.houjiang.mall.mapper.album.MallAlbumFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商城平台管理后台素材库（like-admin 兼容，数据表 hou_merchant_file*，source=0 后台）
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class MallAdminAlbumService {

    private static final int SOURCE_ADMIN = 0;

    private final MallAlbumFileMapper fileMapper;
    private final MallAlbumFileCateMapper fileCateMapper;

    public Map<String, Object> cateList(Integer type, String name) {
        QueryWrapper<MallAlbumFileCate> qw = new QueryWrapper<>();
        qw.isNull("delete_time").orderByDesc("id");
        qw.eq("source", SOURCE_ADMIN);
        if (type != null && type > 0) {
            qw.eq("type", type);
        }
        if (StringUtils.hasText(name)) {
            qw.like("name", name);
        }
        List<MallAlbumFileCate> rows = fileCateMapper.selectList(qw);
        List<Map<String, Object>> lists = rows.stream().map(this::toCateVo).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("lists", buildTree(lists));
        return result;
    }

    public void cateAdd(Integer type, String name, Integer pid) {
        MallAlbumFileCate cate = new MallAlbumFileCate();
        cate.setType(type != null ? type : 10);
        cate.setPid(pid != null ? pid : 0);
        cate.setName(name);
        cate.setSource(SOURCE_ADMIN);
        cate.setSourceId(0);
        long now = System.currentTimeMillis() / 1000;
        cate.setCreateTime(now);
        cate.setUpdateTime(now);
        fileCateMapper.insert(cate);
    }

    public void cateEdit(Integer id, String name) {
        MallAlbumFileCate cate = getCateOrThrow(id);
        cate.setName(name);
        cate.setUpdateTime(System.currentTimeMillis() / 1000);
        fileCateMapper.updateById(cate);
    }

    public void cateDel(Integer id) {
        MallAlbumFileCate cate = getCateOrThrow(id);
        Long fileCount = fileMapper.selectCount(new QueryWrapper<MallAlbumFile>()
                .eq("cid", id).eq("source", SOURCE_ADMIN).isNull("delete_time"));
        Assert.isTrue(fileCount == null || fileCount == 0, "分类下存在文件，无法删除");
        cate.setDeleteTime(System.currentTimeMillis() / 1000);
        fileCateMapper.updateById(cate);
    }

    public Map<String, Object> fileList(int pageNo, int pageSize, Integer cid, Integer type, String name) {
        QueryWrapper<MallAlbumFile> qw = new QueryWrapper<>();
        qw.isNull("delete_time").eq("source", SOURCE_ADMIN).orderByDesc("id");
        if (cid != null) {
            qw.eq("cid", cid);
        }
        if (type != null && type > 0) {
            qw.eq("type", type);
        }
        if (StringUtils.hasText(name)) {
            qw.like("name", name);
        }
        IPage<MallAlbumFile> page = fileMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        List<Map<String, Object>> lists = page.getRecords().stream().map(this::toFileVo).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("count", page.getTotal());
        result.put("page_no", page.getCurrent());
        result.put("page_size", page.getSize());
        result.put("lists", lists);
        result.put("extend", Collections.emptyMap());
        return result;
    }

    public void fileRename(Integer id, String name) {
        MallAlbumFile file = getFileOrThrow(id);
        file.setName(name);
        file.setUpdateTime(System.currentTimeMillis() / 1000);
        fileMapper.updateById(file);
    }

    public void fileMove(List<Integer> ids, Integer cid) {
        int targetCid = cid != null ? cid : 0;
        if (targetCid > 0) {
            getCateOrThrow(targetCid);
        }
        for (Integer id : ids) {
            MallAlbumFile file = getFileOrThrow(id);
            file.setCid(targetCid);
            file.setUpdateTime(System.currentTimeMillis() / 1000);
            fileMapper.updateById(file);
        }
    }

    public void fileDel(List<Integer> ids) {
        long now = System.currentTimeMillis() / 1000;
        for (Integer id : ids) {
            MallAlbumFile file = getFileOrThrow(id);
            file.setDeleteTime(now);
            fileMapper.updateById(file);
        }
    }

    public Integer fileAdd(Integer cid, Integer type, String name, String uri) {
        MallAlbumFile file = new MallAlbumFile();
        file.setCid(cid != null ? cid : 0);
        file.setType(type != null ? type : 10);
        file.setName(name);
        file.setUri(uri);
        file.setSource(SOURCE_ADMIN);
        file.setSourceId(0);
        long now = System.currentTimeMillis() / 1000;
        file.setCreateTime(now);
        file.setUpdateTime(now);
        fileMapper.insert(file);
        return file.getId();
    }

    private MallAlbumFileCate getCateOrThrow(Integer id) {
        MallAlbumFileCate cate = fileCateMapper.selectOne(new QueryWrapper<MallAlbumFileCate>()
                .eq("id", id).eq("source", SOURCE_ADMIN).isNull("delete_time").last("LIMIT 1"));
        Assert.notNull(cate, "分类已不存在！");
        return cate;
    }

    private MallAlbumFile getFileOrThrow(Integer id) {
        MallAlbumFile file = fileMapper.selectOne(new QueryWrapper<MallAlbumFile>()
                .eq("id", id).eq("source", SOURCE_ADMIN).isNull("delete_time").last("LIMIT 1"));
        Assert.notNull(file, "文件丢失！");
        return file;
    }

    private Map<String, Object> toCateVo(MallAlbumFileCate cate) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", cate.getId());
        vo.put("pid", cate.getPid());
        vo.put("type", cate.getType());
        vo.put("name", cate.getName());
        vo.put("create_time", formatTime(cate.getCreateTime()));
        vo.put("update_time", formatTime(cate.getUpdateTime()));
        return vo;
    }

    private Map<String, Object> toFileVo(MallAlbumFile file) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", file.getId());
        vo.put("cid", file.getCid());
        vo.put("type", file.getType());
        vo.put("name", file.getName());
        vo.put("uri", file.getUri());
        vo.put("url", file.getUri());
        vo.put("create_time", formatTime(file.getCreateTime()));
        vo.put("update_time", formatTime(file.getUpdateTime()));
        return vo;
    }

    private String formatTime(Long ts) {
        if (ts == null || ts <= 0) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts * 1000));
    }

    private List<Map<String, Object>> buildTree(List<Map<String, Object>> flat) {
        Map<Object, Map<String, Object>> byId = new LinkedHashMap<>();
        for (Map<String, Object> node : flat) {
            node.put("children", new ArrayList<Map<String, Object>>());
            byId.put(node.get("id"), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : flat) {
            Object pid = node.get("pid");
            if (pid == null || Objects.equals(pid, 0) || !byId.containsKey(pid)) {
                roots.add(node);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) byId.get(pid).get("children");
                children.add(node);
            }
        }
        for (Map<String, Object> node : flat) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children.isEmpty()) {
                node.remove("children");
            }
        }
        return roots;
    }
}
