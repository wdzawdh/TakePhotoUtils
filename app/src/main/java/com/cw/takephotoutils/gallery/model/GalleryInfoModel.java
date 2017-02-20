/**
 *
 *   @function:$
 *   @description: $
 *   @param:$
 *   @return:$
 *   @history:
 * 1.date:$ $
 *           author:$
 *           modification:
 */

package com.cw.takephotoutils.gallery.model;

/**
 *
 * @author Cw
 * @date 16/9/4
 */
public class GalleryInfoModel {
    public String folderId;// 直接包含该图片文件的文件夹ID，防止在不同下的文件夹重名
    public String folderName;// 直接包含该图片文件的文件夹名
    public String firstPhotoPath;// 文件夹第一张图片绝对路径
    public String photoCount;
}