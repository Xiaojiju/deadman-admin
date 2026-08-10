package com.mtfm.deadman.plugin.crypto.facade;

import java.nio.charset.StandardCharsets;

import com.mtfm.deadman.plugin.crypto.constant.CryptoAlgorithms;
import com.mtfm.deadman.plugin.crypto.constant.CryptoModuleCodes;
import com.mtfm.deadman.plugin.crypto.spi.CryptoContext;

/**
 * 加解密统一门面：业务侧<strong>只依赖本接口</strong>，由内部策略按算法标识选择具体实现。
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li>门面模式：业务不感知 AES/信封等细节，只调用 {@link #encrypt}/{@link #decrypt}。</li>
 *   <li>策略模式：按 {@link CryptoContext#algorithmId()} 或配置默认算法选择 {@code EncryptionStrategy}。</li>
 *   <li>双轨道密钥：加密时优先模块密钥；{@code strict-module-key=true}（默认）时禁止静默回退 default。</li>
 *   <li>AAD 用途绑定：{@link CryptoContext#purpose()} 非空时写入 GCM AAD，防止密文跨字段移植。</li>
 *   <li>密文可落库：{@code v1.{algorithm}.{keyId}.{wrappedDek}.{iv}.{ciphertext}}（Base64URL）。</li>
 *   <li>历史兼容：非本模块密文格式原样返回；无 purpose 的旧密文用 {@link CryptoContext#defaults()} 解密。</li>
 *   <li>fail-closed：{@code enabled=false} 或无密钥时加解密抛错，不再静默透传明文。</li>
 * </ul>
 *
 * <h2>配置示例（application.yaml）</h2>
 * <pre>{@code
 * deadman:
 *   plugin:
 *     crypto:
 *       enabled: true
 *       require-keys: true
 *       strict-module-key: true
 *       default-algorithm: AES_GCM_ENVELOPE
 *       default-key-id: default
 *       default-key: ${DEADMAN_CRYPTO_DEFAULT_KEY}
 *       module-keys:
 *         pay:
 *           key-id: pay
 *           key: ${DEADMAN_CRYPTO_PAY_KEY}
 *       additional-keys:
 *         pay-v1: ${DEADMAN_CRYPTO_PAY_V1_KEY}   # 轮换后仅解密
 * }</pre>
 * 生成密钥：{@code openssl rand -base64 32}
 *
 * <h2>使用示例</h2>
 *
 * <h3>1. Spring 注入（推荐）</h3>
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class TransferService {
 *     private final CryptoFacade cryptoFacade;
 *
 *     public void save(String userName) {
 *         CryptoContext ctx = CryptoContext.forModule(CryptoModuleCodes.PAY)
 *                 .withPurpose("transfer.user_name");
 *         String cipher = cryptoFacade.encrypt(userName, ctx);
 *     }
 *
 *     public String load(String stored) {
 *         return cryptoFacade.decrypt(stored,
 *                 CryptoContext.forModule(CryptoModuleCodes.PAY).withPurpose("transfer.user_name"));
 *     }
 * }
 * }</pre>
 *
 * <h3>2. 脱离 Spring（工厂）</h3>
 * <pre>{@code
 * CryptoFacade facade = CryptoFacades.create(properties);
 * String cipher = facade.encrypt("张三");
 * }</pre>
 *
 * <h3>3. 默认密钥轨道</h3>
 * <pre>{@code
 * String cipher = cryptoFacade.encrypt("张三");
 * String plain  = cryptoFacade.decrypt(cipher);
 * }</pre>
 *
 * <h3>4. 显式模块 + 算法 + 用途</h3>
 * <pre>{@code
 * CryptoContext ctx = CryptoContext.of(
 *         CryptoModuleCodes.PAY,
 *         CryptoAlgorithms.AES_GCM_ENVELOPE,
 *         "transfer.user_name");
 * String cipher = cryptoFacade.encrypt("张三", ctx);
 * }</pre>
 *
 * <h3>5. 二进制明文</h3>
 * <pre>{@code
 * byte[] raw = CryptoFacade.utf8("敏感内容");
 * String token = cryptoFacade.encryptBytes(raw, CryptoContext.forModule("pay"));
 * byte[] back = cryptoFacade.decryptBytes(token, CryptoContext.forModule("pay"));
 * }</pre>
 *
 * @see CryptoContext
 * @see CryptoModuleCodes
 * @see CryptoAlgorithms
 * @see CryptoFacades
 * @see DefaultCryptoFacade
 */
