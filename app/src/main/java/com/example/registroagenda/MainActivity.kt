package com.example.registroagenda

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.*
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var txtNombre : TextView
    lateinit var txtApellido : TextView
    lateinit var txtTelefono : TextView
    lateinit var txtCorreo : TextView
    lateinit var imgPhoto : ImageView
    lateinit var btnCamera : ImageButton
    lateinit var btnGuardar : Button
    val REQUEST_IMAGE_CAPTURE = 1
    val PERMISSION_REQUEST_CODE: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtNombre = findViewById(R.id.txtName)
        txtApellido = findViewById(R.id.txtApellido)
        txtTelefono = findViewById(R.id.txtTelefono)
        txtCorreo = findViewById(R.id.txtCorreo)
        imgPhoto = findViewById(R.id.imgPhoto)
        btnCamera = findViewById(R.id.btnCamera)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCamera.setOnClickListener {
            if (checkPermission()) {
                takePicture()
            } else {
                requestPermissions()
            }
        }
        btnGuardar.setOnClickListener {
            saveContact()
        }
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun checkPermission() : Boolean {
        return (checkSelfPermission(this, CAMERA)== PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(CAMERA), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePicture()
                } else {
                    Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val bitmap = data!!.extras!!.get("data") as Bitmap
                imgPhoto.setImageBitmap(bitmap)
            }

        }
    }


    private fun saveContact() {
        if (!txtNombre.text.toString().isEmpty() && !txtTelefono.text.toString().isEmpty()) {
            val intent = Intent(Intent.ACTION_INSERT)
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE)
            intent.putExtra(ContactsContract.Intents.Insert.NAME, txtNombre.text.toString() + " " + txtApellido.text.toString())
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, txtCorreo.text.toString())
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, txtTelefono.text.toString())
            val bitmap = (imgPhoto.getDrawable() as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte = baos.toByteArray()
            val row = ContentValues().apply {
                put(ContactsContract.CommonDataKinds.Photo.PHOTO, imageInByte)
                put(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
            }
            val data = arrayListOf(row)
            intent.putParcelableArrayListExtra(
                    ContactsContract.Intents.Insert.DATA, data)
            startActivity(intent)
        }
    }
}