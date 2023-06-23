package com.example.kotlin_aws_localdynaamodb

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
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


class TenantActivity : AppCompatActivity() {
    private var tableLayout: TableLayout? = null

    private lateinit var accessKeyId: String
    private lateinit var secretAccessKey: String
    private lateinit var endpoint: String
    private lateinit var region: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant)

        val inputStream = resources.openRawResource(R.raw.aws_config)
        val properties = java.util.Properties().apply { load(inputStream) }
        accessKeyId = properties.getProperty("ACCESS_KEY_ID")
        secretAccessKey = properties.getProperty("SECRET_ACCESS_KEY")
        endpoint = properties.getProperty("ENDPOINT")
        region = properties.getProperty("REGION")

        tableLayout = findViewById(R.id.table_layout)

        scan()
    }

    companion object {
        // フラグメントのタグ
        const val TAG = "RawDialogFragment"
        // フラグメントのインスタンスを作成するメソッド
        fun newInstance(id: String, name: String): RawDialogFragment {
            val fragment = RawDialogFragment()
            val args = Bundle()
            args.putString("id", id) // 引数としてidを渡す
            args.putString("name", name) // 引数としてnameを渡す
            fragment.arguments = args
            return fragment
        }
    }

    private fun createTableRows(items: MutableList<MutableMap<String, AttributeValue>>) {
        for (item in items) {
            println("###")
            println(item)
            val dataRow = TableRow(this).also {
                it.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, // 幅を親に合わせる
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                val id = TextView(this)
                id.setPadding(5, 0, 5, 0)
                id.text = item["userId"]?.s
                id.setBackgroundResource(R.drawable.row_border) // 線の設定
                id.layoutParams = TableRow.LayoutParams(
                    0, // 幅を0dpにする
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f // 重みを1にする
                )
                val name = TextView(this)
                name.setPadding(5, 0, 5, 0)
                name.text = item["userName"]?.s
                name.setBackgroundResource(R.drawable.row_border) // 線の設定
                name.layoutParams = TableRow.LayoutParams(
                    0, // 幅を0dpにする
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f // 重みを1にする
                )

                it.addView(id)
                it.addView(name)
            }
            // マージンを設定する部分
            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            tableRowParams.setMargins(5, 0, 5, 0) // 上下左右に10dpのマージン
            dataRow.layoutParams = tableRowParams
            dataRow.setBackgroundResource(R.drawable.row_border) // 線の設定
            tableLayout?.addView(dataRow)

            dataRow.setOnClickListener {
                // クリックしたときの処理を書く
                // Raw要素のidとnameを取得する
                val id = item["userId"]?.s
                val name = item["userName"]?.s
                // DialogFragmentのインスタンスを作成する
                val dialog = RawDialogFragment.newInstance(id, name)
                // DialogFragmentを表示する
                dialog.show(supportFragmentManager, RawDialogFragment.TAG)
            }

        }

    }

    private fun scan() {
        lifecycleScope.launch(Dispatchers.IO) {
            //val credentials: AWSCredentials = BasicAWSCredentials(accessKeyId, secretAccessKey)
            //val client = AmazonDynamoDBClient(credentials)


            val credentials: AWSCredentials = BasicAWSCredentials(accessKeyId, secretAccessKey)
            val client = AmazonDynamoDBClient(credentials)
            client.endpoint = endpoint
            //client.setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1))
            //client.setRegion(Region.getRegion(Regions.US_WEST_2))
            //client.setRegion(com.amazonaws.regions.Region.getRegion(region))

            val scanRequest = ScanRequest()
                .withTableName("tenantTable")
            val scanResponse = client.scan(scanRequest)


            for (item in scanResponse.items) {

                println(item.toString())
            }
            val mainHandler = Handler(Looper.getMainLooper()) // ここでHandlerオブジェクトを作成する
            mainHandler.post { // ここでpostメソッドにRunnableオブジェクトを渡す
                createTableRows(scanResponse.items) // UIを更新するメソッドをRunnableオブジェクトとして渡す
            }
        }
    }
}

class RawDialogFragment : DialogFragment() {

    // ダイアログが生成されるときに呼ばれるメソッド
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // AlertDialogのビルダーを作成
        val builder = AlertDialog.Builder(activity)
        // ダイアログのタイトルを設定
        builder.setTitle("Raw Dialog")
        // ダイアログのメッセージを設定
        builder.setMessage("ID: ${arguments?.getString("id")},  氏名: ${arguments?.getString("name")} さんでよろしいですか？")
        // ダイアログのボタンを設定
        builder.setPositiveButton("OK") { dialog, id ->
            // OKボタンが押されたときの処理
            Toast.makeText(activity, "OK", Toast.LENGTH_SHORT).show()
            // RecordActivityにIntentする
            val intent = Intent(activity, RecordActivity::class.java)
            // ダイアログから受け取ったデータをIntentにセットする
            intent.putExtra("id", arguments?.getString("id"))
            intent.putExtra("name", arguments?.getString("name"))
            // RecordActivityを起動する
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, id ->
            // Cancelボタンが押されたときの処理
            Toast.makeText(activity, "Cancel", Toast.LENGTH_SHORT).show()
        }
        // AlertDialogのインスタンスを作成して返す
        return builder.create()
    }

    // フラグメントのインスタンスを作成するメソッド
    companion object {
        val TAG = "RawDialogFragment"
        fun newInstance(id: String?, name: String?): RawDialogFragment {
            val fragment = RawDialogFragment()
            val args = Bundle()
            args.putString("id", id) // 引数としてidを渡す
            args.putString("name", name) // 引数としてnameを渡す
            fragment.arguments = args // Bundleオブジェクトをargumentsプロパティにセットする
            return fragment
        }
    }
}