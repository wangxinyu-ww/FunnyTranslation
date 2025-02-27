package com.funny.translation

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import com.tencent.mmkv.MMKV
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates


open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityStack.push(activity)
                Log.d(TAG, "【${activity::class.java.simpleName}】 Created! currentStackSize:${activityStack.size}")
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (activityStack.isNotEmpty()) activityStack.pop()
                Log.d(TAG, "【${activity::class.java.simpleName}】 Destroyed! currentStackSize:${activityStack.size}")
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }
        })
    }

    companion object {
        var ctx : Application by Delegates.notNull()
        val resources: Resources get() = ctx.resources

        @Throws(PackageManager.NameNotFoundException::class)
        fun getLocalPackageInfo(): PackageInfo? {
            return ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_CONFIGURATIONS)
        }

        fun getCurrentActivity() = if (activityStack.isNotEmpty()) activityStack.peek() else null

        private val activityStack = Stack<Activity>()
        private const val TAG = "BaseApplication"
    }


}