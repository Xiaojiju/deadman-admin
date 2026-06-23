package com.mtfm.deadman.support.client.file.controller;

import com.mtfm.deadman.common.result.Result;
import com.mtfm.deadman.component.client.ClientAuthSupport;
import com.mtfm.deadman.component.client.auth.ClientLoginUser;
import com.mtfm.deadman.plugin.file.service.FileService;
import com.mtfm.deadman.plugin.file.vo.FileMetadataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端文件上传 API，桥接 file 插件与 client 鉴权。
 */
@RestController
@RequestMapping("/client/api/files")
@RequiredArgsConstructor
public class ClientFileController {

    private final FileService fileService;

    /**
     * 用户端上传文件，默认使用 file 插件配置的存储 Provider（可切换 OSS）。
     *
     * @param file       上传文件
     * @param bizType    业务分类（须已注册，如 rent、spare-part）
     * @param providerId 存储 Provider，为空时使用默认
     * @param loginUser  当前登录用户
     * @return 文件元数据（含 accessUrl，OSS 模式下为 CDN 或签名 URL）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileMetadataVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") String bizType,
            @RequestParam(value = "providerId", required = false) String providerId,
            @AuthenticationPrincipal ClientLoginUser loginUser) {
        ClientLoginUser user = ClientAuthSupport.requireLogin(loginUser);
        return Result.ok(fileService.upload(file, bizType, providerId, user.getUserId()));
    }
}
