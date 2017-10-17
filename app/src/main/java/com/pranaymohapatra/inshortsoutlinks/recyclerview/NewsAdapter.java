package com.pranaymohapatra.inshortsoutlinks.recyclerview;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.pranaymohapatra.inshortsoutlinks.FavoritesActvity;
import com.pranaymohapatra.inshortsoutlinks.R;
import com.pranaymohapatra.inshortsoutlinks.WebViewActivity;
import com.pranaymohapatra.inshortsoutlinks.model.NewsModel;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsContentProvider;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsDBContract;

import java.util.List;


public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int LOADING = 0;
    final int ITEM = 1;
    public Context mContext;
    boolean isFooterAdded = true;
    private List<NewsModel> dataset;
    public OnLikeListener likeListener = new OnLikeListener() {
        @Override
        public void liked(LikeButton likeButton) {
            int position = Integer.parseInt(likeButton.getTag().toString());
            ContentValues value = new ContentValues();
            value.put(NewsDBContract.Schema.COLUMN_NAME_FAVORITE, 1);
            String selection = NewsDBContract.Schema.COLUMN_NAME_TITLE + "=?";
            String[] selectionargs = {dataset.get(position).getTITLE()};
            try {
                mContext.getContentResolver().update(NewsContentProvider.CONTENT_URI, value, selection, selectionargs);
                dataset.get(position).setIsFavorite(1);
                if ("com.pranaymohapatra.inshortsoutlinks.FavoritesActvity".equals((((Activity) mContext).getLocalClassName()))) {
                    FavoritesActvity.favchanged = true;
                }

            } catch (Exception e) {
                Toast.makeText(mContext, "Cant make favorite", Toast.LENGTH_SHORT);
                e.printStackTrace();
            }
        }

        @Override
        public void unLiked(LikeButton likeButton) {
            int position = Integer.parseInt(likeButton.getTag().toString());
            ContentValues value = new ContentValues();
            value.put(NewsDBContract.Schema.COLUMN_NAME_FAVORITE, 0);
            String selection = NewsDBContract.Schema.COLUMN_NAME_TITLE + "=?";
            String[] selectionargs = {dataset.get(position).getTITLE()};
            try {
                mContext.getContentResolver().update(NewsContentProvider.CONTENT_URI, value, selection, selectionargs);
                dataset.get(position).setIsFavorite(0);
                if ("com.pranaymohapatra.inshortsoutlinks.FavoritesActvity".equals((((Activity) mContext).getLocalClassName()))) {
                    FavoritesActvity.favchanged = true;
                }
            } catch (Exception e) {
                Toast.makeText(mContext, "Cant make favorite", Toast.LENGTH_SHORT);
                e.printStackTrace();
            }
        }
    };

    public View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            int position = Integer.parseInt(v.getTag().toString());
            switch (id) {
                case R.id.textholder:
                    Intent intent = new Intent();
                    intent.setClass(mContext, WebViewActivity.class);
                    intent.putExtra("URL", dataset.get(position).getURL());
                    mContext.startActivity(intent);
                    break;

                case R.id.sharebutton:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TITLE, dataset.get(position).getTITLE());
                    sendIntent.putExtra(Intent.EXTRA_TEXT, dataset.get(position).getURL());
                    sendIntent.setType("text/plain");
                    mContext.startActivity(Intent.createChooser(sendIntent, "Share With..."));
            }

        }
    };


    public NewsAdapter(List<NewsModel> list, Context context) {
        dataset = list;
        mContext = context;
    }

    public void setData(List<NewsModel> data) {
        this.dataset = data;
        notifyDataSetChanged();

    }

    //invoked by layout manager
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        switch (viewType) {
            case ITEM:
                View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card_item, parent, false);
                vh = new ViewHolder(cardView);
                break;

            case LOADING:
                View progressBar = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_progress, parent, false);
                vh = new LoadingView(progressBar);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case ITEM:
                Log.d("PranayLogs", "onBindViewHolder for " + position);
                ViewHolder vh = (ViewHolder) holder;

                vh.textViewHolder.setTag(position);      //setting onclick
                vh.textViewHolder.setOnClickListener(clickListener);

                vh.URLView.setText(dataset.get(position).getURL());         //setting text and button drwbl
                vh.titleView.setText(dataset.get(position).getTITLE());
                if (dataset.get(position).getIsFavorite() == 1) {
                    vh.likeButton.setLiked(true);
                } else
                    vh.likeButton.setLiked(false);

                vh.shareButton.setOnClickListener(clickListener);
                vh.shareButton.setTag(position);

                vh.likeButton.setOnLikeListener(likeListener);   //setting onclick for like
                vh.likeButton.setTag(position);
                break;
            case LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public int getItemViewType(int position) {
        if ((position == dataset.size() - 1) && isFooterAdded) {
            Log.d("Pranay", "loading" + position);
            return LOADING;
        } else return ITEM;
    }

    public void addData(List<NewsModel> newdata) {

        dataset.addAll(newdata);
        notifyDataSetChanged();
    }

    public void add(NewsModel m) {
        dataset.add(m);
        notifyItemInserted(dataset.size() - 1);
    }

    public void addFooter() {
        isFooterAdded = true;
        add(new NewsModel());
    }

    public void removeFooter() {
        isFooterAdded = false;
        int position = dataset.size() - 1;
        NewsModel item = getItem(position);

        if (item != null) {
            dataset.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeAllData() {
        dataset.clear();
    }

    private NewsModel getItem(int position) {
        return dataset.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;
        public TextView URLView;
        public LikeButton likeButton;
        public LinearLayout textViewHolder;
        Button shareButton;

        public ViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.textview1);
            URLView = (TextView) v.findViewById(R.id.textview2);
            likeButton = (LikeButton) v.findViewById(R.id.favbutton);
            likeButton.setUnlikeDrawableRes(R.drawable.starbuttonnotlike);
            textViewHolder = (LinearLayout) v.findViewById(R.id.textholder);
            shareButton = (Button) v.findViewById(R.id.sharebutton);
        }
    }

    public static class LoadingView extends RecyclerView.ViewHolder {
        public LoadingView(View v) {
            super(v);
        }
    }

}
