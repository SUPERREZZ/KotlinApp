package com.example.kotlinapp1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.kotlinapp1.api.HttpHelper
import com.example.kotlinapp1.model.School
import com.example.kotlinapp1.model.Students
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class UpdateActivity : Activity() {

    private lateinit var sekolahList: MutableList<School>
    private lateinit var etNama: EditText
    private lateinit var spAsalSekolah: Spinner
    private lateinit var rgJenisKelamin: RadioGroup
    private lateinit var rbLaki: RadioButton
    private lateinit var rbPerempuan: RadioButton
    private lateinit var btnSimpan: Button
    private lateinit var back: ImageView

    private var siswaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update)


        etNama = findViewById(R.id.etNama)
        spAsalSekolah = findViewById(R.id.spAsalSekolah)
        rgJenisKelamin = findViewById(R.id.rgJenisKelamin)
        rbLaki = findViewById(R.id.rbLaki)
        rbPerempuan = findViewById(R.id.rbPerempuan)
        btnSimpan = findViewById(R.id.btnSimpan)
        back = findViewById<ImageView>(R.id.back)
        siswaId = intent.getIntExtra("SISWA_ID", -1)
        if (siswaId == -1) {
            Toast.makeText(this, "ID siswa tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sekolahList = mutableListOf()
        getSekolahList()
        populateForm(siswaId)

        btnSimpan.setOnClickListener {
            simpanPerubahan()
        }
        back.setOnClickListener{
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SISWA_ID", siswaId)
            startActivity(intent)
        }
    }

    private fun populateForm(id: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.get("http://10.0.2.2:5000/api/Siswa/$id")
                val siswares = JSONObject(response)
                val asalSekolahObj = siswares.optJSONObject("asalSekolah")
                val asalSekolah = if (asalSekolahObj != null) {
                    School(
                        id = asalSekolahObj.getInt("id"),
                        name = asalSekolahObj.getString("name")
                    )
                } else {
                    null
                }

                val siswa = Students(
                    id = siswares.getInt("id"),
                    nama = siswares.getString("name"),
                    kelamin = siswares.getString("sex"),
                    asalSekolah = asalSekolah
                )
                runOnUiThread {
                    etNama.setText(siswa.nama)
                    if (siswa.kelamin == "L") {
                        rbLaki.isChecked = true
                    } else {
                        rbPerempuan.isChecked = true
                    }
                    val sekolahAdapter = ArrayAdapter(
                        this@UpdateActivity,
                        android.R.layout.simple_spinner_item,
                        sekolahList.map { it.name }
                    )
                    sekolahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spAsalSekolah.adapter = sekolahAdapter
                    if (siswa.asalSekolah?.id != -1) {
                        val selectedPosition = sekolahList.indexOfFirst { it.id == siswa.asalSekolah?.id }
                        if (selectedPosition != -1) {
                            spAsalSekolah.setSelection(selectedPosition)
                            Log.d("UpdateActivity", "Selected position: $selectedPosition")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateActivity", "Error fetching data siswa", e)
            }
        }
    }

    private fun simpanPerubahan() {
        val nama = etNama.text.toString()
        val selectedJenisKelaminId = rgJenisKelamin.checkedRadioButtonId
        val jenisKelamin = if (selectedJenisKelaminId == R.id.rbLaki) "L" else "P"
        val selectedSekolahPosition = spAsalSekolah.selectedItemPosition

        if (selectedSekolahPosition < 0 || selectedSekolahPosition >= sekolahList.size) {
            Toast.makeText(this, "Pilih sekolah asal yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSekolahId = sekolahList[selectedSekolahPosition].id
        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val siswaUpdated = JSONObject()
        siswaUpdated.put("name", nama)
        siswaUpdated.put("sex", jenisKelamin)
        siswaUpdated.put("asalSekolahId", selectedSekolahId)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("UpdateActivity", "Mengirim JSON: $siswaUpdated")
                val response = HttpHelper.put("http://10.0.2.2:5000/api/Siswa/$siswaId",siswaUpdated.toString()
                )
                Log.d("UpdateActivity", "Response dari server: $response")

                runOnUiThread {
                    if (!response.contains("404")) {
                        Toast.makeText(this@UpdateActivity, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@UpdateActivity, DetailActivity::class.java)
                        intent.putExtra("SISWA_ID", siswaId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@UpdateActivity, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateActivity", "Error updating data siswa", e)
                runOnUiThread {
                    Toast.makeText(this@UpdateActivity, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getSekolahList() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.get("http://10.0.2.2:5000/api/AsalSekolah")
                val jsonArray = JSONArray(response)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val sekolah = School(
                        id = jsonObject.getInt("id"),
                        name = jsonObject.getString("name")
                    )
                    sekolahList.add(sekolah)
                }
                runOnUiThread {
                    val sekolahAdapter = ArrayAdapter(
                        this@UpdateActivity,
                        android.R.layout.simple_spinner_item,
                        sekolahList.map { it.name }
                    )
                    sekolahAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spAsalSekolah.adapter = sekolahAdapter
                }
            } catch (e: Exception) {
                Log.e("UpdateActivity", "Error fetching sekolah list", e)
            }
        }
    }
}
