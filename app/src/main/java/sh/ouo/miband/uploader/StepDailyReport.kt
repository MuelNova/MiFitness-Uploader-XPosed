package sh.ouo.miband.uploader

import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

class StepDailyReport(lpparam: XC_LoadPackage.LoadPackageParam,
                      instance: Any
) : DailyReportBase(lpparam, instance) {
    init {
        setEnumValue("STEP")
    }

    override fun toJson(obj: ArrayList<*>): JsonElement {
        Log.d("MiBand", "${obj.size}: $obj")

        if (obj.isNotEmpty()) {
            try {
                // 转换列表中的每个对象
                val reports = obj.mapNotNull { item ->
                    try {
                        convertToSerializableReport(item)
                    } catch (e: Exception) {
                        Log.e("MiBand", "Error converting item to report: $e")
                        null // 在转换失败时返回null，并继续处理列表中的其他元素
                    }
                }

                // 编码转换后的列表为JsonElement
                return Json.encodeToJsonElement(reports)
            } catch (e: Exception) {
                Log.e("MiBand", "Error encoding reports to JSON: $e")
                throw e
            }
        } else {
            throw NoSuchFieldException("No data fetched")
        }
    }

    @Serializable
    data class SerializableDailyStepReport(
        val time: Long,
        val tag: String,
        val steps: Int,
        val distance: Int,
        val calories: Int,
        val minStartTime: Long?,
        val maxEndTime: Long?,
        val avgStep: Int,
        val avgDis: Int,
        val stepRecords: List<SerializableStepRecord>,
        val activeStageList: List<SerializableActiveStageItem>
    )

    @Serializable
     data class SerializableStepRecord(
        val time: Long,
        val steps: Int,
        val distance: Int,
        val calories: Int
    )

    @Serializable
    data class SerializableActiveStageItem(
        val calories: Int,
        val distance: Int,
        val endTime: Long,
        val riseHeight: Float?,
        val startTime: Long,
        val steps: Int?,
        val type: Int
    )

    private fun convertToSerializableReport(xposedReport: Any): SerializableDailyStepReport {
        val stepRecordsObject = XposedHelpers.getObjectField(xposedReport, "stepRecords") as List<*>
        val activeStageListObject = XposedHelpers.getObjectField(xposedReport, "activeStageList") as List<*>

        val stepRecords = stepRecordsObject.mapNotNull { record ->
            if (record != null) {
                SerializableStepRecord(
                    time = XposedHelpers.getLongField(record, "time"),
                    steps = XposedHelpers.getIntField(record, "steps"),
                    distance = XposedHelpers.getIntField(record, "distance"),
                    calories = XposedHelpers.getIntField(record, "calories")
                )
            } else null
        }

        val activeStageList = activeStageListObject.mapNotNull { activeStageItem ->
            if (activeStageItem != null) {
                SerializableActiveStageItem(
                    calories = XposedHelpers.getIntField(activeStageItem, "calories"),
                    distance = XposedHelpers.getIntField(activeStageItem, "distance"),
                    endTime = XposedHelpers.getLongField(activeStageItem, "endTime"),
                    riseHeight = XposedHelpers.getObjectField(activeStageItem, "riseHeight") as? Float,
                    startTime = XposedHelpers.getLongField(activeStageItem, "startTime"),
                    steps = XposedHelpers.getObjectField(activeStageItem, "steps") as? Int,
                    type = XposedHelpers.getIntField(activeStageItem, "type")
                )
            } else null
        }

        return SerializableDailyStepReport(
            time = XposedHelpers.getLongField(xposedReport, "time"),
            tag = XposedHelpers.getObjectField(xposedReport, "tag") as String,
            steps = XposedHelpers.getIntField(xposedReport, "steps"),
            distance = XposedHelpers.getIntField(xposedReport, "distance"),
            calories = XposedHelpers.getIntField(xposedReport, "calories"),
            minStartTime = XposedHelpers.getObjectField(xposedReport, "minStartTime") as Long?,
            maxEndTime = XposedHelpers.getObjectField(xposedReport, "maxEndTime") as Long?,
            avgStep = XposedHelpers.callMethod(xposedReport, "getAvgStepsPerDay") as Int,
            avgDis = XposedHelpers.callMethod(xposedReport, "getAvgDistancePerDay") as Int,
            stepRecords = stepRecords,
            activeStageList = activeStageList
        )
    }


}