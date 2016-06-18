package com.vardemin.wiwing;

import java.util.List;

/**
 * Created by xavie on 23.05.2016.
 */
interface UsersListener {
    void onUserReceive(User user);
    void onUserExit(User user);
}
