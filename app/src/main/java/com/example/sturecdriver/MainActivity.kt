package com.example.sturecdriver

import android.content.ContentValues
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var number: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val signUptxt = findViewById<TextView>(R.id.signIn_Signup)
        val logIn = findViewById<Button>(R.id.signInBtn)
        auth = FirebaseAuth.getInstance()





        val sp = getSharedPreferences("login", MODE_PRIVATE)
        if (sp.getBoolean("logged", false)) {
            startActivity(Intent(this@MainActivity, Home::class.java))
        }


        logIn.setOnClickListener {
            credentialVerify()
        }


    }
    private fun credentialVerify() {


        val phNo = findViewById<EditText>(R.id.signInPhNo)
        val schoolCode = findViewById<EditText>(R.id.signInSchoolCode)
        val drivercode = findViewById<EditText>(R.id.signInDriverCode)
        val busCode = findViewById<EditText>(R.id.signInBusCode)
        val password = findViewById<EditText>(R.id.signInPassword)


        val sp = getSharedPreferences("login", MODE_PRIVATE)

        if(phNo.text.toString().isEmpty())
        {
            phNo.text.clear()
            phNo.setHintTextColor(Color.RED)
            phNo.hint = "Enter Phone No. first"
            //Toast.makeText(this@MainActivity, "Enter Phone No. first", Toast.LENGTH_LONG).show()
            return
        }
        if(drivercode.text.toString().isEmpty())
        {
            drivercode.text.clear()
            drivercode.setHintTextColor(Color.RED)
            drivercode.hint = "Enter School Code first"
            //Toast.makeText(this@MainActivity, "Enter School Code first", Toast.LENGTH_LONG).show()
            return
        }
        if(password.text.toString().isEmpty())
        {
            password.text.clear()
            password.setHintTextColor(Color.RED)
            password.hint = "Enter Password first"
            //Toast.makeText(this@MainActivity, "Enter Password first", Toast.LENGTH_LONG).show()
            return
        }

        //Log.d("check", "check")
        val user: Task<DataSnapshot> = FirebaseDatabase.getInstance().reference.child(schoolCode.text.toString()).child("Buses")
            .child(busCode.text.toString()).get().addOnSuccessListener {

                if(it.exists())
                {
                    val userPh = it.child("busDriver").value.toString()
                    FirebaseDatabase.getInstance().reference.child(drivercode.text.toString()).child(userPh).get().addOnSuccessListener {test->
                        if(test.exists())
                        {
                            val recPassword = test.child("password").value.toString()
                            if(password.text.toString() ==recPassword)
                            {
                                sp.edit().putBoolean("logged", true).apply()
//                                val intent = Intent(this@MainActivity, signUp::class.java)
//                                startActivity(intent)
//                                finish()
                                otpSend()
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

    private fun otpSend() {
        val phNo = findViewById<EditText>(R.id.signInPhNo)
        number = phNo.text.trim().toString()
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
            Toast.makeText(this,"Please Enter  number",Toast.LENGTH_SHORT).show()

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
        startActivity(Intent(this@MainActivity,Home::class.java))
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
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

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(ContentValues.TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later

            val intent = Intent(this@MainActivity,otp::class.java)
            intent.putExtra("OTP",verificationId)
            intent.putExtra("resendToken",token)
            intent.putExtra("phonenUmber",number)
            startActivity(intent)

        }

}
}