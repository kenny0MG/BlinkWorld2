package org.robotics.blinkworld.Activity.RegistrationPhone

import android.content.Intent
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import org.robotics.blinkworld.Activity.MainActivity
import org.robotics.blinkworld.Activity.Web.WebActivity
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.auth
import java.util.concurrent.TimeUnit

class RegistrationPhoneActivity : AppCompatActivity() {
    private lateinit var phoneContinue: AppCompatButton
    private lateinit var phoneEditText:EditText
    private lateinit var number : String
    private lateinit var privacyPhone:TextView
    private lateinit var cpp:CountryCodePicker



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_phone)

        //инициализация объектов активити (поиск по id)
        phoneContinue = findViewById(R.id.phone_continue_button)
        phoneEditText = findViewById(R.id.phone_edit_text)
        privacyPhone = findViewById(R.id.privacy_phone_text)
        cpp =findViewById(R.id.cpp_country)

        phoneEditText.requestFocus()

        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        if(width == 720 ){
            phoneEditText.layoutParams.width = 515
        }else if(width == 1080){
            phoneEditText.layoutParams.width = 765
        }

        privacyPhone.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra("link","https://docs.google.com/document/d/e/2PACX-1vR1cSthMdPZZjgXYlw3Wt15ouqunmuuir-f6WiaRbd9nrRQ1YUYSG_4aP9ii_iZMQ/pub")
            this.startActivities(arrayOf(intent))
        }

        //обработчик нажатий
        phoneContinue.setOnClickListener {
            number = phoneEditText.text.trim().toString()
            if (number.isNotEmpty()){
                cpp.registerCarrierNumberEditText(phoneEditText)
                number = "+"+cpp.fullNumber

                    val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)

            }else{
                Toast.makeText(this , "Please Enter Number" , Toast.LENGTH_SHORT).show()

            }
        }
    }
    private fun sendToMain(){
        startActivity(Intent(this , MainActivity::class.java))
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
                    sendToMain()
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
            val username =intent.getStringExtra("username")!!
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            // Save verification ID and resending token so we can use them later
            val intent = Intent(this@RegistrationPhoneActivity , OTPVerificationActivity::class.java)
            intent.putExtra("OTP" , verificationId)
            intent.putExtra("resendToken" , token)
            intent.putExtra("phoneNumber" , number)
            intent.putExtra("username" , username)

            startActivity(intent)

        }
    }


}