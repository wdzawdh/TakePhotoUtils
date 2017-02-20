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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Cw
 * @date 17/2/9
 */
public class CompressConfig implements Parcelable {
    /**
     * 长或宽不超过的最大像素,单位px
     */
    private int mMaxPixel = 1200;

    /**
     * 压缩到的最大大小，单位B
     */
    private int mMaxSize = 100 * 1024;

    /**
     * 是否开启多进程
     */
    private boolean mIsOpenProcess;

    public int getMaxSize() {
        return mMaxSize;
    }

    public int getMaxPixel() {
        return mMaxPixel;
    }

    public boolean isOpenProcess() {
        return mIsOpenProcess;
    }

    public void setMaxPixel(int maxPixel) {
        mMaxPixel = maxPixel;
    }

    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
    }

    public void setOpenProcess(boolean openProcess) {
        mIsOpenProcess = openProcess;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMaxPixel);
        dest.writeInt(this.mMaxSize);
        dest.writeByte(this.mIsOpenProcess ? (byte) 1 : (byte) 0);
    }

    public CompressConfig() {
    }

    protected CompressConfig(Parcel in) {
        this.mMaxPixel = in.readInt();
        this.mMaxSize = in.readInt();
        this.mIsOpenProcess = in.readByte() != 0;
    }

    public static final Parcelable.Creator<CompressConfig> CREATOR = new Parcelable.Creator<CompressConfig>() {
        @Override
        public CompressConfig createFromParcel(Parcel source) {
            return new CompressConfig(source);
        }

        @Override
        public CompressConfig[] newArray(int size) {
            return new CompressConfig[size];
        }
    };
}