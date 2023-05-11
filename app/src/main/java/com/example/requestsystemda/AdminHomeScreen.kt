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

class AdminHomeScreen : AppCompatActivity() {

    private lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_screen)

        listView = findViewById(R.id.listView1)
        val docIDs: MutableList<String> = mutableListOf()
        val clientReq: MutableList<String> = mutableListOf()
        val userReqOrderId: MutableList<String> = mutableListOf()
        val userReqDetails: MutableList<String> = mutableListOf()
        var isGrantAccess: Boolean? = null


        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.custom_list_layout
        )

        val db = Firebase.firestore
        db.collection("request")
            .whereEqualTo("access", false)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val docID = document.id
                        docIDs.add(docID)

                        val data = document.toObject(RequestModel::class.java)

                        val fname = data.first_name
                        val mname = data.middle_name
                        val lname = data.last_name
                        val address = data.address
                        val date = data.date

                        adapter.add("Request access from $fname $mname $lname.\nAddress: $address.\nDate: $date")


                    }
                } else {
                    Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                }
            }


        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->

            if (isGrantAccess == true) {
                val displayValue = parent.getItemAtPosition(position)
                val reqID = parent.getItemIdAtPosition(position)


                val dialogBinding = layoutInflater.inflate(R.layout.admin_fertilizer_request, null)

                val myDialog = Dialog(this)
                myDialog.setContentView(dialogBinding)

                val window = myDialog.window
                window?.setGravity(Gravity.CENTER)
                window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency

                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.setCanceledOnTouchOutside(true)
                myDialog.show()


                val details = dialogBinding.findViewById<TextView>(R.id.textDetails)
                details.text = displayValue.toString()

                val accept = dialogBinding.findViewById<Button>(R.id.btnAccept)
                accept.setOnClickListener {

                    val clientId = userReqOrderId[reqID.toInt()]
                    val displayReqId = clientReq[reqID.toInt()]
                    val detailVal = userReqDetails[reqID.toInt()]

                    val txtQuantity = dialogBinding.findViewById<EditText>(R.id.txtStocks)
                    val quantity = txtQuantity.text.toString()

                    val txtMessage = dialogBinding.findViewById<EditText>(R.id.txtMessage)
                    val message = txtMessage.text.toString()

                    val granted = true

                    val feedbackData = FeedbackModel(clientId, detailVal, quantity, message, granted)

                    db.collection("feedbacks")
                        .add(feedbackData)
                        .addOnSuccessListener { documentReference ->

                            db.collection("client_request")
                                .document(displayReqId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Feedback sent..!", Toast.LENGTH_LONG).show()

                                    // Refresh adapter and dismiss dialog here
                                    db.collection("client_request")
                                        .get()
                                        .addOnCompleteListener { task ->
                                            adapter.clear()
                                            if (task.isSuccessful) {
                                                for (document in task.result!!) {
                                                    val docID = document.id
                                                    docIDs.add(docID)

                                                    val data = document.toObject(ProductRequestedModel::class.java)

                                                    val reqOrderId = data.client_id
                                                    userReqOrderId.add(reqOrderId.toString())

                                                    val reqDetail = data.request_deteail
                                                    userReqDetails.add(reqDetail.toString())

                                                    val cDetail = data.client_deteail
                                                    val cAddress = data.client_address
                                                    val cDate = data.client_date
                                                    val cReqDetail = data.request_deteail
                                                    val cReqArea = data.request_area

                                                    adapter.add("From: $cDetail \n\nAddress: $cAddress \n\nDate: $cDate \n\n$cReqDetail \n\nArea is: $cReqArea hectare")
                                                }
                                                adapter.notifyDataSetChanged()
                                            } else {
                                                Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    myDialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    // There was an error adding the data
                                    Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            // There was an error adding the data
                            Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }

                val decline = dialogBinding.findViewById<Button>(R.id.btnDecline)
                decline.setOnClickListener {
                    val clientId = userReqOrderId[reqID.toInt()]

                    val displayReqId = clientReq[reqID.toInt()]

                    val detailVal = userReqDetails[reqID.toInt()]

                    val quantity = ""
                    val message = "Your request has been declined..!"
                    val granted = false

                    val feedbackData = FeedbackModel(clientId, detailVal, quantity, message, granted)

                    db.collection("feedbacks")
                        .add(feedbackData)
                        .addOnSuccessListener { documentReference ->

                            db.collection("client_request")
                                .document(displayReqId)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this, "You have declined the request. The request will be deleted.", Toast.LENGTH_LONG  ).show()

                                    // Refresh adapter and dismiss dialog here
                                    db.collection("client_request")
                                        .get()
                                        .addOnCompleteListener { task ->
                                            adapter.clear()
                                            if (task.isSuccessful) {
                                                for (document in task.result!!) {
                                                    val docID = document.id
                                                    docIDs.add(docID)

                                                    val data = document.toObject(ProductRequestedModel::class.java)

                                                    val reqOrderId = data.client_id
                                                    userReqOrderId.add(reqOrderId.toString())

                                                    val reqDetail = data.request_deteail
                                                    userReqDetails.add(reqDetail.toString())

                                                    val cDetail = data.client_deteail
                                                    val cAddress = data.client_address
                                                    val cDate = data.client_date
                                                    val cReqDetail = data.request_deteail
                                                    val cReqArea = data.request_area

                                                    adapter.add("From: $cDetail \n\nAddress: $cAddress \n\nDate: $cDate \n\n$cReqDetail \n\nArea is: $cReqArea hectare")
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Error getting documents: ${task.exception}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            adapter.notifyDataSetChanged()
                                        }
                                    myDialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    // There was an error adding the data
                                    Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        }
                }
            }else {

                val dialogBinding = layoutInflater.inflate(R.layout.grant_access, null)

                val myDialog = Dialog(this)
                myDialog.setContentView(dialogBinding)

                val window = myDialog.window
                window?.setGravity(Gravity.CENTER)
                window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency


                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.setCanceledOnTouchOutside(true)
                myDialog.show()

                val yes = dialogBinding.findViewById<Button>(R.id.btnYes)
                yes.setOnClickListener {
                    val selectedID = parent.getItemIdAtPosition(position).toInt()
                    val id = docIDs[selectedID]

                    db.collection("request")
                        .document(id)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                // Create a new message in the messages collection
                                val messageData = hashMapOf(
                                    "user_id" to id,
                                    "message" to "Your request access has been granted"
                                )
                                db.collection("messages")
                                    .add(messageData)
                                    .addOnSuccessListener {
                                        // Update the user's access to true
                                        db.collection("request")
                                            .document(id)
                                            .update("access", true)
                                            .addOnSuccessListener {
                                                db.collection("request")
                                                    .whereEqualTo("access", false)
                                                    .get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            adapter.clear() // clear the adapter before adding new data
                                                            for (document in task.result!!) {
                                                                val docID = document.id
                                                                docIDs.add(docID)

                                                                val data = document.toObject(RequestModel::class.java)

                                                                val fname = data.first_name
                                                                val mname = data.middle_name
                                                                val lname = data.last_name
                                                                val address = data.address
                                                                val date = data.date

                                                                adapter.add("Request access from $fname $mname $lname.\nAddress: $address.\nDate: $date")
                                                            }
                                                            adapter.notifyDataSetChanged() // notify the adapter that the data has changed
                                                        } else {
                                                            Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                Toast.makeText(this, "Access granted..!", Toast.LENGTH_LONG).show()
                                                myDialog.dismiss()
                                            }
                                    }
                            } else {
                                Toast.makeText(this, "Error getting document", Toast.LENGTH_LONG).show()
                            }
                        }


                }

                val no = dialogBinding.findViewById<Button>(R.id.btnNo)
                no.setOnClickListener {
                    val selectedID = parent.getItemIdAtPosition(position).toInt()
                    val id = docIDs[selectedID]
                    db.collection("request")
                        .document(id)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                // Create a new message in the messages collection
                                val messageData = hashMapOf(
                                    "user_id" to id,
                                    "message" to "Your request access has been declined! Please refer to the DA Surallah for further details"
                                )
                                db.collection("messages")
                                    .add(messageData)
                                    .addOnSuccessListener {
                                        // Delete the request document
                                        db.collection("request")
                                            .document(id)
                                            .delete()
                                            .addOnSuccessListener {
                                                // Refresh the adapter
                                                db.collection("request")
                                                    .whereEqualTo("access", false)
                                                    .get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            adapter.clear() // clear the adapter before adding new data
                                                            for (document in task.result!!) {
                                                                val docID = document.id
                                                                docIDs.add(docID)

                                                                val data = document.toObject(RequestModel::class.java)

                                                                val fname = data.first_name
                                                                val mname = data.middle_name
                                                                val lname = data.last_name
                                                                val address = data.address
                                                                val date = data.date

                                                                adapter.add("Request access from $fname $mname $lname.\nAddress: $address.\nDate: $date")
                                                            }
                                                            adapter.notifyDataSetChanged() // notify the adapter that the data has changed
                                                        } else {
                                                            Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                Toast.makeText(this, "Access declined..!", Toast.LENGTH_LONG).show()
                                                myDialog.dismiss()
                                            }
                                    }
                            } else {
                                Toast.makeText(this, "Error getting document", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        val logout = findViewById<ImageButton>(R.id.btnLogout)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        val products = findViewById<TextView>(R.id.button)
        products.setOnClickListener {
            val intent = Intent(this, AdminProduct::class.java)
            startActivity(intent)
        }
        val grantFertilizer = findViewById<ImageButton>(R.id.btnFertilizer)
        grantFertilizer.setOnClickListener {
            adapter.clear()
            isGrantAccess = true

            db.collection("client_request")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val docID = document.id
                            clientReq.add(docID)

                            val data = document.toObject(ProductRequestedModel::class.java)

                            val reqOrderId = data.client_id
                            userReqOrderId.add(reqOrderId.toString())

                            val reqDetail = data.request_deteail
                            userReqDetails.add(reqDetail.toString())

                            val cDetail = data.client_deteail
                            val cAddress = data.client_address
                            val cDate = data.client_date
                            val cReqDetail = data.request_deteail
                            val cReqArea = data.request_area

                            adapter.add("From: $cDetail \n\nAddress: $cAddress \n\nDate: $cDate \n\n$cReqDetail \n\nArea is: $cReqArea hectare")


                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this,
                            "Error getting documents: ${task.exception}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        val grantAccess = findViewById<ImageButton>(R.id.btnAccess)
        grantAccess.setOnClickListener {
            adapter.clear()
            isGrantAccess = false

            db.collection("request")
                .whereEqualTo("access", false)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val docID = document.id
                            docIDs.add(docID)

                            val data = document.toObject(RequestModel::class.java)

                            val fname = data.first_name
                            val mname = data.middle_name
                            val lname = data.last_name
                            val address = data.address
                            val date = data.date

                            adapter.add("Request access from $fname $mname $lname.\nAddress: $address.\nDate: $date")


                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this,
                            "Error getting documents: ${task.exception}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

    }

}