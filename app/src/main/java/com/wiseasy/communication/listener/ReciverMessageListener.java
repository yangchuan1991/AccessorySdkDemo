package com.wiseasy.communication.listener;

/**
 * Created by Administrator on 2019\7\18 0018.
 */

public interface ReciverMessageListener {
    void onSuccess(byte[] bytes);
    void onFailed(String msg);
}
