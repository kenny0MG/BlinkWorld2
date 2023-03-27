package org.robotics.blinkworld.Activity.RegistrationPhone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.robotics.blinkworld.Activity.MainActivity
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User
import org.robotics.blinkworld.stories.ui.main.StoriesActivity
import java.util.concurrent.TimeUnit

class OTPVerificationActivity : AppCompatActivity() {
    private lateinit var otpContinue: AppCompatButton
    private lateinit var otpTextView:TextView
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String
    private lateinit var username: String
    private lateinit var resend: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpverification)


        //инициализация объектов активити (поиск по id)
        otpContinue = findViewById(R.id.otp_continue_button)
        otpTextView = findViewById(R.id.otp_edit_text)
        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        username = intent.getStringExtra("username").toString()


        //
        otpTextView.requestFocus()








        //обработчик нажатий
        otpContinue.setOnClickListener {
                //collect otp from all the edit texts
                val typedOTP = otpTextView.text


                if (typedOTP.isNotEmpty()) {
                    if (typedOTP.length == 6) {
                        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                            OTP, typedOTP.toString()
                        )

                        signInWithPhoneAuthCredential(credential)
                    } else {
                        Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
                }



        }
    }


    private fun resendOTPTvVisibility() {

//        otpTextView.setText("")
//        resendTV.visibility = View.INVISIBLE
//        resendTV.isEnabled = false
//
//        Handler(Looper.myLooper()!!).postDelayed(Runnable {
//            resendTV.visibility = View.VISIBLE
//            resendTV.isEnabled = true
//        }, 60000)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
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
            // Save verification ID and resending token so we can use them later
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    database.child(NODE_USERS).orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(object :
                        ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.getValue() != null){
                                finish()
                                Toast.makeText(applicationContext, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                                sendToMain()
                            }else{
                                val user = MkUser(username,phoneNumber, currentUid()!!)
                                database.child(NODE_USERS).child(task.result.user!!.uid).setValue(user).addOnCompleteListener {
                                    // Sign in success, update UI with the signed-in user's information
                                    finish()
                                    Toast.makeText(applicationContext, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                                    sendToMain()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })





                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendToMain() {
        finish()
        val intent = Intent(this , MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun MkUser(fullName: String, email: String,uid: String): User {
        return User(username = fullName, phone = email, uid = uid, photo = "https://sun9-81.userapi.com/impg/agaaimYTddm3Zu7YduQnj__A-WwxEwrpY_SJKw/OtbFXqfaFSg.jpg?size=480x480&quality=95&sign=1e52016751587e96ed44de101a6e425a&c_uniq_tag=zxpz3oH1xxz987k1wp6zcMnggXLcx9K_0DxHonkaw4Y&type=album")
    }




    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }






}