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
import android.content.Intent;
import android.support.annotation.NonNull;

import com.cw.takephotoutils.R;
import com.cw.takephotoutils.weiget.OptionDialog;

import java.util.ArrayList;

/**
 * @author Cw
 * @date 17/2/9
 */
public class GetPhotoUtils {

    private TakePhotoUtils mTakePhotoUtils;

    public GetPhotoUtils() {
        mTakePhotoUtils = new TakePhotoUtils();
    }

    private OnGetPhotoListener listener;

    public interface OnGetPhotoListener {
        //bitmap缩略图
        void onStart(String orgPath, String itemKey, int position);

        void onNext(String basePath, String cropPath, String compressPath, String itemKey, int position);

        void onFailure(String itemKey, int position);

        void onCancel(int position);
    }

    /**
     * 企业活动使用(特殊)
     */
    public interface ShowSpecialOptsListener {
        void showSpecialGallery();
    }

    /**
     * 获取图片监听,请在onActivityResult中使用,将requestCode,resultCode,intent传入
     */
    public void setOnGetPhotoListener(@NonNull OnGetPhotoListener listener) {
        this.listener = listener;
    }

    /**
     * 取单张图片
     * 访问系统相机,自定义图库,不需要压缩,不需要裁剪
     */
    public void showDialog(Activity act) {
        showDialog(act, 0, true, false, false, false, 0, 0, "", null, null);
    }

    /**
     * 取单张图片
     * 访问系统相机,自定义图库,需要压缩,需要裁剪
     *
     * @param maxSize 压缩后图片最大尺寸(k)
     */
    public void showDialog(Activity act, int maxSize) {
        showDialog(act, 0, true, false, true, true, 1, maxSize, "", null, null);
    }

    /**
     * 取多张图片
     * 访问系统相机,自定义图库,不需要裁剪,需要压缩
     *
     * @param position     图片索引
     * @param maxPickCount 最大选择张数(此次弹出Dialog后还可以选择几张)
     * @param maxSize      压缩后图片最大尺寸(k)
     */
    public void showDialog(Activity act, int position, int maxPickCount, int maxSize) {
        showDialog(act, position, true, false, false, true, maxPickCount, maxSize, "", null, null);
    }

    /**
     * 取多张图片(需要维护不同的映射表使用)
     * 访问系统相机,自定义图库,不需要裁剪,需要压缩
     *
     * @param position          图片索引
     * @param maxPickCount      最大选择张数(此次弹出Dialog后还可以选择几张)
     * @param maxSize           压缩后图片最大尺寸(k)
     * @param itemKey           每个映射表的key
     * @param haveSelectedPaths 已经被选的原文件路径
     */
    public void showDialog(Activity act, int position, int maxPickCount, int maxSize, String itemKey, ArrayList<String> haveSelectedPaths) {
        showDialog(act, position, true, false, false, true, maxPickCount, maxSize, itemKey, haveSelectedPaths, null);
    }

    /**
     * 取多张图片(使用自定义相机)
     * 访问自定义相机,自定义图库,不需要裁剪,需要压缩
     *
     * @param position          图片索引
     * @param maxPickCount      最大选择张数(此次弹出Dialog后还可以选择几张)
     * @param maxSize           压缩后图片最大尺寸(k)
     * @param itemKey           每个映射表的key
     * @param haveSelectedPaths 已经被选的原文件路径
     * @param gotoCameraAct     自定义相机的class(自定义相机种类很多,所以由外部传入)
     */
    public void showDialog(Activity act, int position, int maxPickCount, int maxSize, String itemKey, ArrayList<String> haveSelectedPaths, Class gotoCameraAct) {
        showDialog(act, position, false, false, false, true, maxPickCount, maxSize, itemKey, haveSelectedPaths, gotoCameraAct);
    }

