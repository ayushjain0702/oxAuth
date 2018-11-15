package org.xdi.oxauth.comp;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import org.apache.commons.codec.Charsets;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.Test;
import org.xdi.oxauth.model.crypto.encryption.BlockEncryptionAlgorithm;
import org.xdi.oxauth.model.crypto.encryption.KeyEncryptionAlgorithm;
import org.xdi.oxauth.model.jwe.Jwe;
import org.xdi.oxauth.model.jwe.JweDecrypterImpl;
import org.xdi.oxauth.model.jwe.JweEncrypterImpl;
import org.xdi.oxauth.model.jwt.JwtType;
import org.xdi.oxauth.model.util.Base64Util;

import java.security.Security;

import static org.junit.Assert.assertTrue;

public class CrossEncryptionTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    final String encryptedJweProducedByGluu = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkExMjhHQ00iLCJraWQiOiIyIn0.M9YXhzGlMBxJRFjpIZ3ybfNODALPz_08WADIpWSLHOoCBdwqPWQ3fwDf-uaiw7wyTTf9piuKVUOeYHnPE6C_EmS9gj5fmckHBCHcNxZanobT0QXZdy-64wb4GK3ar66lPPFnJMVLLCqZfUjB1gHxmAcwrVJQTUPO0ogk2nZCujp4mOuJ0QnOQmJ0R1rHTjbYmKBDySIavmkXosoJaLZI4N1CltCKj66P_XKYLfgAE0yevuwtNxkkRc2EGMyPpZ8pVjBL5TPQF3b5AyAstUvB4l6o90JZQLzvAdHJyGuCW1zwzGPBtVBVYvb2vBBAuj7EPKDU9UQDuDoklwj5Hwc6wg.qBM-41MJ46_eUv4I.mak_e28_onSOODjdH06wWuA0MfJMTGConWSekPIArQoFKAgcxVRvg-JNqjaBFaG4ck8cp0ViAke_Cbfl4AyN-gAFI2pqEMiXkoEB193SyD6Yev0P1zKTJORWS6tpznYAGYgIPh_rWyWPFSdT1WPB7Qgzarf-JNYrNe5H_P8JRrArWyCEJx4w6_WLcGnM1EQQPkThoYC4utS47W0OHf2SNr-PRUhCeoEIuoMaQUmjYq386BjCWhQEoQCZNftUjUXZBq8MepW92v1spNLCb7NTEJ1p3s45KIVwPt5qnXI6-ouQE4_KFXVNe5-SSfyzrEf1jxTyerNqlU5bIZ0v4aPS6i3bXSSHIfgyvrFCzDPq9x-5B98OI0sVDKxzzp7UWjqEjjmuQbdN4eGZUtSGYcWNFI29vl4Pr8HvqMjnQaaEtGZeX_nJG27xzlwlD1pI_rjO_QMAQpfbNuxLm5-HhB0fZOngjNAnOhipyY_tTMMtiWLmoJUuicwTTSpERC_9ny8tnsiyCOEJEyeZFEzh52jfox_WHLVkIrjCUCYtTwCvuYdtu4Sgl-WPCa2y-4uF7u2DcIKdIRRMdjgE1RNUAp-W2ui8PDrIaSVxkWbuLQJ2oXEyWN8gFHEZPko-n80IjGG8Si3Qh1kum_vO9Ub7AiIm0pk65ph_CQH0BSVSLwN-e4iAd1C6h_J2O-aGEKWKrvvRC31ApCr5RkOdaKTAYVGUKQSMBdqucq47JbBynP7dqE0Kxl3miBo_dyYXCim9Gw.DSoXCEJ7-uT7Xv7eb3g-7A";

    final String senderJwkJson = "{\"kty\":\"RSA\",\"d\":\"iSx-zxihgOITpEhz6WwGiiCZjxx597wqblhSYgFWa_bL9esLY3FT_Kq9sdvGPiI8QmObRxPZuTi4n3BVKYUWcfjVz3swq7VmESxnJJZE-vMI9NTaZ-CT2b4I-c3qwAsejhWagJf899I3MRtPOnyxMimyOw4_5YYvXjBkXkCMfCsbj5TBR3RbtMrUYzDMXsVT1EJ_7H76DPBFJx5JptsEAA17VMtqwvWhRutnPyQOftDGPxD-1aGgpteKOUCv7Lx-mFX-zV6nnPB8vmgTgaMqCbCFKSZI567p714gzWBkwnNdRHleX8wos8yZAGbdwGqqUz5x3iKKdn3c7U9TTU7DAQ\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"1\",\"alg\":\"RS256\",\"n\":\"i6tdK2fREwykTUU-qkYkiSHgg9B31-8EjVCbH0iyrewY9s7_WYPT7I3argjcmiDkufnVfGGW0FadtO3br-Qgk_N2e9LqGMtjUoGMZKFS3fJhqjnLYDi_E5l2FYU_ilw4EXPsZJY0CaM7BxjwUBoCjopYrgvtdxA9G6gpGoAH4LopAkgX-gkawVLpB4NpLvA09FLF2OlYZL7aaybvM2Lz_IXEPa-LSOwLum80Et-_A1-YMx_Z767Iwl1pGTpgZ87jrDD1vEdMdiLcWFG3UIYAAIxtg6X23cvQVLMaXKpyV0USDCWRJrZYxEDgZngbDRj3Sd2-LnixPkMWAfo_D9lBVQ\"}";

    final String recipientJwkJson = "{\"kty\":\"RSA\",\"d\":\"jAFM0c4oXxh5YcEujZRVY5LNUzkm0OZf8OUZ31DockQE07BwSAsi4_y6vursS4Z74EurjYlfPx7WoZZokTLyBReVvG8XQZ-AQ5smU9gXQrsiVdU2kOp17oYnOP3OKc0HtvlfTPKdz0DhoA--wAsPFCL2ei4Qly_J3IQTF9ffJJMEyzgabcV1xqrk8NEK5XfEHOdNHzzg-doRe4lCsDcEfIppCIxPHTozhYpwH0_OrssAX1OwX5Jx6-5pXc_BIBrymIkjfwlPYBC32f0iD6VTntJfIngMOdeu0t6krOaWlbfmf6RdoM5sugT-j3mYnd3w4c2eFW23Z9sPCrQvDNlTcQ\",\"e\":\"AQAB\",\"use\":\"enc\",\"kid\":\"2\",\"alg\":\"RS256\",\"n\":\"oaPsFKHgVnK0d04rjN5GgZFqCh9HwYkLMdDQDIgkM3x4sxTpctS5NJQK7iKWNxPTtULdzrY6NLqtrNWmIrJFC6f2h4q5p46Kmc8vdhm_Ph_jpYfsXWTdsHAoee6iJPMoie7rBGoscr3y2DdNlyxAO_jHLUkaaSAqDQrH_f4zVTO0XKisJu8DxKoh2U8myOow_kxx4PUxEdlH6XclpxYT5lIZijOZ8wehFad_BAJ2iZM40JDoqOgspUF1Jyq7FjOoMQabYYwDMyfs2rEALcTU1UsvLeWbl95T3mdAw64Ux3uFCZzHdXF4IDr7xH4NrEVT7SMAlwNoaRfmFbtL-WoISw\"}";
    public static final String PAYLOAD = "{\"iss\":\"https:devgluu.saminet.local\",\"sub\":\"testing\"}";

    @Test
    public void testNimbusJoseJwt_first() {

        //jwe produced by gluu 3.1.2 in development environment
        assertTrue(testDecryptNimbusJoseJwt(encryptedJweProducedByGluu));
    }

    @Test
    public void testNimbusJoseJwt_second() {

        //jwe produced by Gluu JweEncrypter
        assertTrue(testDecryptNimbusJoseJwt(encryptWithGluuJweEncrypter()));
    }

    @Test
    public void testNimbusJoseJwt_third() {

        //jwe produced by Nimbus Jose+JWT
        assertTrue(testDecryptNimbusJoseJwt(encryptWithNimbusJoseJwt()));
    }

    @Test
    public void testNimbusJose4J_first() {

        //jwe produced by gluu 3.1.2 in development environment
        assertTrue(testDecryptWithJose4J(encryptedJweProducedByGluu));
    }

    @Test
    public void testNimbusJose4J_second() {

        //jwe produced by Gluu JweEncrypter
        assertTrue(testDecryptWithJose4J(encryptWithGluuJweEncrypter()));
    }

    @Test
    public void testNimbusJose4J_third() {

        //jwe produced by Nimbus Jose+JWT
        assertTrue(testDecryptWithJose4J(encryptWithNimbusJoseJwt()));
    }

    @Test
    public void testGluuJweDecrypter_first() {
        String str = encryptWithNimbusJoseJwt();
        System.out.println(str);
        System.out.println(encryptedJweProducedByGluu);

        //jwe produced by gluu 3.1.2 in development environment
        assertTrue(testDecryptWithGluuDecrypter(encryptedJweProducedByGluu));
    }

    @Test
    public void testGluuJweDecrypter_second() {

        //jwe produced by Gluu JweEncrypter
        assertTrue(testDecryptWithGluuDecrypter(encryptWithGluuJweEncrypter()));
    }

    @Test
    public void testGluuJweDecrypter_third() {

        //jwe produced by Nimbus Jose+JWT
        assertTrue(testDecryptWithGluuDecrypter(encryptWithNimbusJoseJwt()));
    }

    private boolean testDecryptNimbusJoseJwt(String jwe) {

        try {
            EncryptedJWT encryptedJwt = EncryptedJWT.parse(jwe);
            //EncryptedJWT encryptedJwt = EncryptedJWT.parse(encryptWithGluu());
            //EncryptedJWT encryptedJwt = EncryptedJWT.parse(encryptWithNimbus());

            JWK jwk = JWK.parse(recipientJwkJson);
            java.security.interfaces.RSAPrivateKey rsaPrivateKey = ((RSAKey) jwk).toRSAPrivateKey();

            JWEDecrypter decrypter = new RSADecrypter(rsaPrivateKey);
            decrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());

            encryptedJwt.decrypt(decrypter);
            final String decryptedPayload = new String(Base64Util.base64urldecode(encryptedJwt.getPayload().toString()));
            System.out.println("Nimbusds decrypt succeed: " + decryptedPayload);
            if (decryptedPayload.equals(PAYLOAD)) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Nimbusds decrypt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean testDecryptWithJose4J(String jwe) {

        try {

            PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(recipientJwkJson);

            JsonWebEncryption receiverJwe = new JsonWebEncryption();

            AlgorithmConstraints algConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, KeyManagementAlgorithmIdentifiers.RSA_OAEP);
            receiverJwe.setAlgorithmConstraints(algConstraints);
            AlgorithmConstraints encConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, ContentEncryptionAlgorithmIdentifiers.AES_128_GCM);
            receiverJwe.setContentEncryptionAlgorithmConstraints(encConstraints);

            receiverJwe.setKey(jwk.getPrivateKey());

            receiverJwe.setCompactSerialization(jwe);
            final String decryptedPayload = new String(Base64Util.base64urldecode(receiverJwe.getPlaintextString()));
            System.out.println("Jose4j decrypt succeed: " + decryptedPayload);
            if (decryptedPayload.equals(PAYLOAD)) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Jose4j decrypt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean testDecryptWithGluuDecrypter(String jwe) {

        try {
            JWK jwk = JWK.parse(recipientJwkJson);
            java.security.interfaces.RSAPrivateKey rsaPrivateKey = ((RSAKey) jwk).toRSAPrivateKey();

            JweDecrypterImpl decrypter = new JweDecrypterImpl(rsaPrivateKey);

            decrypter.setKeyEncryptionAlgorithm(KeyEncryptionAlgorithm.RSA_OAEP);
            decrypter.setBlockEncryptionAlgorithm(BlockEncryptionAlgorithm.A128GCM);
            System.out.println("Gluu decrypt succeed: " + decrypter.decrypt(jwe).getClaims().toJsonString().toString());
            return true;
        } catch (Exception e) {
            System.out.println("Gluu decrypt failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String encryptWithGluuJweEncrypter() {

        try {
            RSAKey recipientPublicJWK = (RSAKey) (JWK.parse(recipientJwkJson));

            BlockEncryptionAlgorithm blockEncryptionAlgorithm = BlockEncryptionAlgorithm.A128GCM;
            KeyEncryptionAlgorithm keyEncryptionAlgorithm = KeyEncryptionAlgorithm.RSA_OAEP;
            Jwe jwe = new Jwe();
            jwe.getHeader().setType(JwtType.JWT);
            jwe.getHeader().setAlgorithm(keyEncryptionAlgorithm);
            jwe.getHeader().setEncryptionMethod(blockEncryptionAlgorithm);
            jwe.getClaims().setIssuer("https:devgluu.saminet.local");
            jwe.getClaims().setSubjectIdentifier("testing");
            jwe.getHeader().setKeyId("1");

            JweEncrypterImpl encrypter = new JweEncrypterImpl(keyEncryptionAlgorithm, blockEncryptionAlgorithm, recipientPublicJWK.toPublicKey());
            jwe = encrypter.encrypt(jwe);
            //		System.out.println("EncodedHeader: " + jwe.getEncodedHeader());
            //		System.out.println("EncodedEncryptedKey: " + jwe.getEncodedEncryptedKey());
            //		System.out.println("EncodedInitializationVector: " + jwe.getEncodedInitializationVector());
            //		System.out.println("EncodedCiphertext: " + jwe.getEncodedCiphertext());
            //		System.out.println("EncodedIntegrityValue: " + jwe.getEncodedIntegrityValue());
            return jwe.toString();
        } catch (Exception e) {
            System.out.println("Error encryption with Gluu JweEncrypter: " + e.getMessage());
            return null;
        }
    }

    private String encryptWithNimbusJoseJwt() {

        try {
            RSAKey senderJWK = (RSAKey) JWK.parse(senderJwkJson);

            RSAKey recipientPublicJWK = (RSAKey) (JWK.parse(recipientJwkJson));

            // Create JWT
//			SignedJWT signedJWT = new SignedJWT(
//			    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(senderJWK.getKeyID()).build(),
//			    new JWTClaimsSet.Builder()
//			        .subject("testi")
//			        .issuer("https:devgluu.saminet.local")
//			        .build());

            // Sign the JWT
            //		signedJWT.sign(new RSASSASigner(senderJWK));

            // Create JWE object with signed JWT as payload
            //		JWEObject jweObject = new JWEObject(
            //		    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM)
            //		        .contentType("JWT") // required to indicate nested JWT
            //		        .build(),
            //		    new Payload(signedJWT));

            @SuppressWarnings("deprecation")
            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM)
                            .type(JOSEObjectType.JWT)
                            .keyID(senderJWK.getKeyID())
                            .build(),
                    new Payload(Base64Util.base64urlencode(PAYLOAD.getBytes(Charsets.UTF_8))));


            // Encrypt with the recipient's public key
            RSAEncrypter encrypter = new RSAEncrypter(recipientPublicJWK);
            jweObject.encrypt(encrypter);

            //		System.out.println("Header: " +  jweObject.getHeader());
            //		System.out.println("Encrypted Key: " + jweObject.getEncryptedKey());
            //		System.out.println("Cipher Text: " + jweObject.getCipherText());
            //		System.out.println("IV: " + jweObject.getIV());
            //		System.out.println("Auth Tag: " + jweObject.getAuthTag());


            // Serialise to JWE compact form
            return jweObject.serialize();
        } catch (Exception e) {
            System.out.println("Error encryption with Nimbus: " + e.getMessage());
            return null;
        }
    }
}
