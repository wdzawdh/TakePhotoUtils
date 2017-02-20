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

package com.cw.takephotoutils.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.GridView;

import com.cw.takephotoutils.MyEnter;
import com.cw.takephotoutils.MyExtra;
import com.cw.takephotoutils.R;
import com.cw.takephotoutils.gallery.adapter.GalleryGridAdapter;
import com.cw.takephotoutils.gallery.model.GalleryInfoModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * @author Cw
 * @date 16/9/4
 */
public class GalleryActivity extends Activity {

    public static final int RESULT_PICK_IMAGE = 200;//选择图片后的结果码

    @BindView(R.id.gv_photo_list)
    GridView gv_photo_list;

    private int mMaxSize;
    private String mMimeType;
    private String[] mHaveSelectedPaths;
    private GalleryGridAdapter mGalleryGridAdapter;
    private List<GalleryInfoModel> mGalleryData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.platform_activity_gallery);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mGalleryGridAdapter = new GalleryGridAdapter();
        gv_photo_list.setAdapter(mGalleryGridAdapter);
    }

    private void initData() {
        Intent intent = getIntent();
        mMaxSize = intent.getIntExtra(MyExtra.KEY_GALLERY_MAX_SIZE, -1);
        mHaveSelectedPaths = intent.getStringArrayExtra(MyExtra.KEY_GALLERY_HAS_SELECTED);
        mMimeType = intent.getStringExtra(MyExtra.KEY_GALLERY_MIME_TYPE);
        mGalleryData = getGalleryData(this, mMimeType);
        mGalleryGridAdapter.update(mGalleryData);
    }


    @OnItemClick(R.id.gv_photo_list)
    public void onPhotoListClick(int position) {
        MyEnter.openGalleryDetail(this, mMaxSize, mHaveSelectedPaths, getFolderId(position), mMimeType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (RESULT_PICK_IMAGE == requestCode) {
            String[] paths = data.getStringArrayExtra(MyExtra.KEY_GALLERY_FOLDER_ARRAY);
            Intent intent = new Intent();
            intent.putExtra(MyExtra.KEY_GALLERY_FOLDER_ARRAY, paths);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public GalleryInfoModel getGalleryInfoModel(int position) {
        if (mGalleryData != null) {
            return mGalleryData.get(position);
        }
        return null;
    }

    public String getFolderId(int position) {
        GalleryInfoModel galleryInfoModel = getGalleryInfoModel(position);
        if (galleryInfoModel != null) {
            return galleryInfoModel.folderId;
        }
        return "";
    }

    public List<GalleryInfoModel> getGalleryData(Context context, String mimeType) {
        if (mimeType == null) {
            mimeType = "";
        }
        String selection;
        switch (mimeType) {
            case "image/jpeg":
                selection = MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\") group by (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                break;
            case "image/png":
                selection = MediaStore.Images.Media.MIME_TYPE + "=\"image/png\") group by (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                break;
            default:
                selection = MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\" or " + MediaStore.Images.Media.MIME_TYPE + "=\"image/png\") group by (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
        }

        String detailsSelection;
        switch (mimeType) {
            case "image/jpeg":
                detailsSelection = MediaStore.Images.Media.BUCKET_ID + "=? and " + MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\"";
                break;
            case "image/png":
                detailsSelection = MediaStore.Images.Media.BUCKET_ID + "=? and " + MediaStore.Images.Media.MIME_TYPE + "=\"image/png\"";
                break;
            default:
                detailsSelection = MediaStore.Images.Media.BUCKET_ID + "=? and (" + MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\" or " + MediaStore.Images.Media.MIME_TYPE + "=\"image/png\")";
        }
        List<GalleryInfoModel> data = new ArrayList<GalleryInfoModel>();
        String[] projection = new String[]{MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID, // 直接包含该图片文件的文件夹ID，防止在不同下的文件夹重名
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, // 直接包含该图片文件的文件夹名
                MediaStore.Images.Media.MIME_TYPE,//mimeType
                MediaStore.Images.Media.DATA // 图片绝对路径
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, "");

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int folderIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                int folderColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int pathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String bucketId = cursor.getString(folderIdColumn);
                if (TextUtils.isEmpty(bucketId)) {
                    continue;
                }
                String[] pathProjection = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE}; // 图片绝对路径

                Cursor c = context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, pathProjection, detailsSelection, new String[]{bucketId}, "");

                if (c != null) {
                    GalleryInfoModel galleryInfo = new GalleryInfoModel();
                    galleryInfo.folderId = cursor.getString(folderIdColumn);
                    galleryInfo.folderName = cursor.getString(folderColumn);
                    galleryInfo.firstPhotoPath = cursor.getString(pathColumn);
                    galleryInfo.photoCount = c.getCount() + "";
                    data.add(galleryInfo);
                    c.close();
                }
            }
            cursor.close();
        }
        return data;
    }

}