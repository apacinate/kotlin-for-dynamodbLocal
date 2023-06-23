package com.example.kotlin_aws_localdynaamodb
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button: Button = findViewById<Button>(R.id.recordButton)
        button.setOnClickListener{
            val intent = Intent(this,TenantActivity::class.java)
            val text = "Hello,Kotlin!"
            intent.putExtra("TEXT_KEY",text)
            startActivity(intent)
        }
        /*button.setOnClickListener{
            val intent = Intent(this,MainActivity2::class.java)
            val text = "Hello,Kotlin!"
            intent.putExtra("TEXT_KEY",text)
        }*/
    }
}