package com.pranaymohapatra.inshortsoutlinks.offlinestorage;

import android.provider.BaseColumns;


public class NewsDBContract {
    private NewsDBContract() {
    }

    public static class Schema implements BaseColumns {
        public static final String DATABASE_NAME = "NewsArticles.db";
        public static final String TABLE_NAME = "articles";
        public static final String COLUMN_NAME_ID = "ID";
        public static final String COLUMN_NAME_TITLE = "TITLE";
        public static final String COLUMN_NAME_URL = "URL";
        public static final String COLUMN_NAME_PUBLISHER = "PUBLISHER";
        public static final String COLUMN_NAME_CATEGORY = "CATEGORY";
        public static final String COLUMN_NAME_HOSTNAME = "HOSTNAME";
        public static final String COLUMN_NAME_TIMESTAMP = "TIMESTAMP";
        public static final String COLUMN_NAME_FAVORITE = "FAVORITE";
    }
}
