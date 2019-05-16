package com.example.tools.baseadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter{
    private ArrayList<T> list;
    private int mLayout;

    public CommonAdapter(ArrayList<T> list,int mLayout){
        this.list=list;
        this.mLayout=mLayout;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder holder = CommonViewHolder.bind(parent.getContext(), convertView, parent, mLayout, position);
        bindView(holder, getItem(position));
        return holder.getItemView();
    }

    protected abstract void bindView(CommonViewHolder holder, T item);

}
