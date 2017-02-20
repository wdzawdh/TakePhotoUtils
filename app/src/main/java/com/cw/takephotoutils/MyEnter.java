package com.cw.takephotoutils;

import android.app.Activity;
import android.content.Intent;

import com.cw.takephotoutils.gallery.GalleryActivity;
import com.cw.takephotoutils.gallery.GalleryDetailActivity;


public class MyEnter {

    public static void openGallery(Activity act, int maxSize, String[] haveSelected, String mimeType) {
        Intent i = new Intent(act, GalleryActivity.class);
        i.putExtra(MyExtra.KEY_GALLERY_MAX_SIZE, maxSize);
        i.putExtra(MyExtra.KEY_GALLERY_HAS_SELECTED, haveSelected);
        i.putExtra(MyExtra.KEY_GALLERY_MIME_TYPE, mimeType);
        act.startActivityForResult(i, GalleryActivity.RESULT_PICK_IMAGE);
    }

    public static void openGalleryDetail(Activity act, int maxSize, String[] haveSelected, String folderId, String mimeType) {
        Intent i = new Intent(act, GalleryDetailActivity.class);
        i.putExtra(MyExtra.KEY_GALLERY_MAX_SIZE, maxSize);
        i.putExtra(MyExtra.KEY_GALLERY_HAS_SELECTED, haveSelected);
        i.putExtra(MyExtra.KEY_GALLERY_FOLDER_ID, folderId);
        i.putExtra(MyExtra.KEY_GALLERY_MIME_TYPE, mimeType);
        act.startActivityForResult(i, GalleryActivity.RESULT_PICK_IMAGE);
    }
}
