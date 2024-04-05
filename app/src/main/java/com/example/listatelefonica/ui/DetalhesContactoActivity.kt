package com.example.listatelefonica.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.listatelefonica.R
import com.example.listatelefonica.databinding.ActivityDetalhesContactoBinding
import com.example.listatelefonica.model.Contacto
import com.example.listatelefonica.viewmodel.DetalheContactoViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class DetalhesContactoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhesContactoBinding
    private lateinit var viewModel: DetalheContactoViewModel
    private lateinit var i: Intent
    private lateinit var contacto: Contacto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        i = intent

        val id = i.getIntExtra("id", 0)
        if (id <= 0) {
            setResult(0, i)
            finish()
        }

        viewModel = ViewModelProvider(this)[DetalheContactoViewModel::class.java]

        observe()

        viewModel.getContacto(id)

        binding.buttonCancelar.setOnClickListener {
            changeEditable(false)
            binding.layoutEditar.visibility = View.VISIBLE
            binding.layoutEditarEliminar.visibility = View.GONE
        }

        binding.buttonEditar.setOnClickListener {
            changeEditable(true)
            binding.layoutEditar.visibility = View.GONE
            binding.layoutEditarEliminar.visibility = View.VISIBLE
        }

        binding.imagemFoto.setOnClickListener {
            if (binding.editNome.isEnabled) {
                val i = Intent(applicationContext, SelecionarImagemContactoActivity::class.java)
                startActivity(i)
            }
        }

        binding.buttonEliminar.setOnClickListener {
            viewModel.delete(contacto)
            setResult(1, i)
            finish()
        }

        binding.buttonGravar.setOnClickListener {
            viewModel.update(
                Contacto(
                    id = contacto.id,
                    nome = binding.editNome.text.toString(),
                    endereco = binding.editEndereco.text.toString(),
                    email = binding.editEmail.text.toString(),
                    telefone = binding.editTelefone.text.toString(),
                    imagemId = contacto.imagemId
                )
            )
            setResult(1, i)
            finish()
        }

        binding.buttonVoltar.setOnClickListener { finish() }

        binding.imagemTelefone.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + contacto.telefone)
            startActivity(dialIntent)
        }

        binding.imagemEmail.setOnClickListener {
            sendEmail(contacto.email, "Contato", "Enviado de ListaPhone APP")
        }

        binding.imagemLocation.setOnClickListener {
            openMapWithAddress(contacto.endereco)
        }

        changeEditable(false)
    }

    private fun sendEmail(destinatario: String, assunto: String, mensagem: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.data = Uri.parse("mailto:")
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
        i.putExtra(Intent.EXTRA_SUBJECT, assunto)
        i.putExtra(Intent.EXTRA_TEXT, mensagem)

        try {
            startActivity(Intent.createChooser(i, "Escolha o cliente de email"))
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeEditable(status: Boolean) {
        binding.editNome.isEnabled = status
        binding.editEmail.isEnabled = status
        binding.editEndereco.isEnabled = status
        binding.editTelefone.isEnabled = status
    }

    private fun observe() {
        viewModel.contacto().observe(this, Observer {
            contacto = it
            populate()
        })

        viewModel.delete().observe(this, Observer {
            Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
        })
        viewModel.update().observe(this, Observer {
            Toast.makeText(applicationContext, it, Toast.LENGTH_SHORT).show()
        })
    }

    private fun populate() {
        binding.editNome.setText(contacto.nome)
        binding.editEndereco.setText(contacto.endereco)
        binding.editEmail.setText(contacto.email)
        binding.editTelefone.setText(contacto.telefone)
        if (contacto.imagemId > 0) {
            binding.imagemFoto.setImageResource(contacto.imagemId)
        } else {
            binding.imagemFoto.setImageResource(R.drawable.profiledefault)
        }
    }

    private fun openMapWithAddress(address: String) {
        val geoIntentUri = Uri.parse("geo:0,0?q=$address")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Use o Google Maps

        // Verifica se há aplicativos que podem lidar com a Intent
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Se não houver, avisa o usuário
            Toast.makeText(this, "Google Maps não está instalado", Toast.LENGTH_SHORT).show()
        }
    }
}
