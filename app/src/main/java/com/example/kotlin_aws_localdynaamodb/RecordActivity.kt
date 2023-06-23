package com.example.kotlin_aws_localdynaamodb

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import kotlinx.coroutines.Dispatchers


class RecordActivity : AppCompatActivity() {
    private var tableLayout: TableLayout? = null

    private lateinit var accessKeyId: String
    private lateinit var secretAccessKey: String
    private lateinit var endpoint: String
    private lateinit var region: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_table)

        val inputStream = resources.openRawResource(R.raw.aws_config)
        val properties = java.util.Properties().apply { load(inputStream) }
        accessKeyId = properties.getProperty("ACCESS_KEY_ID")
        secretAccessKey = properties.getProperty("SECRET_ACCESS_KEY")
        endpoint = properties.getProperty("ENDPOINT")
        region = properties.getProperty("REGION")

    }
}