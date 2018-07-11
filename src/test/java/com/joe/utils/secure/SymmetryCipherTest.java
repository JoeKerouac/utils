package com.joe.utils.secure;

import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.07.11 20:59
 */
public class SymmetryCipherTest {
    private SymmetryCipher aesCipher;
    private SymmetryCipher desCipher;

    @Before
    public void init() {
        desCipher = new SymmetryCipher(AbstractCipher.Algorithms.DES, "123123123123123123");
        aesCipher = new SymmetryCipher(AbstractCipher.Algorithms.AES, "123123123123123123" , 128);
    }

    @Test
    public void doAesCipher() {
        CipherUtilTest.checkCipher(aesCipher);
    }

    @Test
    public void doDesCipher() {
        CipherUtilTest.checkCipher(desCipher);
    }
}
