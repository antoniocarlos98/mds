package com.example.listatelefonica.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.listatelefonica.R
import com.example.listatelefonica.databinding.ActivityNovoContactoBinding
import com.example.listatelefonica.viewmodel.NovoContactoViewModel

const val CHANNEL_ID = "channelId"

class NovoContatoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovoContactoBinding
    private lateinit var viewModel: NovoContactoViewModel
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var i: Intent
    private var imagemId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        i = intent

        viewModel = ViewModelProvider(this)[NovoContactoViewModel::class.java]
        observe()

        binding.imagemFoto.setOnClickListener {
            launcher.launch(
                Intent(
                    applicationContext,
                    SelecionarImagemContactoActivity::class.java
                )
            )
        }

        binding.buttonGravar.setOnClickListener {
            val nome = binding.editNome.text.toString()
            val email = binding.editEmail.text.toString()
            val endereco = binding.editEndereco.text.toString()
            val telefone = binding.editTelefone.text.toString()
            viewModel.insert(nome, email, endereco, telefone, imagemId)
        }

        binding.buttonCancelar.setOnClickListener {
            setResult(0, i)
            finish()
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null && it.resultCode == 1) {
                imagemId = it.data?.getIntExtra("id", 0)!!
                if (imagemId > 0) {
                    binding.imagemFoto.setImageResource(imagemId)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "test description for my channel"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun observe() {
        viewModel.novoContacto().observe(this, Observer {
            // Create the notification
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("PhoneCJK")
                .setContentText("Contato salvo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(1, builder.build())
            }

            // Toast message

            setResult(1, i)
            finish()
        })
    }
}
