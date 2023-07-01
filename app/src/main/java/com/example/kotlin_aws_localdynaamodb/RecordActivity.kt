package com.example.kotlin_aws_localdynaamodb

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class RecordActivity : AppCompatActivity() {

    private lateinit var accessKeyId: String
    private lateinit var secretAccessKey: String
    private lateinit var endpoint: String
    private lateinit var region: String
    private var speechRecognizer : SpeechRecognizer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_table)

        // intentからデータを取得する
        val intentId = intent.getStringExtra("id")
        val intentName = intent.getStringExtra("name")

        val inputStream = resources.openRawResource(R.raw.aws_config)
        val properties = java.util.Properties().apply { load(inputStream) }
        accessKeyId = properties.getProperty("ACCESS_KEY_ID")
        secretAccessKey = properties.getProperty("SECRET_ACCESS_KEY")
        endpoint = properties.getProperty("ENDPOINT")
        region = properties.getProperty("REGION")

        // Spinnerの取得
        //AutoCompleteTextViewの取得
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autocomplete)
        val array = resources.getStringArray(R.array.list)
        // リストを取得
        val arrayAdapter = ArrayAdapter(this, R.layout.spinner_item, array)
        // アダプターを作成
        autoCompleteTextView.setAdapter(arrayAdapter)
        // アダプターをセット
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            // リスナーを実装
            val item = parent.getItemAtPosition(position).toString()
            // 選択されたアイテムを取得
            Toast.makeText(this@RecordActivity, "選択されたタイプ： $item", Toast.LENGTH_SHORT).show()
            // トーストで表示
            }

        //前の画面に戻る
        val button: Button = findViewById<Button>(R.id.back_button)
        button.setOnClickListener{
            val intent = Intent(this,TenantActivity::class.java)
            val text = "Hello,Kotlin!"
            intent.putExtra("TEXT_KEY",text)
            startActivity(intent)
        }

        // Intent からデータを取り出す
        val id = intent.getStringExtra("id")
        val name = intent.getStringExtra("name")

        val recordButtonTextView = findViewById<TextView>(R.id.record_button)
        val stopButtonTextView = findViewById<TextView>(R.id.stop_button)
        val idTextView = findViewById<TextView>(R.id.id)
        val nameTextView = findViewById<TextView>(R.id.name)
        val temperatureTextView = findViewById<TextView>(R.id.temperature)
        val pressureHighTextView = findViewById<TextView>(R.id.pressure_high)
        val pressureLowTextView = findViewById<TextView>(R.id.pressure_low)
        val pulseTextView = findViewById<TextView>(R.id.pulse)
        val spo2TextView = findViewById<TextView>(R.id.spo2)
        val stapleTextView = findViewById<TextView>(R.id.staple)
        val sidedishTextView = findViewById<TextView>(R.id.sidedish)
        val soupTextView = findViewById<TextView>(R.id.soup)
        val hydrationTextView = findViewById<TextView>(R.id.hydration)
        val medicineTextView = findViewById<TextView>(R.id.medicine)
        val bathTextView = findViewById<AutoCompleteTextView>(R.id.autocomplete)
        val specialTextView = findViewById<TextView>(R.id.special)


        // setOnClickListener でクリック動作を登録し、クリックで音声入力が開始するようにする
        recordButtonTextView.setOnClickListener { speechRecognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) }

        // setOnclickListner でクリック動作を登録し、クリックで音声入力が停止するようにする
        stopButtonTextView.setOnClickListener { speechRecognizer?.stopListening() }



        // TextView の id を使ってテキストを設定する
        idTextView.text = id
        nameTextView.text = name

        ////////////////////////////////////////////////////////////////////////////////
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPANESE.toString())
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
        speechRecognizer?.startListening(intent)
        // Activity での生成になるので、ApplicationContextを渡してやる
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer?.setRecognitionListener(createRecognitionListenerStringStream {  result ->
            var RESULT = result.replace(" ", "")

            Log.d("TAG", "AAA")
            Log.d("RESULT", RESULT)
            // 正規表現を使って数字を抽出する
            val temperaturePattern = Regex("(?i)体温(\\d+\\.?\\d*)")
            val pressurePattern = Regex("(?i)血圧(\\d+)(-(\\d+)|の(\\d+)| (\\d+)|(,)(\\d+))")
            val pulsePattern = Regex("(?i)脈拍(\\d+)")
            val spo2Pattern = Regex("(?i)SpO2(\\d+)|spo2(\\d+)|SBO2(\\d+)|sbo2(\\d+)|spo2-(\\d+)|sbo2-(\\d+)")
            val staplePattern = Regex("(?i)主食(\\d+)|(?i)試食(\\d+)|(?i)就職(\\d+)")
            val sidedishPattern = Regex("(?i)副食(\\d+)|(?i)副職(\\d+)|(?i)復職(\\d+)|(?i)服飾(\\d+)")
            val soupPattern = Regex("(?i)汁物(\\d+)")
            val hydrationPattern = Regex("(?i)水分(\\d+)")
            val medicinePattern = Regex("(?i)服薬(あり|なし|有|無)")
            val bathPattern = Regex("(?i)入浴(一般|シャワー|清拭|正式|中止)")
            val specialPattern = Regex("(?i)特記(.*)|(?i)土地(.*)|(?i)どっち(.*)|(?i)突起(.*)")
            // マッチする部分を探して値を取得する
            val temperatureMatch = temperaturePattern.find(RESULT)
            val pressureMatch = pressurePattern.find(RESULT)
            val pulseMatch = pulsePattern.find(RESULT)
            val spo2Match = spo2Pattern.find(RESULT)
            val stapleMatch = staplePattern.find(RESULT)
            val sidedishMatch = sidedishPattern.find(RESULT)
            val soupMatch = soupPattern.find(RESULT)
            val hydrationMatch = hydrationPattern.find(RESULT)
            val medicineMatch = medicinePattern.find(RESULT)
            val bathMatch = bathPattern.find(RESULT)

            // マッチした要素をリストに格納する
            val matches = listOfNotNull(
                temperatureMatch,
                pressureMatch,
                pulseMatch,
                spo2Match,
                stapleMatch,
                sidedishMatch,
                soupMatch,
                hydrationMatch,
                medicineMatch,
                bathMatch
            )

            // specialPatternでマッチした要素をリストに格納する
            val specialMatches = specialPattern.findAll(RESULT).toList()

            // specialMatchesからmatchesに含まれる要素を除く
            val specialMatch = specialMatches.filterNot { it in matches }

            // マッチする部分がnullでないか確認してテキストビューを更新する
            if (temperatureMatch != null) {
                val temperature = temperatureMatch.groupValues[1]
                temperatureTextView.text = temperature
                Log.d("temperature", temperature)
            }
            if (pressureMatch != null) {
                val pressureHigh = pressureMatch.groupValues[1]
                var pressureLow = pressureMatch.groupValues[2]
                pressureLow = pressureLow.replace(" ", "")
                pressureLow = pressureLow.replace("の", "")
                pressureLow = pressureLow.replace("-", "")
                pressureLow = pressureLow.replace(",", "")
                pressureHighTextView.text = pressureHigh
                pressureLowTextView.text = pressureLow
                Log.d("pressure", "$pressureHigh-$pressureLow")
            }
            if (pulseMatch != null) {
                val pulse = pulseMatch.groupValues[1]
                pulseTextView.text = pulse
                Log.d("pulse", pulse)
            }
            if (spo2Match != null) {
                if(spo2Match.groupValues[1] != "") {
                    val spo2 = spo2Match.groupValues[1]
                    spo2TextView.text = spo2
                    Log.d("spo2", spo2)
                } else if(spo2Match.groupValues[2] != "") {
                    val spo2 = spo2Match.groupValues[2]
                    spo2TextView.text = spo2
                    Log.d("spo2", spo2)
                } else if(spo2Match.groupValues[3] != "") {
                    val spo2 = spo2Match.groupValues[3]
                    spo2TextView.text = spo2
                    Log.d("spo2", spo2)
                } else if(spo2Match.groupValues[4] != "") {
                    val spo2 = spo2Match.groupValues[4]
                    spo2TextView.text = spo2
                    Log.d("spo2", spo2)
                } else if(spo2Match.groupValues[5] != "") {
                    val spo2 = spo2Match.groupValues[5]
                    spo2TextView.text = spo2
                    Log.d("spo2", spo2)
                }
                else {}
            }
            if (stapleMatch != null) {
                if(stapleMatch.groupValues[1] != "") {
                    val staple = stapleMatch.groupValues[1]
                    stapleTextView.text = staple
                    Log.d("staple", staple)
                } else if(stapleMatch.groupValues[2] != ""){
                    val staple = stapleMatch.groupValues[2]
                    stapleTextView.text = staple
                    Log.d("staple", staple)
                } else if(stapleMatch.groupValues[3] != ""){
                    val staple = stapleMatch.groupValues[3]
                    stapleTextView.text = staple
                    Log.d("staple", staple)
                } else {}
            }
            if (sidedishMatch != null) {
                if(sidedishMatch.groupValues[1] != "") {
                    val sidedish = sidedishMatch.groupValues[1]
                    sidedishTextView.text = sidedish
                    Log.d("sidedish", sidedish)
                } else if(sidedishMatch.groupValues[2] != "") {
                    val sidedish = sidedishMatch.groupValues[2]
                    sidedishTextView.text = sidedish
                    Log.d("sidedish", sidedish)
                } else if(sidedishMatch.groupValues[2] != "") {
                    val sidedish = sidedishMatch.groupValues[2]
                    sidedishTextView.text = sidedish
                    Log.d("sidedish", sidedish)
                } else if(sidedishMatch.groupValues[3] != "") {
                    val sidedish = sidedishMatch.groupValues[3]
                    sidedishTextView.text = sidedish
                    Log.d("sidedish", sidedish)
                } else if(sidedishMatch.groupValues[4] != "") {
                    val sidedish = sidedishMatch.groupValues[4]
                    sidedishTextView.text = sidedish
                    Log.d("sidedish", sidedish)
                } else {}
            }
            if (soupMatch != null) {
                val soup = soupMatch.groupValues[1]
                soupTextView.text = soup
                Log.d("soup", soup)
            }
            if (hydrationMatch != null) {
                val hydration = hydrationMatch.groupValues[1]
                hydrationTextView.text = hydration
                Log.d("hydration", hydration)
            }

            Log.d("medicineMatch:", medicineMatch.toString());
            if (medicineMatch != null) {
                val medicine = medicineMatch.groupValues[1] // 1番目のキャプチャグループの値
                medicineTextView.text = medicine
                Log.d("medicine", medicine)
            }
            if (bathMatch != null) {
                if (bathMatch.groupValues[1] != "") {
                    var bath = bathMatch.groupValues[1]
                    if(bath.equals("一般") ||bath.equals("一般浴")) {
                        bath="一般浴"
                        bathTextView.setText(bath)
                    } else if(bath.equals("シャワー") ||bath.equals("シャワー浴")) {
                        bath="シャワー浴"
                        bathTextView.setText(bath)
                    } else if(bath.equals("正式") ||bath.equals("清拭")) {
                        bath="清拭"
                        bathTextView.setText(bath)
                    }else {
                        bathTextView.setText(bath)
                    }
                    Log.d("bath", bath)
                }
            }
            // specialMatchの最初の要素を取り出す
            if (specialMatch.isNotEmpty()) {
                val firstMatch = specialMatch[0]
                if (firstMatch.groupValues[1] != "") {
                    val special = firstMatch.groupValues[1]
                    specialTextView.text = special
                    Log.d("special", special)
                    Log.d("special", special)
                } else if (firstMatch.groupValues[2] != "") {
                    val special = firstMatch.groupValues[2]
                    specialTextView.text = special
                    Log.d("special", special)
                } else if (firstMatch.groupValues[3] != "") {
                    val special = firstMatch.groupValues[3]
                    specialTextView.text = special
                    Log.d("special", special)
                } else if (firstMatch.groupValues[4] != "") {
                    val special = firstMatch.groupValues[4]
                    specialTextView.text = special
                    Log.d("special", special)
                } else {}

            }
        })

        //DynamoDBに保存する。
        // ボタンの参照を取得する
        val sabeButton = findViewById<Button>(R.id.save_button)
