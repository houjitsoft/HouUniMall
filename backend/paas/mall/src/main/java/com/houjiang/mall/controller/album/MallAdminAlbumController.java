package com.houjiang.mall.controller.album;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.album.MallAdminAlbumService;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城平台管理后台素材库（兼容 like-admin /file/* 路径契约）
 * 实际路径：/mall/admin/album/*
 * @author Amos
 */
@RestController
@RequestMapping("/mall/admin/album")
@RequiredArgsConstructor
public class MallAdminAlbumController {

    private final MallAdminAlbumService albumService;
    private final MallAdminTokenService mallAdminTokenService;

    @GetMapping("/listCate")
    public ResponseResult<Map<String, Object>> listCate(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String name,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        return ResponseResult.success(albumService.cateList(type, name));
    }

    @GetMapping("/lists")
    public ResponseResult<Map<String, Object>> lists(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size,
            @RequestParam(required = false) Integer page_no,
            @RequestParam(required = false) Integer page_size,
            @RequestParam(required = false) Integer cid,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String name,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        int pageNo = page_no != null ? page_no : page;
        int pageSz = page_size != null ? page_size : size;
        return ResponseResult.success(albumService.fileList(pageNo, pageSz, cid, type, name));
    }

    @PostMapping("/addCate")
    public ResponseResult<Void> addCate(@RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        albumService.cateAdd(intVal(body.get("type"), 10), str(body.get("name")),
                intVal(body.get("pid"), 0));
        return ResponseResult.success();
    }

    @PostMapping("/editCate")
    public ResponseResult<Void> editCate(@RequestBody Map<String, Object> body,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        albumService.cateEdit(intVal(body.get("id"), null), str(body.get("name")));
        return ResponseResult.success();
    }

    @PostMapping("/delCate")
    public ResponseResult<Void> delCate(@RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        albumService.cateDel(intVal(body.get("id"), null));
        return ResponseResult.success();
    }

    @PostMapping("/delete")
    public ResponseResult<Void> delete(@RequestBody Map<String, Object> body,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        albumService.fileDel(ids);
        return ResponseResult.success();
    }

    @PostMapping("/move")
    public ResponseResult<Void> move(@RequestBody Map<String, Object> body,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) body.get("ids");
        albumService.fileMove(ids, intVal(body.get("cid"), 0));
        return ResponseResult.success();
    }

    @PostMapping("/rename")
    public ResponseResult<Void> rename(@RequestBody Map<String, Object> body,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        albumService.fileRename(intVal(body.get("id"), null), str(body.get("name")));
        return ResponseResult.success();
    }

    @PostMapping("/record")
    public ResponseResult<Integer> record(@RequestBody Map<String, Object> body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!isAuthed(authorization)) {
            return ResponseResult.fail(401, "请先登录");
        }
        int type = intVal(body.get("type"), 10);
        Integer id = albumService.fileAdd(intVal(body.get("cid"), 0), type,
                str(body.get("name")), str(body.get("uri")));
        return ResponseResult.success(id);
    }

    private boolean isAuthed(String authorization) {
        return mallAdminTokenService.isAuthenticated(authorization);
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private static Integer intVal(Object o, Integer def) {
        if (o == null) {
            return def;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return def;
        }
    }
}
