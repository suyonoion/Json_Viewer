package com.zuhri.jsontool

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private val jsonTreeAdapter = JsonTreeAdapter()

    // Katup Intake: Pemilih file berbasis Storage Access Framework (SAF)
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { processFileIntake(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup daftar tampilan struktur JSON/XML
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = jsonTreeAdapter

        // Sambungkan tombol FAB ke pemilih file
        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        fabAdd.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/json", "text/xml"))
        }
    }

    // Mekanisme pembacaan arus data dari penyimpanan ke memori sementara
    private fun processFileIntake(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val rawData = reader.readText()
                
                // Teruskan data mentah ke blok pemrosesan JSON
                val jsonObject = JSONObject(rawData)
                
                // Contoh instruksi operasional: Eksekusi penghapusan serempak
                // targetKeyToDestroy adalah kunci yang ingin dilenyapkan dari sistem
                val processedData = executeMassExtraction(jsonObject, targetKeyToDestroy = "id_tidak_berguna")
                
                // processedData sekarang berisi struktur JSON yang telah dibersihkan
                val flatList = mutableListOf<JsonNode>()
                flattenJson(processedData, "root", 0, flatList)

                runOnUiThread {
                    jsonTreeAdapter.submitList(flatList)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memproses material data", Toast.LENGTH_SHORT).show()
        }
    }

    // Meratakan struktur JSON bersarang menjadi daftar baris untuk RecyclerView
    private fun flattenJson(element: Any, key: String, depth: Int, result: MutableList<JsonNode>) {
        when (element) {
            is JSONObject -> {
                result.add(JsonNode(key, "{...}", depth))
                val keys = element.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    flattenJson(element.get(k), k, depth + 1, result)
                }
            }
            is JSONArray -> {
                result.add(JsonNode(key, "[...]", depth))
                for (i in 0 until element.length()) {
                    flattenJson(element.get(i), "[$i]", depth + 1, result)
                }
            }
            else -> {
                result.add(JsonNode(key, element.toString(), depth))
            }
        }
    }

    // Mesin Ekstraksi Presisi: Membedah dan menghapus simpul (node) secara rekursif
    private fun executeMassExtraction(jsonElement: Any, targetKeyToDestroy: String): Any {
        return when (jsonElement) {
            is JSONObject -> {
                val newJsonObject = JSONObject()
                val keys = jsonElement.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    // Jika kunci ini bukan target penghancuran, proses dan simpan
                    if (key != targetKeyToDestroy) {
                        newJsonObject.put(key, executeMassExtraction(jsonElement.get(key), targetKeyToDestroy))
                    }
                }
                newJsonObject
            }
            is JSONArray -> {
                val newJsonArray = JSONArray()
                for (i in 0 until jsonElement.length()) {
                    newJsonArray.put(executeMassExtraction(jsonElement.get(i), targetKeyToDestroy))
                }
                newJsonArray
            }
            else -> {
                // Tipe data primitif (String, Int, Boolean), biarkan utuh
                jsonElement
            }
        }
    }
}
