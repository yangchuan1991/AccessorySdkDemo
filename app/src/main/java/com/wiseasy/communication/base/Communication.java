package com.wiseasy.communication.base;

import com.wiseasy.communication.listener.OpenListener;
import com.wiseasy.communication.listener.ReciverMessageListener;

/**
 * Created by Administrator on 2019\7\18 0018.
 */

public interface Communication {

    /**
     * 开启通讯接口
     * @return 0 表示成功开启
     */
    public void openCommunication(OpenListener listener);

    /**
     * 关闭通讯接口
     */
    public void closeCommunication();

    /**
     * 发送byte[]类型数据
     */
    public int sendMessage(byte[] bytes/*, SendMessageListener listener*/);


    /**
     * 接收byte[]类型数据
     */
    public  void receiveMessage(ReciverMessageListener listener);

}
