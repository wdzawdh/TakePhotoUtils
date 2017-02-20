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
import android.widget.ImageView;
import android.widget.TextView;

import com.cw.takephotoutils.R;
import com.cw.takephotoutils.gallery.model.GalleryInfoModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Cw
 * @date 16/9/4
 */
public class GalleryGridAdapter extends BaseAdapter {

    private List<GalleryInfoModel> mList;

    public void update(List<GalleryInfoModel> data) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.platform_view_gallery_item, null, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        GalleryInfoModel info = mList.get(position);
        String filePath = info.firstPhotoPath;

        Picasso.with(context).load("file://" + filePath).fit().placeholder(R.drawable.platform_gallery_img_normal_normal).into(viewHolder.iv_image);

        viewHolder.tv_count.setText(info.photoCount + "张");
        String folderName = info.folderName;
        if (folderName.length() > 10) {
            viewHolder.tv_name.setText(folderName.substring(0, 10) + "...");
        } else {
            viewHolder.tv_name.setText(folderName);
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.iv_image)
        ImageView iv_image;
        @BindView(R.id.tv_count)
        TextView tv_count;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}