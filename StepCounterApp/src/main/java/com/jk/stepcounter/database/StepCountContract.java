package com.jk.stepcounter.database;

import android.provider.BaseColumns;

/**
 * 数据库契约类
 * 契约类是用于定义 URI、表格和列名称的常数的容器。 契约类允许您跨同一软件包中的所有其他类使用相同的常数。 您可以在一个位置更改列名称并使其在您整个代码中传播。
 */

public final class StepCountContract {
    //私有构造函数，防止被实例化
    private StepCountContract() {
    }

    //定义表格内容的内部类
    public static class StepData implements BaseColumns {
        /**
         * 表名
         */
        public static final String TABLE_NAME = "step_count";

        /**
         * 记录数据时的日期 <br/>
         *
         * type : TEXT
         */
        public static final String COLUMN_NAME_DATE = "date";


        /**
         * 今天最后的总步数 <br/>
         *
         * type : TEXT
         */
        public static final String COLUMN_NAME_TODAY_FINAL_STEP_COUNT = "today_step_final_count";

        /**
         * 记录数据时检测到最后一步的时间戳 <br/>
         *
         * type : TEXT
         */
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

        /**
         * 最后一次记录数据时的时间戳 <br/>
         *
         * type : TEXT
         */
        public static final String WRITE_DATABASE_TIMESTAMP = "write_timestamp";



        //今天他第一次写数据的时间戳和最后一次写数据的时间戳


        //当天步数等于
        // 1.不关机时，不停止服务：今天结束步数（COLUMN_NAME_BOOT_STEP_COUNT_SUM） - 今天开始记录时步数（COLUMN_NAME_BOOT_YESTERDAY_STEP_COUNT）+ 临时步数 （COLUMN_NAME_BOOT_YESTERDAY_STEP_COUNT）
        //
        // 2.今天关机时，今天又开机：关机时或停止服务时将今天的步数写入COLUMN_NAME_TODAY_STEP_COUNT，启动服务时记录当前的步数写入进起始步数
        /**
         * 历史步数总和的表名
         */
        public static final String TABLE_NAME_SUM = "step_count_sum";


    }
}