public interface CryptoFacade {

    /**
     * 加密 UTF-8 字符串，返回可落库密文 token。
     *
     * <p>行为约定：
     * <ul>
     *   <li>{@code plaintext} 为空或空白：原样返回，不产生密文。</li>
     *   <li>插件未启用（{@code enabled=false}）：抛出业务异常（fail-closed）。</li>
     *   <li>已启用但未配置任何密钥：抛出业务异常。</li>
     *   <li>{@code context.purpose()} 非空时绑定 GCM AAD，解密须使用相同 purpose。</li>
     * </ul>
     *
     * @param plaintext 明文；空则原样返回
     * @param context   模块/算法/用途上下文；{@code null} 时等同 {@link CryptoContext#defaults()}
     * @return 密文 token
     */
    String encrypt(String plaintext, CryptoContext context);

    /**
     * 使用默认密钥轨道与默认算法加密字符串（无 AAD purpose）。
     *
     * @param plaintext 明文
     * @return 密文 token
     */
    default String encrypt(String plaintext) {
        return encrypt(plaintext, CryptoContext.defaults());
    }

    /**
     * 按模块编码加密（无 purpose 时不写 AAD）。
     *
     * <p>若开启 {@code strict-module-key}，未配置该模块密钥时失败而非回退 default。
     *
     * @param plaintext  明文
     * @param moduleCode 模块编码（如 {@link CryptoModuleCodes#PAY}）
     * @return 密文 token
     */
    default String encryptForModule(String plaintext, String moduleCode) {
        return encrypt(plaintext, CryptoContext.forModule(moduleCode));
    }

    /**
     * 解密密文 token（无 AAD，兼容历史无 purpose 密文）。
     *
     * <p>非本模块密文格式视为历史明文原样返回。
     *
     * @param ciphertext 密文 token 或历史明文
     * @return 明文
     */
    String decrypt(String ciphertext);

    /**
     * 按上下文解密（须与加密时相同的 purpose / 模块 AAD 约定）。
     *
     * @param ciphertext 密文 token 或历史明文
     * @param context    解密上下文
     * @return 明文
     */
    String decrypt(String ciphertext, CryptoContext context);

    /**
     * 加密任意字节数组，返回可落库密文 token。
     *
     * <p>未启用时抛出异常（与字符串加密 fail-closed 语义一致）。
     *
     * @param plaintext 明文字节；{@code null} 或空数组返回空字符串
     * @param context   上下文；{@code null} 时等同默认上下文
     * @return 密文 token
     */
    String encryptBytes(byte[] plaintext, CryptoContext context);

    /**
     * 将密文 token 解密为原始字节（无 AAD）。
     *
     * @param ciphertext 密文 token
     * @return 明文字节
     */
    byte[] decryptBytes(String ciphertext);

    /**
     * 按上下文将密文 token 解密为原始字节。
     *
     * @param ciphertext 密文 token
     * @param context    解密上下文
     * @return 明文字节
     */
    byte[] decryptBytes(String ciphertext, CryptoContext context);

    /**
     * 将文本按 UTF-8 编码为字节，供 {@link #encryptBytes} 使用。
     *
     * @param text 文本；{@code null} 时返回空数组
     * @return UTF-8 字节
     */
    static byte[] utf8(String text) {
        return text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
    }
}
