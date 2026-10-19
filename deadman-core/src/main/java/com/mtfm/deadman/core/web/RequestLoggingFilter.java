package com.mtfm.deadman.core.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 把每次 HTTP 请求的方法、路径、正文和响应状态打到日志，方便在控制台核对客户端调用。
 */
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** 正文最多打印的字节数，避免大文件把控制台刷满 */
    private static final int MAX_BODY_BYTES = 8 * 1024;

    /**
     * 包装请求以便事后读取正文，处理完成后再打印。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 后续过滤链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, MAX_BODY_BYTES);
        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            logRequest(wrapped, response, startedAt);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, HttpServletResponse response, long startedAt) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
        String query = request.getQueryString();
        String path = request.getRequestURI() + (query == null || query.isBlank() ? "" : "?" + query);
        String body = bodyText(request);
        if (body == null) {
            log.info("请求 {} {} 状态={} 耗时={}ms", request.getMethod(), path, response.getStatus(), elapsedMs);
            return;
        }
        log.info("请求 {} {} 状态={} 耗时={}ms 正文={}", request.getMethod(), path, response.getStatus(), elapsedMs,
                body);
    }

    /**
     * 只打印文本正文。文件上传和二进制内容不展开。
     */
    private static String bodyText(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && (contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)
                || contentType.startsWith(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                || contentType.startsWith("image/")
                || contentType.startsWith("video/"))) {
            return "[文件或二进制，已省略正文]";
        }
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        Charset charset = charsetOf(request.getCharacterEncoding());
        String text = new String(content, charset).replaceAll("\\s+", " ").trim();
        if (content.length >= MAX_BODY_BYTES) {
            return text + "...(已截断)";
        }
        return text;
    }

    private static Charset charsetOf(String encoding) {
        if (encoding == null || encoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException ex) {
            return StandardCharsets.UTF_8;
        }
    }
}
