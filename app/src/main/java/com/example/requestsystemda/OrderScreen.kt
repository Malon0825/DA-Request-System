package com.example.requestsystemda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class OrderScreen : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_screen)

        searchView = findViewById(R.id.searchView1)
        listView = findViewById(R.id.listView1)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid.toString()

        val currentDate = LocalDate.now()
        val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

        val products: MutableList<String> = mutableListOf()
        val docIDs: MutableList<String> = mutableListOf()
        val stocks_id: MutableList<String> = mutableListOf()

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.custom_list_layout
        )

        val db = Firebase.firestore
        db.collection("products")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    products.clear()
                    stocks_id.clear()
                    docIDs.clear()
                    for (document in task.result!!) {

                        val docID = document.id
                        docIDs.add(docID)

                        val data = document.toObject(ProductModel::class.java)

                        val name = data.name
                        val ratio = data.npk_ratio
                        val type = data.type
                        val stocks = data.stocks

                        products.add(name.toString())
                        stocks_id.add(stocks.toString())

                        adapter.add("Name: $name \nN-P-K Ratio: $ratio \nType: $type \nStocks: $stocks")


                    }
                } else {
                    Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                }
            }
        listView.adapter = adapter

        searchView.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (products.contains(query)){
                    adapter.filter.filter(query)
                }else{
                    Toast.makeText(applicationContext, "Item not found", Toast.LENGTH_LONG).show()
                }
                return false
            }
            override fun onQueryTextChange(query: String?): Boolean {
                adapter.filter.filter(query)
                return false
            }

        })

        listView.setOnItemClickListener { parent, view, position, id ->

            val displayValue = parent.getItemAtPosition(position)
            var selectedValue = parent.getItemAtPosition(position).toString()
            searchView.setQuery(selectedValue, false)

            val selectedID = parent.getItemIdAtPosition(position).toInt()
            val id = docIDs[selectedID]
            val stockValue = stocks_id[selectedID]

            val dialogBinding = layoutInflater.inflate(R.layout.client_order_product, null)

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




            val done = dialogBinding.findViewById<Button>(R.id.btnDone)
            done.setOnClickListener {

                val inputStockValue = dialogBinding.findViewById<EditText>(R.id.txtStocks)
                val inputStock = inputStockValue.text.toString()

                val spinner = dialogBinding.findViewById<Spinner>(R.id.spinner)
                val spinnerStr = spinner.selectedItem.toString()

                val request_area = "$inputStock:$spinnerStr"

                val requestRef = db.collection("request").document(uid)

                if (inputStock.isNotEmpty()) {
                    // Run the transaction to update the access field
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(requestRef)
                        transaction.update(requestRef, "access", true)
                        val doc = snapshot.toObject(RequestModel::class.java)

                        // Retrieve the latest data and perform necessary operations
                        if (doc != null) {

                            val year = currentDate.year
                            val month = currentDate.monthValue
                            val day = currentDate.dayOfMonth

                            val currentMonthName = months[month - 1]

                            val date = "$currentMonthName/$day/$year"

                            val fname = doc.first_name
                            val mname = doc.middle_name
                            val lname = doc.last_name

                            val clientName = "$fname $mname $lname"
                            val address = doc.address

                            val clientReqDetails = ProductRequestedModel(uid, clientName, address, date, displayValue.toString(), request_area)
                            // Return the necessary data to be used after the transaction completes
                            clientReqDetails
                        } else {
                            null
                        }
                    }
                        .addOnSuccessListener { clientReqDetails ->
                            if (clientReqDetails != null) {
                                // Save the transaction data into the "client_request" collection
                                db.collection("client_request")
                                    .add(clientReqDetails)
                                    .addOnSuccessListener { documentReference ->
                                        Toast.makeText(this, "The request has been sent. Please wait for our feedback.", Toast.LENGTH_LONG).show()
                                        myDialog.dismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "Error: Document not found", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Transaction failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }else {
                    // Invalid inputStock value
                    Toast.makeText(this, "Please input the total area.", Toast.LENGTH_SHORT).show()
                }


            }

            val exit = dialogBinding.findViewById<Button>(R.id.btnBack)
            exit.setOnClickListener {
                myDialog.dismiss()
            }

            android.os.Handler().postDelayed({
                selectedValue = ""
                searchView.setQuery(selectedValue, false)
            },2000)
        }




    }
}