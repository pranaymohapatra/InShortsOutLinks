package com.pranaymohapatra.inshortsoutlinks;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;

import com.pranaymohapatra.inshortsoutlinks.model.NewsModel;
import com.pranaymohapatra.inshortsoutlinks.offlinestorage.NewsDBContract;

import java.util.ArrayList;
import java.util.List;


public class MyDataLoader extends AsyncTaskLoader<List<NewsModel>> {

    final ForceLoadContentObserver mObserver;

    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    List<NewsModel> mList;
    CancellationSignal mCancellationSignal;

    public MyDataLoader(Context context, Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;

    }

    @Override
    public List<NewsModel> loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder, mCancellationSignal);
            ArrayList list = new ArrayList<>();
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        NewsModel model = new NewsModel();
                        model.setID(cursor.getInt(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_ID)));
                        model.setTITLE(cursor.getString(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_TITLE)));
                        model.setURL(cursor.getString(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_URL)));
                        model.setCATEGORY(cursor.getString(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_CATEGORY)));
                        model.setHOSTNAME(cursor.getString(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_HOSTNAME)));
                        model.setPUBLISHER(cursor.getString(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_PUBLISHER)));
                        model.setTIMESTAMP(cursor.getLong(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_TIMESTAMP)));
                        model.setIsFavorite(cursor.getInt(cursor.getColumnIndex(NewsDBContract.Schema.COLUMN_NAME_FAVORITE)));
                        list.add(model);
                    }
                    getContext().getContentResolver().registerContentObserver(mUri, false, mObserver);

                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            return list;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    @Override
    public void deliverResult(List<NewsModel> list) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }
        mList = list;

        if (isStarted()) {
            super.deliverResult(list);
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    @Override
    protected void onStartLoading() {
        if (mList != null) {
            deliverResult(mList);
        }
        if (takeContentChanged() || mList == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }
}
