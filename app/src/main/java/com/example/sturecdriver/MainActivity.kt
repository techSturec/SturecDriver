package com.example.sturecdriver

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val signUptxt = findViewById<TextView>(R.id.signIn_Signup)
        val phNo = findViewById<EditText>(R.id.signInPhNo)
        val drivercode = findViewById<EditText>(R.id.signInDriverCode)
        val password = findViewById<EditText>(R.id.signInPassword)
        val logIn = findViewById<Button>(R.id.signInBtn)


        val sp = getSharedPreferences("login", MODE_PRIVATE)
        if (sp.getBoolean("logged", false)) {
            startActivity(Intent(this@MainActivity, Home::class.java))
        }
        signUptxt.setOnClickListener {
            startActivity(Intent(this@MainActivity, signUp::class.java))
            finish()
        }

        logIn.setOnClickListener {

            if(phNo.text.toString().isEmpty())
            {
                phNo.text.clear()
                phNo.setHintTextColor(Color.RED)
                phNo.hint = "Enter Phone No. first"
                //Toast.makeText(this@MainActivity, "Enter Phone No. first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(drivercode.text.toString().isEmpty())
            {
                drivercode.text.clear()
                drivercode.setHintTextColor(Color.RED)
                drivercode.hint = "Enter School Code first"
                //Toast.makeText(this@MainActivity, "Enter School Code first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(password.text.toString().isEmpty())
            {
                password.text.clear()
                password.setHintTextColor(Color.RED)
                password.hint = "Enter Password first"
                //Toast.makeText(this@MainActivity, "Enter Password first", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //Log.d("check", "check")
            val user: Task<DataSnapshot> = FirebaseDatabase.getInstance().reference.child("ss").child("Buses")
                .child(drivercode.text.toString()).get().addOnSuccessListener {
//
                    if(it.exists())
                    {
                        val userPh = it.child("busDriver").value.toString()
                        FirebaseDatabase.getInstance().reference.child("ssd").child(userPh).get().addOnSuccessListener {test->
                            if(test.exists())
                            {
                                val recPassword = test.child("password").value.toString()
                                if(password.text.toString() ==recPassword)
                                {
                                    sp.edit().putBoolean("logged", true).apply()
                                    val intent = Intent(this@MainActivity, Home::class.java)
                                    startActivity(intent)
                                    finish()
                                }else
                                {
                                    password.text.clear()
                                    password.setHintTextColor(Color.RED)
                                    password.hint = "Incorrect Password"
                                }
                            }else
                            {
                                phNo.text.clear()
                                phNo.setHintTextColor(Color.RED)
                                phNo.hint = "Phone Number not found"
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "Network Error occurred", Toast.LENGTH_LONG).show()
                        }
                    }else
                    {
                        drivercode.text.clear()
                        drivercode.setHintTextColor(Color.RED)
                        drivercode.hint = "Driver Code not found"
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "failure, network error", Toast.LENGTH_LONG).show()
                }


        }


    }
}