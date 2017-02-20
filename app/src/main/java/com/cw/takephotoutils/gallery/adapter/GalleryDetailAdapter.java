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

package com.cw.takephotoutils.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.cw.takephotoutils.R;
import com.cw.takephotoutils.gallery.model.GalleryDetailInfoModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @author Cw
 * @date 16/9/4
 */
public class GalleryDetailAdapter extends BaseAdapter {

    private List<GalleryDetailInfoModel> mList;

    public void update(List<GalleryDetailInfoModel> data) {
        mList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mList != null ? mList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.platform_view_gallery_detail_item, null, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String filePath = mList.get(position).filePath;
        Picasso.with(context).load("file://" + filePath).fit().placeholder(R.drawable.platform_gallery_img_normal_normal).into(viewHolder.iv_image);
        viewHolder.cb_check.setChecked(mList.get(position).isChecked);

        //点击图片改变checkBox状态
        viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = mList.get(position).isChecked;
                //checkBox将要改变的状态
                boolean changeCheck = !checked;
                //如果将要变为选中状态
                if (changeCheck) {
                    if (listener.onCheck(filePath)) {//告诉调用者被点击的文件,由外部告诉是否可以选中
                        viewHolder.cb_check.setChecked(true);
                        mList.get(position).isChecked = true;//防止图片错位,记录状态
                    }
                } else {
                    viewHolder.cb_check.setChecked(false);
                    mList.get(position).isChecked = false;
                    listener.unCheck(filePath);//告诉调用者被取消的文件
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.iv_image)
        ImageView iv_image;
        @BindView(R.id.cb_check)
        CheckBox cb_check;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public interface OnSelectPhotoListener {
        boolean onCheck(String filePath);

        void unCheck(String filePath);
    }

    private OnSelectPhotoListener listener;

    /**
     * 监听图片是否选中状态
     */
    public void setOnSelectPhotoListener(OnSelectPhotoListener listener) {
        this.listener = listener;
    }
}