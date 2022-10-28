package com.guangdianyun.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.gdy.jsbridge.GdyBridgeWebView
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var webView: GdyBridgeWebView
    private lateinit var btnGetString: Button
    private lateinit var btnGetObject: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        btnGetString = findViewById(R.id.btn_get_string)
        btnGetObject = findViewById(R.id.btn_get_object)

        webView.loadUrl("file:///android_asset/test.html")

        btnGetString.setOnClickListener {
            webView.callJsFunction("getStringFromJs", arrayOf(1, "a", false)) { success: Boolean, value: Any? ->
                if (success) {
                    Toast.makeText(this, value?.toString()?:"无返回值", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show()
                    Log.e("GdyBridgeWebView", "方法methodName调用失败: $value")
                }
            }
        }

        btnGetObject.setOnClickListener {
            webView.callJsFunction("getObjectFromJs") { success, value ->
                if (success) {
                    val jsonObject = value as JSONObject?
                    val str = if (jsonObject == null) {
                        "无返回值"
                    } else {
                        "key1: ${jsonObject.optString("key1")}, key2: ${jsonObject.optString("key2")}"
                    }
                    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show()
                    Log.e("GdyBridgeWebView", "方法getObjectFromJs调用失败: $value")
                }
            }
        }

        webView.register("getStringFromNative") { args: Array<Any> ->
            return@register buildString {
                for (arg in args) {
                    append(arg.toString())
                }
            }
        }

        webView.register("getObjectFromNative") {
            return@register JSONObject().apply {
                put("key1", "value1")
                put("key2", "value2")
            }
        }
    }
}