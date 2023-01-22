package com.example.sturecdriver

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class signUp : AppCompatActivity() {

    private lateinit var number: String
    private  lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val phoneNo = findViewById<EditText>(R.id.signUpPhNo)
        val schoolCode = findViewById<EditText>(R.id.signUpSchoolCode)

        val driverCode = findViewById<EditText>(R.id.signUpDriverCode)
        val BusCode = findViewById<EditText>(R.id.signUpBusCode)
        val signup = findViewById<Button>(R.id.signUpBtn)

        auth = FirebaseAuth.getInstance()

        signup.setOnClickListener{
            FirebaseDatabase.getInstance().reference.child(schoolCode.text.toString()).child("Buses").child(driverCode.text.toString()).get()
                .addOnSuccessListener {
                if(it.exists())
                {
                    if(it.child("busDriver").value.toString()==phoneNo.text.toString())
                    {
                        saveToFirebase()
                        number = phoneNo.text.trim().toString()
                        if(number.isNotEmpty()){
                            if (number.length==10){
                                number = "+91$number"


                                val options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber(number)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build()
                                PhoneAuthProvider.verifyPhoneNumber(options)
                            }else{
                                Toast.makeText(this,"Please Enter correct number",Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this,"Please Enter number",Toast.LENGTH_SHORT).show()
                        }
                    }else
                    {
                        Toast.makeText(this, "Incorrect Phone Number", Toast.LENGTH_LONG).show()
                    }
                }else
                {
                    driverCode.text.clear()
                    driverCode.setHintTextColor(Color.RED)
                    driverCode.hint = "Enter Password first"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Network Error", Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun saveToFirebase() {
        val phoneNo = findViewById<EditText>(R.id.signUpPhNo)
        val driverCode = findViewById<EditText>(R.id.signUpDriverCode)

        val user:DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        val userMap = HashMap<String, Any>()
        userMap["phone"] = phoneNo.text.toString().lowercase()
        userMap["driverCode"] = driverCode.text.toString().lowercase()


        user.child(phoneNo.text.toString()).setValue(userMap)
            .addOnCompleteListener{task->
                if(task.isSuccessful)
                {
                    Toast.makeText(this, "Account was successfully created.", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@signUp, Home::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else
                {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error "+message, Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")

                    sendToMain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendToMain(){
        startActivity(Intent(this@signUp,Home::class.java))
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onCodeSent(verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken) {
            Log.d(ContentValues.TAG, "onCodeSent:$verificationId")

            val intent = Intent(this@signUp,otp::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)
            intent.putExtra("phonenUmber",number)
            startActivity(intent)
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(ContentValues.TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(ContentValues.TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }
    }
}