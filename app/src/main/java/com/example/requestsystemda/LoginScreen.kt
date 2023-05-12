package com.example.requestsystemda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.requestsystemda.databinding.ActivityLoginScreenBinding
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener{
            val intent = Intent(this, SignupScreen::class.java)
            startActivity(intent)
        }

        val showPasswordButton = findViewById<ImageButton>(R.id.btnVisibility)
        val passwordv = findViewById<EditText>(R.id.textPassword)
        var isPasswordVisible = false

        showPasswordButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                passwordv.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                showPasswordButton.setBackgroundResource(R.drawable.show)
            } else {
                // Hide password
                passwordv.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                showPasswordButton.setBackgroundResource(R.drawable.hide)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.txtEmail.text.toString()
            val password = binding.textPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                if (email == "admin@admin.com" && password == "adminadmin") {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, AdminHomeScreen::class.java)
                            startActivity(intent)
                            binding.txtEmail.setText("")
                            binding.textPassword.setText("")
                        } else {
                            Toast.makeText(this, "Please check your username and password.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            binding.txtEmail.setText("")
                            binding.textPassword.setText("")
                        } else {
                            Toast.makeText(this, "Please check your username and password.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else{
                Toast.makeText(this, "Please fill up all the empty fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        firebaseAuth = FirebaseAuth.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid.toString()

        if (user != null){

            if(uid == "XaStBxPlLYQYk8gmQ7UBTldC7KC3") {
                val intent = Intent(this, AdminHomeScreen::class.java)
                startActivity(intent)
            }else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}