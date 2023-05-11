package com.example.requestsystemda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentDate = LocalDate.now()
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

        firebaseAuth = FirebaseAuth.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid.toString()
        val db = Firebase.firestore
        var firstName: String? = null
        var middleName: String? = null
        var lastName: String? = null
        var address: String? = null
        var access: Boolean? = null

        db.collection("request")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    access = document.getBoolean("access")
                }
            }


        val logout = findViewById<ImageButton>(R.id.btnLogout)
        logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        val order = findViewById<ImageButton>(R.id.btn_order)
        order.setOnClickListener{
            order.isEnabled = false

            if (access == true) {
                val intent = Intent(this, OrderScreen::class.java)
                startActivity(intent)
                order.isEnabled = true
            } else if (access == false) {
                Toast.makeText(this, "You are already in the wait-list, please be patient.", Toast.LENGTH_LONG).show()
                order.isEnabled = true
            } else {
                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            firstName = document.getString("first_name")
                            middleName = document.getString("middle_name")
                            lastName = document.getString("last_name")
                            address = document.getString("address")

                            val year = currentDate.year
                            val month = currentDate.monthValue
                            val day = currentDate.dayOfMonth

                            val currentMonthName = months[month - 1]

                            val dateToday = "$currentMonthName/$day/$year"
                            val setAccess = false

                            val requestData = RequestModel(firstName, middleName, lastName, address, dateToday, setAccess)

                            db.collection("request")
                                .document(uid)
                                .set(requestData)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(this, "You will be now on the wait-list, please wait for our feedback.", Toast.LENGTH_LONG).show()
                                    order.isEnabled = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(this, "Document not found.", Toast.LENGTH_LONG).show()
                            order.isEnabled = true
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }


        }
    }
}