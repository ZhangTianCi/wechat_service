package priv.asura.wechat_service.utils;

public interface SymmetricEncryptionInterface<T> {
    /**
     * 加密
     *
     * @param original 原文
     * @return 密文
     */
    T encrypt(T original);

    /**
     * 解密
     *
     * @param cipher 密文
     * @return 原文
     */
    T decrypt(T cipher);
}
