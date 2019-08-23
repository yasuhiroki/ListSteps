package yasuhiroki.liststeps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessStatusCodes
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResult
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler_view).let {
            val adapter = StepsAdapter(mutableListOf("hoge"))
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }

        val client = GoogleApiClient.Builder(this)
            .addApi(Fitness.HISTORY_API)

        val FIT_APP_PACKAGE_NAME = "com.google.android.gms"

        val dataSource = DataSource.Builder()
            .setAppPackageName(FIT_APP_PACKAGE_NAME)
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build()

        val readRequest = DataReadRequest.Builder()
            .aggregate(dataSource, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(date[START], date[END], TimeUnit.MILLISECONDS)
            .build()

        val callback =
            ResultCallback<DataReadResult> { result ->
                // 認証切れなら再接続する
                if (result.status.statusCode == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
                    sClient.connect()
                    return@ResultCallback
                }

                val total = getTotalStep(result)

                if (total > 0) {
                    if (sListener != null) {
                        sListener.gotStep(total, sRequestCode)
                    }
                } else {
                    // デグレると嫌なので、以前と同じパラメータでも取得を試す
                    getStepLegacy(date)
                }
            }

        Fitness.HistoryApi.readData(sClient, readRequest).setResultCallback(callback)

    }
}
