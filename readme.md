# GdyJsBridge2 for Android

## 特性

1. api简单易用

2. 支持原生与js之间互相调用其方法

## 集成

目前本sdk仅支持手动集成(aar)，aar文件可在本demo的app/libs下获取。

1. 首先将aar文件拷贝到app(或其他Module级别的目录)/libs目录下

2. 在当前Module的build.gradle文件中配置如下:

Gradle 7.0之前
   
```groovy
android {
   ...
   repositories {
      flatDir {
         dirs 'libs'
      }
   }
}

dependencies {
   api(name: 'GdyJsBridge-release', ext: 'aar')
}

```

Gradle 7.0之后
```groovy
dependencies {
   api files('libs/GdyJsBridge-release.aar')
}
```

## 使用

### 初始化

Android端初始化

In Java
```java
import com.gdy.jsbridge.GdyBridgeWebView;
...
GdyBridgeWebView webView = (GdyBridgeWebView)findViewById(R.id.webView);
```

In Kotlin
```kotlin
import com.gdy.jsbridge.GdyBridgeWebView
...
val webView = findViewById<GdyBridgeWebView>(R.id.webView);
```
GdyBridgeWebView继承于Android原生的WebView，因此WebView的方法都可以正常使用。

前端初始化

In Html
```html
<script src="./gdyBridge.js"></script>
```
该js脚本会根据当前用户的userAgent自动去初始化，前端不用过多处理，只需保证window下没有挂载"\_gdyBridge"同名对象即可。


### JavaScript提供一个方法，Android 原生去调用

传递字符串:

In JavaScript
```javascript
window._gdyBridge.register('getStringFromJs', function(a, b, c) {
   return a + b + c + " from JavaScript";
})
```
注意: 返回值可以为基本类型或null，也可以是json Element(对象、数组等)

In Java
```java
Object[] args = {1, "a", false};
webView.callJsFunction("getStringFromJs", args, (Boolean success, Object value) -> {
   if (success) {
      Toast.makeText(this, value != null ? value.toString() : "无返回值", Toast.LENGTH_SHORT).show();
   } else {
      Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show();
      Log.e("GdyBridgeWebView", "方法methodName调用失败:" + value.toString());
   }
}); // 屏幕上显示 1afalse from JavaScript
```

In Kotlin
```kotlin
webView.callJsFunction("getStringFromJs", arrayOf(1, "a", false)) { success: Boolean, value: Any? ->
   if (success) {
      Toast.makeText(this, value?.toString()?:"无返回值", Toast.LENGTH_SHORT).show()
   } else {
      Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show()
      Log.e("GdyBridgeWebView", "方法methodName调用失败: $value")
   }
} // 屏幕上显示 1afalse from JavaScript
```
如果调用不成功的话，success 为 false，同时value字段将是错误信息（String）。
value字段前端如果返回的是json Element的话，对应则是org.json包下的相关类(如:org.json.JSONObject, org.json.JSONArray等)。

传递对象:

In JavaScript 
```javascript
window._gdyBridge.register('getObjectFromJs', function() {
   return { key1: "value1", key2: "value2" };
});
```

In Java
```java
webView.callJsFunction("getObjectFromJs", (success, value) -> {
   if (success) {
      JSONObject jsonObject = (JSONObject) value;
      String str;
      if (jsonObject == null) {
         str = "无返回值";
      } else {
         str = "key1: " + jsonObject.optString("key1") + ", key2: " + jsonObject.optString("key2");
      }
      Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
   } else {
      Toast.makeText(this, "调用失败", Toast.LENGTH_SHORT).show();
      Log.e("GdyBridgeWebView", "方法getObjectFromJs调用失败:" + value);
   }
});
```

In Kotlin
```kotlin
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
```

### Android 原生提供方法，JavaScript来调用

传递字符串

In Java
```java
webView.register("getStringFromNative", (Object[] args) -> {
   StringBuilder result = new StringBuilder();
   for (Object arg : args) {
      result.append(arg.toString());
   }
   return result.toString();
});
```

In Kotlin
```kotlin
webView.register("getStringFromNative") { args: Array<Any> -> 
   return@register buildString {
      for (arg in args) {
         append(arg.toString())
      }
   }
}
```

In JavaScript
```javascript
window._gdyBridge.callNative('getStringFromNative', ['aaa','bbb', true], function(ret) {
   if (ret.success) {
      console.log(`返回结果:${ret.data}`);
   } else {
      console.log(`调用失败:${ret.data}`);
   }
});
```

传递对象

如果调用的时候没有传参的话, args将是个空数组, 但不是null

In Java
```java
webView.register("getObjectFromNative", (Object[] args) -> {
   JSONObject jsonObject = new JSONObject();
   jsonObject.put("key1", "value1");
   jsonObject.put("key2", "value2");
   return jsonObject;
});
```

In Kotlin
```kotlin
webView.register("getObjectFromNative") {
   return@register JSONObject().apply {
      put("key1", "value1")
      put("key2", "value2")
   }
}
```

In JavaScript
```javascript 
window._gdyBridge.callNative('getObjectFromNative', null, function(success, data) {
   if (success) {
      const str = JSON.stringify(data);
      console.log(`返回结果:${str}`);
      document.getElementById('bbb').innerHTML = str;
   } else {
      console.log(`调用失败:${data}`);
      document.getElementById('bbb').innerHTML = `调用失败:${data}`;
   }
});
```
### 移除方法

In Java
```java
webView.unRegister("medthodName");

```

In Kotlin
```kotlin
webView.unRegister("methodName")
```

In JavaScript
```javascript
window._gdyBridge.unRegister('methodName');
```

## 混淆

```
-keep class com.gdy.jsbridge.** {*;}
```
