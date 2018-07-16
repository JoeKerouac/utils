package com.joe.utils.secure;

import com.joe.utils.secure.impl.AsymmetricCipher;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.07.16 13:56
 */
public class KeyToolsTest {
    @Test
    public void doBuildKey() {
        KeyTools.KeyHolder holder = KeyTools.buildRSAKey(2048);
        CipherUtil cipher = AsymmetricCipher.buildInstance(holder.getPrivateKey(), holder.getPublicKey());
        CipherUtilTest.checkCipher(cipher);
    }
}
