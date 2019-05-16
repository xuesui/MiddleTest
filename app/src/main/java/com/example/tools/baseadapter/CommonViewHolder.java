package com.example.tools.baseadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonViewHolder {
    private SparseArray<View> mView;
    private Context mContext;
    private View item;
    private int position;

    public CommonViewHolder(Context mContext, ViewGroup partent, int layoutRes) {
        this.mView = new SparseArray<>();
        this.mContext = mContext;
        View convertView = LayoutInflater.from(mContext).inflate(layoutRes, partent, false);
        convertView.setTag(this);
        this.item = convertView;
    }

    public static CommonViewHolder bind(Context mContext, View convertView, ViewGroup partent, int mLayoutRes, int position) {
        CommonViewHolder holder;
        if (convertView == null) {
            holder = new CommonViewHolder(mContext, partent, mLayoutRes);
        } else {
            holder = (CommonViewHolder) convertView.getTag();
            holder.item = convertView;
        }
        holder.position = position;
        return holder;
    }

    public View getItemView() {
        return item;
    }

    public <T extends View> T getView(int id) {
        T t = (T) mView.get(id);
        if (t == null) {
            t = item.findViewById(id);
            mView.put(id, t);
        }
        return t;
    }


    //为TextView赋值
    public CommonViewHolder setTextView(int id, String text) {
        TextView view = getView(id);
        view.setText(text);
        return this;
    }

    //为ImageView赋值——drawableId
    public CommonViewHolder setImageResource(int id, int drawableId) {
        ImageView view = getView(id);
        view.setImageResource(drawableId);
        return this;
    }

    //为ImageView赋值——bitmap

    public CommonViewHolder setImageBitmap(int id, Bitmap bitmap) {
        ImageView view = getView(id);
        view.setImageBitmap(bitmap);
        return this;
    }

}
