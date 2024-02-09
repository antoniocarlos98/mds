package com.example.listatelefonica.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listatelefonica.R
import com.example.listatelefonica.databinding.ActivitySelecionarImagemContactoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SelecionarImagemContactoActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var btnSavePicture: Button
    val REQUEST_IMAGE_CAPTURE = 100

    private lateinit var binding: ActivitySelecionarImagemContactoBinding
    private lateinit var i: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelecionarImagemContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageView = findViewById(R.id.image_save)
        button = findViewById(R.id.btn_take_picture)
        btnSavePicture = findViewById(R.id.btn_save_picture)

        button.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Error " + e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        i = intent

        binding.buttonRemoverImagem.setOnClickListener { sendID(R.drawable.profiledefault) }
        val imageView = findViewById<ImageView>(R.id.image_save) // assumindo que imageView já foi definido

        val btnSavePicture = findViewById<Button>(R.id.btn_save_picture)
        btnSavePicture.setOnClickListener {sendID(R.drawable.profiledefault)
            if (imageView.drawable != null) {
                try {
                    val imageBitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val savedImageFile = saveImage(imageBitmap)
                    val savedBitmap = BitmapFactory.decodeFile(savedImageFile.absolutePath)
                    imageView.setImageBitmap(savedBitmap)
                    // Removido o Toast para "Imagem salva!"
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao salvar a imagem: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Captura uma imagem antes de salvar", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun sendID(imagemId: Int) {
        i.putExtra("id", imagemId)
        setResult(1, i)
        val imageResourceID = 1
        imageView.setImageResource(imageResourceID)
        finish()
    }

    private fun saveImage(bitmap: Bitmap): File {
        val filename = "image.png"
        val file = File(externalCacheDir, filename)

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }}
