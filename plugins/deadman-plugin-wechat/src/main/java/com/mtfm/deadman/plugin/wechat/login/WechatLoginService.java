package com.mtfm.deadman.plugin.wechat.login;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatMiniprogramLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.credential.WechatWebLoginCredential;
import com.mtfm.deadman.plugin.wechat.login.dto.WechatLoginResolveRequest;
import com.mtfm.deadman.plugin.wechat.login.initiate.WechatLoginInitiateResult;
import com.mtfm.deadman.plugin.wechat.login.session.WechatLoginSession;
import com.mtfm.deadman.plugin.wechat.login.spi.WechatLoginInitiator;
import com.mtfm.deadman.plugin.wechat.login.spi.WechatLoginResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 微信登录公开门面，按登录方式标识路由到对应解析器/发起器。
 * <p>
 * 引入 {@code deadman-plugin-wechat} 的模块应通过本服务调用不同微信登录方式，并使用
 * {@link WechatLoginSession} / {@link WechatLoginInitiateResult} 标准模板对象。
 */
@Slf4j
@Service
public class WechatLoginService {

    private final Map<String, WechatLoginResolver> resolvers;
    private final Map<String, WechatLoginInitiator> initiators;

    /**
     * 构造微信登录门面。
     *
     * @param resolverList  所有登录解析器 Bean
     * @param initiatorList 所有登录发起器 Bean
     */
    public WechatLoginService(List<WechatLoginResolver> resolverList, List<WechatLoginInitiator> initiatorList) {
        Map<String, WechatLoginResolver> resolverRegistry = new LinkedHashMap<>();
        for (WechatLoginResolver resolver : resolverList) {
            if (resolverRegistry.containsKey(resolver.loginKind())) {
                log.warn("微信登录解析器重复注册，后者覆盖前者：{}", resolver.loginKind());
            }
            resolverRegistry.put(resolver.loginKind(), resolver);
        }
        this.resolvers = Map.copyOf(resolverRegistry);

        Map<String, WechatLoginInitiator> initiatorRegistry = new LinkedHashMap<>();
        for (WechatLoginInitiator initiator : initiatorList) {
            if (initiatorRegistry.containsKey(initiator.loginKind())) {
                log.warn("微信登录发起器重复注册，后者覆盖前者：{}", initiator.loginKind());
            }
            initiatorRegistry.put(initiator.loginKind(), initiator);
        }
        this.initiators = Map.copyOf(initiatorRegistry);
        log.info("微信登录门面注册完成，解析器 {} 个：{}，发起器 {} 个：{}",
                resolvers.size(), resolvers.keySet(), initiators.size(), initiators.keySet());
    }

    /**
     * 获取当前已注册的登录方式标识。
     *
     * @return 登录方式标识集合
     */
    public Set<String> supportedLoginKinds() {
        return resolvers.keySet();
    }

    /**
     * 是否支持指定登录方式。
     *
     * @param loginKind 登录方式标识
     * @return 是否支持
     */
    public boolean supports(String loginKind) {
        return resolvers.containsKey(loginKind);
    }

    /**
     * 将登录凭证解析为统一会话模板。
     *
     * @param credential 登录凭证
     * @return 登录会话
     */
    public WechatLoginSession resolve(WechatLoginCredential credential) {
        if (credential == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录凭证不能为空");
        }
        WechatLoginResolver resolver = requireResolver(credential.loginKind());
        if (!resolver.supports(credential)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录凭证与登录方式不匹配：" + credential.loginKind());
        }
        return resolver.resolve(credential);
    }

    /**
     * 将 HTTP 请求体解析为登录凭证并换取会话。
     *
     * @param request 解析请求
     * @return 登录会话
     */
    public WechatLoginSession resolve(WechatLoginResolveRequest request) {
        return resolve(toCredential(request));
    }

    /**
     * 发起指定登录方式的预登录流程（如网页扫码授权 URL）。
     *
     * @param loginKind 登录方式标识
     * @return 发起结果
     */
    public WechatLoginInitiateResult initiate(String loginKind) {
        WechatLoginInitiator initiator = initiators.get(loginKind);
        if (initiator == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "微信登录方式不支持发起流程：" + loginKind);
        }
        return initiator.initiate();
    }

    /**
     * 是否支持发起指定登录方式。
     *
     * @param loginKind 登录方式标识
     * @return 是否支持
     */
    public boolean supportsInitiate(String loginKind) {
        return initiators.containsKey(loginKind);
    }

    private WechatLoginResolver requireResolver(String loginKind) {
        WechatLoginResolver resolver = resolvers.get(loginKind);
        if (resolver == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "微信登录方式未启用或不支持：" + loginKind);
        }
        return resolver;
    }

    private WechatLoginCredential toCredential(WechatLoginResolveRequest request) {
        if (request == null || !StringUtils.hasText(request.loginKind())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录方式 loginKind 不能为空");
        }
        String loginKind = request.loginKind().trim();
        if (WechatLoginKinds.MINIPROGRAM.equals(loginKind)) {
            if (!StringUtils.hasText(request.code())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
            }
            return new WechatMiniprogramLoginCredential(request.code().trim());
        }
        if (WechatLoginKinds.WEB.equals(loginKind)) {
            if (!StringUtils.hasText(request.code())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 code 不能为空");
            }
            if (!StringUtils.hasText(request.state())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "微信登录 state 不能为空");
            }
            return new WechatWebLoginCredential(request.code().trim(), request.state().trim());
        }
        throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的微信登录方式：" + loginKind);
    }
}
