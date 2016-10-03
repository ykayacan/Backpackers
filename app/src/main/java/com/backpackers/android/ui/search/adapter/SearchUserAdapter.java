package com.backpackers.android.ui.search.adapter;

import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.ui.listeners.OnItemClickListener;
import com.backpackers.android.ui.widget.CircleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.SearchUserHolder> {

    private final SearchPeopleDiffCallback mCallback;

    private Subscription mSubscription;

    private List<Account> mItems = new ArrayList<>();

    private OnItemClickListener<Object> mOnItemClickListener;

    public SearchUserAdapter(SearchPeopleDiffCallback callback,
                             OnItemClickListener<Object> onItemClickListener) {
        mCallback = callback;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public SearchUserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_people, parent, false);
        final SearchUserHolder holder = new SearchUserHolder(view);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final Account account = getItem(pos);

                    mOnItemClickListener.onItemClick(v, account);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(SearchUserHolder holder, int position) {
        final Account account = getItem(position);

        Glide.with(holder.itemView.getContext())
                .load(account.getProfileImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(80, 80)
                .fitCenter()
                .into(holder.mAvatarIv);

        holder.mUsernameTv.setText(account.getUsername());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(final List<Account> items) {
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

                        diffResult.dispatchUpdatesTo(SearchUserAdapter.this);

                        safelyUnSubscribe();
                    }
                });
    }

    private Account getItem(int position) {
        return mItems.get(position);
    }

    private void safelyUnSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    static class SearchUserHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_avatar)
        CircleImageView mAvatarIv;

        @BindView(R.id.text_username)
        TextView mUsernameTv;

        SearchUserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
