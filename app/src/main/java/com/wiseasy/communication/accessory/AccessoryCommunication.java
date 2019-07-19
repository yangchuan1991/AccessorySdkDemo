package com.wiseasy.communication.accessory;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.wiseasy.communication.base.Communication;
import com.wiseasy.communication.listener.OpenListener;
import com.wiseasy.communication.listener.ReciverMessageListener;
import com.wiseasy.communication.receiver.OpenAccessoryReceiver;
import com.wiseasy.communication.receiver.UsbDetachedReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2019\7\18 0018.
 */

public class AccessoryCommunication implements Communication, UsbDetachedReceiver.UsbDetachedListener, OpenAccessoryReceiver.OpenAccessoryListener {
    private Context mContext;
    private UsbDetachedReceiver mUsbDetachedReceiver;
    private OpenAccessoryReceiver mOpenAccessoryReceiver;
    private UsbManager mUsbManager;
    private Logger logger = LoggerFactory.getLogger("UsbCommunication------------------>");

    //    private static final String USB_ACTION = "com.tcl.navigator.accessorychart";
    private static final String USB_ACTION = "com.yangc.accessory";


    /**
     * 单例模式 初始化
     *
     * @param context
     */
    private AccessoryCommunication(Context context) {
        /**
         * 为了避免在单利模式下的内存泄露，这里将context统一转换为ApplicationContext
         */
        this.mContext = context.getApplicationContext();

        //注册usb连接断开的广播
        mUsbDetachedReceiver = new UsbDetachedReceiver(this);
        IntentFilter intentFilter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbDetachedReceiver, intentFilter);

        //通过context获取到当前系统的USB管理器
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        logger.debug("结果 UsbCommunication---------------------={}", "UsbCommunication(Context context)执行完毕");
    }

    //单例变量
    private static volatile AccessoryCommunication Instance;

    /**
     * 单利模式双重检查
     *
     * @param context
     * @return
     */
    public static AccessoryCommunication getInstance(Context context) {
        if (Instance == null) {
            synchronized (AccessoryCommunication.class) {
                if (Instance == null) {
                    Instance = new AccessoryCommunication(context);
                }
            }
        }
        return Instance;
    }

    private OpenListener openListener;

    @Override
    public void openCommunication(OpenListener listener) {
        openListener = listener;
        mOpenAccessoryReceiver = new OpenAccessoryReceiver(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(USB_ACTION), 0);
        IntentFilter intentFilter = new IntentFilter(USB_ACTION);
        mContext.registerReceiver(mOpenAccessoryReceiver, intentFilter);
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory usbAccessory = (accessories == null ? null : accessories[0]);
        if (usbAccessory != null) {
            if (mUsbManager.hasPermission(usbAccessory)) {
                openAccessory(usbAccessory);
            } else {
                mUsbManager.requestPermission(usbAccessory, pendingIntent);
            }
        } else {
            listener.Failed("usb连接失败");
        }
    }


    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private byte[] mBytes = new byte[1024];

    /**
     * 打开Accessory模式
     *
     * @param usbAccessory
     */
    private void openAccessory(UsbAccessory usbAccessory) {
        mParcelFileDescriptor = mUsbManager.openAccessory(usbAccessory);
        if (mParcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = mParcelFileDescriptor.getFileDescriptor();
            mFileInputStream = new FileInputStream(fileDescriptor);
            mFileOutputStream = new FileOutputStream(fileDescriptor);
//            mSend.setEnabled(true);
            openListener.Success();
        }
    }


    @Override
    public void closeCommunication() {
        if (mOpenAccessoryReceiver != null) {
            mContext.unregisterReceiver(mOpenAccessoryReceiver);
        }
        if (mUsbDetachedReceiver != null) {
            mContext.unregisterReceiver(mUsbDetachedReceiver);
        }
        if (mParcelFileDescriptor != null) {
            try {
                mParcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mFileInputStream != null) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int sendMessage(byte[] bytes) {
        try {
            FileChannel mFileChannel = mFileOutputStream.getChannel();
//            mFileOutputStream.write(ByteBuffer.wrap(bytes));
            mFileChannel.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public void receiveMessage(ReciverMessageListener listener) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        if (mFileInputStream == null) {
            listener.onFailed("接受消息失败");
        } else {

            try {
                FileChannel mFileChannel = mFileInputStream.getChannel();
//                mFileChannel.read(ByteBuffer.wrap(mBytes));
//                if(mFileInputStream.read(mBytes)>0){
                if (mFileChannel.read(ByteBuffer.wrap(mBytes)) > 0) {

                    listener.onSuccess(mBytes);
                } else {
                    listener.onFailed("无数据");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            int i = 0;
//            while (i >= 0) {
//                try {
//                    i = mFileInputStream.read(mBytes);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    break;
//                }
//                if (i > 0) {
//                    listener.onSuccess(mBytes);
//                }
//            }
        }
//            }
//        }).start();
    }

    @Override
    public void usbDetached() {
        closeCommunication();
    }

    @Override
    public void openAccessoryModel(UsbAccessory usbAccessory) {
        openAccessory(usbAccessory);
    }

    @Override
    public void openAccessoryError() {
        closeCommunication();
    }
}
