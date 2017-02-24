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

package com.cw.takephotoutils.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cw.takephotoutils.BaseApplication;
import com.cw.takephotoutils.MyEnter;
import com.cw.takephotoutils.MyExtra;
import com.cw.takephotoutils.compressor.CompressImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Cw
 * @date 17/2/9
 */
public class TakePhotoUtils {

    private static final int RESULT_CAMERA_ONLY = 100;//拍摄并保存完照片的结果码
    private static final int RESULT_PICK_IMAGE = 200;//选择图片后的结果码
    private static final int RESULT_CAMERA_CROP_RESULT = 301;//裁剪并保存完照片的结果码

    //总文件夹路径
    private static String sDir = Environment.getExternalStorageDirectory().getPath() + "/takePhotoUtils/img/";
    //文件夹路径
    private String mPath = sDir + System.currentTimeMillis() + "/";
    //Tag图片标识
    private String mTag = "";
    //图片位置
    private int mPosition;
    //是否要压缩
    private boolean mNeedCompress = true;
    //是否要裁剪
    private boolean mNeedCrop;
    //Map<itemKey, Map<basePath, compressPath>>
    private Map<String, Map<String, StateModel>> mTableManagerMap = new ConcurrentHashMap<>();
    /**
     * 其他临时数据
     */
    private String mCropPath;
    private String mCropBasePath;
    private String mSystemCameraPath;
    private boolean mIsSystemCamera;
    private boolean mIsSystemGallery;
    private CompressImageUtils mCompressImageUtils;

    public TakePhotoUtils() {
        FileUtils.makeDirs(mPath);
        mCompressImageUtils = new CompressImageUtils();
    }

    public void clear() {
        FileUtils.deleteFile(mPath);
        if (mCompressImageUtils != null) {
            mCompressImageUtils.recycle();
        }
    }

    public void setMaxSize(int maxSize) {
        if (mCompressImageUtils != null) {
            mCompressImageUtils.setMaxSize(maxSize);
        }
    }

    public void setNeedCompress(boolean needCompress) {
        mNeedCompress = needCompress;
    }

