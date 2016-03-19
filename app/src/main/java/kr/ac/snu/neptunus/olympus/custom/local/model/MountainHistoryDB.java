package kr.ac.snu.neptunus.olympus.custom.local.model;

import android.provider.BaseColumns;

/**
 * Created by jjc93 on 2016-03-20.
 */
public final class MountainHistoryDB {
    public MountainHistoryDB(){}

    public static abstract class MountainHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "mhentry";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_STEP = "step";
        public static final String COLUMN_NAME_HEART_BEAT = "heart_beat";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
    }

    private static final String TEXT_TYPE = "TEXT";
    private static final String INTEGER_TYPE = "INT";
}
