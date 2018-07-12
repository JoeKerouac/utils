package com.joe.utils.secure.impl;

import com.joe.utils.secure.CipherUtil;
import com.joe.utils.secure.CipherUtilTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author joe
 * @version 2018.07.11 21:50
 */
public class AsymmetricCipherTest {
    private CipherUtil cipher;

    @Before
    public void init() {
        String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC93qGz/A0MKWZEVxrixXrt1PT/e/SKraLQOey7" +
                "" + "4TXDOhenHrEs16kPkvSE0o3myMY6LqaaJ1PwPzKjtRng7XUd3UE4Axrarzzxz" +
                "+c1Zas6QtEWiwDAZCcaLiBsh8adgdn8Fgcr3r8h" +
                "/ZQyZzktkBawl8bixhLhC/zQYfUAIDayrT/NjTnnphgFuK8qtqxXHKcf/5yAEqVoSWIGtCPx26xPcgWvMpVWUrL7QJFdl05ln9dG" +
                "" +
                "1Lll9C/YCXav/Fpt2wSbxZue6Z5go7Uc5eFlB8Mal2VijNGj6ydA741gvlq8mRgvOCgvW3yoqAvhnJDh6raUscEjganZ5zmp0Gh7" +
                "" + "aqffAgMBAAECggEAH7EYdo1ctCn42vFbGHzz7ty75CURhVBEO9NfU2Dc83Av4II7+osouePCkqT+cIYUqEN" +
                "/JX3pAdHapv6kiim4" + "gbqblzjVc6kKWCZmpkAJG5lpgwTCpFpTOIh4ewUSvtmcw/n9SnJMnuTPprYaEiPZ1bIPzWxYXF3" +
                "+3d1r4pB98Ma15a4+Nycj1XDZ" +
                "ZDuNZnwpmK6kbQH6rDxx0PTyx2iPoYgYYL8kuhrcmGVOblZJsZwwhnSkZ0Rzdr0s0nNwPyBiH4BQP3D26ntABeXlznMVTartwxGD" +
                "" + "cvXUNWmGXeMTzptseb+/Qh64NNbg2d91FN3bcGMEx2s6+kSXvY5DWmIgyQKBgQD+N6PT/QnysAhoND+EZSiQXq7rA0cub" +
                "+DhYfDS" + "sIylzO8GijDe4WNpthsiT3gkRm7Qd/IECdGeFH6r7gM6sQ4/gTCNo/Wp5kCiNgpOh0J4wefN2isUSKYg7BT25" +
                "+4kOjUNFoyN39XX" + "wSWfR2YGs2RlcGSwc9KMGMbZTx2aYA42/QKBgQC/M3o9F9ywtNnAF5rOf/hYowbbGvZ8awfrTYYgQ" +
                "+v8L+hC7AJnWborZwkM9dAo" + "LovZOnaFnvPdQYC9PWRQhLxz2mEDaHjBfzo5gzA0PekmGg4sv0cSddkumf24WV+Vde9tPg" +
                "/z2gq//cljS0To9Ez5QnrEMlcLF0f5" +
                "JVE4AvDnCwKBgD9WDXMbcAcO5IlRuyF5MooFjP7waiOfrB97D0zuv5vvWv3+H/7nmKUVwdzif8RJ6AH4YEuxRpBj+stLGXLd+fbI" +
                "" +
                "cRhY6TPTMfc4D4lRGbaMWPxohqMfFoTNTMvXLPXJqr4ACDSwt85xSLZOS5UPzMuZHPz5XlOZOLGbjHyRwfjhAoGBAJ96CWBBsfK1" +
                "" + "tdXyGk5ZEMSlngdM8wCNqXpLHC93V7LuJpUDE3IutgiTlUn5EdkiEcVrbQZAsXj" +
                "/tKijNzOtsx4SrM5R6j7CpsbntGIurXP5UY9j" +
                "L52nLDIhgS2tZftc9goxNM8ZM9Q1ky89zclcU4OoPou8zTtT/NVDmf3U/ihHAoGBAMTSE5be12gLjsz7RpabXJcoVG+wRo5nvn6P" +
                "" + "58dKwyQ/+GlboU3aMFHeBDkCZkC6ncc/yMsjgw+wWbNvzxhjDUBGvTNjNyJAQM0vstSJMJp+ciynfGic5k8ciiN" +
                "/2Nd9qLENeKd4" + "oIRn0LXsBHcKYP/oLFpbFhyJ1a3+BH1Zo3ZL";

        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvd6hs/wNDClmRFca4sV67dT0/3v0iq2i0Dnsu+E1wzoXp" +
                "" + "x6xLNepD5L0hNKN5sjGOi6mmidT8D8yo7UZ4O11Hd1BOAMa2q888c/nNWWrOkLRFosAwGQnGi4gbIfGnYHZ/BYHK96" +
                "/If2UMmc5L" + "ZAWsJfG4sYS4Qv80GH1ACA2sq0/zY0556YYBbivKrasVxynH/+cgBKlaEliBrQj8dusT3IFrzKVVlKy" +
                "+0CRXZdOZZ/XRtS5ZfQv2" +
                "Al2r/xabdsEm8WbnumeYKO1HOXhZQfDGpdlYozRo+snQO+NYL5avJkYLzgoL1t8qKgL4ZyQ4eq2lLHBI4Gp2ec5qdBoe2qn3wIDA" +
                "" + "QAB";

        cipher = AsymmetricCipher.buildInstance(privateKey, publicKey);
    }

    @Test
    public void doCipher() {
        CipherUtilTest.checkCipher(cipher);
    }
}
