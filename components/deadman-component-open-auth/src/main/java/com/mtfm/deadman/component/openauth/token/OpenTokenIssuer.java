package com.mtfm.deadman.component.openauth.token;

/**
 * 开放 access_token 签发器抽象。
 */
public interface OpenTokenIssuer {

    /**
     * 签发 open_access_token。
     *
     * @param context 签发上下文
     * @return 签发结果
     */
    OpenTokenIssueResult issue(OpenTokenIssueContext context);
}
