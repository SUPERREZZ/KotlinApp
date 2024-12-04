package com.example.kotlinapp1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlinapp1.api.HttpHelper
import com.example.kotlinapp1.model.School
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CreateActivity : AppCompatActivity() {
    private lateinit var etNama: EditText
    private lateinit var spAsalSekolah: Spinner
    private lateinit var rgJenisKelamin: RadioGroup
    private lateinit var rbLaki: RadioButton
    private lateinit var rbPerempuan: RadioButton
    private lateinit var btnSimpan: Button
    private lateinit var back: ImageView

    private var sekolahList: MutableList<School> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        etNama = findViewById(R.id.etNama)
        spAsalSekolah = findViewById(R.id.spAsalSekolah)
        rgJenisKelamin = findViewById(R.id.rgJenisKelamin)
        rbLaki = findViewById(R.id.rbLaki)
        rbPerempuan = findViewById(R.id.rbPerempuan)
        btnSimpan = findViewById(R.id.btnSimpan)
        back = findViewById(R.id.back)
        loadSchools()
        btnSimpan.setOnClickListener {
            simpanData()
        }
        back.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loadSchools() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.get("http://10.0.2.2:5000/api/AsalSekolah")
                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val school = School(
                        id = jsonObject.getInt("id"),
                        name = jsonObject.getString("name")
                    )
                    sekolahList.add(school)
                }
                runOnUiThread {
                    val adapter = ArrayAdapter(
                        this@CreateActivity,
                        android.R.layout.simple_spinner_item,
                        sekolahList.map { it.name }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spAsalSekolah.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("CreateActivity", "Error fetching schools", e)
                runOnUiThread {
                    Toast.makeText(this@CreateActivity, "Gagal memuat data sekolah", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun simpanData() {
        val nama = etNama.text.toString()
        val selectedJenisKelaminId = rgJenisKelamin.checkedRadioButtonId
        val jenisKelamin = if (selectedJenisKelaminId == R.id.rbLaki) "L" else "P"
        val selectedSekolahPosition = spAsalSekolah.selectedItemPosition
        val selectedSekolahId = sekolahList[selectedSekolahPosition].id

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val siswaBaru = JSONObject()
        siswaBaru.put("name", nama)
        siswaBaru.put("sex", jenisKelamin)
        siswaBaru.put("asalSekolahId", selectedSekolahId)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.post("http://10.0.2.2:5000/api/Siswa", siswaBaru.toString())
                val responseJson = JSONObject(response)
                if (!responseJson.has("status")) {
                    runOnUiThread {
                        Toast.makeText(this@CreateActivity, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CreateActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateActivity, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateActivity", "Error saving data", e)
                runOnUiThread {
                    Toast.makeText(this@CreateActivity, "Terjadi kesalahan saat menyimpan data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
