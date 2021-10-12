package com.funny.translation.js

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.funny.translation.js.bean.JsBean
import kotlinx.coroutines.flow.Flow

@Dao
interface JsDao {
    @Query("select * from table_js")
    fun getAllJs() : Flow<List<JsBean>>

    @Query("select count(*) from table_js")
    fun getJsCount() : Int

    @Insert
    fun insertJs(jsBean: JsBean)

    @Insert
    fun insertJsList(jsBeans: List<JsBean>)
}