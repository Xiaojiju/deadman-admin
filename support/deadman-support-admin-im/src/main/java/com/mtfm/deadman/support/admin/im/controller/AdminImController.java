package com.mtfm.deadman.support.admin.im.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.plugin.im.tencent.service.ImService;
import com.mtfm.deadman.plugin.im.tencent.vo.ImCredentialVO;
import com.mtfm.deadman.plugin.im.tencent.vo.ImUserLookupVO;
import com.mtfm.deadman.support.admin.im.constant.AdminImRealmConstants;

import lombok.RequiredArgsConstructor;

/**
 * 管理端 IM API，桥接腾讯云 IM 插件与管理端鉴权。
 */
@RestController
@RequestMapping("/api/im")
@RequiredArgsConstructor
public class AdminImController {

    private final ImService imService;

    /**
     * 为当前登录管理端用户签发腾讯云 IM 登录凭证。
     *
     * @return IM 登录凭证
     */
    @GetMapping("/credential")
    public Result<ImCredentialVO> credential() {
        return Result.ok(imService.issueCredentialForCurrentUser(AdminImRealmConstants.REALM_ID));
    }

    /**
     * 按用户域与业务主键查询 IM UserID 映射，供客服等场景定位对端 IM 账号。
     *
     * @param realmId   用户域标识，如 client、admin
     * @param subjectId 域内稳定主键，如 userCode
     * @return IM 用户映射
     */
    @GetMapping("/users/lookup")
    public Result<ImUserLookupVO> lookup(
            @RequestParam("realm") String realmId, @RequestParam("subjectId") String subjectId) {
        return Result.ok(imService.lookupImUser(realmId, subjectId));
    }
}
