package com.example.trevana.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.trevana.R
import com.example.trevana.activities.MainActivity
import com.example.trevana.databinding.ActivityLoginBinding
import com.example.trevana.utils.ToastHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        setupButtonListeners()
        initializeUIComponents()
    }

    private fun initializeUIComponents() {
        updateLoginButtonState(enabled = true)
        binding.progressBar.isVisible = false
    }

    private fun setupButtonListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            clearErrors()
            if (validateCredentials(email, password)) {
                attemptLogin(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun clearErrors() {
        binding.etEmail.error = null
        binding.etPassword.error = null
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        return when {
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

    private fun attemptLogin(email: String, password: String) {
        updateLoginButtonState(enabled = false)
        binding.progressBar.isVisible = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.isVisible = false
                updateLoginButtonState(enabled = true)

                if (task.isSuccessful) {
                    handleSuccessfulLogin()
                } else {
                    handleLoginFailure(task.exception?.message)
                }
            }
    }

    private fun updateLoginButtonState(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnLogin.background = ContextCompat.getDrawable(
            this,
            if (enabled) R.drawable.btn_primary_background else R.drawable.btn_disabled_background
        )
    }

    private fun handleSuccessfulLogin() {
        ToastHelper.showSuccessToast(this, getString(R.string.login_success))
        navigateToMainActivity()
    }

    private fun handleLoginFailure(errorMessage: String?) {
        val error = errorMessage ?: getString(R.string.unknown_error)
        ToastHelper.showErrorToast(this, getString(R.string.login_failure, error))
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}