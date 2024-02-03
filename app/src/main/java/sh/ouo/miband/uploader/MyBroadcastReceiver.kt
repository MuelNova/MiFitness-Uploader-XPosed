package sh.ouo.miband.uploader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.Exception
import java.lang.reflect.Array
import java.lang.reflect.Method
import java.util.Objects
import kotlin.reflect.KClass

class MyBroadcastReceiver(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val instance: Any
    ) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.extras?.let { extras ->
            val type_ = extras.getString("type", "STEP")
        }
//        Log.d("MiBand", "I have received it!")
//        val stepEnumValue = XposedHelpers.getStaticObjectField(homeDataType, "STEP") // 假设 STEP 是枚举中的一个值
//        Log.d("MiBand", stepEnumValue.toString())
//        Log.d("MiBand", homeDataType.toGenericString())
////        Log.d("MiBand", func.toGenericString())
//        val result = XposedHelpers.callMethod(instance, "getReportList", stepEnumValue, 1706745600, 1706832000) as ArrayList<*>
//        result.forEach {report ->
//            Log.d("MiBand", XposedHelpers.getIntField(report, "steps").toString())
//            Log.d("MiBand", report.toString())
//        }
    }
}