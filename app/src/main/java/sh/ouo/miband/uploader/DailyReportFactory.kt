package sh.ouo.miband.uploader

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class DailyReportFactory {
    companion object {
        fun createDailyReport(lpparam: LoadPackageParam, instance: Any, type: String): DailyReportBase {
            return when (type) {
                "SLEEP" -> SleepDailyReport(lpparam, instance)
                "STEP" -> StepDailyReport(lpparam, instance)
                else -> throw NoSuchMethodException("No DailyReportType `$type`")
            }
        }
    }
}