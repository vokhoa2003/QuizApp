/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.taskmanager.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Sử dụng CBC mode với padding

    // Tạo khóa động dựa trên email người dùng
    private static SecretKey getSecretKey(String userEmail) throws Exception {
        String baseKey = "TaskManagerBaseKey"; // Khóa cơ sở (có thể lấy từ biến môi trường)
        String dynamicKey = baseKey + (userEmail != null ? userEmail : "default_user");
        
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(dynamicKey.getBytes(StandardCharsets.UTF_8));
        key = Arrays.copyOf(key, 16); // AES-128 cần 16 byte
        return new SecretKeySpec(key, ALGORITHM);
    }

    // Tạo IV ngẫu nhiên
    private static byte[] generateIV() {
        byte[] iv = new byte[16]; // AES cần IV 16 byte
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static String encrypt(String data, String userEmail) {
        try {
            SecretKey secretKey = getSecretKey(userEmail);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Tạo IV và khởi tạo cipher
            byte[] iv = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Mã hóa dữ liệu
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Kết hợp IV và dữ liệu mã hóa (IV được lưu trữ để giải mã sau)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            System.err.println("Error during encryption: " + e.getMessage());
            return null;
        }
    }

    public static String decrypt(String encryptedData, String userEmail) {
        try {
            SecretKey secretKey = getSecretKey(userEmail);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Giải mã dữ liệu: tách IV và dữ liệu mã hóa
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            byte[] iv = Arrays.copyOfRange(combined, 0, 16); // IV là 16 byte đầu
            byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);

            // Khởi tạo cipher với IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Giải mã
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            System.err.println("Error during decryption: " + e.getMessage());
            return null;
        }
    }
}