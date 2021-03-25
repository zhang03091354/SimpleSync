package com.zcj.nettyClinet.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashcode {
    public static byte[] getMD5(byte[] data){

        try {
            MessageDigest md= MessageDigest.getInstance("MD5");
            byte[] ret = md.digest(data);
            return ret;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
