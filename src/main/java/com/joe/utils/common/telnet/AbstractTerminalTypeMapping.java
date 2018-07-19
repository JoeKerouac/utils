package com.joe.utils.common.telnet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author joe
 * @version 2018.07.19 18:06
 */
public abstract class AbstractTerminalTypeMapping {

    public static String getDefaultTerminalType() {
        return File.separatorChar == '/' ? "XTERM" : "ANSI";
    }

    protected Map<String, KEYS> escKeys;

    protected byte              backSpace;

    protected byte              del;

    public AbstractTerminalTypeMapping(byte backSpace, byte del) {
        this.backSpace = backSpace;
        this.del = del;
        escKeys = new HashMap<>();
        escKeys.put("[C", KEYS.RIGHT);
        escKeys.put("[D", KEYS.LEFT);
        escKeys.put("[3~", KEYS.DEL);
    }

    public byte getBackspace() {
        return backSpace;
    }

    public byte getDel() {
        return del;
    }

    public KEYS getMatchKeys(String str) {
        if (escKeys.get(str) != null) {
            return escKeys.get(str);
        }
        if (isPossibleEscKeys(str)) {
            return KEYS.UNFINISHED;
        }
        return KEYS.UNKNOWN;
    }

    protected boolean isPossibleEscKeys(String str) {
        for (String key : escKeys.keySet()) {
            if (key.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public enum KEYS {
                      RIGHT, LEFT, DEL, UNFINISHED, UNKNOWN
    }
}
