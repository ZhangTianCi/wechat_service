package priv.asura.wechat_service.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;

@Slf4j
public class DESUtil implements SymmetricEncryptionInterface<byte[]> {

    private static Key key;
    private static String ALGORITHM = "DES";

    public DESUtil(String secret) {
        try {
            // 生成DES算法对象
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            // 运用SHA1安全策略
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            // 设置上密钥种子
            secureRandom.setSeed(secret.getBytes());
            // 初始化基于SHA1的算法对象
            generator.init(secureRandom);
            // 生成密钥对象
            key = generator.generateKey();
            generator = null;
        } catch (Exception e) {
            log.error("DESUtil 类加载 初始化加密对象异常");
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encrypt(byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            log.error("DESUtil 加密 发生异常 ：", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            log.error("DESUtil 解密 Exception ：", e);
            throw new RuntimeException(e);
        }
    }
}