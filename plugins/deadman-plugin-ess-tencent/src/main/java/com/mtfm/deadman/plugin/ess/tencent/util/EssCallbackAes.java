package com.mtfm.deadman.plugin.ess.tencent.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.common.result.ResultCode;

/**
 * 腾讯电子签回调解密工具（AES/CBC + PKCS7）。
 * <p>
 * 对齐官方 ess-java-kit {@code CallbackAes} 实现。
 */
public final class EssCallbackAes {

    private EssCallbackAes() {
    }

    /**
     * 解密回调密文。
     *
     * @param encryptedBase64 Base64 密文
     * @param aesKey 回调 AES 密钥
     * @return 明文 JSON
     */
    public static String decrypt(String encryptedBase64, String aesKey) {
        try {
            byte[] plain = aesDecrypt(
                encryptedBase64.getBytes(StandardCharsets.UTF_8),
                aesKey.getBytes(StandardCharsets.UTF_8));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签回调解密失败", ex);
        }
    }

    /**
     * AES 解密。
     *
     * @param crypted Base64 密文字节
     * @param key 密钥字节
     * @return 明文
     * @throws Exception 解密异常
     */
    public static byte[] aesDecrypt(byte[] crypted, byte[] key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(crypted);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        int blockSize = cipher.getBlockSize();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        byte[] iv = Arrays.copyOf(key, blockSize);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return pkcs7UnPadding(cipher.doFinal(decoded));
    }

    /**
     * AES 加密。
     *
     * @param origData 明文
     * @param key 密钥字节
     * @return Base64 密文
     * @throws Exception 加密异常
     */
    public static byte[] aesEncrypt(byte[] origData, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        int blockSize = cipher.getBlockSize();
        byte[] padded = pkcs7Padding(origData, blockSize);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        byte[] iv = Arrays.copyOf(key, blockSize);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        return Base64.getEncoder().encode(cipher.doFinal(padded));
    }

    private static byte[] pkcs7Padding(byte[] ciphertext, int blockSize) {
        int padding = blockSize - ciphertext.length % blockSize;
        byte[] padText = new byte[padding];
        Arrays.fill(padText, (byte) padding);
        byte[] result = new byte[ciphertext.length + padding];
        System.arraycopy(ciphertext, 0, result, 0, ciphertext.length);
        System.arraycopy(padText, 0, result, ciphertext.length, padding);
        return result;
    }

    private static byte[] pkcs7UnPadding(byte[] origData) {
        int unpadding = origData[origData.length - 1] & 0xFF;
        if (unpadding <= 0 || unpadding > origData.length) {
            throw new BusinessException(ResultCode.ESS_CALLBACK_INVALID, "电子签回调解密填充非法");
        }
        return Arrays.copyOf(origData, origData.length - unpadding);
    }
}
