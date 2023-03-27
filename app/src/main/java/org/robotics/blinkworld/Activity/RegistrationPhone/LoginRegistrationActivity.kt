package org.robotics.blinkworld.Activity.RegistrationPhone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import org.robotics.blinkworld.R

class LoginRegistrationActivity : AppCompatActivity() {

    private lateinit var continueButton: AppCompatButton
    private lateinit var nameTextView:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_registration)
        //инициализация объектов активити (поиск по id)
        continueButton = findViewById(R.id.login_reg_continue_button)
        nameTextView = findViewById(R.id.name_login_text_view)


        nameTextView.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
               if(p0!!.length >= 30){
                   Toast.makeText(applicationContext , "Max 25 symbol" , Toast.LENGTH_SHORT).show()
               }
            }

        }

        )




        //обработчик нажатий
        continueButton.setOnClickListener {
            if(nameTextView.text.isEmpty()){
                Toast.makeText(this , "Write name" , Toast.LENGTH_SHORT).show()

            }else{
                val intent = Intent(this, RegistrationPhoneActivity::class.java)
                intent.putExtra("username",nameTextView.text.toString())
                startActivities(arrayOf(intent))
            }

        }
        nameTextView.requestFocus()



    }
}