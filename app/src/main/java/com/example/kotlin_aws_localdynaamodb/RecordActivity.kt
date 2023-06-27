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
            if(result.contains("体温")){
                    Log.d("TAG","BBB")
                    // result に数値が入っている場合の処理
                    val list = result.split("体温")
                    val text = list[1] //数値部分
                    val firstFour = text.substring(0, 4) // 頭の4文字
                    //℃や度が入っているかもしれないので
                    var split =""
                    if(firstFour.contains("度")){
                        split = firstFour.split("度").toString()
                        temperatureTextView.text = split
                    } else if(firstFour.contains("℃")){
                        split = firstFour.split("℃").toString()
                        temperatureTextView.text = split
                    } else {
                        //val number = list[1].toInt() // 数値部分
                        temperatureTextView.text = firstFour
                    }
                Log.d("temperature", result)

            } else if(result.contains("血圧")){
                val list = result.split("血圧")
                    // result に数値が入っている場合の処理
                    val text = list[1] //数値部分
                    var split =""
                    if(text.contains("の")){
                        split[] = text.split("の").toString()
                    } else if(text.contains("-")){
                        split = text.split("-").toString()
                    }else if(text.contains(",")) {
                        split = text.split(",").toString()
                    }
                    val first = split // 頭の3文字
                    pressureHighTextView.text = first
                    val second = split// 頭の3文字
                    pressureLowTextView.text = second
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