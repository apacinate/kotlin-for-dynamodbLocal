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
        val bathTextView = findViewById<Spinner>(R.id.spinner)
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
        speechRecognizer?.startListening(intent)
        // Activity での生成になるので、ApplicationContextを渡してやる
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer?.setRecognitionListener(createRecognitionListenerStringStream {  result ->
            var RESULT = result.replace(" ", "")

            Log.d("TAG", "AAA")
            Log.d("RESULT", RESULT)
            // 正規表現を使って数字を抽出する
            val temperaturePattern = Regex("体温(\\d+\\.?\\d*)")
            val pressurePattern = Regex("血圧(\\d+)(-(\\d+)|の(\\d+)| (\\d+)|(,)(\\d+))")
            val pulsePattern = Regex("脈拍(\\d+)")
            val spo2Pattern = Regex("SpO2(\\d+)|spo2(\\d+)|SBO2(\\d+)|sbo2(\\d+)|spo2-(\\d+)|sbo2-(\\d+)")
            // マッチする部分を探して値を取得する
            val temperatureMatch = temperaturePattern.find(RESULT)
            val pressureMatch = pressurePattern.find(RESULT)
            val pulseMatch = pulsePattern.find(RESULT)
            val spo2Match = spo2Pattern.find(RESULT)
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
        })

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
}