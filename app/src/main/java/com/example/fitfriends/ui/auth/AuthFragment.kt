package com.example.fitfriends.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fitfriends.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController

class AuthFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)
        auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<TextInputEditText>(R.id.et_email)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.et_password)
        val confirmPasswordEditText = view.findViewById<TextInputEditText>(R.id.et_confirm_password)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.til_confirm_password)
        val registerButton = view.findViewById<MaterialButton>(R.id.btn_register)
        val loginButton = view.findViewById<MaterialButton>(R.id.btn_login)

        registerButton.setOnClickListener {
            confirmPasswordLayout.visibility = View.VISIBLE
            val email = emailEditText.text?.toString()?.trim() ?: ""
            val password = passwordEditText.text?.toString() ?: ""
            val confirmPassword = confirmPasswordEditText.text?.toString() ?: ""

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Uzupełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Hasła nie są takie same!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(requireContext(), "Hasło musi mieć co najmniej 6 znaków!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Rejestracja zakończona sukcesem!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_home, null, androidx.navigation.NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
                        val activity = requireActivity()
                        val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                        bottomNav?.selectedItemId = R.id.navigation_home
                    } else {
                        Toast.makeText(requireContext(), "Błąd rejestracji: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loginButton.setOnClickListener {
            confirmPasswordLayout.visibility = View.GONE
            val email = emailEditText.text?.toString()?.trim() ?: ""
            val password = passwordEditText.text?.toString() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Uzupełnij e-mail i hasło!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_home, null, androidx.navigation.NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
                        val activity = requireActivity()
                        val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                        bottomNav?.selectedItemId = R.id.navigation_home
                    } else {
                        Toast.makeText(requireContext(), "Błąd logowania: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        return view
    }
} 