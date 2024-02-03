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
    private lateinit var enumValue: Any

    protected fun setEnumValue(type: String) {
        val homeDataType = XposedHelpers.findClass("com.xiaomi.fit.fitness.export.data.annotation.HomeDataType", lpparam.classLoader)
        enumValue = XposedHelpers.getStaticObjectField(homeDataType, type)
    }

    private fun getDay(day: String?): Pair<Long, Long> {
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
        val endOfDayTimestamp = startOfDay.plusDays(1).minusSeconds(1).toEpochSecond() // 减去1秒以获取当天结束时间
        return Pair(startOfDayTimestamp, endOfDayTimestamp)
    }

    fun getDailyReport(day: String?): JsonElement {
        val (j1, j2) = getDay(day)
        Log.d("MiBand", "Ready to call: $instance, $enumValue, $j1, $j2")
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

