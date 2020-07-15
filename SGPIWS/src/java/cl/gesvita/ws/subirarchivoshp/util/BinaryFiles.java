/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.gesvita.ws.subirarchivoshp.util;

/**
 *
 * @author Felipe
 */
public class BinaryFiles {

//    public static String bin2Hex(String bin) {
//        StringBuilder resultado = new StringBuilder();
//        int len = bin.length();
//        int next_i;
//        for (int i = 0; i < len; i = next_i) {
//            next_i = i + 4;
//            int numero = Integer.parseInt(bin.substring(i, next_i), 2);
//            resultado.append((String) Integer.toString(numero, 16));
//        }
//        return resultado.toString();
//    }
    private static String digits = "0123456789ABCDEF";

    public static String bin2Hex(String bin) {
        StringBuilder buf = new StringBuilder();
        int len = bin.length();
        for (int i = 0; i < len; i++) {
            int v = bin.charAt(i) & 0xff;
            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }
        return buf.toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int len) {
        char[] hexChars = new char[len * 2];
        for ( int j = 0; j < len; j++ ) {
            int v = bytes[j] & 0xFF;
           hexChars[j * 2] = hexArray[v >>> 4];
           hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hex2Bin(String hex) {
        StringBuilder resultado = new StringBuilder();
        int len = hex.length();
        len -= (len % 2);
        int next_i;
        for (int i = 0; i < len; i = next_i) {
            next_i = i + 2;
            resultado.append((char) Integer.parseInt(hex.substring(i, next_i), 16));
        }
        return resultado.toString();
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
