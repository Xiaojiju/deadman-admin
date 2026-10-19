package com.mtfm.deadman.system.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.system.service.DictAdminService;
import com.mtfm.deadman.system.vo.dict.ClientDictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户端字典查询。匿名可访问，只返回启用的字典组与字典项。
 */
@RestController
@RequestMapping("/client/api/dicts")
@RequiredArgsConstructor
public class ClientDictController {

    private final DictAdminService dictAdminService;

    /**
     * 按组编码筛选已启用字典。
     *
     * @param groupCode 字典组编码，可重复传参；不传则返回全部启用组
     * @return 字典组及其启用项树
     */
    @GetMapping
    public Result<List<ClientDictVO>> list(@RequestParam(name = "groupCode", required = false) List<String> groupCode) {
        return Result.ok(dictAdminService.listEnabled(groupCode));
    }
}
