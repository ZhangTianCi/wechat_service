package priv.asura.wechat_service.service;

import java.io.*;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JavaType;
import priv.asura.wechat_service.utils.DESUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import priv.asura.wechat_service.utils.SymmetricEncryptionInterface;

@Service
public abstract class DataService {

    @Value("${application.data.directory}")
    String applicationDataDirectory;

    @Value("${application.data.secret}")
    String applicationDataSecret;

    /**
     * 对称加密工具
     */
    private SymmetricEncryptionInterface<byte[]> symmetricEncryptionInterface = null;

    /**
     * 获取数据文件路径，交由子类实现
     *
     * @return 数据文件路径
     */
    public abstract String getFilePath();

    /**
     * 数据序列化工具
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 读取数据
     *
     * @param valueType -
     * @param <T>       -
     * @return -
     * @throws Exception -
     */
    public <T> T readData(Class<T> valueType) throws Exception {
        try {
            byte[] data = readData();
            return objectMapper.readValue(data, valueType);
        } catch (Exception e) {
            throw new Exception("获取数据失败", e);
        }
    }

    /**
     * 读取数据为集合
     *
     * @param valueType -
     * @param <T>       -
     * @return -
     * @throws Exception -
     */
    protected <T> ArrayList<T> readDataWhitList(Class<T> valueType) throws Exception {
        try {
            byte[] data = readData();
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, valueType);
            return objectMapper.readValue(data, javaType);
        } catch (Exception e) {
            throw new Exception("获取数据失败", e);
        }
    }

    /**
     * 保存数据
     *
     * @param object -
     * @throws Exception -
     */
    protected void writeData(Object object) throws Exception {
        try {
            byte[] data = objectMapper.writeValueAsBytes(object);
            saveData(data);
        } catch (Exception e) {
            throw new Exception("保存数据失败", e);
        }
    }

    /**
     * 从文件读取数据 - 自解密
     *
     * @return -
     * @throws IOException -
     */
    private byte[] readData() throws IOException {
        //1、得到数据文件
        File file = getDataFile();
        //2、建立数据通道
        FileInputStream fileInputStream = new FileInputStream(file);
        //3. 建立缓冲区
        int readOnce = 0;
        byte[] buffer = new byte[1024];
        //4. 利用缓冲区读取文件
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((readOnce = fileInputStream.read(buffer)) >= 0) {
            output.write(buffer, 0, readOnce);
        }
        //最后记得，关闭流
        fileInputStream.close();
        byte[] original = output.toByteArray();
        return getDesUtil().decrypt(original);
    }

    /**
     * 保存数据到文件 - 自加密
     *
     * @param data 需要保存的数据
     * @throws IOException -
     */
    private void saveData(byte[] data) throws IOException {
        File file = getDataFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(getDesUtil().encrypt(data));
    }

    /**
     * 获取数据文件 - 带锁
     *
     * @return 数据文件
     */
    private synchronized File getDataFile() {
        return new File(applicationDataDirectory + File.separator + getFilePath());
    }

    /**
     * 获取加密解密工具
     *
     * @return -
     */
    private SymmetricEncryptionInterface<byte[]> getDesUtil() {
        if (symmetricEncryptionInterface == null) {
            symmetricEncryptionInterface = new DESUtil(applicationDataSecret);
        }
        return symmetricEncryptionInterface;
    }
}
