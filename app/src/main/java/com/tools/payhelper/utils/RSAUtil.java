package com.tools.payhelper.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class RSAUtil {

    public static final String DEFAULT_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVaILsU5yKodQxxmqgarW+jCt/\n" +
                    "xhO4irh3/uVeMiP/UsRMRbc6BW2OndBZ9L95Jx5zzt+qGcEetPHChfqMxuUjrh4N\n" +
                    "Wd44wpm8sLRzppEQ7vKEoKW7Vl4f9fXrvNMqdAyE9fEfizQ3SQ7XY1jo6+l10kaU\n" +
                    "hBG5XbG8Evg1o7TytwIDAQAB";

    public static final String DEFAULT_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALnPGrwtZd7xqIIA\n" +
                    "I7S4l16Xs1Kb7EvCK8r85s4KTIvJW0KbCD2cdchr8ydS/OKP86ZmZ2E+6WG/uRt6\n" +
                    "tk70NDLG8X20v2uiGQUMkVGGemJ8jcuIFaDjsDTWAOkEaSNz4ppv/Ow1ymxZG28u\n" +
                    "BV5MzZ+KE9bFkHzLjfr93UKMWpQFAgMBAAECgYAB572+cDi7MH8p1haHo9zpe2S7\n" +
                    "VB34KOWKD03Ojwx54natGCndd909UN4NaSxupks5SZOFy79Y4T5kqb100cw3NJn3\n" +
                    "s9ecH9fjOCZOLmj/ojrbkoXzGAwPWruYNgx0HvEFfA2TrYcE3eOf82aWRtiAy1Do\n" +
                    "BfqTPFbeXSnBnfybgQJBANvK0fVbJDh5upJBenVGuT8VO4x0T6AHsjmfSc//x88W\n" +
                    "61Mdx5hNJF3UKVJEGtqrJWIfwJZOEDmO3RwoKsRYFFMCQQDYayBkOaAAeY2C37bL\n" +
                    "a0NwVNQHPadcXIw2r70OECaKdKohzPW34/z4CyS0sWLmaLWnTj46dcVdYqiJR9fh\n" +
                    "EStHAkEAlB+Y5fi5bVIkOYLlS3oRSAFAM4UDUH8/TnQaI5JmjxsMFuS/6dU7R/y+\n" +
                    "qFF7o5ipxfpOKT2M062DOAGd6NrPPwJAfu8+XWqrHPwucw+SIHe2Y2Ftxx1zVyn1\n" +
                    "F3I2GdSBNn4893xGtufjDP1ENzM/xdKukQXEW/eNnjtqjLJ1vU2bqwJAe2tplAT1\n" +
                    "d2Dn1mwK86tEy8FJLuVkgV32azQvMdNoKMGlqXfmzaTigKz8iINcKQY42axFHJUK\n" +
                    "Kr8xkx2CMwCCBA==";

    public static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 加载公钥
     * @param in
     * @throws Exception
     */
    public void loadPublicKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPublicKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("公钥数据流读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥输入流为空");
        }
    }


    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("公钥非法");
        } catch (IOException e) {
            throw new Exception("公钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("公钥数据为空");
        }
    }

    /**
     * 从文件中加载私钥
     * @return 是否成功
     * @throws Exception
     */
    public void loadPrivateKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            loadPrivateKey(sb.toString());
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }

    public static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (IOException e) {
            throw new Exception("私钥数据内容读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 签名
     * @param privateKey 私钥
     * @return
     * @throws SignatureException
     */
    public static String rsaSign(String privateKey, String signStr) throws SignatureException {
        try {
            RSAPrivateKey rsaPrivateKey=loadPrivateKey(privateKey);
            if (privateKey == null) {
                throw new Exception("加密公钥为空, 请设置");
            }
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(rsaPrivateKey);
            signature.update(signStr.getBytes());

            byte[] signed = signature.sign();
            String output = new BASE64Encoder().encode(signed);
            return output;
        } catch (Exception e) {
            throw new SignatureException("RSAcontent = " + signStr);
        }
    }

    /**
     * 验签
     */
    public static boolean rsaVerify(String publicKey, String sign,String signStr)
    {
        try
        {
            RSAPublicKey rsaPublicKey=loadPublicKey(publicKey);
            if (publicKey == null) {
                throw new Exception("解密私钥为空, 请设置");
            }
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(rsaPublicKey);
            signature.update(signStr.getBytes());

            //把签名反解析，并验证
            byte[] decodeSign=new BASE64Decoder().decodeBuffer(sign);
            return signature.verify(decodeSign);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //LogMan.log("[NeteaseSignUtil][verifySHA1withRSASigature]"+e);
            return false;
        }
    }


    public static final byte[] hexStrToBytes(String s)  {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++){
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /**
     * 加密过程
     *
     * @param privateKey     公钥
     * @param encryptStr 明文数据
     * @return
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String privateKey, String encryptStr) throws Exception {
        byte[] plainTextData=encryptStr.getBytes();
        RSAPrivateKey rsaPrivateKey=loadPrivateKey(privateKey);

        if (privateKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
            byte[] crypted = cipher.doFinal(plainTextData);

            String output = new BASE64Encoder().encode(crypted);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 解密过程
     *
     * @param publicKey 私钥
     * @param data 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String publicKey, String data) throws Exception {
        RSAPublicKey rsaPublicKey=loadPublicKey(publicKey);
        if (publicKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            byte[] cipherData=new BASE64Decoder().decodeBuffer(data);
            byte[] output = cipher.doFinal(cipherData);
            return new String(output);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }
    /**
     *
     * @param privateKey
     * @param encryptStr
     * @return
     * @throws Exception
     */
    public static String encryptLong(String privateKey, String encryptStr) throws Exception {
        byte[] plainTextData=encryptStr.getBytes();
        RSAPrivateKey rsaPrivateKey=loadPrivateKey(privateKey);

        if (privateKey == null) {
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);

            int inputLen=plainTextData.length;
            int offLen=0;
            int i=0;
            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();

            while (inputLen-offLen>0){
                byte[] cache;
                if(inputLen-offLen>117){
                    cache=cipher.doFinal(plainTextData,offLen,117);
                }else{
                    cache=cipher.doFinal(plainTextData,offLen,inputLen-offLen);
                }

                outputStream.write(cache);
                i++;
                offLen=117*i;
            }
            outputStream.close();
            byte[] encryptedData=outputStream.toByteArray();
            String output = new BASE64Encoder().encode(encryptedData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

    /**
     * 公钥解密过程
     *
     * @param publicKey 私钥
     * @param data 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decryptLong(String publicKey, String data) throws Exception {
        RSAPublicKey rsaPublicKey=loadPublicKey(publicKey);
        if (publicKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
            byte[] bytes=new BASE64Decoder().decodeBuffer(data);
            int inputLen=bytes.length;
            int offLen=0;
            int i=0;
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            while (inputLen-offLen>0){
                byte[] cache;
                if(inputLen-offLen>128){
                    cache=cipher.doFinal(bytes,offLen,128);
                }else{
                    cache=cipher.doFinal(bytes,offLen,inputLen-offLen);
                }
                byteArrayOutputStream.write(cache);
                i++;
                offLen=128*i;
            }
            byteArrayOutputStream.close();
            byte[] byteArray=byteArrayOutputStream.toByteArray();
            return new String(byteArray);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

}
