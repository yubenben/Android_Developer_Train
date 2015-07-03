package com.ben.sample.myfirstapp.com.ben.sample.myfirstapp.data;


import android.provider.BaseColumns;

public class FeedReaderContract {

    public FeedReaderContract() {}

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRYID = "entryid";
        public static final String COLUMN_NAME_TITILE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }
}
