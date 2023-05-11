package com.example.requestsystemda

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.widget.*

class AdminProduct : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_product)

        listView = findViewById(R.id.listView1)

        val adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            R.layout.custom_list_layout
        )

        val db = Firebase.firestore
        db.collection("products")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

                        val data = document.toObject(ProductModel::class.java)

                        val name = data.name
                        val ratio = data.npk_ratio
                        val type = data.type
                        val stocks = data.stocks

                        adapter.add("Name: $name \nN-P-K Ratio: $ratio \nType: $type \nStocks: $stocks")


                    }
                } else {
                    Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                }
            }
        listView.adapter = adapter


        val refresh = findViewById<Button>(R.id.btnRefresh)
        refresh.setOnClickListener{
            db.collection("products")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        adapter.clear() // clear the adapter before adding new data
                        for (document in task.result!!) {
                            val data = document.toObject(ProductModel::class.java)

                            val name = data.name
                            val ratio = data.npk_ratio
                            val type = data.type
                            val stocks = data.stocks

                            adapter.add("Name: $name \nN-P-K Ratio: $ratio \nType: $type \nStocks: $stocks")
                        }
                        adapter.notifyDataSetChanged() // notify the adapter that the data has changed
                    } else {
                        Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                    }
                }
        }


        val addProduct = findViewById<Button>(R.id.btnAdd)
        addProduct.setOnClickListener{

            val dialogBinding = layoutInflater.inflate(R.layout.admin_add_product, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            val window = myDialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency


            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.setCanceledOnTouchOutside(true)
            myDialog.show()

            val back = dialogBinding.findViewById<Button>(R.id.btnBack)
            back.setOnClickListener{
                myDialog.dismiss()
            }


            val add = dialogBinding.findViewById<Button>(R.id.btnAdd)
            add.setOnClickListener{
                val pname = dialogBinding.findViewById<EditText>(R.id.txtName)
                val pratio = dialogBinding.findViewById<EditText>(R.id.txtRatio)
                val ptype = dialogBinding.findViewById<EditText>(R.id.txtType)
                val pstocks = dialogBinding.findViewById<EditText>(R.id.txtStocks)

                val string_stocks = pstocks.text.toString()
                val name = pname.text.toString()
                val ratio = pratio.text.toString()
                val type = ptype.text.toString()

                if (name.isNotEmpty() && ratio.isNotEmpty() && type.isNotEmpty() && string_stocks.isNotEmpty()) {
                    val stocks = string_stocks.toInt()

                    db.collection("products")
                        .whereEqualTo("name", name)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                adapter.clear()
                                // No product with the same name exists, add the new product
                                val productData = ProductModel(name, ratio, type, stocks)

                                db.collection("products")
                                    .add(productData)
                                    .addOnSuccessListener { documentReference ->

                                        db.collection("products")
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    for (document in task.result!!) {

                                                        val data = document.toObject(ProductModel::class.java)

                                                        val nname = data.name
                                                        val nratio = data.npk_ratio
                                                        val ntype = data.type
                                                        val nstocks = data.stocks

                                                        adapter.add("Name: $nname \nN-P-K Ratio: $nratio \nType: $ntype \nStocks: $nstocks")


                                                    }
                                                } else {
                                                    Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        Toast.makeText(this, "Product has been added.", Toast.LENGTH_LONG).show()
                                        myDialog.dismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        // There was an error adding the data
                                        Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                adapter.notifyDataSetChanged()
                            } else {
                                // A product with the same name already exists
                                Toast.makeText(this, "A product with the same name already exists.", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            // There was an error checking for existing products
                            Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }else {
                    Toast.makeText(this, "Please fill all the empty fields..!", Toast.LENGTH_LONG).show()
                }

            }


        }


        val editProduct = findViewById<Button>(R.id.btnEdit)
        editProduct.setOnClickListener{

            val dialogBinding = layoutInflater.inflate(R.layout.admin_edit_product, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            val window = myDialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency


            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.setCanceledOnTouchOutside(true)
            myDialog.show()

            val back = dialogBinding.findViewById<Button>(R.id.btnBack)
            back.setOnClickListener{
                myDialog.dismiss()
            }

            val edit = dialogBinding.findViewById<Button>(R.id.btnEditDialog)
            edit.setOnClickListener{
                val oname = dialogBinding.findViewById<EditText>(R.id.txtOldName)
                val pname = dialogBinding.findViewById<EditText>(R.id.txtName)
                val pratio = dialogBinding.findViewById<EditText>(R.id.txtRatio)
                val ptype = dialogBinding.findViewById<EditText>(R.id.txtType)
                val pstocks = dialogBinding.findViewById<EditText>(R.id.txtStocks)
                val string_stocks = pstocks.text.toString()

                val oldName = oname.text.toString()
                val name = pname.text.toString()
                val ratio = pratio.text.toString()
                val type = ptype.text.toString()

                db.collection("products")
                    .whereEqualTo("name", oldName)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val docID = document.id

                            val productData = mutableMapOf<String, Any>()
                            if (name.isNotEmpty()) productData["name"] = name
                            if (ratio.isNotEmpty()) productData["npk_ratio"] = ratio
                            if (type.isNotEmpty()) productData["type"] = type
                            if (string_stocks.isNotEmpty()) productData["stocks"] = string_stocks.toInt()
                            adapter.clear()


                            db.collection("products")
                                .document(docID)
                                .update(productData)
                                .addOnSuccessListener {

                                    db.collection("products")
                                        .get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                for (document in task.result!!) {

                                                    val data = document.toObject(ProductModel::class.java)

                                                    val nname = data.name
                                                    val nratio = data.npk_ratio
                                                    val ntype = data.type
                                                    val nstocks = data.stocks

                                                    adapter.add("Name: $nname \nN-P-K Ratio: $nratio \nType: $ntype \nStocks: $nstocks")


                                                }
                                            } else {
                                                Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    Toast.makeText(this, "Product has been updated.", Toast.LENGTH_LONG).show()
                                    myDialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    // There was an error updating the document
                                    Toast.makeText(this, "Error updating document: $e", Toast.LENGTH_LONG).show()
                                }
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .addOnFailureListener { e ->
                        // There was an error getting the document
                        Toast.makeText(this, "Error getting document: $e", Toast.LENGTH_LONG).show()
                    }

            }

        }

        val deleteProduct = findViewById<Button>(R.id.btnDelete)
        deleteProduct.setOnClickListener{

            val dialogBinding = layoutInflater.inflate(R.layout.admin_delete_product, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinding)

            val window = myDialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setDimAmount(0.5F) // Set dim amount to 0 for full transparency


            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.setCanceledOnTouchOutside(true)
            myDialog.show()

            val back = dialogBinding.findViewById<Button>(R.id.btnBack)
            back.setOnClickListener{
                myDialog.dismiss()
            }

            val delete = dialogBinding.findViewById<Button>(R.id.btnDeleteDilog)
            delete.setOnClickListener{
                val pname = dialogBinding.findViewById<EditText>(R.id.txtName)
                val name = pname.text.toString()

                if (name.isNotEmpty()){
                    db.collection("products")
                        .whereEqualTo("name", name)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val docID = document.id

                                db.collection("products")
                                    .document(docID)
                                    .delete()
                                    .addOnSuccessListener {
                                        adapter.clear()
                                        db.collection("products")
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    for (document in task.result!!) {

                                                        val data = document.toObject(ProductModel::class.java)

                                                        val nname = data.name
                                                        val nratio = data.npk_ratio
                                                        val ntype = data.type
                                                        val nstocks = data.stocks

                                                        adapter.add("Name: $nname \nN-P-K Ratio: $nratio \nType: $ntype \nStocks: $nstocks")


                                                    }
                                                } else {
                                                    Toast.makeText(this, "Error getting documents: ${task.exception}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        adapter.notifyDataSetChanged()
                                        Toast.makeText(this, "Product has been deleted.", Toast.LENGTH_LONG).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // There was an error deleting the document
                                        Toast.makeText(this, "Error deleting document: $e", Toast.LENGTH_LONG).show()
                                    }
                                myDialog.dismiss()
                            }
                        }
                        .addOnFailureListener { e ->
                            // There was an error getting the document
                            Toast.makeText(this, "Error getting document: $e", Toast.LENGTH_LONG).show()
                            myDialog.dismiss()
                        }
                }else{
                    Toast.makeText(this, "Please input the product name that you want to delete.", Toast.LENGTH_LONG).show()
                }



            }
        }
    }
}