    /**
     * 访问相机图库dialog
     *
     * @param position          图片索引
     * @param useSystemCream    是否使用系统相机
     * @param useSystemGallery  是否使用系统图库
     * @param needCrop          是否需要裁剪
     * @param needCompress      是否需要压缩
     * @param maxPickCount      最大选择张数
     * @param maxSize           压缩后图片最大尺寸(k)
     * @param itemKey           每个映射表的key
     * @param haveSelectedPaths 已经被选的文件路径
     * @param gotoCameraAct     自定义相机的class(自定义相机种类很多,所以由外部传入)
     */
    private void showDialog(final Activity act, final int position
            , final boolean useSystemCream, final boolean useSystemGallery
            , final boolean needCrop, final boolean needCompress
            , final int maxPickCount, final int maxSize, final String itemKey
            , final ArrayList<String> haveSelectedPaths, final Class gotoCameraAct) {

        mTakePhotoUtils.setMaxSize(maxSize);
        mTakePhotoUtils.setNeedCompress(needCompress);
        mTakePhotoUtils.setNeedCrop(needCrop);
        mTakePhotoUtils.setTag(itemKey);
        mTakePhotoUtils.setPosition(position);
        OptionDialog optionDialog = new OptionDialog(act);
        optionDialog.setOptionArray(R.string.app_cream_photo, R.string.app_select_photo);
        optionDialog.setOnSelectListener(new OptionDialog.OnSelectListener() {
            @Override
            public void OnSelect(int i, String value) {
                switch (i) {
                    case 0:
                        if (useSystemCream) {
                            mTakePhotoUtils.takeCameraBySystem(act);
                        } else {
                            mTakePhotoUtils.takeCameraByCustom(act, gotoCameraAct);
                        }
                        break;
                    case 1:
                        if (useSystemGallery) {
                            mTakePhotoUtils.pickPhotoBySystem(act);
                        } else {
                            mTakePhotoUtils.pickPhotoByCustom(act, maxPickCount, haveSelectedPaths);
                        }
                        break;
                    case -1:
                        if (listener != null) {
                            listener.onCancel(position);
                        }
                        break;
                }
            }
        });
    }

    /**
     * for 企业活动 特殊
     *
     * @param position     图片索引
     * @param maxPickCount 最大选择张数
     * @param maxSize      压缩后图片最大尺寸(k)
     */
    public void showActDialog(final Activity act, final int position, final int maxPickCount, final int maxSize, final ShowSpecialOptsListener showListener) {
        OptionDialog optionDialog = new OptionDialog(act);
        optionDialog.setOptionArray(R.string.from_act_gallery, R.string.app_cream_photo, R.string.app_select_photo);
        optionDialog.setOnSelectListener(new OptionDialog.OnSelectListener() {
            @Override
            public void OnSelect(int i, String value) {
                mTakePhotoUtils.setMaxSize(maxSize);
                switch (i) {
                    case 0:
                        if (showListener != null) {
                            showListener.showSpecialGallery();
                        }
                        break;
                    case 1:
                        mTakePhotoUtils.takeCameraBySystem(act);
                        break;
                    case 2:
                        mTakePhotoUtils.pickPhotoByCustom(act, maxPickCount, null);
                        break;
                    case -1:
                        if (listener != null) {
                            listener.onCancel(position);
                        }
                        break;
                }
            }
        });
    }

    public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
        if (listener == null) {
            throw new RuntimeException("OnGetPhotoListener can not be null");
        }
        mTakePhotoUtils.onActivityResult(act, requestCode, resultCode, data, new TakePhotoUtils.OnTakePhotoListener() {
            @Override
            public void onStart(String basePath, int position, int pickIndex, String tag) {
                listener.onStart(basePath, tag, pickIndex);
            }

            @Override
            public void onNext(String basePath, String cropPath, String compressPath, int position, int pickIndex, String tag) {
                listener.onNext(basePath, cropPath, compressPath, tag, pickIndex);
            }

            @Override
            public void onFailed(String basePath, int position, int pickIndex, String tag) {
                listener.onFailure(tag, pickIndex);
            }

            @Override
            public void onCancel(int pickIndex) {
                listener.onCancel(pickIndex);
            }
        });
    }

    public void clear() {
        if (mTakePhotoUtils != null) {
            mTakePhotoUtils.clear();
        }
    }

}