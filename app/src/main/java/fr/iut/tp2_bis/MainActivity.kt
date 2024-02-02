package fr.iut.tp2_bis

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        val postalCodeEditText = findViewById<AutoCompleteTextView>(R.id.editText)
        val cityEditText = findViewById<AutoCompleteTextView>(R.id.cityEditText)
        val button = findViewById<Button>(R.id.button)
        val cityTextView = findViewById<TextView>(R.id.cityTextView)

        postalCodeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val request = Request.Builder()
                    .url("http://api.zippopotam.us/FR/$input")
                    .build()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val json = JSONObject(response.body?.string() ?: "")
                            val cities = json.getJSONArray("places")
                                .let { 0.until(it.length()).map { i -> it.getJSONObject(i) } }
                                .map { it.getString("place name") }

                            runOnUiThread {
                                val adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_dropdown_item_1line, cities)
                                cityEditText.setAdapter(adapter)
                                cityEditText.showDropDown()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cityEditText.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedCity = cityEditText.adapter.getItem(position) as String
            postalCodeEditText.setText(selectedCity)
        }

        button.setOnClickListener {
            val city = cityEditText.text.toString()
            cityTextView.text = city
        }
    }
}