    public void setNeedCrop(boolean needCrop) {
        mNeedCrop = needCrop;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    /**
     * 启动系统相机,保存图片
     */
    public void takeCameraBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isCameraCanUse()) {
            return;
        }
        mSystemCameraPath = mPath + System.currentTimeMillis() + ".jpg";
        mIsSystemCamera = true;
        File file = new File(mSystemCameraPath);
        Uri imageUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        act.startActivityForResult(intent, RESULT_CAMERA_ONLY);
    }

    /**
     * 启动自定义相机,保存图片
     *
     * @param gotoCameraAct 自定义相机的class(自定义相机种类很多,所以由外部传入)
     */
    public void takeCameraByCustom(Activity act, Class gotoCameraAct) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isCameraCanUse()) {
            return;
        }
        mIsSystemCamera = false;
        Intent intent = new Intent(act, gotoCameraAct);
        act.startActivityForResult(intent, RESULT_CAMERA_ONLY);
    }

    /**
     * 打开系统图库
     */
    public void pickPhotoBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        mIsSystemGallery = true;
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        act.startActivityForResult(i, RESULT_PICK_IMAGE);
    }

    /**
     * 打开自定义图库
     *
     * @param maxCount          可以选择的张数
     * @param haveSelectedPaths 已经被选的路径
     */
    public void pickPhotoByCustom(Activity act, int maxCount, List<String> haveSelectedPaths) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        mIsSystemGallery = false;
        String[] haveSelectedBasePath = getHaveSelectedBasePath(haveSelectedPaths);
        MyEnter.openGallery(act, maxCount, haveSelectedBasePath, "image/jpeg");
    }

    /**
     * 裁剪图片
     *
     * @param basePath 原图路径
     * @param cropPath 裁剪图片路径
     */
    public void cropImage(Activity act, String basePath, String cropPath) {
        mCropBasePath = basePath;
        mCropPath = cropPath;
        File file = new File(basePath);
        Uri imageUri = getImageContentUri(act, file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 450);
        intent.putExtra("outputY", 450);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cropPath)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        act.startActivityForResult(intent, RESULT_CAMERA_CROP_RESULT);
    }

    /**
     * 接收onActivityResult参数
     */
    public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data, final OnTakePhotoListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("OnTakePhotoListener can not be null");
        }
        if (resultCode != Activity.RESULT_OK) {
            listener.onCancel(mPosition);
            return;
        }
        if (!FileUtils.makeDirs(mPath)) {
            Log.i("TakePhotoUtils", mPath + "img dir don't find");
            return;
        }
        if (mCompressImageUtils == null) {
            Log.i("TakePhotoUtils", "CompressImageUtils is null");
            return;
        }
        switch (requestCode) {
            case TakePhotoUtils.RESULT_CAMERA_ONLY: {
                String[] cameraPhotoPath = getCameraPhotoPath(data, mIsSystemCamera);
                if (mNeedCrop && cameraPhotoPath.length > 0) {
                    cropImage(act, cameraPhotoPath[0], refreshCropFileName());
                    return;
                }
                //情况一:不需要裁剪,需要压缩(拍照)
                if (mNeedCompress) {
                    String[] compressPaths = refreshCompressFileName(cameraPhotoPath.length, mTag);
                    for (int i = 0; i < compressPaths.length; i++) {
                        final String tag = mTag;
                        final int index = i;
                        final int pickIndex = mPosition + i;
                        String basePath = cameraPhotoPath[i];
                        String compressPath = compressPaths[i];
                        tableManagerAddMap(tag, basePath, compressPath, StateModel.STATE_COMPRESSING);
                        listener.onStart(basePath, index, pickIndex, tag);
                        mCompressImageUtils.compress(basePath, compressPath, new CompressImageUtils.CompressImageListener() {
                            @Override
                            public void onCompressSuccess(String basePath, String compressPath) {
                                tableManagerModifyState(tag, basePath, compressPath, StateModel.STATE_FINISH);
                                listener.onNext(basePath, "", compressPath, index, pickIndex, tag);
                            }

                            @Override
                            public void onCompressFailed(String basePath) {
                                tableManagerModifyState(tag, basePath, "", StateModel.STATE_FINISH);
                                listener.onFailed(basePath, index, pickIndex, tag);
                            }
                        });
                    }
                    return;
                }
                //情况二:不需要裁剪,不需要压缩(拍照)
                for (int i = 0; i < cameraPhotoPath.length; i++) {
                    int pickIndex = mPosition + i;
                    listener.onStart(cameraPhotoPath[i], i, pickIndex, mTag);
                    listener.onNext(cameraPhotoPath[i], "", cameraPhotoPath[i], i, pickIndex, mTag);
                }
                return;
            }
            case TakePhotoUtils.RESULT_PICK_IMAGE: {
                String[] pickPhotoPath = getPickPhotoPath(BaseApplication.sAppContext, data, mIsSystemGallery);
                if (mNeedCrop && pickPhotoPath.length > 0) {
                    cropImage(act, pickPhotoPath[0], refreshCropFileName());
                    return;
                }
                //情况一:不需要裁剪,需要压缩(图库)
                if (mNeedCompress) {
                    String[] compressPaths = refreshCompressFileName(pickPhotoPath.length, mTag);
                    for (int i = 0; i < compressPaths.length; i++) {
                        final String tag = mTag;
                        final int index = i;
                        final int pickIndex = mPosition + i;
                        final String basePath = pickPhotoPath[i];
                        String compressPath = compressPaths[i];
                        tableManagerAddMap(tag, basePath, compressPath, StateModel.STATE_COMPRESSING);
                        listener.onStart(basePath, index, pickIndex, tag);
                        mCompressImageUtils.compress(basePath, compressPath, new CompressImageUtils.CompressImageListener() {
                            @Override
                            public void onCompressSuccess(String basePath, String compressPath) {
                                tableManagerModifyState(tag, basePath, compressPath, StateModel.STATE_FINISH);
                                listener.onNext(basePath, "", compressPath, index, pickIndex, tag);
                            }

                            @Override
                            public void onCompressFailed(String basePath) {
                                tableManagerModifyState(tag, basePath, "", StateModel.STATE_FINISH);
                                listener.onFailed(basePath, index, pickIndex, tag);
                            }
                        });
                    }
                    return;
                }
                //情况二:不需要裁剪,不需要压缩(图库)
                for (int i = 0; i < pickPhotoPath.length; i++) {
                    int pickIndex = mPosition + i;
                    listener.onStart(pickPhotoPath[i], i, pickIndex, mTag);
                    listener.onNext(pickPhotoPath[i], "", pickPhotoPath[i], i, pickIndex, mTag);
                }
                return;
            }
            case TakePhotoUtils.RESULT_CAMERA_CROP_RESULT: {
                String[] cropPaths = refreshCompressFileName(1, mTag);
                //情况三:需要裁剪,需要压缩
                if (mNeedCompress) {
                    final String tag = mTag;
                    final String cropBasePath = mCropBasePath;
                    final int position = mPosition;
                    String cropPath = mCropPath;
                    String compressPath = cropPaths[0];
                    tableManagerAddMap(tag, cropPath, compressPath, StateModel.STATE_COMPRESSING);
                    listener.onStart(cropPath, 0, mPosition, tag);
                    mCompressImageUtils.compress(cropPath, compressPath, new CompressImageUtils.CompressImageListener() {
                        @Override
                        public void onCompressSuccess(String basePath, String compressPath) {
                            tableManagerModifyState(tag, basePath, compressPath, StateModel.STATE_FINISH);
                            listener.onNext(cropBasePath, basePath, compressPath, 0, position, tag);
                        }

                        @Override
                        public void onCompressFailed(String basePath) {
                            tableManagerModifyState(tag, basePath, "", StateModel.STATE_FINISH);
                            listener.onFailed(cropBasePath, 0, position, tag);
                        }
                    });
                    return;
                }
                //情况四:需要裁剪,不需要压缩
                listener.onStart(mCropPath, 0, mPosition, mTag);
                listener.onNext(mCropBasePath, mCropPath, mCropPath, 0, mPosition, mTag);
            }
        }
    }

    /**
     * 获取拍照的图片的路径
     */
    private String[] getCameraPhotoPath(Intent data, boolean isSystemCamera) {
        //系统相机
        if (isSystemCamera) {
            if (mSystemCameraPath == null) {
                mSystemCameraPath = "";
            }
            String[] paths = new String[1];
            paths[0] = mSystemCameraPath;//使用系统拍照时返回启动相机时设置的path
            return paths;
        }
        //自定义相机
        if (data != null) {
            return data.getStringArrayExtra(MyExtra.KEY_CAMERA_FOLDER_ARRAY);
        }
        return new String[0];
    }

    /**
     * 获取图库中选择的图片的路径
     *
     * @param data            图库返回的Intent
     * @param isSystemGallery 是否是系统图库
     * @return 选择的图片路径
     */
    private String[] getPickPhotoPath(Context context, Intent data, boolean isSystemGallery) {
        //系统图库
        if (isSystemGallery) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                String[] strings = {picturePath};
                cursor.close();
                return strings;
            }
            return new String[0];
        }
        //自定义图库
        if (data != null) {
            return data.getStringArrayExtra(MyExtra.KEY_GALLERY_FOLDER_ARRAY);
        }
        return new String[0];
    }

    /**
     * 相机是否可以使用
     */
    private boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
            // 对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
        }
        return canUse;
    }

    /**
     * 将File转换为Content Uri
     * 为适配 7.0 原来的File Uri需要更改为Content Uri
     */
    private Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            Uri uri = Uri.withAppendedPath(baseUri, "" + id);
            cursor.close();
            return uri;
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 生成压缩图片文件名
     *
     * @param fileCount 文件数
     * @param flag      文件名标记
     */
    private String[] refreshCompressFileName(int fileCount, String flag) {
        String[] fileNames = new String[fileCount];
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < fileCount; i++) {
            String path = mPath + currentTimeMillis + "_" + flag + "_compress_" + i + ".jpg";
            fileNames[i] = path;
        }
        return fileNames;
    }

    /**
     * 生成裁剪文件名
     */
    private String refreshCropFileName() {
        long currentTimeMillis = System.currentTimeMillis();
        return mPath + currentTimeMillis + "_crop.jpg";
    }

    /**
     * mTableManagerMap添加映射表
     */
    private void tableManagerAddMap(String key, String basePath, String processedPath, int state) {
        //ConcurrentHashMap不能有null键和null值
        if (key == null || basePath == null) {
            return;
        }
        for (Map.Entry<String, Map<String, StateModel>> tableManager : mTableManagerMap.entrySet()) {
            //以前添加过此key,就在原有映射表上增加内容
            if (TextUtils.equals(tableManager.getKey(), key)) {
                Map<String, StateModel> pathMap = tableManager.getValue();
                pathMap.put(basePath, new StateModel(processedPath, state));
                return;
            }
        }
        //如果没有添加过key,就新建一个映射表添加
        Map<String, StateModel> pathMap = new ConcurrentHashMap<>();
        pathMap.put(basePath, new StateModel(processedPath, state));
        mTableManagerMap.put(key, pathMap);
    }

    /**
     * mTableManagerMap修改映射表的某个键值对的状态
     */
    private void tableManagerModifyState(String key, String basePath, String processedPath, int state) {
        for (Map.Entry<String, Map<String, StateModel>> tableManager : mTableManagerMap.entrySet()) {
            //找到此key对应的映射表
            if (TextUtils.equals(tableManager.getKey(), key)) {
                Map<String, StateModel> pathMap = tableManager.getValue();
                for (Map.Entry<String, StateModel> pathEntry : pathMap.entrySet()) {
                    //再通过basePath找出对应的StateModel并覆盖
                    if (TextUtils.equals(pathEntry.getKey(), basePath)) {
                        if (TextUtils.isEmpty(processedPath)) {
                            pathMap.remove(pathEntry.getKey());
                        } else {
                            pathMap.put(pathEntry.getKey(), new StateModel(processedPath, state));
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * 获取已经选择过的BasePath
     */
    private String[] getHaveSelectedBasePath(List<String> haveSelectedPaths) {
        ArrayList<String> list = new ArrayList<>();
        //将正在压缩的BasePath添加到list
        for (Map.Entry<String, Map<String, StateModel>> pathMapEntry : mTableManagerMap.entrySet()) {
            if (TextUtils.equals(pathMapEntry.getKey(), mTag)) {
                for (Map.Entry<String, StateModel> pathEntry : pathMapEntry.getValue().entrySet()) {
                    if (pathEntry.getValue().state == StateModel.STATE_COMPRESSING) {
                        list.add(pathEntry.getKey());
                    }
                }
            }
        }
        //将传入的haveSelectedPaths对应的BasePath添加到list
        for (int i = 0; haveSelectedPaths != null && i < haveSelectedPaths.size(); i++) {
            for (Map.Entry<String, Map<String, StateModel>> pathMapEntry : mTableManagerMap.entrySet()) {
                for (Map.Entry<String, StateModel> pathEntry : pathMapEntry.getValue().entrySet()) {
                    if (TextUtils.equals(haveSelectedPaths.get(i), pathEntry.getValue().compressPath)) {
                        list.add(pathEntry.getKey());
                    }
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 压缩状态类
     */
    private static class StateModel {
        static final int STATE_COMPRESSING = 0;//正在压缩状态
        static final int STATE_FINISH = 1;//压缩完成状态

        String compressPath;
        int state;

        StateModel(String compressPath, int state) {
            this.compressPath = compressPath;
            this.state = state;
        }
    }

    public interface OnTakePhotoListener {
        void onStart(String basePath, int position, int pickIndex, String tag);

        void onNext(String basePath, String cropPath, String compressPath, int position, int pickIndex, String tag);

        void onFailed(String basePath, int position, int pickIndex, String tag);

        void onCancel(int position);
    }

}