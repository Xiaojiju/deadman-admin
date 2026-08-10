package com.mtfm.deadman.plugin.crypto.strategy;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;
import com.mtfm.deadman.plugin.crypto.constant.CryptoAlgorithms;
import com.mtfm.deadman.plugin.crypto.spi.EncryptedPayload;
import com.mtfm.deadman.plugin.crypto.spi.EncryptionStrategy;

/**
 * AES-256-GCM 对称信封加密策略。
 *
 * <p>加密步骤：
 * <ol>
 *   <li>随机生成数据加密密钥 DEK（AES-256）。</li>
 *   <li>用 DEK + 随机 IV 对明文做 AES-GCM（可带 AAD），得到数据密文。</li>
 *   <li>用配置中的 KEK + 另一随机 IV 包装 DEK；{@code wrappedDek} 布局为 {@code wrapIv(12) || encryptedDek+tag}。</li>
 * </ol>
 *
 * <p>AAD 仅绑定在数据加密层，用于防止密文跨字段移植；DEK 包装不使用 AAD。
 *
 * @see CryptoAlgorithms#AES_GCM_ENVELOPE
 */
public class AesGcmEnvelopeEncryptionStrategy implements EncryptionStrategy {

    /** AES-GCM 变换 */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    /** GCM 认证标签位数 */
    private static final int GCM_TAG_BITS = 128;
    /** GCM 推荐 IV 长度（字节） */
    private static final int IV_BYTES = 12;
    /** DEK 位数（AES-256） */
    private static final int DEK_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * {@inheritDoc}
     */
    @Override
    public String algorithmId() {
        return CryptoAlgorithms.AES_GCM_ENVELOPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EncryptedPayload encrypt(byte[] plaintext, SecretKey kek, String keyId, byte[] aad) {
        try {
            SecretKey dek = generateDek();
            byte[] dataIv = randomBytes(IV_BYTES);
            byte[] ciphertext = aesGcm(Cipher.ENCRYPT_MODE, dek, dataIv, plaintext, aad);

            byte[] wrapIv = randomBytes(IV_BYTES);
            byte[] wrappedDek = aesGcm(Cipher.ENCRYPT_MODE, kek, wrapIv, dek.getEncoded(), null);
            // wrappedDek 落库布局：wrapIv(12) || encryptedDek+tag
            byte[] wrappedWithIv = concat(wrapIv, wrappedDek);

            return new EncryptedPayload(1, algorithmId(), keyId, wrappedWithIv, dataIv, ciphertext);
        } catch (GeneralSecurityException ex) {
            throw new BusinessException(ResultCode.CRYPTO_ENCRYPT_FAILED, "AES 信封加密失败", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decrypt(EncryptedPayload payload, SecretKey kek, byte[] aad) {
        try {
            byte[] wrappedWithIv = payload.wrappedDek();
            if (wrappedWithIv == null || wrappedWithIv.length <= IV_BYTES) {
                throw new BusinessException(ResultCode.CRYPTO_PAYLOAD_INVALID, "包装 DEK 无效");
            }
            byte[] wrapIv = new byte[IV_BYTES];
            System.arraycopy(wrappedWithIv, 0, wrapIv, 0, IV_BYTES);
            byte[] encryptedDek = new byte[wrappedWithIv.length - IV_BYTES];
            System.arraycopy(wrappedWithIv, IV_BYTES, encryptedDek, 0, encryptedDek.length);

            byte[] dekBytes = aesGcm(Cipher.DECRYPT_MODE, kek, wrapIv, encryptedDek, null);
            SecretKey dek = new SecretKeySpec(dekBytes, "AES");
            return aesGcm(Cipher.DECRYPT_MODE, dek, payload.iv(), payload.ciphertext(), aad);
        } catch (BusinessException ex) {
            throw ex;
        } catch (GeneralSecurityException ex) {
            throw new BusinessException(ResultCode.CRYPTO_DECRYPT_FAILED, "AES 信封解密失败", ex);
        }
    }

    /**
     * 生成随机 AES-256 DEK。
     *
     * @return 数据加密密钥
     */
    private SecretKey generateDek() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(DEK_BITS, secureRandom);
        return keyGenerator.generateKey();
    }

    /**
     * 生成指定长度的安全随机字节。
     *
     * @param length 字节数
     * @return 随机字节
     */
    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    /**
     * AES-GCM 加/解密，可选 AAD。
     *
     * @param mode  {@link Cipher#ENCRYPT_MODE} 或 {@link Cipher#DECRYPT_MODE}
     * @param key   对称密钥
     * @param iv    12 字节 IV
     * @param input 明文或密文
     * @param aad   关联数据；null 或空表示不设置
     * @return 密文或明文
     */
    private static byte[] aesGcm(int mode, SecretKey key, byte[] iv, byte[] input, byte[] aad)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        if (aad != null && aad.length > 0) {
            cipher.updateAAD(aad);
        }
        return cipher.doFinal(input);
    }

    /**
     * 拼接两个字节数组。
     *
     * @param a 前段
     * @param b 后段
     * @return a||b
     */
    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
