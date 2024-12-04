package com.example.kotlinapp1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinapp1.StudentsAdapter.StudentsAdapter
import com.example.kotlinapp1.api.HttpHelper
import com.example.kotlinapp1.model.Students
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray


class MainActivity : AppCompatActivity() {

    private lateinit var siswaList: MutableList<Students>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        siswaList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentsAdapter(siswaList){ siswa: Students ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("SISWA_ID", siswa.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        fetchData()

        val fabTambah = findViewById<FloatingActionButton>(R.id.fabTambah)

        fabTambah.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

    }
    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = HttpHelper.get("http://10.0.2.2:5000/api/Siswa")
                val jsonArray = JSONArray(response)

                siswaList.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val siswa = Students(
                        id = jsonObject.getInt("id"),
                        nama = jsonObject.getString("name"),
                        kelamin = jsonObject.getString("sex")
                    )
                    siswaList.add(siswa)
                }
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching data", e)
            }
        }

    }
}
