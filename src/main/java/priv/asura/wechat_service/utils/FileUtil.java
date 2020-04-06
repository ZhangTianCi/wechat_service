package priv.asura.wechat_service.utils;

import priv.asura.wechat_service.global.ServiceException;

import java.io.*;

public class FileUtil {
    public String getText(String filePath) {
        StringBuilder fileContent = new StringBuilder();
        try {
            File file = new File(filePath);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String lineContent = "";
            while ((lineContent = bufferedReader.readLine()) != null) {
                fileContent.append(lineContent).append("\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            throw new ServiceException("读取文件为String失败。", e);
        }
        return fileContent.toString();
    }

    public byte[] getByte(String filePath) {
        try {
            //1、得到数据文件
            File file = new File(filePath);
            //2、建立数据通道
            FileInputStream fileInputStream = new FileInputStream(file);
            //3. 建立缓冲区
            int readOnce;
            byte[] buffer = new byte[1024];
            //4. 利用缓冲区读取文件
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((readOnce = fileInputStream.read(buffer)) >= 0) {
                output.write(buffer, 0, readOnce);
            }
            fileInputStream.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new ServiceException("读取文件为Byte[]失败。", e);
        }
    }

    public void write(String filePath, byte[] content) {
        try {
            File file = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new ServiceException("数据写入文件失败", e);
        }
    }
}