// ボタンのクリックリスナーを設定する
        sabeButton.setOnClickListener {
            if (intentName != null) {
                if (intentId != null) {
                    save(intentId, intentName)
                }
            };
        }
    }
    // Activity のライフサイクルにあわせて SpeechRecognizer を破棄する
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    /** 公開関数で受け取った TextView の更新処理を各関数で呼び出す*/
    private fun createRecognitionListenerStringStream(onResult : (String)-> Unit) : RecognitionListener {
        return object : RecognitionListener {
            override fun onRmsChanged(rmsdB: Float) { /** 今回は特に利用しない */ }
            override fun onReadyForSpeech(params: Bundle) { onResult("onReadyForSpeech") }
            override fun onBufferReceived(buffer: ByteArray) { onResult("onBufferReceived") }
            override fun onPartialResults(partialResults: Bundle) { onResult("onPartialResults") }
            override fun onEvent(eventType: Int, params: Bundle) { onResult("onEvent") }
            override fun onBeginningOfSpeech() { onResult("onBeginningOfSpeech") }
            override fun onEndOfSpeech() { onResult("onEndOfSpeech") }
            override fun onError(error: Int) { onResult("onError") }
            override fun onResults(results: Bundle) {
                val stringArray = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                //onResult("onResults " + stringArray.toString())
                onResult(stringArray?.get(0) ?: "")
            }
        }
    }

    private fun save(intentId: String, intentName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // DynamoDbClientのインスタンスを作成する
            val credentials: AWSCredentials = BasicAWSCredentials(accessKeyId, secretAccessKey)
            val client = AmazonDynamoDBClient(credentials)
            client.endpoint = endpoint


            // DescribeTable操作を使用して、テーブルの詳細情報を取得。
            val describeVitalTableResult =
                client.describeTable(DescribeTableRequest().withTableName("vitalTable"))
            // AttributeDefinitionsプロパティから、特定のカラムが存在するかどうかを確認。
            //val columnExists = describeTableResult.table.attributeDefinitions.any { it.attributeName == "YourColumnName" }
            println("describeVitalTableResult: $describeVitalTableResult")

            //テーブルにカラムを追加
            // アイテムの属性を作成する
            var item: Map<String, AttributeValue>? = null
            if (describeVitalTableResult == null) {
                item = mapOf(
                    "dateTime" to AttributeValue().withS(""),
                    "name" to AttributeValue().withS(""),
                    "temperature" to AttributeValue().withS(""),
                    "pressure" to AttributeValue().withS(""),
                    "pulse" to AttributeValue().withS("20"),
                    "spo2" to AttributeValue().withS("20"),
                )
                // putItemメソッドでアイテムを追加する
                client.putItem("vitalTable", item) // テーブル名とアイテムを指定します
            }


            // DescribeTable操作を使用して、テーブルの詳細情報を取得。
            val describeMealTableResult =
                client.describeTable(DescribeTableRequest().withTableName("mealTable"))
            // AttributeDefinitionsプロパティから、特定のカラムが存在するかどうかを確認。
            //val columnExists = describeTableResult.table.attributeDefinitions.any { it.attributeName == "YourColumnName" }
            println("describeMealTableResult: $describeMealTableResult")

            //テーブルにカラムを追加
            // アイテムの属性を作成する
            var item2: Map<String, AttributeValue>? = null
            if (describeMealTableResult == null) {
                item2 = mapOf(
                    "dateTime" to AttributeValue().withS(""),
                    "name" to AttributeValue().withS(""),
                    "staple" to AttributeValue().withS(""),
                    "sidedish" to AttributeValue().withS(""),
                    "soup" to AttributeValue().withS(""),
                    "hydration" to AttributeValue().withS(""),
                    "medicine" to AttributeValue().withS(""),
                )
                // putItemメソッドでアイテムを追加する
                client.putItem("mealTable", item2) // テーブル名とアイテムを指定します
            }

            // DescribeTable操作を使用して、テーブルの詳細情報を取得。
            val describeBathTableResult =
                client.describeTable(DescribeTableRequest().withTableName("bathTable"))
            // AttributeDefinitionsプロパティから、特定のカラムが存在するかどうかを確認。
            //val columnExists = describeTableResult.table.attributeDefinitions.any { it.attributeName == "YourColumnName" }
            println("describeBathTableResult: $describeBathTableResult")

            //テーブルにカラムを追加
            // アイテムの属性を作成する
            var item3: Map<String, AttributeValue>? = null
            if (describeBathTableResult == null) {
                item3 = mapOf(
                    "dateTime" to AttributeValue().withS(""),
                    "name" to AttributeValue().withS(""),
                    "bath" to AttributeValue().withS(""),
                    // putItemメソッドでアイテムを追加する
                )
                client.putItem("bathTable", item3) // テーブル名とアイテムを指定します
            }

            // DescribeTable操作を使用して、テーブルの詳細情報を取得。
            val describeSpecialTableResult =
                client.describeTable(DescribeTableRequest().withTableName("specialNoteTable"))
            // AttributeDefinitionsプロパティから、特定のカラムが存在するかどうかを確認。
            //val columnExists = describeTableResult.table.attributeDefinitions.any { it.attributeName == "YourColumnName" }
            println("describeSpecialTableResult: $describeSpecialTableResult")

            //テーブルにカラムを追加
            // アイテムの属性を作成する
            var item4: Map<String, AttributeValue>? = null
            if (describeSpecialTableResult == null) {
                item4 = mapOf(
                    "dateTime" to AttributeValue().withS(""),
                    "name" to AttributeValue().withS(""),
                    "special" to AttributeValue().withS(""),
                    // putItemメソッドでアイテムを追加する
                )
                client.putItem("specialTable", item4) // テーブル名とアイテムを指定します
            }

            //画面から要素を取得
            val temperatureTextView = findViewById<TextView>(R.id.temperature)
            val pressureHighTextView = findViewById<TextView>(R.id.pressure_high)
            val pressureLowTextView = findViewById<TextView>(R.id.pressure_low)
            val pulseTextView = findViewById<TextView>(R.id.pulse)
            val spo2TextView = findViewById<TextView>(R.id.spo2)
            val stapleTextView = findViewById<TextView>(R.id.staple)
            val sidedishTextView = findViewById<TextView>(R.id.sidedish)
            val soupTextView = findViewById<TextView>(R.id.soup)
            val hydrationTextView = findViewById<TextView>(R.id.hydration)
            val medicineTextView = findViewById<TextView>(R.id.medicine)
            val bathTextView = findViewById<AutoCompleteTextView>(R.id.autocomplete)
            val specialTextView = findViewById<TextView>(R.id.special)

            // 今日の日付と日時を取得する
                val date = Date()
            // 日付と日時を任意のフォーマットに変換する
                val dateTimeFormat = SimpleDateFormat("yyyyMMdd HH:mm:ss")
            // フォーマットした日付と日時を文字列として出力する
                println(dateTimeFormat.format(date))


            // 書き込むデータを作成します。
            val itemVital: Map<String, AttributeValue> = mapOf(
                "group" to AttributeValue("A"),
                "userId" to AttributeValue(intentId),
                "name" to AttributeValue(intentName),
                "dateTime" to AttributeValue(dateTimeFormat.format(date)),
                "temperature" to AttributeValue(temperatureTextView.getText().toString()),
                "pressure" to AttributeValue(pressureHighTextView.getText().toString() + "/" + pressureLowTextView.getText().toString()),
                "pulse" to AttributeValue().withS(pulseTextView.getText().toString()),
                "spo2" to AttributeValue().withS(spo2TextView.getText().toString()),
            )

            val itemMeal: Map<String, AttributeValue> = mapOf(
                "group" to AttributeValue("A"),
                "userId" to AttributeValue(intentId),
                "name" to AttributeValue(intentName),
                "dateTime" to AttributeValue(dateTimeFormat.format(date)),
                "staple" to AttributeValue().withS(stapleTextView.getText().toString()),
                "sidedish" to AttributeValue().withS(sidedishTextView.getText().toString()),
                "soup" to AttributeValue().withS(soupTextView.getText().toString()),
                "hydration" to AttributeValue().withS(hydrationTextView.getText().toString()),
                "medicine" to AttributeValue().withS(medicineTextView.getText().toString()),
            )

            val itemBath: Map<String, AttributeValue> = mapOf(
                "group" to AttributeValue("A"),
                "userId" to AttributeValue(intentId),
                "name" to AttributeValue(intentName),
                "dateTime" to AttributeValue(dateTimeFormat.format(date)),
                "bath" to AttributeValue().withS(bathTextView.getText().toString()),
            )

            val itemSpecial: Map<String, AttributeValue> = mapOf(
                "facilityCode" to AttributeValue("A"),
                "userId" to AttributeValue(intentId),
                "name" to AttributeValue(intentName),
                "dateTime" to AttributeValue(dateTimeFormat.format(date)),
                "special" to AttributeValue().withS(specialTextView.getText().toString())
            )

            // PutItem操作を使用して、データを書き込みます。
            client.putItem(PutItemRequest().withTableName("vitalTable").withItem(itemVital))
            client.putItem(PutItemRequest().withTableName("mealTable").withItem(itemMeal))
            client.putItem(PutItemRequest().withTableName("bathTable").withItem(itemBath))
            client.putItem(PutItemRequest().withTableName("specialNoteTable").withItem(itemSpecial))


            // DynamoDbClientを閉じる
            client.shutdown()
        }
    }
}