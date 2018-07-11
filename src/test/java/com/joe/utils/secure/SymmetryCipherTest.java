package com.joe.utils.secure;

import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.07.11 20:59
 */
public class SymmetryCipherTest {
    private SymmetryCipher cipher;

    @Before
    public void init() {
        cipher = new SymmetryCipher(AbstractCipher.Algorithms.DES, "123123123123123123", 100);
    }

    @Test
    public void doCipher() {
        CipherUtilTest.checkCipher(cipher);
    }
}
