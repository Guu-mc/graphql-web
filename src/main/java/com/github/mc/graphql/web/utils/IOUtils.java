package com.github.mc.graphql.web.utils;

import java.io.*;

public class IOUtils {

    public static void write(byte[] bytes, OutputStream os) {
        try (BufferedOutputStream bos = new BufferedOutputStream(os)){
            bos.write(bytes);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toString(InputStream in) throws UnsupportedEncodingException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try (BufferedInputStream bin = new BufferedInputStream(in)){
            while ((length = bin.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString("UTF-8");
    }
}
