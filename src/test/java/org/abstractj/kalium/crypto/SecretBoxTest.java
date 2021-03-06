/**
 * Copyright 2013 Bruno Oliveira, and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.abstractj.kalium.crypto;

import org.junit.Test;

import java.util.Arrays;

import static org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES;
import static org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES;
import static org.abstractj.kalium.NaCl.sodium;
import static org.abstractj.kalium.encoders.Encoder.HEX;
import static org.abstractj.kalium.fixture.TestVectors.BOX_CIPHERTEXT;
import static org.abstractj.kalium.fixture.TestVectors.BOX_MESSAGE;
import static org.abstractj.kalium.fixture.TestVectors.BOX_NONCE;
import static org.abstractj.kalium.fixture.TestVectors.SECRET_KEY;
import static org.junit.Assert.*;

public class SecretBoxTest {

    @Test
    public void testAcceptStrings() throws Exception {
        try {
            new SecretBox(SECRET_KEY, HEX);
        } catch (Exception e) {
            fail("SecretBox should accept strings");
        }
    }

    @Test(expected = RuntimeException.class)
    public void testNullKey() throws Exception {
        byte[] key = null;
        new SecretBox(key);
        fail("Should raise an exception");
    }

    @Test(expected = RuntimeException.class)
    public void testShortKey() throws Exception {
        String key = "hello";
        new SecretBox(key.getBytes());
        fail("Should raise an exception");
    }

    @Test
    public void testEncrypt() throws Exception {
        SecretBox box = new SecretBox(SECRET_KEY, HEX);

        byte[] nonce = HEX.decode(BOX_NONCE);
        byte[] message = HEX.decode(BOX_MESSAGE);
        byte[] ciphertext = HEX.decode(BOX_CIPHERTEXT);

        byte[] result = box.encrypt(nonce, message);
        assertTrue("failed to generate ciphertext", Arrays.equals(result, ciphertext));
    }

    @Test
    public void testDecrypt() throws Exception {

        SecretBox box = new SecretBox(SECRET_KEY, HEX);

        byte[] nonce = HEX.decode(BOX_NONCE);
        byte[] expectedMessage = HEX.decode(BOX_MESSAGE);
        byte[] ciphertext = box.encrypt(nonce, expectedMessage);

        byte[] message = box.decrypt(nonce, ciphertext);

        assertTrue("failed to decrypt ciphertext", Arrays.equals(message, expectedMessage));
    }

    @Test(expected = RuntimeException.class)
    public void testDecryptCorruptedCipherText() throws Exception {
        SecretBox box = new SecretBox(SECRET_KEY, HEX);
        byte[] nonce = HEX.decode(BOX_NONCE);
        byte[] message = HEX.decode(BOX_MESSAGE);
        byte[] ciphertext = box.encrypt(nonce, message);
        ciphertext[23] = ' ';

        box.decrypt(nonce, ciphertext);
        fail("Should raise an exception");
    }

    @Test
    public void testWithGeneratedKeyAndNonce() {
        byte[] key = new byte[CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES];
        int result = sodium().crypto_secretbox_keygen(key);

        assertEquals(result, CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES);

        byte[] nonce = new byte[CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES];
        sodium().randombytes(nonce, nonce.length);

        SecretBox box = new SecretBox(key);

        byte[] cypherText = box.encrypt(nonce, "secret message".getBytes());
        String message = new String(box.decrypt(nonce, cypherText));

        assertEquals("secret message", message);
    }

}
