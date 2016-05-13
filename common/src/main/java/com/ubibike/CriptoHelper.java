package com.ubibike;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class CriptoHelper {

//        private static final String path = "../common/src/keys";
        private static final String path = "./keys";


        public static byte[] convertArrayListByteToByteArray(ArrayList<byte[]> list) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            for (byte[] element : list) {
                try {
                    out.write(element, 0, element.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return baos.toByteArray();
        }
//
//        public static Pk_t priKeyToPk (PrivateKey priv) {
//            return new Pk_t(priv.getEncoded());
//        }
//
//        public static Pk_t pubKeyToPk (PublicKey pub) {
//            return new Pk_t(pub.getEncoded());
//        }
//
//
//        public static byte[] sign(byte[] privateKey, byte[] blockPayload) throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
//
//            PrivateKey pkey = convertPrivateFromByteArray(privateKey.getPayload());
//
//            Signature signature = Signature.getInstance("SHA1withDSA");
//            signature.initSign(pkey);
//            signature.update(blockPayload);
//            return signature.sign();
//
//        }

        public static boolean verifySignature(byte[] publicKey, byte[] blockPayload, byte[] signature) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {


            byte[] digitalSignature = signature;

            PublicKey pubKey = convertPublicFromByteArray(publicKey);
            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(pubKey);

            byte[] bytes = blockPayload;
            sig.update(bytes);

            if (!sig.verify(digitalSignature))
            {
                System.out.println("Blocks were tempered with!");
                return false;
//                throw new FalseSignatureException("Blocks were tempered with!");
            } else
                return true;
        }

//    public static String EncryptByteArrayWithPriv(byte[] array, PrivateKey privKey) throws Exception
//    {
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        SecretKeySpec secretKey = new SecretKeySpec(privKey.getEncoded(), "AES");
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        String encryptedString = Base64.getEncoder().encodeToString(cipher.doFinal(array));
//        return encryptedString;
//    }
//
//    public static byte[] decryptByteArrayWithPub(String strToDecrypt, PublicKey pubKey) throws Exception
//    {
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
//        SecretKeySpec secretKey = new SecretKeySpec(pubKey.getEncoded(), "AES");
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//        return cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
//    }
//
//        public static String encryptByteArrayWithPub(byte[] array, PublicKey pubKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
//            // specify mode and padding instead of relying on defaults (use OAEP if available!)
//            Cipher encrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            // init with the *public key*!
//            encrypt.init(Cipher.ENCRYPT_MODE, pubKey);
//            // encrypt with known character encoding, you should probably use hybrid cryptography instead
////        byte[] encryptedMessage = encrypt.doFinal(msg.getBytes(StandardCharsets.UTF_8));
//            String encryptedString = Base64.encodeToString(encrypt.doFinal(array),1);
//
//            return encryptedString;
//        }
//
//        public static byte[] decryptByteArrayWithPriv(String strToDecrypt, PrivateKey privKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
//            SecretKeySpec secretKey = new SecretKeySpec(privKey.getEncoded(), "AES");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//            return cipher.doFinal(Base64.decode(strToDecrypt, 1));
//        }


//    public static boolean verifyMetadataSignature(Pk_t publicKey, ArrayList<byte[]> blockPayload, byte[] signature) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
//
//
//        PublicKey pubKey = convertPublicFromByteArray(publicKey.getPayload());
//        byte[] bytes =
//
//
//        //getBlockId(blockPayload);
//        //decript signature
//        Signature verifyalg = Signature.getInstance("SHA256withRSA");
//        verifyalg.initVerify(pubKey);
//
//
//
//        return !verifyalg.verify(signature);
//
//    }

        public static byte[] getBlockId(byte[] blockPayload) throws NoSuchAlgorithmException {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(blockPayload);
            return messageDigest.digest();
        }

        public static PrivateKey convertPrivateFromByteArray(byte[] payload) throws NoSuchAlgorithmException, InvalidKeySpecException {

            KeyFactory kf = KeyFactory.getInstance("DSA");
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(payload);
            PrivateKey pp= kf.generatePrivate(ks);
            return pp;
        }

        public static PublicKey convertPublicFromByteArray(byte[] payload) throws NoSuchAlgorithmException, InvalidKeySpecException {

            KeyFactory kf = KeyFactory.getInstance("DSA");
            X509EncodedKeySpec ks = new X509EncodedKeySpec(payload);
            return kf.generatePublic(ks);
        }

//        public static String encode64(byte[] bytes) {
//
////        byte[] encoded = Base64.getEncoder().encode(bytes);
//// encode with padding
//            String encoded = Base64.encodeToString(bytes,1);
//
//            return(new String(encoded));
//
//        }
//
//        public static byte[] decode64 (String str) {
//
//            byte[] decoded = Base64.decode(str.getBytes(), 1);
//
//            return decoded;
//        }

        // TODO: 05/05/16 gerar chaves antes, implementar funçoes depois
        public static byte[] getMac(int timeStamp, byte[] sharedKey) throws UnsupportedEncodingException {

            String algo = "HmacSHA1";
            String digest = null;
            byte[] bytes = null;

            try {
                SecretKeySpec key = new SecretKeySpec(sharedKey, algo);
                Mac mac = Mac.getInstance(algo);
                mac.init(key);

                bytes = mac.doFinal(String.valueOf(timeStamp).getBytes());
//
//            StringBuffer hash = new StringBuffer();
//            for (int i = 0; i < bytes.length; i++) {
//                String hex = Integer.toHexString(0xFF & bytes[i]);
//                if (hex.length() == 1) {
//                    hash.append('0');
//                }
//                hash.append(hex);
//            }
//            digest = hash.toString();
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return bytes;

        }

        public static boolean verifyMac(int timeStamp, byte[] sharedKey ,byte[] receivedHmac) throws UnsupportedEncodingException {

            byte[] correctHmac = getMac(timeStamp, sharedKey);
            // MessageDiges.isEqual is the most secure way that I found to compare byte arrays
            boolean result = MessageDigest.
                    isEqual(correctHmac,receivedHmac);

            return result;
        }

        public static byte[] generateSimKey(String serverName) throws NoSuchAlgorithmException, IOException {


            //Generate Symmetric key
            KeyGenerator generator = null;
            generator = KeyGenerator.getInstance("AES");

            generator.init(128);
            SecretKey key = generator.generateKey();
            byte[] symmetricKey =key.getEncoded();

            FileOutputStream fos = new FileOutputStream(path + "/secret" + "_" + serverName + ".key");
            fos.write(symmetricKey);
            fos.close();


            return  symmetricKey;
            // TODO: 05-May-16 store on file

        }

        public static byte[] getSimKey(String serverName) throws IOException {

            File simKeyFile = new File(path + "/secret" + "_" + serverName + ".key");
            FileInputStream fis = new FileInputStream(path + "/secret" + "_" + serverName + ".key");
            byte[] simKeyByte = new byte[(int) simKeyFile.length()];

            // TODO: 05-May-16 fis.read e close nao usados
            fis.read(simKeyByte);
            fis.close();

            return simKeyByte;
        }
//
//    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException, NoSuchProviderException {
//        Keyz keyPair = new Keyz();
//        KeyPair pair = keyPair.LoadKeyPair("Joana");
//
//        byte[] data = new byte[1024];
//        for(int i=0;i<1024;i++){
//            char k= (char) ('A'+i);
//            data[i]= (byte) k;
//        }
//
//        PublicKey pubKey = pair.getPublic();
//        PrivateKey privKey = pair.getPrivate();
////
////        Pk_t priv = priKeyToPk(privKey);
////        Pk_t pub = pubKeyToPk(pubKey);
////
////        Tests convertPrivate and Pub from Byte array funcs
//        PrivateKey newPrivKey = convertPrivateFromByteArray(privKey.getEncoded());
//        System.out.println(privKey.equals(newPrivKey));
//
//        PublicKey newPubKey = convertPublicFromByteArray(pubKey.getEncoded());
//        System.out.println(pubKey.equals(newPubKey));
//
//
//        byte[] signature = sign(privKey.getEncoded(),data);
//        System.out.println(verifySignature(pubKey.getEncoded(),data,signature));
//
//    }



    // De Sistemas Distribuios

    public static PrivateKey getPrivateKey(String privateKeyFile) throws IOException, GeneralSecurityException {
        byte[] privEncoded = readFile(privateKeyFile);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFacPriv.generatePrivate(privSpec);
        return privKey;
    }

    public static PublicKey getPublicKey(String publicKeyFile) throws IOException, GeneralSecurityException {
        byte[] pubEncoded = readFile(publicKeyFile);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFacPub.generatePublic(pubSpec);
        return pubKey;
    }

    private static byte[] readFile(String path)	throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }


    // Digital Signature

    public static boolean verifyDigitalSignature(byte[] cipherDigest,
                                                 byte[] text,
                                                 PublicKey pubKey) throws Exception {

        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        // calculate the digest and print it out
        messageDigest.update(text);
        byte[] digest = messageDigest.digest();


        // compare digests
        if (digest.length != cipherDigest.length)
            return false;

        for (int i=0; i < digest.length; i++)
            if (digest[i] != cipherDigest[i])
                return false;
        return true;
    }

    public static byte[] makeDigitalSignature(byte[] bytes,
                                              PrivateKey priv) throws Exception {

        // get a message digest object using the MD5 algorithm
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        // calculate the digest and print it out
        messageDigest.update(bytes);
        byte[] digest = messageDigest.digest();

        // get an RSA cipher object
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        // encrypt the plaintext using the private key
        cipher.init(Cipher.ENCRYPT_MODE, priv);
        byte[] cipherDigest = cipher.doFinal(digest);

        return cipherDigest;
    }


    // Assimetric Keys

    private static byte[] AsymCrypto(PrivateKey priv, byte[] msgByteArray, String encryptWay) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        if(encryptWay.equals("ENCRYPT")){
            cipher.init(Cipher.ENCRYPT_MODE, priv);
        }else{
            cipher.init(Cipher.DECRYPT_MODE, priv);
        }
        byte[] cipherBytes = cipher.doFinal(msgByteArray);
        return cipherBytes;

    }

    private static byte[] AsymCrypto(PublicKey pub, byte[] msgByteArray, String encryptWay) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        if(encryptWay.equals("ENCRYPT")){
            cipher.init(Cipher.ENCRYPT_MODE, pub);
        }else{
            cipher.init(Cipher.DECRYPT_MODE, pub);
        }
        byte[] cipherBytes = cipher.doFinal(msgByteArray);
        return cipherBytes;
    }

    private static byte[] SymCrypto(Key secretKey, byte[] msgByteArray, String encryptWay) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException{
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        if(encryptWay.equals("ENCRYPT")){
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,ivspec);
        }else{
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        }
        byte[] cipherBytes = cipher.doFinal(msgByteArray);
        return cipherBytes;
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException{
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey key = keyGen.generateKey();
        return key;
    }

    public static String getKeyFileName(String myName, boolean isPrivate) {
        return "keys/" + (isPrivate ? "private" : "public") + "_" + myName + ".key";
    }
}
