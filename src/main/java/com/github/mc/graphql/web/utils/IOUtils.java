package com.github.mc.graphql.web.utils;

import java.io.*;

public class IOUtils {

    public static void write(byte[] bytes, OutputStream os) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(os)){
            bos.write(bytes);
        }
    }

    public static String toString(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
             BufferedInputStream bis = new BufferedInputStream(is)){
            byte[] bs = new byte[1024];
            int ch;
            while ((ch = bis.read(bs)) != -1) {
                os.write(bs, 0, ch);
            }
            return os.toString();
        }
    }
}
