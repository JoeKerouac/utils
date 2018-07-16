package com.joe.utils.secure;

import org.junit.Test;

import com.joe.utils.secure.impl.AsymmetricCipher;

/**
 * @author joe
 * @version 2018.07.16 13:56
 */
public class KeyToolsTest {
    @Test
    public void doBuildKey() {
        KeyTools.KeyHolder holder = KeyTools.buildRSAKey(2048);
        CipherUtil cipher = AsymmetricCipher.buildInstance(holder.getPrivateKey(),
            holder.getPublicKey());
        CipherUtilTest.checkCipher(cipher);
    }
}
