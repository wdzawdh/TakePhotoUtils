/**
 *   @function:$
 *   @description: $
 *   @param:$
 *   @return:$
 *   @history:
 * 1.date:$ $
 *           author:$
 *           modification:
 */

package com.cw.takephotoutils.compressor;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.cw.takephotoutils.utils.ThreadPoolManager;


/**
 * @author Cw
 * @date 16/12/1
 */
public class CompressorService extends Service {

    public static final int FOREGROUND_PUSH_ID = 1;

    private final ICompressService.Stub mBinder = new ICompressService.Stub() {
        @Override
        public void compress(final String basePath, final String compressPath, final int maxSize, final int maxPixel
                , final ICompressCallback callback) throws RemoteException {
            if (callback == null) {
                throw new IllegalArgumentException("ICompressCallback con not be null");
            }
            ThreadPoolManager.getThreadProxyPool().excute(new Runnable() {
                @Override
                public void run() {
                    String path = CompressImageUtils.compressImage(basePath, compressPath, maxSize, maxPixel);
                    try {
                        callback.onCallBack(basePath, path);
                    } catch (RemoteException e) {
                        stopSelf();
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /**
         * 通过实现一个内部 Service，两个Service同时发送具有相同 ID的 Notification，
         * 然后将内部 Service 结束掉。随着内部 Service 的结束，Notification
         * 将会消失，但优先级依然保持为前台进程。
         */
        startForeground(FOREGROUND_PUSH_ID, new Notification());
        // API >= 18 ，此方法能有效隐藏Notification上的图标
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startService(new Intent(this, InnerService.class));
        }
        return mBinder;
    }

    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(FOREGROUND_PUSH_ID, new Notification());
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

}