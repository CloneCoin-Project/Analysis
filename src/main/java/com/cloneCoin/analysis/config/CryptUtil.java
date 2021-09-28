package com.cloneCoin.analysis.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptUtil {

    /** Aes 인스턴스 */
    public static Aes aes = new Aes();
    /** Digest 인스턴스 */
    public static Digest digest = new Digest();
    /** Aes 인스턴스를 반환한다. */
    public static Aes getAES() {
        return aes;
    }//:
    /** Digest 인스턴스를 반환한다.*/
    public static Digest getDigest() {
        return digest;
    }

    /**
     * MessageDigest를 이용한 HASH 값을 생성한다.
     * @author behap
     *
     */
    public static class Digest {

        /**
         * 문자열을 HASH 값으로 변환한다.
         * @param strToDigest  변환할 문자열
         * @param algorithm  알고리즘.
         * @return
         * 		변환된 바이트 배열
         * @throws Exception
         */
        public byte[] digest(String strToDigest, AlgorithmEnum algorithm)   {
            try {
                //final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
                final MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
                final byte[] hashbytes = digest.digest(strToDigest.getBytes(StandardCharsets.UTF_8));
                return hashbytes;
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        /**
         * 문자열을 해쉬값으로 변환하고 Hex 문자열로 반환한다.
         * @param strToDigest 변환할 문자열
         * @param algorithm 알고리즘
         * @return
         * 		변환된 문자열
         * @throws Exception
         */
        public String digestToHex(String strToDigest, AlgorithmEnum algorithm)   {
            try {
                byte[] bytesDigested = digest(strToDigest, algorithm);
                return byteArrayToHex(bytesDigested);
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        }//:

    }//:

    /**
     * AES 암호화를 지원한다.  암호화를 위한 키는 16바이트여야 한다.
     */
    public static class Aes {
        /**
         * 문자열을 암호화 한다.
         *
         * @param key  암호화할 키
         * @param strToEncrypt  암호화할 문자열
         * @return
         * 		암호화된 문자열을 바이트 배열로 반환한다.
         * @throws Exception
         */

        public byte[] encryptToBytes(String key, String strToEncrypt) throws Exception  {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(strToEncrypt.getBytes("UTF-8"));

        }//:

        /**
         * 문자열을 암호화 한다.
         *
         * @param key  암호화할 키
         * @param strToEncrypt  암호화할 문자열
         * @return
         * 		암호화된 문자열을 base64로 encode해서  반환한다
         * @throws Exception
         */
        public String encrypt(String key, String strToEncrypt) throws Exception  {
            String encryptedStr = Base64.getEncoder().encodeToString(encryptToBytes(key, strToEncrypt));
            return encryptedStr;
        }//:

        /**
         * 암호화된 바이트 배열을 복호화 한다.
         *
         * @param key 암호화 키
         * @param bytesToDecrypt  복호화할 바이트 배열
         * @return
         * 		복호화된 바이트 배열
         * @throws Exception
         */
        public byte[] decryptToBytes(String key, byte[] bytesToDecrypt) throws Exception {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return  cipher.doFinal(bytesToDecrypt);
        }//:


        /**
         * Base 64로 감싸진 문자열을 복호화 한다.
         *
         * @param key 암호화 키
         * @param strToDecrypt  복호화할 문자열
         * @return
         * 		복호화된 문자열
         * @throws Exception
         */
        public String decrypt(String key, String strToDecrypt) throws Exception {
            byte[] bytesToDecrypt = Base64.getDecoder().decode(strToDecrypt);
            String decreptedStr = new String(decryptToBytes(key, bytesToDecrypt));
            return decreptedStr;
        }//:
    }///~ AES

    public static String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for(int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

}