package yasuhiroki.liststeps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var stepsAdapter: StepsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1
            )
        }


        stepsAdapter = StepsAdapter(mutableListOf("hoge"))
        findViewById<RecyclerView>(R.id.recycler_view).let {
            it.adapter = stepsAdapter
            it.layoutManager = LinearLayoutManager(this)
        }

        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        ) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                2,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        } else {
            accessGoogleFit(stepsAdapter, fitnessOptions)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .build()

                accessGoogleFit(stepsAdapter, fitnessOptions)
            }
        }
    }

    fun accessGoogleFit(adapter: StepsAdapter, fitnessOptions: FitnessOptions) {
        val googleSignInAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        GlobalScope.launch(Dispatchers.Main) {
            val response = Fitness.getHistoryClient(baseContext, googleSignInAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)

            async(Dispatchers.Default) { Tasks.await(response) }.await().let { dataSet ->
                for (dataPoint in dataSet.dataPoints) {
                    val total = dataPoint.getValue(Field.FIELD_STEPS)
                    adapter.addItem(total.toString())
                }
            }
        }
    }
}
