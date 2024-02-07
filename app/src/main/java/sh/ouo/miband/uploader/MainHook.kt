package sh.ouo.miband.uploader

import android.R.attr.classLoader
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class MainHook : IXposedHookLoadPackage {
    companion object {
        @Volatile
        var isReceiverRegistered = false
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.mi.health") return
        hook(lpparam)
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookConstructor(
            "com.xiaomi.fit.fitness.remote.FitnessSyncRemoteImpl",
            lpparam.classLoader,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    FitnessSyncRemoteImpl.instance = param.thisObject
                    Log.d("MiBand", "FitnessSyncRemoteObject: ${FitnessSyncRemoteImpl.instance}")
                    super.beforeHookedMethod(param)
                }

                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)
                }
            })
        XposedHelpers.findAndHookMethod(
            "com.xiaomi.fitness.keep_alive.KeepAliveHelper",
            lpparam.classLoader,
            "startService",
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    if ( !isReceiverRegistered ) {
                        Log.d("MiBand", "MiUploader Hook Startup...")
                        val updaterClass = XposedHelpers.findClass("com.xiaomi.fitness.aggregation.health.updater.CommonSummaryUpdater", lpparam.classLoader)
                        val companionInstance = XposedHelpers.getStaticObjectField(updaterClass, "Companion")
                        val commonSummaryUpdaterInstance = XposedHelpers.callMethod(companionInstance, "getInstance")
//                        val receiver = MyBroadcastReceiver(lpparam, commonSummaryUpdaterInstance);
//                        val filter = IntentFilter("sh.ouo.miband.uploader")
//                        val app = XposedHelpers.findMethodExact(
//                            "com.xiaomi.fitness.common.utils.AppUtil",
//                            lpparam.classLoader,
//                            "getApp").invoke(null)
//                        val ctx = XposedHelpers.callMethod(app, "getApplicationContext") as Context
//                        Log.d("MiBand", "ctx: $ctx")
//                        Log.d("MiBand", "receiver: $receiver")
//
//                        ctx.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
                        MySocketServer(23235, lpparam, commonSummaryUpdaterInstance).startServerInBackground()


                        Log.d("MiBand","MiUploader Receiver Deployed!")

                        isReceiverRegistered = true
                    }
                    super.afterHookedMethod(param)
                }
            })
    }
}