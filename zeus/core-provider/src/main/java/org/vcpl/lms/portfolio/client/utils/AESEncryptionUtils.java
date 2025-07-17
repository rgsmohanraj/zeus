package org.vcpl.lms.portfolio.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;

public class AESEncryptionUtils{

    private static final Logger LOG=LoggerFactory.getLogger(AESEncryptionUtils.class);
    public static String IV_PARAMETER;
    public static String PASSWORD;
    public static String SALT;
    public static String PRIVATE_KEY;
    private static final String ALGORITHM="AES/CBC/PKCS5Padding";


    public static String encryptWithKeyAndIv(String input)throws NoSuchPaddingException,NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,InvalidKeyException,
            BadPaddingException,IllegalBlockSizeException,InvalidKeySpecException{
        Cipher cipher=Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,getKeyFromPassword(PASSWORD,SALT),createIvParameter(IV_PARAMETER));
        byte[]cipherText=cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decryptWithKeyAndIv(String cipherText){
        if(Objects.isNull(cipherText))return null;
        String originalText=null;
        try{
            Cipher cipher=Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,getKeyFromPassword(PASSWORD,SALT),createIvParameter(IV_PARAMETER));
            byte[]plainText=cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            originalText=new String(plainText);
        }catch(Exception exception){
            LOG.error("Exception:"+exception.getMessage());
        }
        return originalText;
    }

    public static SecretKey generateKey(int n)throws NoSuchAlgorithmException{
        KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static SecretKey getKeyFromPassword(String password,String salt)
    throws NoSuchAlgorithmException,InvalidKeySpecException{
        SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec=new PBEKeySpec(password.toCharArray(),salt.getBytes(),65536,256);
        return new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(),"AES");
    }

    public static IvParameterSpec createIvParameter(String iv){
        return new IvParameterSpec(iv.getBytes());
    }

    public static String encryptWithKey(String data)throws IllegalBlockSizeException,
            BadPaddingException,InvalidKeyException,NoSuchPaddingException,
            NoSuchAlgorithmException{
        Key key=generateKey(PRIVATE_KEY.getBytes());
        Cipher c=Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE,key);
        return Base64.getEncoder()
                .encodeToString(c.doFinal(data.getBytes()));
    }
    public static String decryptWithKey(String encryptedData){
        String decryptedValue="";
        try{
            Key key=generateKey(PRIVATE_KEY.getBytes());
            Cipher c=Cipher.getInstance("AES");//UseALGORITHM
            c.init(Cipher.DECRYPT_MODE,key);
            byte[]decodedValue=Base64.getDecoder().decode(encryptedData);
            byte[]decValue=c.doFinal(decodedValue);
            decryptedValue=new String(decValue);
        }
        catch(Exception exception){
            LOG.error("Exception:"+exception.getMessage());
        }
        return decryptedValue;
    }
    private static Key generateKey(byte[]value){
        return new SecretKeySpec(value,"AES");
    }
    public static String decrypt(String encryptedData,Boolean isSalt) {
        try {
            return (isSalt) ? decryptWithKeyAndIv(encryptedData) : decryptLoginCredential(encryptedData);
        }catch (Exception e)
        {
            throw new RuntimeException("Error occured in decryption");
        }
    }
    public static String decryptLoginCredential(String credentials) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] encryptedBytes = Base64.getDecoder().decode(credentials);
        SecretKeySpec key = new SecretKeySpec(PRIVATE_KEY.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes());
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key,iv);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

}
