package com.example.sturecdriver

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class otp : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var verifyBtn : Button
    private lateinit var resend : Button
    private lateinit var otp1 : EditText
    private lateinit var otp2 : EditText
    private lateinit var otp3 : EditText
    private lateinit var otp4 : EditText
    private lateinit var otp5 : EditText
    private lateinit var otp6 : EditText

    private lateinit var otp : String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        otp = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
//        phoneNumber = intent.getStringExtra("phoneNumber")!!

        init()
        addTextChangeListener()

        verifyBtn.setOnClickListener{
            //collect otp from all edittexts

            val typedOTP = otp1.text.toString() + otp2.text.toString() + otp3.text.toString() + otp4.text.toString() + otp5.text.toString() + otp6.text.toString()

            if(typedOTP.isNotEmpty()){
                if (typedOTP.length==6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(otp,typedOTP)
                    signInWithPhoneAuthCredential(credential)
                }else{

                }
            }else{
                Toast.makeText(this,"Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "signInWithCredential:success")

                    val sp = getSharedPreferences("login",MODE_PRIVATE);
                    if(sp.getBoolean("logged",false)){
                        startActivity(Intent(this@otp,Home::class.java))
                    }
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
        startActivity(Intent(this@otp,Home::class.java))
    }
    private fun addTextChangeListener(){
        otp1.addTextChangedListener(EditTextWatcher(otp1))
        otp2.addTextChangedListener(EditTextWatcher(otp2))
        otp3.addTextChangedListener(EditTextWatcher(otp3))
        otp4.addTextChangedListener(EditTextWatcher(otp4))
        otp5.addTextChangedListener(EditTextWatcher(otp5))
        otp6.addTextChangedListener(EditTextWatcher(otp6))

    }
    private fun init(){
        auth = FirebaseAuth.getInstance()
        verifyBtn = findViewById(R.id.verifyotp)
        resend = findViewById(R.id.resendotp)
        otp1 = findViewById(R.id.otp1)
        otp2 = findViewById(R.id.otp2)
        otp3 = findViewById(R.id.otp3)
        otp4 = findViewById(R.id.otp4)
        otp5 = findViewById(R.id.otp5)
        otp6 = findViewById(R.id.otp6)

    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            when(view.id){
                R.id.otp1 -> if(text.length==1) otp2.requestFocus()
                R.id.otp2 -> if(text.length==1) otp3.requestFocus() else if(text.isEmpty()) otp1.requestFocus()
                R.id.otp3 -> if(text.length==1) otp4.requestFocus() else if(text.isEmpty()) otp2.requestFocus()
                R.id.otp4 -> if(text.length==1) otp5.requestFocus() else if(text.isEmpty()) otp3.requestFocus()
                R.id.otp5 -> if(text.length==1) otp6.requestFocus() else if(text.isEmpty()) otp4.requestFocus()
                R.id.otp5 -> if(text.isEmpty()) otp5.requestFocus()


            }
        }

    }
}