package com.backpackers.android.ui.search.adapter;

import com.google.android.gms.location.places.PlaceLikelihood;

import com.backpackers.android.R;
import com.backpackers.android.ui.listeners.OnItemClickListener;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<PlaceLikelihood> mItems = new ArrayList<>();

    private OnItemClickListener<PlaceLikelihood> mOnItemClickListener;

    public LocationAdapter(OnItemClickListener<PlaceLikelihood> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_location, parent, false);
        final LocationViewHolder holder = new LocationViewHolder(view);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    final PlaceLikelihood place = mItems.get(pos);
                    mOnItemClickListener.onItemClick(v, place);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        final PlaceLikelihood item = mItems.get(position);

        holder.mLocationText.setText(item.getPlace().getName());
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    public void setItems(final List<PlaceLikelihood> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyItemRangeInserted(0, mItems.size());
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_location)
        TextView mLocationText;

        public LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
