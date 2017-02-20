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

package com.cw.takephotoutils.weiget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 弹出位置在屏幕最下方
 * 选项个数和文字均可自定义
 *
 * @author Cw
 * @date 16/7/7
 */
public class OptionDialog extends Dialog {

    private Context mContext;
    private LinearLayout rootView;
    private TextView mCancelView;

    public OptionDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public OptionDialog(Context context) {
        this(context, android.support.v7.appcompat.R.style.Base_Theme_AppCompat_Dialog);
        mContext = context;
        init();
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.addContentView(view, params);
    }

    private void init() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setBackgroundColor(Color.TRANSPARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        rootView = linearLayout;
        setContentView(rootView);
        mCancelView = new TextView(mContext);
        mCancelView.setTextColor(0xff333333);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        //设置dialog宽度充满屏幕
        window.getDecorView().setPadding(0, 0, 0, 0);
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        this.show();
    }

    /**
     * 设置选项
     *
     * @param stringId stringID
     */
    public void setOptionArray(int... stringId) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        for (int id : stringId) {
            map.put(id, "");
        }
        setOptionArray(map);
    }

    /**
     * 设置选项
     *
     * @param map Key显示的值,Value点击后返回的值
     */
    public void setOptionArray(LinkedHashMap<Integer, String> map) {
        Set<Map.Entry<Integer, String>> set = map.entrySet();
        final ArrayList<Map.Entry<Integer, String>> list = new ArrayList<>(set);
        for (int i = 0; i < list.size(); i++) {
            TextView textView = new TextView(mContext);
            textView.setText(mContext.getString(list.get(i).getKey()));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dip2px(mContext, 48));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(layoutParams);
            textView.setBackgroundColor(Color.WHITE);
            textView.setTextColor(0xff333333);
            rootView.addView(textView);

            final int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnSelect(finalI, list.get(finalI).getValue());
                    dismiss();
                }
            });
            Drawable pressdrawable = drawDrawable(0xcccccccc, 0);
            Drawable normalDrawable = drawDrawable(Color.WHITE, 0);
            Drawable selector = getStateListDrawable(normalDrawable, pressdrawable);
            textView.setBackgroundDrawable(selector);

            View view = new View(mContext);
            view.setBackgroundColor(0xffe4e4e4);
            LinearLayout.LayoutParams cancelLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            view.setLayoutParams(cancelLayoutParams);
            rootView.addView(view);
        }

        mCancelView.setText("取消");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dip2px(mContext, 48));
        layoutParams.setMargins(0, dip2px(mContext, 10), 0, 0);
        mCancelView.setLayoutParams(layoutParams);
        mCancelView.setGravity(Gravity.CENTER);
        Drawable pressdrawable = drawDrawable(0xcccccccc, 0);
        Drawable normalDrawable = drawDrawable(Color.WHITE, 0);
        Drawable selector = getStateListDrawable(normalDrawable, pressdrawable);
        mCancelView.setBackgroundDrawable(selector);
        rootView.addView(mCancelView);
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnSelect(-1, "");
                dismiss();
            }
        });

    }

    /**
     * 设置是否需要取消按钮
     */
    public void setCancleVisible(boolean need) {
        mCancelView.setVisibility(need ? View.VISIBLE : View.GONE);
    }

    /**
     * dip转换px
     */
    private static int dip2px(Context context, int dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * 绘制一个圆角矩形
     */
    private static Drawable drawDrawable(int rgb, int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setGradientType(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(rgb);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }

    /**
     * 代码设置适配器
     */
    private static Drawable getStateListDrawable(Drawable drawDrawable, Drawable pressDrawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed
                , android.R.attr.state_enabled}, pressDrawable);
        stateListDrawable.addState(new int[]{}, drawDrawable);
        return stateListDrawable;
    }

    /**
     * 设置点击回调
     */
    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    protected OnSelectListener listener;

    public interface OnSelectListener {
        void OnSelect(int index, String value);
    }

}