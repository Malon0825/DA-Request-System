package com.example.requestsystemda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.requestsystemda.databinding.ActivitySignupScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.Tag
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignupScreenBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val db = Firebase.firestore

        binding.btnGoback.setOnClickListener{

            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)

        }
        binding.btnSignup.setOnClickListener {

            binding.btnSignup.isEnabled = false
            val fName = binding.txtFName.text.toString()
            val mName = binding.txtMName.text.toString()
            val lName = binding.txtLName.text.toString()
            val email = binding.txtEmail.text.toString()
            val address = binding.txtAddress.text.toString()
            val password = binding.textPassword.text.toString()
            val conPassword = binding.txtConfPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && conPassword.isNotEmpty() && fName.isNotEmpty() && mName.isNotEmpty() && lName.isNotEmpty() && address.isNotEmpty()){
                if (password == conPassword){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            val user = FirebaseAuth.getInstance().currentUser
                            val uid = user?.uid

                            val userData = UserModel(fName, mName, lName, email, address)

                            db.collection("users")
                                .document(uid!!)
                                .set(userData)
                                .addOnSuccessListener { documentReference ->
                                    val intent = Intent(this, LoginScreen::class.java)
                                    startActivity(intent)
                                    binding.btnSignup.isEnabled = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                                    binding.btnSignup.isEnabled = true
                                }

                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                            binding.btnSignup.isEnabled = true
                        }
                    }
                }else{
                    Toast.makeText(this, "Password is not matching!!", Toast.LENGTH_SHORT).show()
                    binding.btnSignup.isEnabled = true
                }
            }else{
                Toast.makeText(this, "Please fill up all the empty fields.", Toast.LENGTH_SHORT).show()
                binding.btnSignup.isEnabled = true
            }
        }


    }
}