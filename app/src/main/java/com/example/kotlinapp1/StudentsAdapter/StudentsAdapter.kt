package com.example.kotlinapp1.StudentsAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinapp1.R
import com.example.kotlinapp1.model.Students

class StudentsAdapter(private val siswaList: List<Students>,private val onDetailClick: (siswa : Students) -> Unit) :
    RecyclerView.Adapter<StudentsAdapter.SiswaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.students_item, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = siswaList[position]
        holder.namaTextView.text = siswa.nama
        holder.kelaminTextView.text = if (siswa.kelamin == "L") "Laki-laki" else "Perempuan"
        holder.buttonDetail.setOnClickListener{
            onDetailClick(siswa)
        }
        val drawableimg : Int = when (siswa.kelamin){
            "L" -> R.drawable.man
            "P" -> R.drawable.girl
            else -> {
                R.drawable.img
            }
        }
        holder.profileImageView.setImageResource(drawableimg)
    }

    override fun getItemCount(): Int = siswaList.size

    class SiswaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val namaTextView: TextView = view.findViewById(R.id.namaTextView)
        val kelaminTextView: TextView = view.findViewById(R.id.kelaminTextView)
        val buttonDetail: Button = view.findViewById(R.id.detail)
        val profileImageView : ImageView =  view.findViewById(R.id.profileImageView)
    }
}
