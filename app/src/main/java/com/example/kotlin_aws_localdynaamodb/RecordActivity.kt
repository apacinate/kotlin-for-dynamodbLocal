package com.example.kotlin_aws_localdynaamodb

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
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

        // Spinnerの取得
        val spinner = findViewById<Spinner>(R.id.spinner)
        val array = resources.getStringArray(R.array.list) // リストを取得
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, array) // アダプターを作成
        spinner.adapter = arrayAdapter // アダプターをセット
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { // リスナーを実装
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = spinner.selectedItem.toString() // 選択されたアイテムを取得
                Toast.makeText(this@RecordActivity, "選択されたタイプ： $item", Toast.LENGTH_SHORT).show() // トーストで表示
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 何も選択されなかった場合の処理
            }
        }
    }
}