package com.pranaymohapatra.inshortsoutlinks;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pranaymohapatra.inshortsoutlinks.model.NewsApiClient;
import com.pranaymohapatra.inshortsoutlinks.model.NewsModel;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsContentProvider;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsDBContract;
import com.pranaymohapatra.inshortsoutlinks.recyclerview.NewsAdapter;
import com.pranaymohapatra.inshortsoutlinks.recyclerview.NewsScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsModel>>, FavoritesActvity.NewsCallBacks {

    public static MainActivity instance;
    public static boolean makeFav;
    public List<NewsModel> mList = new ArrayList<>();
    //flags
    boolean isLoading = false;
    boolean isLastPage = false;
    int TOTAL_PAGES;
    int currentPage = 1;
    int PAGE_SIZE = 100;
    int LOADERID = 10010;
    boolean isDesc = true;
    RecyclerView mArticleRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    NewsAdapter mAdapter;
    View progressBar;
    ContentResolver cr;
    MyDataLoader myDataLoader;
    String[] mProjection = {NewsDBContract.Schema.COLUMN_NAME_ID,
            NewsDBContract.Schema.COLUMN_NAME_TITLE,
            NewsDBContract.Schema.COLUMN_NAME_URL,
            NewsDBContract.Schema.COLUMN_NAME_PUBLISHER,
            NewsDBContract.Schema.COLUMN_NAME_CATEGORY,
            NewsDBContract.Schema.COLUMN_NAME_HOSTNAME,
            NewsDBContract.Schema.COLUMN_NAME_TIMESTAMP,
            NewsDBContract.Schema.COLUMN_NAME_FAVORITE};

    public static MainActivity getActivityInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = MainActivity.this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        progressBar = findViewById(R.id.progressbarlayoutmain);


        mArticleRecyclerView = (RecyclerView) findViewById(R.id.ArticleRecyclerView);
        mAdapter = new NewsAdapter(mList, this);
        mLayoutManager = new LinearLayoutManager(this);
        mArticleRecyclerView.setHasFixedSize(true);
        mArticleRecyclerView.setAdapter(mAdapter);
        mArticleRecyclerView.setLayoutManager(mLayoutManager);
        mArticleRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mArticleRecyclerView.addOnScrollListener(new NewsScrollListener(mLayoutManager) {

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
        initLoader();

        //api calls
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://starlord.hackerearth.com/").addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        NewsApiClient service = retrofit.create(NewsApiClient.class);

        service.getNews()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<NewsModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<NewsModel> value) {
                        new MyLoaderTask().execute(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void loadFirstPage() {
        Log.d("Pranay", "LoadFirstCalled");
        List<NewsModel> firstPage = mList.subList(0, PAGE_SIZE);
        mAdapter.addData(firstPage);
        progressBar.setVisibility(View.INVISIBLE);

        if (currentPage < TOTAL_PAGES) {
            mAdapter.addFooter();
        } else
            isLastPage = true;
    }

    private void loadNext() {
        int toLoad = mList.size() - mAdapter.getItemCount();
        if (toLoad > 0)
            toLoad = toLoad < PAGE_SIZE ? toLoad : PAGE_SIZE;
        else {
            toLoad = 0;
        }

        try {
            List<NewsModel> nextPage = mList.
                    subList(mAdapter.getItemCount(),
                            mAdapter.getItemCount() + toLoad);
            mAdapter.removeFooter();
            isLoading = false;
            mAdapter.addData(nextPage);
        } catch (Exception e) {
            e.printStackTrace();
            mAdapter.removeFooter();
            isLoading = false;
        }

        if (currentPage <= TOTAL_PAGES)
            mAdapter.addFooter();
        else isLastPage = true;
    }

    public int insertIntoTable(List<NewsModel> list) {     //inserts whole data from api
        cr = getContentResolver();
        ContentValues[] cvs = new ContentValues[list.size()];
        for (int i = 0; i < list.size(); i++) {
            NewsModel model = list.get(i);
            cvs[i] = new ContentValues();
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_ID, model.getID());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_TITLE, model.getTITLE());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_URL, model.getURL());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_PUBLISHER, model.getPUBLISHER());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_CATEGORY, model.getCATEGORY());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_HOSTNAME, model.getHOSTNAME());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_TIMESTAMP, model.getTIMESTAMP());
            cvs[i].put(NewsDBContract.Schema.COLUMN_NAME_FAVORITE, 0);
        }
        return cr.bulkInsert(NewsContentProvider.CONTENT_URI, cvs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.favorites:
                Intent intent = new Intent(this, FavoritesActvity.class);
                startActivity(intent);
                break;
            case R.id.sort:
                if (isDesc) {
                    Collections.sort(mList, new ArticleComparator());
                    item.setIcon(R.drawable.sortascending);
                    isDesc = false;
                } else {
                    item.setIcon(R.drawable.sortdown);
                    Collections.sort(mList, new ArticleDescComparator());
                    isDesc = true;
                }
                mAdapter.removeAllData();
                currentPage = 1;
                isLastPage = false;
                isLoading = false;
                loadFirstPage();
                break;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<NewsModel>> onCreateLoader(int id, Bundle args) {
        if (id == LOADERID) {
            myDataLoader = new MyDataLoader(MainActivity.this, NewsContentProvider.CONTENT_URI, mProjection, null, null, null);
            return myDataLoader;
        } else return null;
    }

    @Override
    public void onLoadFinished(Loader<List<NewsModel>> loader, List<NewsModel> data) {
        Log.i("pranay", "        CursorLoaderMain");
        mList = data;

        if (data.size() > 0) {
            setPages(mList.size());
            loadFirstPage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsModel>> loader) {

    }

    private void initLoader() {
        getLoaderManager().initLoader(LOADERID, null, this);
    }

    private void setPages(int size) {
        PAGE_SIZE = (mList.size() > PAGE_SIZE) ? PAGE_SIZE : mList.size();
        TOTAL_PAGES = size % PAGE_SIZE == 0 ? size / PAGE_SIZE : size / PAGE_SIZE + 1;
    }

    @Override
    public void updateFavorite() {
        progressBar.setVisibility(View.VISIBLE);
        mAdapter.removeAllData();
        getLoaderManager().restartLoader(LOADERID, null, this);
    }

    public static class ArticleComparator implements Comparator<NewsModel> {
        @Override
        public int compare(NewsModel o1, NewsModel o2) {
            return Long.compare(o2.getTIMESTAMP(), o1.getTIMESTAMP());
        }
    }

    public static class ArticleDescComparator implements Comparator<NewsModel> {
        @Override
        public int compare(NewsModel o1, NewsModel o2) {
            return (Long.compare(o1.getTIMESTAMP(), o2.getTIMESTAMP()));
        }
    }

    public class MyLoaderTask extends AsyncTask<List<NewsModel>, Void, Integer> {

        @Override
        protected Integer doInBackground(List<NewsModel>... params) {
            int result = insertIntoTable(params[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}
