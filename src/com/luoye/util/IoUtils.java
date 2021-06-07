package com.luoye.util;
import java.io.*;
/**
 * @author luoyesiqiu
 */
public class IoUtils {
    public static final String ROOT_OF_OUT_DIR = System.getProperty("java.io.tmpdir");

    public static byte[] readFile(String file){
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            fileInputStream = new FileInputStream(file);
            int len = -1;
            byte[] buf = new byte[4096];
            while((len = fileInputStream.read(buf)) != -1){
                byteArrayOutputStream.write(buf,0,len);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            close(fileInputStream);
            close(byteArrayOutputStream);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void writeFile(String dest,byte[] data){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(dest);
            fileOutputStream.write(data);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            close(fileOutputStream);
        }
    }

    public static void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
