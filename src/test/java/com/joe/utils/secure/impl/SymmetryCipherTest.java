package com.joe.utils.secure.impl;

import org.junit.Before;
import org.junit.Test;

import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.CipherUtilTest;

/**
 * @author joe
 * @version 2018.07.11 20:59
 */
public class SymmetryCipherTest {
    private CipherUtil aesCipher;
    private CipherUtil desCipher;

    @Before
    public void init() {
        desCipher = SymmetryCipher.buildInstance(AbstractCipher.Algorithms.DES,
            "123123123123123123", 56);
        aesCipher = SymmetryCipher.buildInstance(AbstractCipher.Algorithms.AES,
            "123123123123123123", 128);
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
