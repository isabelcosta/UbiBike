import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

/**
 * Created by boss on 09/03/16.
 */
public class Keyz {

    public static int numberOfClients;
    private static final String path = "common/src/keys";

    public void SaveKeyPair(KeyPair keyPair, int i) throws IOException {
        int client = i;
        String path = Keyz.path;
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
                publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(path + "/public"+"_"+client+".key");
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        fos = new FileOutputStream(path + "/private"+"_"+client+".key");
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
        numberOfClients++;
    }

    public static ArrayList<byte[]> getPublicKeysList() throws IOException {
        ArrayList<byte[]> keylist = new ArrayList<>();
        int client = Keyz.numberOfClients;
        String path = Keyz.path;

        for(int i=0;i<client;i++) {
            File filePublicKey = new File(path + "/public" + "_" + client + ".key");
            FileInputStream fis = new FileInputStream(path + "/public" + "_" + client + ".key");
            byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
            keylist.add(encodedPublicKey);
            fis.read(encodedPublicKey);
            fis.close();
        }

        return keylist;
    }

    public KeyPair LoadKeyPair( int client)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {
//        int client = Keyz.numberOfClients;
        String path = Keyz.path;
        // Read Public Key.
        File filePublicKey = new File(path + "/public"+"_"+client+".key");
        FileInputStream fis = new FileInputStream(path + "/public"+"_"+client+".key");
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();

        // Read Private Key.
        File filePrivateKey = new File(path + "/private"+"_"+client+".key");
        fis = new FileInputStream(path + "/private"+"_"+client+".key");
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        fis.close();

        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
                encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
                encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair justGiveTheKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");

        keyGen.initialize(1024);
        KeyPair generatedKeyPair = keyGen.genKeyPair();

        return generatedKeyPair;
    }


//    public static void main(String args[]) throws NoSuchAlgorithmException, IOException {
//        Keyz jo = new Keyz();
//        KeyPair ii = jo.justGiveTheKeys();

    public static void main(String args[]) throws NoSuchAlgorithmException, IOException {
        Keyz jo = new Keyz();
        KeyPair ii = jo.justGiveTheKeys();

        for(int i=0;i<5;i++){
            ii = jo.justGiveTheKeys();
            jo.SaveKeyPair(ii,i);
        }
    }


}
