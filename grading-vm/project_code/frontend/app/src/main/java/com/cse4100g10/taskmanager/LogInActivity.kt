package com.cse4100g10.taskmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        auth = FirebaseAuth.getInstance()

        val btLogIn = findViewById<MaterialButton>(R.id.bt_login)
        btLogIn.setOnClickListener{
            //val user = findViewById<EditText>(R.id.et_username).text.toString()
            val password = findViewById<EditText>(R.id.et_password).text.toString()
            val email = findViewById<EditText>(R.id.et_email).text.toString()
            when {
                password.isEmpty() and email.isEmpty() ->  {
                    val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                    emailInputLayout.error = "Email is empty."

                    val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                    passwordInputLayout.error = "Password is empty."
                    //Snackbar.make(btLogIn, "Email and password are empty.", Snackbar.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                    passwordInputLayout.isErrorEnabled = false

                    val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                    emailInputLayout.error = "Email is empty."
                    //Snackbar.make(btLogIn, "Email is empty.", Snackbar.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                    emailInputLayout.isErrorEnabled = false

                    val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                    passwordInputLayout.error = "Password is empty."
                    //Snackbar.make(btLogIn, "Password is empty.", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                    passwordInputLayout.isErrorEnabled = false

                    val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                    emailInputLayout.isErrorEnabled = false

                    //Log.d("user", email)
                    //Log.d("password", password)
                    loginUser(email, password)
                }
            }
        }

        val btSignUp = findViewById<MaterialButton>(R.id.bt_signup)
        btSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d("success", "signInWithEmail:success")
                    //Toast.makeText(baseContext, "Authentication succesful.",
                    //Toast.LENGTH_SHORT).show()

                    val mainActivityIntent = Intent(applicationContext, ProjectListActivity::class.java)
                    startActivity(mainActivityIntent)
                    this.finish()
                } else {
                    println(task.exception.toString())
                    when (task.exception.toString()) {
                        "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted." -> {
                            val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                            emailInputLayout.error = "The email address is badly formatted."

                            val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                            passwordInputLayout.isErrorEnabled = false
                        }

                        "com.google.firebase.auth.FirebaseAuthInvalidUserException: There is no user record corresponding to this identifier. The user may have been deleted." -> {
                            val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                            emailInputLayout.error = "There is no user corresponding to this email"

                            val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                            passwordInputLayout.isErrorEnabled = false
                        }

                        "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The password is invalid or the user does not have a password." -> {
                            val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                            passwordInputLayout.error = "The password is invalid or the user does not have this password."

                            val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                            emailInputLayout.isErrorEnabled = false
                        }
                    }

                    // If sign in fails, display a message to the user.
                    /*Log.w("failure", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()*/
                }
            }
    }
}
