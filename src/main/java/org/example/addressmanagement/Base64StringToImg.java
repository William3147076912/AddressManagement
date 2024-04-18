package org.example;

import java.io.*;
import java.util.Base64;

public class Base64StringToImg {
public static String ImgtoString(String imgFile) throws IOException {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
    //"D:\\desktop\\量子计算.jpg"
    InputStream in = null;
    byte[] data = null;
    //读取图片字节数组
    try
    {
        in = new FileInputStream(imgFile);
        data = new byte[in.available()];
        in.read(data);
        in.close();
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
    return Base64.getEncoder().encodeToString(data);
}
    public static String Toimg(String encoded) throws IOException {
        String filename="D:\\desktop\\result.jpg";
        File file=new File(filename);
        file.createNewFile();
        FileOutputStream fos=new FileOutputStream(file);
        fos.write(decode(encoded));
        fos.close();
        return encoded;
    }
    public static byte[] decode(String encoded)
    {
        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
        return decodedBytes;
    }
}
