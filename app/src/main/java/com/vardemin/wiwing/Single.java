package com.vardemin.wiwing;

/**
 * Created by xavie on 23.05.2016.
 */
public class Single {
    private static Single ourInstance = new Single();

    public static Single getInstance() {
        return ourInstance;
    }

    private Single() {
    }
}
