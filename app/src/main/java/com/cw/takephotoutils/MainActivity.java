package com.cw.takephotoutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.cw.takephotoutils.utils.GetPhotoUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.frame1)
    ImageView frame1;
    @BindView(R.id.frame2)
    ImageView frame2;
    @BindView(R.id.frame3)
    ImageView frame3;
    @BindView(R.id.frame4)
    ImageView frame4;
    @BindView(R.id.frame_a)
    ImageView frameA;
    @BindView(R.id.frame_b)
    ImageView frameB;
    @BindView(R.id.frame_c)
    ImageView frameC;
    @BindView(R.id.frame_d)
    ImageView frameD;
    @BindView(R.id.frame_crop)
    ImageView frameCrop;

    private static final String KEY_ITEM_1 = "key_item_1";
    private static final String KEY_ITEM_2 = "key_item_2";

    private ArrayList<String> list1 = new ArrayList<>();
    private ArrayList<String> list2 = new ArrayList<>();
    private GetPhotoUtils mGetPhotoUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mGetPhotoUtils = new GetPhotoUtils();
        mGetPhotoUtils.setOnGetPhotoListener(new GetPhotoUtils.OnGetPhotoListener() {
            @Override
            public void onStart(String orgPath, String itemKey, int position) {
                if (TextUtils.equals(KEY_ITEM_1, itemKey)) {
                    switch (position) {
                        case 0:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frame1);
                            break;
                        case 1:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frame2);
                            break;
                        case 2:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frame3);
                            break;
                        case 3:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frame4);
                            break;
                    }
                }
                if (TextUtils.equals(KEY_ITEM_2, itemKey)) {
                    switch (position) {
                        case 0:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frameA);
                            break;
                        case 1:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frameB);
                            break;
                        case 2:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frameC);
                            break;
                        case 3:
                            Picasso.with(MainActivity.this).load("file://" + orgPath).fit().into(frameD);
                            break;
                    }
                }
            }

            @Override
            public void onNext(String basePath, String cropPath, String compressPath, String itemKey, int position) {

                switch (itemKey) {
                    case KEY_ITEM_1:
                        list1.add(compressPath);
                        break;
                    case KEY_ITEM_2:
                        list2.add(compressPath);
                        break;
                    default:
                        Picasso.with(MainActivity.this).load("file://" + compressPath).fit().into(frameCrop);//测试裁剪
                }
            }

            @Override
            public void onFailure(String itemKey, int position) {
                Log.d("cw", "onFailure");
            }

            @Override
            public void onCancel(int position) {
                Log.d("cw", "onCancel");
            }
        });
    }

    @OnClick(R.id.cream1)
    public void onClick1() {
        mGetPhotoUtils.showDialog(MainActivity.this, 0, 4, 200, KEY_ITEM_1, list1);
    }

    @OnClick(R.id.cream2)
    public void onClick2() {
        mGetPhotoUtils.showDialog(MainActivity.this, 1, 3, 200, KEY_ITEM_1, list1);
    }

    @OnClick(R.id.cream3)
    public void onClick3() {
        mGetPhotoUtils.showDialog(MainActivity.this, 2, 2, 200, KEY_ITEM_1, list1);
    }

    @OnClick(R.id.cream4)
    public void onClick4() {
        mGetPhotoUtils.showDialog(MainActivity.this, 3, 1, 200, KEY_ITEM_1, list1);
    }

    @OnClick(R.id.cream_a)
    public void onClicka() {
        mGetPhotoUtils.showDialog(MainActivity.this, 0, 4, 200, KEY_ITEM_2, list2);
    }

    @OnClick(R.id.cream_b)
    public void onClickb() {
        mGetPhotoUtils.showDialog(MainActivity.this, 1, 3, 200, KEY_ITEM_2, list2);
    }

    @OnClick(R.id.cream_c)
    public void onClickc() {
        mGetPhotoUtils.showDialog(MainActivity.this, 2, 2, 200, KEY_ITEM_2, list2);
    }

    @OnClick(R.id.cream_d)
    public void onClickd() {
        mGetPhotoUtils.showDialog(MainActivity.this, 3, 1, 200, KEY_ITEM_2, list2);
    }

    @OnClick(R.id.bt_crop)
    public void onClickCrop() {
        mGetPhotoUtils.showDialog(MainActivity.this, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGetPhotoUtils.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetPhotoUtils.clear();
    }
}
