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
        val feedbackID: MutableList<String> = mutableListOf()

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

            val dialogBinding = layoutInflater.inflate(R.layout.logout_confirm, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            val window = myDialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency


            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.setCanceledOnTouchOutside(true)


            if (access == true) {
                val intent = Intent(this, OrderScreen::class.java)
                startActivity(intent)
                order.isEnabled = true
            } else if (access == false) {

                myDialog.show()

                val message = dialogBinding.findViewById<TextView>(R.id.txtFeedBack)
                message.setText("You are already in the wait-list, please be patient.")

                val logout_yes = dialogBinding.findViewById<Button>(R.id.btnLogYes)
                logout_yes.setOnClickListener {
                    myDialog.dismiss()
                }
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

                                    myDialog.show()

                                    val message = dialogBinding.findViewById<TextView>(R.id.txtFeedBack)
                                    message.setText("You will be now on the wait-list, please wait for our feedback.")

                                    val logout_yes = dialogBinding.findViewById<Button>(R.id.btnLogYes)
                                    logout_yes.setOnClickListener {
                                        myDialog.dismiss()
                                    }
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

            adapter.clear()

            db.collection("feedbacks")
                .whereEqualTo("client_id", uid)
                .get()
                .addOnCompleteListener { task ->
                    feedbackID.clear()
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val docID = document.id
                            feedbackID.add(docID)

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
            adapter.notifyDataSetChanged()


            listview.adapter = adapter

            listview.setOnItemClickListener { parent, view, position, id ->

                val displayValue = parent.getItemAtPosition(position)
                val reqID = parent.getItemIdAtPosition(position)
                val displayReqId = feedbackID[reqID.toInt()]

                val dialogBinding2 = layoutInflater.inflate(R.layout.logout_confirm, null)

                val myDialog2 = Dialog(this)
                myDialog2.setContentView(dialogBinding2)

                val window2 = myDialog2.window
                window2?.setGravity(Gravity.CENTER)
                window2?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency

                myDialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog2.setCanceledOnTouchOutside(true)
                myDialog2.show()

                val message = dialogBinding2.findViewById<TextView>(R.id.txtFeedBack)
                message.setText("Are you sure you want to delete this message?")

                val confirm = dialogBinding2.findViewById<Button>(R.id.btnLogYes)
                confirm.setOnClickListener {
                    db.collection("feedbacks")
                        .document(displayReqId)
                        .delete()
                        .addOnSuccessListener {
                            adapter.clear()

                            db.collection("feedbacks")
                                .whereEqualTo("client_id", uid)
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        feedbackID.clear()
                                        for (document in task.result!!) {
                                            val docID = document.id
                                            feedbackID.add(docID)

                                            val data = document.toObject(FeedbackModel::class.java)

                                            val req_detail = data.detail_req
                                            val req_quantity = data.quantity
                                            val feedback_message = data.message
                                            granted = data.granted

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
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this, "You have deleted the message.", Toast.LENGTH_LONG).show()
                            myDialog2.dismiss()
                        }

                }
            }
        }
    }

}

