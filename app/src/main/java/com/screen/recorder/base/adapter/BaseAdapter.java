package com.screen.recorder.base.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.screen.recorder.base.adapter.holder.BaseViewHolder;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context context;
    private List<T> list;

    public BaseAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public void addItem(int position, T data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void changeItem(int position, T data) {
        list.set(position, data);
        notifyItemChanged(position, data);
    }

    public void updateList(List<T> listData) {
        list = listData;
        notifyDataSetChanged();
    }

    protected abstract int getResLayout();

    protected abstract BaseViewHolder getViewHolder(ViewGroup viewGroup);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (this.context == null)
            return null;

        return getViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        bindView(viewHolder, position);
    }

    protected abstract void bindView(RecyclerView.ViewHolder viewHolder, int position);
}