package com.pranaymohapatra.inshortsoutlinks.recyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


public abstract class NewsScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager layoutManager;
    NewsAdapter mAdapter;
    Context main;

    public NewsScrollListener(RecyclerView.LayoutManager manager) {
        this.layoutManager = (LinearLayoutManager) manager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItems = layoutManager.getChildCount();
        int totalItems = layoutManager.getItemCount();
        int positionOfFirstVisible = layoutManager.findFirstVisibleItemPosition();
        boolean isloading = isLoading();
        boolean islastpage = isLastPage();

        if (!isloading && !islastpage) {
            if ((visibleItems + positionOfFirstVisible) >= totalItems
                    && positionOfFirstVisible >= 0) {
                loadNextPage();
            }
        }
    }

    public abstract void loadNextPage();

    public abstract boolean isLoading();

    public abstract boolean isLastPage();

    public abstract int getPageCount();

}
