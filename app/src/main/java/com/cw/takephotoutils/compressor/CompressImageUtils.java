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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.cw.takephotoutils.BaseApplication;
import com.cw.takephotoutils.utils.ThreadPoolManager;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author Cw
 * @date 17/2/9
 */
public class CompressImageUtils {

    public static String COMPRESS_SERVICE_ACTION = "com.cw.takephotoutils.compressor.ICompressService";

    private static boolean sHasBind;
    private Handler UIHandle = new Handler(Looper.getMainLooper());
    private CompressConfig mConfig;
    private ICompressService mService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ICompressService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    public CompressImageUtils() {
        //初始化压缩工具
        mConfig = new CompressConfig();
        mConfig.setMaxSize(700);
        mConfig.setMaxPixel(1280);
        mConfig.setOpenProcess(true);
        bindCompressService();
    }

    public CompressImageUtils(CompressConfig config) {
        this.mConfig = config;
        if (config.isOpenProcess()) {
            bindCompressService();
        }
    }

    public void setMaxSize(int maxSize) {
        if (mConfig != null) {
            mConfig.setMaxSize(maxSize);
        }
    }

    public void recycle() {
        unBindCompressService();
        mServiceConnection = null;
    }

    public void compress(final String basePath, final String compressPath, final CompressImageListener listener) {
        if (listener == null || mConfig == null) {
            throw new RuntimeException("CompressConfig or CompressImageListener can not be null");
        }

        if (mConfig.isOpenProcess() && mService != null && isServiceRunning(CompressorService.class)) {
            try {
                mService.compress(basePath, compressPath, mConfig.getMaxSize(), mConfig.getMaxPixel(), new ICompressCallback.Stub() {
                    @Override
                    public void onCallBack(final String basePath, final String compress) throws RemoteException {
                        UIHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(compress)) {
                                    Log.d("CompressorService", "onCallBack" + compress);
                                    listener.onCompressSuccess(basePath, compress);
                                } else {
                                    listener.onCompressFailed(basePath);
                                }
                            }
                        });
                    }
                });
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        ThreadPoolManager.getThreadProxyPool(1, 1, 0L).excute(new Runnable() {
            @Override
            public void run() {
                final String path = compressImage(basePath, compressPath, mConfig.getMaxSize(), mConfig.getMaxPixel());
                UIHandle.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(path)) {
                            Log.d("NoCompressorService", "onCallBack" + path);
                            listener.onCompressSuccess(basePath, path);
                        } else {
                            listener.onCompressFailed(basePath);
                        }
                    }
                });
            }
        });
    }

    private boolean isServiceRunning(Class clz) {
        ActivityManager manager = (ActivityManager) BaseApplication.sAppContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (clz.getCanonicalName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void bindCompressService() {
        if (!sHasBind) {
            Intent intent = new Intent(BaseApplication.sAppContext, CompressorService.class);
            intent.setAction(COMPRESS_SERVICE_ACTION);
            intent.setPackage(CompressorService.class.getPackage().getName());
            BaseApplication.sAppContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            sHasBind = true;
        }
    }

    private void unBindCompressService() {
        if (sHasBind) {
            BaseApplication.sAppContext.unbindService(mServiceConnection);
            sHasBind = false;
        }
    }

    /**
     * 图片压缩
     *
     * @param basePath     basePath
     * @param compressPath compressPath
     * @return compress path
     */
    public static String compressImage(String basePath, String compressPath, int maxSize, int maxPixel) {
        int quality = 100;
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = null;
        try {
            //压缩好比例大小后再进行质量压缩
            bitmap = getSmallBitmap(basePath, maxPixel);
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            Log.d("GetPhotoUtils", basePath + "图片初始大小" + baos.toByteArray().length / 1024 + "k");
            while (baos.toByteArray().length > maxSize * 1024) {
                //每超出200k质量减1
                int i = (baos.toByteArray().length / 1024 - maxSize) / 200;
                quality -= i > 0 ? i : 1;
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            Log.d("GetPhotoUtils", basePath + "图片压缩完成" + baos.toByteArray().length / 1024 + "k");
            FileOutputStream out = new FileOutputStream(compressPath);
            baos.writeTo(out);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (baos != null) {
                try {
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
            //System.gc();
        }
        return compressPath;
    }

    /**
     * 根据路径获得图片并压缩返回bitmap
     */
    private static Bitmap getSmallBitmap(String filePath, int maxPixel) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxPixel, maxPixel);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算图片的缩放值
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩监听
     */
    public interface CompressImageListener {
        /**
         * 压缩成功
         *
         * @param imgPath 压缩图片的路径
         */
        void onCompressSuccess(String basePath, String imgPath);

        /**
         * 压缩失败
         *
         * @param basePath 压缩失败的原图
         */
        void onCompressFailed(String basePath);
    }
}