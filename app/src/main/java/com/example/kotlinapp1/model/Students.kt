package com.example.kotlinapp1.model

data class Students(
    val id: Int,
    val nama: String,
    val kelamin: String,
    val asalSekolah: School? = null
)
