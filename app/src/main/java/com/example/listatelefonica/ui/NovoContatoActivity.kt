package com.example.listatelefonica.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private val Window.FEATURE_NO_TITLE: Int
    get() {
        TODO("Not yet implemented")
    }
const val CHANNEL_ID = "channelId"

class NovoContatoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovoContactoBinding
    private lateinit var viewModel: NovoContactoViewModel
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var i: Intent
    private var imagemId: Int = -1
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.buttonGravar.setOnClickListener {
            showProgressBar()
            val nome = binding.editNome.text.toString()
            val endereco = binding.editEndereco.text.toString()
            val telefone = binding.editTelefone.text.toString()
            val email = binding.editEmail.text.toString()

            Log.d("NovoContatoActivity", "Tentando salvar usuário: nome=$nome, endereco=$endereco, telefone=$telefone, email=$email")

            val user = User(nome, endereco, telefone, email)
            if (uid != null){

                databaseReference.child(uid).setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d("NovoContatoActivity", "Usuário salvo com sucesso no banco de dados")
                        uploadProfilePic()

                    }else{
                        hideProgressBar()
                        Toast.makeText( this@NovoContatoActivity, "Falha ao atualizar o perfil", Toast.LENGTH_SHORT).show()
                        Log.e("NovoContatoActivity", "Erro ao salvar usuário no banco de dados: ${task.exception}")
                    }
                }
            }
        }

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

    private fun User(nome: String, endereco: String, telefone: String, email: String) {

    }

    private fun uploadProfilePic() {
        imageUri = Uri.parse("android.resource://$packageName/${R.drawable.profiledefault}")
        storageReference = FirebaseStorage.getInstance().getReference("Users/"+auth.currentUser?.uid)
        storageReference.putFile(imageUri).addOnSuccessListener {
            hideProgressBar()
            Toast.makeText( this@NovoContatoActivity, "Profile Sucessfuly update", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            hideProgressBar()
            Toast.makeText( this@NovoContatoActivity, "Failed to update the image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgressBar(){
        dialog = Dialog(this@NovoContatoActivity)
        dialog.requestWindowFeature(window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun hideProgressBar(){
        dialog.dismiss()
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


