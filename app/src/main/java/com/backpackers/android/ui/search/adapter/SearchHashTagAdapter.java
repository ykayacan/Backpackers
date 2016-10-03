package com.backpackers.android.ui.search.adapter;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.HashTag;
import com.backpackers.android.ui.listeners.OnItemClickListener;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SearchHashTagAdapter extends RecyclerView.Adapter<SearchHashTagAdapter.SearchHashTagHolder> {

    private final SearchHashTagDiffCallback mCallback;

    private Subscription mSubscription;

    private List<HashTag> mItems = new ArrayList<>();

    private OnItemClickListener<Object> mOnItemClickListener;

    public SearchHashTagAdapter(SearchHashTagDiffCallback callback,
                                OnItemClickListener<Object> onItemClickListener) {
        mCallback = callback;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public SearchHashTagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_hashtag, parent, false);
        final SearchHashTagHolder holder = new SearchHashTagHolder(view);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final HashTag hashTag = getItem(pos);

                    mOnItemClickListener.onItemClick(v, hashTag);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(SearchHashTagHolder holder, int position) {
        final HashTag hashTag = getItem(position);

        holder.mHashTagTv.setText(hashTag.getHashTag());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(final List<HashTag> items) {
        mCallback.setOldList(mItems);
        mCallback.setNewList(items);

        mSubscription = Observable.just(DiffUtil.calculateDiff(mCallback))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DiffUtil.DiffResult>() {
                    @Override
                    public void call(DiffUtil.DiffResult diffResult) {
                        mItems.clear();
                        mItems.addAll(items);

                        diffResult.dispatchUpdatesTo(SearchHashTagAdapter.this);

                        safelyUnSubscribe();
                    }
                });
    }

    private HashTag getItem(int position) {
        return mItems.get(position);
    }

    private void safelyUnSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    static class SearchHashTagHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_hashTag)
        TextView mHashTagTv;

        public SearchHashTagHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
