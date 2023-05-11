package com.example.requestsystemda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.*
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
        var granted: Boolean? = null

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.custom_list_layout
        )

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

        val btnMessage = findViewById<ImageButton>(R.id.btnMessage)
        btnMessage.setOnClickListener{

            val dialogBinding = layoutInflater.inflate(R.layout.message_screen, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            val window = myDialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency

            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.setCanceledOnTouchOutside(true)
            myDialog.show()

            val listview = dialogBinding.findViewById<ListView>(R.id.lvMessage)

            db.collection("messages")
                .whereEqualTo("user_id", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val data = document.toObject(MessageModel::class.java)
                            val message = data.message

                            adapter.add(
                                "$message"
                            )
                        }
                    }
                }

            db.collection("feedbacks")
                .whereEqualTo("client_id", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val data = document.toObject(FeedbackModel::class.java)

                            val req_id = data.client_id
                            val req_detail = data.detail_req
                            val req_quantity = data.quantity
                            val feedback_message = data.message
                            granted = data.granted

                            // Set the text color based on the value of granted
                            if (granted == true) {
                                adapter.add(
                                    "Your request is being processed, please refer to the message below. \n\n$req_detail \n\nFertilizer granted: $req_quantity sacks \n" +
                                            "\nMessage from DA:\n$feedback_message"
                                )
                            } else {
                                adapter.add(
                                    "$feedback_message \n\n$req_detail"
                                )

                            }

                        }
                    } else {
                        Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                    }
                }


            listview.adapter = adapter

        }
    }

}

