// ICompressService.aidl
package com.cw.takephotoutils.compressor;

// Declare any non-default types here with import statements
import com.cw.takephotoutils.compressor.ICompressCallback;

interface ICompressService {
    void compress(String basePath, String compressPath,int maxSize,int maxPixel,ICompressCallback callback);
}
