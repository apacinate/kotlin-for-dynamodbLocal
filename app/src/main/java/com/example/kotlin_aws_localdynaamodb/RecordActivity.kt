package com.example.kotlin_aws_localdynaamodb

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest

class RecordActivity : AppCompatActivity() {

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

        val idTextView = findViewById<TextView>(R.id.id)
        val nameTextView = findViewById<TextView>(R.id.name)

        // TextView の id を使ってテキストを設定する
        idTextView.text = id
        nameTextView.text = name

        ////////////////////////////////////////////////////////////////////////////////
        //音性入力
        // Speech Recognizer のインスタンスを作成
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        // ボタンのインスタンスを取得
        val recordButton = findViewById<Button>(R.id.record_button)
        // ボタンにクリックリスナーを設定
        recordButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            }
            // Intent を作成して音声入力の設定を行う
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP")
            // 音声入力のタイムアウト時間を設定
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60000) // 60 秒
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 20000) // 無音許可時間20 秒

            // Intent を recognizer の startListening メソッドに渡して音声入力を開始する
            recognizer.startListening(intent)
        }


        // RecognitionListener を設定
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // 音声入力が開始されると呼ばれる
                Log.d("TAG","onReadyForSpeech(params: Bundle?)")
            }

            override fun onBeginningOfSpeech() {
                // 音声入力が始まると呼ばれる
                Log.d("TAG","onBeginningOfSpeech()")
                val mediaPlayer = MediaPlayer.create(this@RecordActivity, R.raw.button)
                mediaPlayer.start()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 音声入力中に呼ばれる
                Log.d("TAG","onRmsChanged")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 音声データがバッファに書き込まれると呼ばれる
                Log.d("TAG","onBufferReceived")
            }

            override fun onEndOfSpeech() {
                // 音声入力が終了すると呼ばれる
                Log.d("TAG","onEndOfSpeech")
            }

            override fun onError(error: Int) {
                // エラーが発生すると呼ばれる
                Log.d("TAG","onError")
                Log.e("Error",error.toString())
            }

            override fun onResults(results: Bundle?) {

                val temperatureTextView = findViewById<TextView>(R.id.temperature)
                //val pressure_highTextView = findViewById<TextView>(R.id.pressure_high)
                // 音声入力の結果が返されると呼ばれる
                // 結果を取得
                val candidates = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("List",results.toString())
                Log.d("Result",candidates.toString())
                // 最も信頼度の高い文字列を取得
                val text = candidates?.firstOrNull()
                // TextView にセット
                if (text != null) {
                    temperatureTextView.text = text
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // 部分的な音声入力の結果が返されると呼ばれる
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // イベントが発生すると呼ばれる
            }
        })
    }
}