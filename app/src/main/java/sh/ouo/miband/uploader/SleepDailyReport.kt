package sh.ouo.miband.uploader

import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.full.declaredMembers

class SleepDailyReport(lpparam: XC_LoadPackage.LoadPackageParam,
                      instance: Any
) : DailyReportBase(lpparam, instance) {
    init {
        setEnumValue("SLEEP")
    }

    override fun toJson(obj: ArrayList<*>): JsonElement {
        Log.d("MiBand", obj.toString())
        if (obj.isNotEmpty()) {
            try {
                // 转换列表中的每个对象
                val reports = obj.mapNotNull { item ->
                    try {
                        convertToSerializableAllDaySleepReport(item)
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

    companion object {
        @Serializable
        data class SerializableAllDaySleepReport(
            val time: Long,
            val totalDuration: Int,
            val avgDuration: Int,
            val deepDuration: Int,
            val lightDuration: Int,
            val remDuration: Int?,
            val awakeDuration: Int?,
            val avgHr: Int?,
            val avgSpo2: Int?,
            val stage: Int,
            val longSleepCommentCode: Int,
            val commentCode: Int,
            val score: Int,
            val scoreOutperformedPercent: Int?,
            val sleepSegments: List<SerializableSleepSegmentReport>?,
            val maxHr: Int?,
            val maxSpo2: Int?,
            val minHr: Int?,
            val minSpo2: Int?,
            val tzInSec: Int,
            val tag: String
        )

        @Serializable
        data class SerializableSleepSegmentReport(
            val avgHr: Int?,
            val avgSpo2: Int?,
            val bedTime: Long,
            val breathQuality: Int?,
            val deepDuration: Int,
            val deviceBedTime: Long?,
            val deviceWakeupTime: Long?,
//        val dreamTalkAudioList: List<SerializableSnoreDreamTalkAudio>?,
            val dreamTalkCount: Int?,
//        val hrDetailRecords: List<SerializableTimedValue<Int>>?,
//        val hrRecords: List<SerializableTimesDataRecordInt>?,
            val lightDuration: Int,
            val maxHr: Int?,
            val maxSpo2: Int?,
            val minHr: Int?,
            val minSpo2: Int?,
            val protoTime: Long,
            val remDuration: Int?,
            val sleepDuration: Int,
            val sleepItems: List<SerializableSleepStateItem>?,
            val sleepTraceDuration: Int?,
//        val snoreAudioList: List<SerializableSnoreDreamTalkAudio>?,
            val snoreDuration: Int?,
//        val snoreRecordInfo: SerializableSleepAssistItemInfo?,
//        val spo2DetailRecords: List<SerializableTimedValue<Int>>?,
//        val spo2Records: List<SerializableTimesDataRecordInt>?,
            val time: Long,
            val tzIn15Min: Int,
            val version: Int?,
            val wakeCount: Int?,
            val wakeDuration: Int?,
            val wakeupTime: Long
        )

        @Serializable
        data class SerializableSleepStateItem(
            val startTime: Long,
            val endTime: Long,
            val sleepState: Int
        )


        fun convertToSerializableAllDaySleepReport(xposedReport: Any): SerializableAllDaySleepReport {
            return SerializableAllDaySleepReport(
                time = XposedHelpers.getLongField(xposedReport, "time"),
                totalDuration = XposedHelpers.getIntField(xposedReport, "totalDuration"),
                avgDuration = XposedHelpers.getIntField(xposedReport, "avgDuration"),
                deepDuration = XposedHelpers.getIntField(xposedReport, "deepDuration"),
                lightDuration = XposedHelpers.getIntField(xposedReport, "lightDuration"),
                remDuration = XposedHelpers.getObjectField(xposedReport, "remDuration") as? Int,
                awakeDuration = XposedHelpers.getObjectField(xposedReport, "awakeDuration") as? Int,
                avgHr = XposedHelpers.getObjectField(xposedReport, "avgHr") as? Int,
                avgSpo2 = XposedHelpers.getObjectField(xposedReport, "avgSpo2") as? Int,
                stage = XposedHelpers.getIntField(xposedReport, "stage"),
                longSleepCommentCode = XposedHelpers.getIntField(xposedReport, "longSleepCommentCode"),
                commentCode = XposedHelpers.getIntField(xposedReport, "commentCode"),
                score = XposedHelpers.getIntField(xposedReport, "score"),
                scoreOutperformedPercent = XposedHelpers.getObjectField(xposedReport, "scoreOutperformedPercent") as? Int,
                sleepSegments = convertSleepSegments(XposedHelpers.getObjectField(xposedReport, "sleepSegments") as? List<Any>),
                maxHr = XposedHelpers.getObjectField(xposedReport, "maxHr") as? Int,
                maxSpo2 = XposedHelpers.getObjectField(xposedReport, "maxSpo2") as? Int,
                minHr = XposedHelpers.getObjectField(xposedReport, "minHr") as? Int,
                minSpo2 = XposedHelpers.getObjectField(xposedReport, "minSpo2") as? Int,
                tzInSec = XposedHelpers.getIntField(xposedReport, "tzInSec"),
                tag = XposedHelpers.getObjectField(xposedReport, "tag") as String
            )
        }

        fun convertSleepSegments(segments: List<Any>?): List<SerializableSleepSegmentReport> {
            return segments?.map { segment ->
                SerializableSleepSegmentReport(
                    avgHr = XposedHelpers.getObjectField(segment, "avgHr") as? Int,
                    avgSpo2 = XposedHelpers.getObjectField(segment, "avgSpo2") as? Int,
                    bedTime = XposedHelpers.getLongField(segment, "bedTime"),
                    breathQuality = XposedHelpers.getObjectField(segment, "breathQuality") as? Int,
                    deepDuration = XposedHelpers.getIntField(segment, "deepDuration"),
                    deviceBedTime = XposedHelpers.getObjectField(segment, "deviceBedTime") as? Long,
                    deviceWakeupTime = XposedHelpers.getObjectField(segment, "deviceWakeupTime") as? Long,
//                    dreamTalkAudioList = convertDreamTalkAudioList(XposedHelpers.getObjectField(segment, "dreamTalkAudioList") as? List<Any>),
                    dreamTalkCount = XposedHelpers.getObjectField(segment, "dreamTalkCount") as? Int,
//                    hrDetailRecords = convertTimedValueList(XposedHelpers.getObjectField(segment, "hrDetailRecords") as? List<Any>),
//                    hrRecords = convertTimesDataRecordIntList(XposedHelpers.getObjectField(segment, "hrRecords") as? List<Any>),
                    lightDuration = XposedHelpers.getIntField(segment, "lightDuration"),
                    maxHr = XposedHelpers.getObjectField(segment, "maxHr") as? Int,
                    maxSpo2 = XposedHelpers.getObjectField(segment, "maxSpo2") as? Int,
                    minHr = XposedHelpers.getObjectField(segment, "minHr") as? Int,
                    minSpo2 = XposedHelpers.getObjectField(segment, "minSpo2") as? Int,
                    protoTime = XposedHelpers.getLongField(segment, "protoTime"),
                    remDuration = XposedHelpers.getObjectField(segment, "remDuration") as? Int,
                    sleepDuration = XposedHelpers.getIntField(segment, "sleepDuration"),
                    sleepItems = convertSleepStateItemList(XposedHelpers.getObjectField(segment, "sleepItems") as? List<Any>),
                    sleepTraceDuration = XposedHelpers.getObjectField(segment, "sleepTraceDuration") as? Int,
//                    snoreAudioList = convertSnoreDreamTalkAudioList(XposedHelpers.getObjectField(segment, "snoreAudioList") as? List<Any>),
                    snoreDuration = XposedHelpers.getObjectField(segment, "snoreDuration") as? Int,
//                    snoreRecordInfo = convertSleepAssistItemInfo(XposedHelpers.getObjectField(segment, "snoreRecordInfo")),
//                    spo2DetailRecords = convertTimedValueList(XposedHelpers.getObjectField(segment, "spo2DetailRecords") as? List<Any>),
//                    spo2Records = convertTimesDataRecordIntList(XposedHelpers.getObjectField(segment, "spo2Records") as? List<Any>),
                    time = XposedHelpers.getLongField(segment, "time"),
                    tzIn15Min = XposedHelpers.getIntField(segment, "tzIn15Min"),
                    version = XposedHelpers.getObjectField(segment, "version") as? Int,
                    wakeCount = XposedHelpers.getObjectField(segment, "wakeCount") as? Int,
                    wakeDuration = XposedHelpers.getObjectField(segment, "wakeDuration") as? Int,
                    wakeupTime = XposedHelpers.getObjectField(segment, "wakeupTime") as Long
                )
            } ?: emptyList()
        }

        fun convertSleepStateItemList(items: List<Any>?): List<SerializableSleepStateItem> {
            return items?.map { item ->
                SerializableSleepStateItem(
                    startTime = XposedHelpers.getLongField(item, "startTime"),
                    endTime = XposedHelpers.getLongField(item, "endTime"),
                    sleepState = XposedHelpers.getIntField(item, "sleepState"),
                )
            } ?: emptyList()
        }
    }




}