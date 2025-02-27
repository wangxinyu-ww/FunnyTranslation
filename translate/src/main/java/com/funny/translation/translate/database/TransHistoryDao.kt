package com.funny.translation.translate.database

import androidx.annotation.Keep
import androidx.paging.PagingSource
import androidx.room.*
import com.funny.translation.helper.JsonX

@Keep
@Entity(tableName = "table_trans_history")
data class TransHistoryBean(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo
    val sourceString: String,
    @ColumnInfo
    val sourceLanguageId: Int,
    @ColumnInfo
    val targetLanguageId: Int,
    @ColumnInfo
    val engineNames: List<String>,
    @ColumnInfo
    val time: Long = System.currentTimeMillis(),
)

@Dao
interface TransHistoryDao {
//    @Query("select * from table_trans_history limit :size offset (:page * :size)")
//    fun queryPaged(page: Int = 0, size: Int = 10) : PagingSource<Int, TransHistoryBean>

    @Query("select * from table_trans_history where id in " +
            "(select max(id) as id from table_trans_history group by sourceString) order by id desc")
    fun queryAllPaging(): PagingSource<Int, TransHistoryBean>

    @Query("delete from table_trans_history where id = :id")
    fun deleteTransHistory(id: Int)

    @Query("delete from table_trans_history where sourceString = :sourceString")
    fun deleteTransHistoryByContent(sourceString: String)

    @Insert
    fun insertTransHistory(transHistoryBean: TransHistoryBean)

    @Query("select * from table_trans_history where time between :startTime and :endTime")
    fun queryAllBetween(startTime: Long, endTime: Long): List<TransHistoryBean>

    @Query("delete from table_trans_history")
    fun clearAll()
}


class StringListConverter {
    @TypeConverter
    fun stringToObject(value: String): List<String> {
        return JsonX.fromJson(value)
    }

    @TypeConverter
    fun objectToString(list: List<String>): String {
        return JsonX.toJson(list)
    }
}