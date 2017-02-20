package com.cw.takephotoutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.cw.takephotoutils.utils.GetPhotoUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.img)
    ImageView mImg;
    private GetPhotoUtils mGetPhotoUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGetPhotoUtils = new GetPhotoUtils();
        mGetPhotoUtils.setOnGetPhotoListener(new GetPhotoUtils.OnGetPhotoListener() {
            @Override
            public void onStart(String orgPath, String itemKey, int position) {
                Log.d("GetPhotoUtils", "onStart" + orgPath);
            }

            @Override
            public void onNext(String basePath, String cropPath, String compressPath, String itemKey, int position) {
                Picasso.with(BaseApplication.sAppContext).load("file://" + compressPath).into(mImg);
                Log.d("GetPhotoUtils", "onNext" + compressPath);
            }

            @Override
            public void onFailure(String itemKey, int position) {
                Log.d("GetPhotoUtils", "onFailure" + position);
            }

            @Override
            public void onCancel(int position) {
                Log.d("GetPhotoUtils", "onCancel" + position);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGetPhotoUtils.onActivityResult(this, requestCode, resultCode, data);
    }

    @OnClick(R.id.btn)
    public void onClick1() {
        mGetPhotoUtils.showDialog(this, 0, 8, 400);
    }

    @OnClick(R.id.btn_unbind)
    public void onClick2() {
        mGetPhotoUtils.clear();
    }
}
