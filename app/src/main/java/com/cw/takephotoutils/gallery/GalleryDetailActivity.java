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
import android.widget.Toast;

import com.cw.takephotoutils.MyExtra;
import com.cw.takephotoutils.R;
import com.cw.takephotoutils.gallery.adapter.GalleryDetailAdapter;
import com.cw.takephotoutils.gallery.model.GalleryDetailInfoModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Cw
 * @date 16/9/4
 */
public class GalleryDetailActivity extends Activity {

    @BindView(R.id.gv_photo_list)
    GridView gv_photo_list;

    private int mMaxSize;
    private GalleryDetailAdapter mGalleryDetailAdapter;
    private ArrayList<String> mPaths = new ArrayList<>();
    private String[] mHaveSelectedPaths;//已选path


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.platform_activity_gallery_detail);
        ButterKnife.bind(this);
        initView();
        initListener();
        initData();
    }

    private void initView() {
        mGalleryDetailAdapter = new GalleryDetailAdapter();
        gv_photo_list.setAdapter(mGalleryDetailAdapter);
    }

    private void initListener() {

        mGalleryDetailAdapter.setOnSelectPhotoListener(new GalleryDetailAdapter.OnSelectPhotoListener() {
            @Override
            public boolean onCheck(String filePath) {
                if (mHaveSelectedPaths != null) {
                    for (String path : mHaveSelectedPaths) {
                        if (TextUtils.equals(path, filePath)) {
                            Toast.makeText(GalleryDetailActivity.this, getString(R.string.platform_gallery_photo_has_select), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                }
                if (mPaths.size() < mMaxSize) {
                    mPaths.add(filePath);
                    return true;
                }
                Toast.makeText(GalleryDetailActivity.this, getString(R.string.platform_grllery_more_select) + mMaxSize + getString(R.string.platform_gallery_zhang), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void unCheck(String filePath) {
                for (int i = 0; i < mPaths.size(); i++) {
                    if (TextUtils.equals(mPaths.get(i), filePath)) {
                        mPaths.remove(i);
                    }
                }
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        mMaxSize = intent.getIntExtra(MyExtra.KEY_GALLERY_MAX_SIZE, -1);
        mHaveSelectedPaths = intent.getStringArrayExtra(MyExtra.KEY_GALLERY_HAS_SELECTED);
        String folderId = intent.getStringExtra(MyExtra.KEY_GALLERY_FOLDER_ID);
        String mimeType = intent.getStringExtra(MyExtra.KEY_GALLERY_MIME_TYPE);
        List<GalleryDetailInfoModel> galleryDetailData = getGalleryDetailData(getContentResolverData(this, folderId, mimeType));
        mGalleryDetailAdapter.update(galleryDetailData);

    }


    @OnClick(R.id.bt_select)
    public void onClick() {
        if (mPaths.size() > 0) {
            Intent intent = new Intent();
            String[] paths = mPaths.toArray(new String[mPaths.size()]);
            intent.putExtra(MyExtra.KEY_GALLERY_FOLDER_ARRAY, paths);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private List<GalleryDetailInfoModel> getGalleryDetailData(List<String> data) {
        List<GalleryDetailInfoModel> detailInfoList = new ArrayList<>();
        if (data != null) {
            for (String filePath : data) {
                GalleryDetailInfoModel detailInfoModel = new GalleryDetailInfoModel();
                detailInfoModel.filePath = filePath;
                detailInfoModel.isChecked = false;//初始为未选中状态
                detailInfoList.add(detailInfoModel);
            }
        }
        return detailInfoList;
    }

    public List<String> getContentResolverData(Context context, String folderId, String mimeType) {
        if (TextUtils.isEmpty(folderId)) {
            return null;
        }
        if (mimeType == null) {
            mimeType = "";
        }
        String selection;
        switch (mimeType) {
            case "image/jpeg":
                selection = MediaStore.Images.Media.BUCKET_ID + "=? and " + MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\"";
                break;
            case "image/png":
                selection = MediaStore.Images.Media.BUCKET_ID + "=? and " + MediaStore.Images.Media.MIME_TYPE + "=\"image/png\"";
                break;
            default:
                selection = MediaStore.Images.Media.BUCKET_ID + "=? and (" + MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\" or " + MediaStore.Images.Media.MIME_TYPE + "=\"image/png\")";
        }
        List<String> data = new ArrayList<String>();
        String[] projection = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE};

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{folderId}, "");

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int pathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int size = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                if (cursor.getInt(size) != 0) {//size为0说明是失效文件
                    data.add(cursor.getString(pathColumn));
                }
            }
            cursor.close();
        }
        return data;
    }

}