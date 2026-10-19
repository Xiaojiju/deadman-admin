package com.mtfm.deadman.support.client.file.access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 本地存储静态路径的守门过滤器：敏感证件类目录（如 /files/wgb-id-card/**）不带有效签名一律 403，
 * 路径里有可疑字符的也拒绝；公开文件照常放行。签名地址只由管理端 /api/files 接口签发。
 */
public class SensitiveStaticFileFilter extends OncePerRequestFilter {

    private static final String FORBIDDEN_BODY = "{\"code\":40300,\"msg\":\"无权访问该文件\",\"success\":false}";

    private final SensitiveFilePolicy policy;

    /**
     * @param policy 敏感文件规则
     */
    public SensitiveStaticFileFilter(SensitiveFilePolicy policy) {
        this.policy = policy;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathOf(request);
        return path == null || !path.toLowerCase().startsWith(policy.localPrefix().toLowerCase() + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String path = pathOf(request);
        String decoded;
        try {
            decoded = UriUtils.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            forbid(response);
            return;
        }
        switch (policy.classifyStaticPath(decoded)) {
            case REJECT -> forbid(response);
            case SENSITIVE -> {
                if (policy.verify(decoded, request.getParameter(SensitiveFilePolicy.PARAM_EXPIRES),
                    request.getParameter(SensitiveFilePolicy.PARAM_SIGNATURE))) {
                    response.setHeader("Cache-Control", "private, no-store");
                    response.setHeader("Referrer-Policy", "no-referrer");
                    chain.doFilter(request, response);
                } else {
                    forbid(response);
                }
            }
            default -> chain.doFilter(request, response);
        }
    }

    private static String pathOf(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }
        String context = request.getContextPath();
        return context != null && !context.isEmpty() && uri.startsWith(context) ? uri.substring(context.length())
            : uri;
    }

    private static void forbid(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(FORBIDDEN_BODY.getBytes(StandardCharsets.UTF_8));
    }
}
