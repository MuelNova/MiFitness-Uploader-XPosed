package sh.ouo.miband.uploader

import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.serialization.json.JsonElement
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

abstract class DailyReportBase (
    protected val lpparam: LoadPackageParam,
    private val instance: Any
) {
    private var summarySyncManager: Any
    private lateinit var enumValue: Any

    init {
        val updaterClass = XposedHelpers.findClass("com.xiaomi.fitness.aggregation.summary.SummarySyncManager", lpparam.classLoader)
        val companionInstance = XposedHelpers.getStaticObjectField(updaterClass, "Companion")
        summarySyncManager = XposedHelpers.callMethod(companionInstance, "getInstance")
    }

    protected fun setEnumValue(type: String) {
        val homeDataType = XposedHelpers.findClass("com.xiaomi.fit.fitness.export.data.annotation.HomeDataType", lpparam.classLoader)
        enumValue = XposedHelpers.getStaticObjectField(homeDataType, type)
    }

    private fun fitnessSyncRemote() {
        if (FitnessSyncRemoteImpl.instance == null) {
            Log.e("MiBand", "FitnessSyncRemoteImplObject is none, the data might not be updated!")
            return
        }
        XposedHelpers.callMethod(
            FitnessSyncRemoteImpl.instance,
            "triggerDataSync",
            true
        )
    }

    private fun getDay(day: String?, day2: String? = null): Pair<Long, Long> {
        val formatPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val beijingZoneId = ZoneId.of("Asia/Shanghai")
        val today = if (day == null) {
            LocalDate.now(beijingZoneId)
        } else {
            LocalDate.parse(day, formatPattern)
        }
        val startOfDay = today.atStartOfDay(beijingZoneId)
        Log.d("MiBand", startOfDay.toString())
        val startOfDayTimestamp = startOfDay.toEpochSecond()
        val endOfDayTimestamp = if (day2 != null) {
            LocalDate.parse(day2, formatPattern).atStartOfDay(beijingZoneId).toEpochSecond()
        } else {
            startOfDayTimestamp
        }
        return Pair(startOfDayTimestamp, endOfDayTimestamp)
    }

    fun getDailyReport(day: String?, day2: String? = null): JsonElement {
        val (j1, j2) = getDay(day, day2)
        Log.d("MiBand", "Ready to call: $instance, $enumValue, $j1, $j2")
        fitnessSyncRemote()
        val result = XposedHelpers.callMethod(
            instance,
            "getReportList",
            enumValue,
            j1,
            j2
        ) as ArrayList<*>
        return toJson(result)
    }

    abstract fun toJson(obj: ArrayList<*>): JsonElement
}

