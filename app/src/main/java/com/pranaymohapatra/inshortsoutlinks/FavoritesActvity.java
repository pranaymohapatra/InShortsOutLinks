package com.pranaymohapatra.inshortsoutlinks;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pranaymohapatra.inshortsoutlinks.model.NewsModel;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsContentProvider;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsDBContract;
import com.pranaymohapatra.inshortsoutlinks.recyclerview.NewsAdapter;
import com.pranaymohapatra.inshortsoutlinks.recyclerview.NewsScrollListener;

import java.util.ArrayList;
import java.util.List;

import static com.pranaymohapatra.inshortsoutlinks.MainActivity.getActivityInstance;

public class FavoritesActvity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsModel>> {

    public static boolean favchanged = false;
    boolean isLoading = false;
    boolean isLastPage = false;
    int TOTAL_PAGES;
    int currentPage = 1;
    int PAGE_SIZE = 20;
    int LOADERID = 10010;
    String[] projection = {NewsDBContract.Schema.COLUMN_NAME_ID,
            NewsDBContract.Schema.COLUMN_NAME_TITLE,
            NewsDBContract.Schema.COLUMN_NAME_URL,
            NewsDBContract.Schema.COLUMN_NAME_PUBLISHER,
            NewsDBContract.Schema.COLUMN_NAME_CATEGORY,
            NewsDBContract.Schema.COLUMN_NAME_HOSTNAME,
            NewsDBContract.Schema.COLUMN_NAME_TIMESTAMP,
            NewsDBContract.Schema.COLUMN_NAME_FAVORITE};
    String selection = NewsDBContract.Schema.COLUMN_NAME_FAVORITE + " = 1";

    RecyclerView favoriteRecyclerView;
    View progressBar;
    List<NewsModel> favoriteList = new ArrayList<>();
    NewsCallBacks callBacks;
    private NewsAdapter favoriteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_actvity);
        favchanged = false;
        callBacks = (NewsCallBacks) getActivityInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.favtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressbarlayout);

        initLoader();

        favoriteRecyclerView = (RecyclerView) findViewById(R.id.FavoriteRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FavoritesActvity.this);
        favoriteRecyclerView.setLayoutManager(layoutManager);
        favoriteAdapter = new NewsAdapter(favoriteList, FavoritesActvity.this);
        favoriteRecyclerView.setAdapter(favoriteAdapter);
        favoriteRecyclerView.addOnScrollListener(new NewsScrollListener(layoutManager) {

                                                     @Override
                                                     public void loadNextPage() {
                                                         Log.d("Pranay", "loading next pge");
                                                         isLoading = true;
                                                         currentPage += 1;

                                                         // mocking network delay for API call
                                                         new Handler().postDelayed(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                                 loadNext();
                                                             }
                                                         }, 1000);
                                                     }

                                                     @Override
                                                     public boolean isLoading() {
                                                         return isLoading;
                                                     }

                                                     @Override
                                                     public boolean isLastPage() {
                                                         return isLastPage;
                                                     }

                                                     @Override
                                                     public int getPageCount() {
                                                         return TOTAL_PAGES;
                                                     }
                                                 }
        );
    }

    public void loadFirstPage() {
        List<NewsModel> firstPage = favoriteList.subList(0, PAGE_SIZE);
        favoriteAdapter.addData(firstPage);
        progressBar.setVisibility(View.INVISIBLE);

        if (currentPage <= TOTAL_PAGES) {
            favoriteAdapter.addFooter();
        } else
            isLastPage = true;
    }

    private void loadNext() {
        int toLoad = favoriteList.size() - favoriteAdapter.getItemCount();
        if (toLoad > 0)
            toLoad = toLoad < PAGE_SIZE ? toLoad : PAGE_SIZE;
        else {
            toLoad = 0;
        }

        try {
            List<NewsModel> nextPage = favoriteList.
                    subList(favoriteAdapter.getItemCount(),
                            favoriteAdapter.getItemCount() + toLoad);
            favoriteAdapter.removeFooter();
            isLoading = false;
            favoriteAdapter.addData(nextPage);
        } catch (Exception e) {
            e.printStackTrace();
            favoriteAdapter.removeFooter();
            isLoading = false;
        }

        if (currentPage <= TOTAL_PAGES)
            favoriteAdapter.addFooter();
        else isLastPage = true;
    }

    private void initLoader() {
        getLoaderManager().initLoader(LOADERID, null, this);
    }

    @Override
    public Loader<List<NewsModel>> onCreateLoader(int id, Bundle args) {

        if (id == LOADERID) {
            MyDataLoader myDataLoader = new MyDataLoader(FavoritesActvity.this, NewsContentProvider.CONTENT_URI, projection, selection, null, null);
            return myDataLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<NewsModel>> loader, List<NewsModel> data) {
        favoriteList = data;

        if (data.size() > 0) {
            setPages(favoriteList.size());
            loadFirstPage();
        } else {
            new DummyTask().execute();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsModel>> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                //NavUtils.navigateUpFromSameTask(this);   //laggy
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setPages(int size) {
        PAGE_SIZE = (favoriteList.size() > PAGE_SIZE) ? PAGE_SIZE : favoriteList.size();
        TOTAL_PAGES = size % PAGE_SIZE == 0 ? size / PAGE_SIZE : size / PAGE_SIZE + 1;
    }

    @Override
    protected void onPause() {
        if (favchanged) {
            callBacks.updateFavorite();
            favchanged = false;
        }
        super.onPause();
    }

    public interface NewsCallBacks {
        public void updateFavorite();
    }

    public class DummyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "No Favorites", Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
