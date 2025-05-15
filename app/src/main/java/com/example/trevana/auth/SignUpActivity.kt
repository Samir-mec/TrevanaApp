package com.example.trevana.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.trevana.R
import com.example.trevana.activities.MainActivity
import com.example.trevana.databinding.ActivitySignUpBinding
import com.example.trevana.utils.ToastHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        setupButtonListeners()
        initializeUI()
    }

    private fun initializeUI() {
        binding.progressBar.isVisible = false
        updateSignUpButton(enabled = true)
    }

    private fun setupButtonListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            clearErrors()
            if (validateInputs(name, email, password)) {
                attemptSignUp(name, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun clearErrors() {
        binding.etName.error = null
        binding.etEmail.error = null
        binding.etPassword.error = null
    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etName.error = getString(R.string.name_required)
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = getString(R.string.email_required)
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = getString(R.string.password_required)
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = getString(R.string.password_length_error)
                false
            }
            else -> true
        }
    }

    private fun attemptSignUp(name: String, email: String, password: String) {
        updateSignUpButton(enabled = false)
        binding.progressBar.isVisible = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.isVisible = false
                updateSignUpButton(enabled = true)

                if (task.isSuccessful) {
                    saveUserData(name, email)
                } else {
                    handleSignUpError(task.exception?.message)
                }
            }
    }

    private fun updateSignUpButton(enabled: Boolean) {
        binding.btnSignUp.isEnabled = enabled
        binding.btnSignUp.background = ContextCompat.getDrawable(
            this,
            if (enabled) R.drawable.btn_primary_background else R.drawable.btn_disabled_background
        )
    }

    private fun saveUserData(name: String, email: String) {
        val user = hashMapOf(
            "displayName" to name,
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    ToastHelper.showSuccessToast(this, getString(R.string.signup_success))
                    navigateToMain()
                }
                .addOnFailureListener {
                    ToastHelper.showErrorToast(this, getString(R.string.data_save_error))
                }
        } ?: run {
            ToastHelper.showErrorToast(this, getString(R.string.user_not_found))
        }
    }

    private fun handleSignUpError(errorMessage: String?) {
        val error = errorMessage ?: getString(R.string.unknown_error)
        ToastHelper.showErrorToast(this, getString(R.string.signup_failed, error))
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}