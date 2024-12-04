package com.example.kotlinapp1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlinapp1.api.HttpHelper
import com.example.kotlinapp1.model.School
import com.example.kotlinapp1.model.Students
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject


class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.detail_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val siswaId = intent.getIntExtra("SISWA_ID", -1)
        fetchData(siswaId)

        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("SISWA_ID", siswaId)
            startActivity(intent)
        }
        val editbtn = findViewById<Button>(R.id.editbtn)
        editbtn.setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java)
            intent.putExtra("SISWA_ID", siswaId)
            startActivity(intent)
        }

        val hpsBtn = findViewById<Button>(R.id.deletebtn)
        hpsBtn.setOnClickListener {
           showDeleteConfirmation(siswaId)
        }
    }
    private fun fetchData(id: Int){
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
                val siswaTextView : TextView = findViewById(R.id.namaTextView)
                val kelaminTextView : TextView = findViewById(R.id.kelaminTextView)
                val sekolahTextView : TextView = findViewById(R.id.sekolahTextView)
                val gambar : ImageView = findViewById(R.id.gambar)
                val drawableimg : Int = when (siswa.kelamin){
                    "L" -> R.drawable.man
                    "P" -> R.drawable.girl
                    else -> {
                        R.drawable.img
                    }
                }
                siswaTextView.text = "Name : ${siswa.nama}"
                kelaminTextView.text = "Sex : " + if (siswa.kelamin == "L") "Laki-laki" else "Perempuan"
                sekolahTextView.text = "Asal Sekolah : " + " ${siswa.asalSekolah?.name ?: "Tidak diketahui"}"
                gambar.setImageResource(drawableimg)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching data", e)
            }
        }
    }
    private fun hapusData(siswaId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.delete("http://10.0.2.2:5000/api/Siswa/$siswaId")
                runOnUiThread {
                    if (!response.contains("404")) {
                        Toast.makeText(
                            this@DetailActivity,
                            "Data berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@DetailActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val jsonResponse = JSONObject(response)
                        val errorMessage = jsonResponse.optString("title", "Gagal menghapus data")
                        Toast.makeText(
                            this@DetailActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateActivity", "Error deleting data siswa", e)
                runOnUiThread {
                    Toast.makeText(this@DetailActivity, "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showDeleteConfirmation(siswaID: Int) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                hapusData(siswaID)
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}