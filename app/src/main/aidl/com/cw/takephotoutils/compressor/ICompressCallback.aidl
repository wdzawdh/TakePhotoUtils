// ICompressService.aidl
package com.cw.takephotoutils.compressor;

// Declare any non-default types here with import statements

interface ICompressCallback {
    void onCallBack(String basePath,String compress);
}
