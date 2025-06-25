package com.example.fitfriends.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fitfriends.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<TextInputEditText>(R.id.et_email)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.et_password)
        val confirmPasswordEditText = view.findViewById<TextInputEditText>(R.id.et_confirm_password)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.til_confirm_password)
        val registerButton = view.findViewById<MaterialButton>(R.id.btn_register)
        val loginButton = view.findViewById<MaterialButton>(R.id.btn_login)

        // Pokazuj pole potwierdzenia hasła przy rejestracji
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
                        // Możesz tu dodać logikę przełączenia na widok profilu
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
                        // Możesz tu dodać logikę przełączenia na widok profilu
                    } else {
                        Toast.makeText(requireContext(), "Błąd logowania: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardAuth = view.findViewById<View>(R.id.card_auth)
        val cardProfile = view.findViewById<View>(R.id.card_profile)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btn_logout)
        val tvUserEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvUserId = view.findViewById<TextView>(R.id.tv_user_id)
        val etMotivation = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_motivation)
        val btnSaveMotivation = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_motivation)
        val db = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        if (user != null) {
            // Użytkownik zalogowany
            cardAuth.visibility = View.GONE
            cardProfile.visibility = View.VISIBLE
            tvUserEmail.text = "E-mail: ${user.email}"
            tvUserId.text = "ID użytkownika: ${user.uid}"
            // Pobierz notatkę motywacyjną z Firestore
            db.collection("motivations").document(user.uid).get().addOnSuccessListener { doc ->
                etMotivation.setText(doc.getString("note") ?: "")
            }
            btnSaveMotivation.setOnClickListener {
                val note = etMotivation.text?.toString() ?: ""
                db.collection("motivations").document(user.uid).set(mapOf("note" to note))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Notatka zapisana!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Błąd zapisu notatki", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            // Użytkownik niezalogowany
            cardAuth.visibility = View.VISIBLE
            cardProfile.visibility = View.GONE
            etMotivation.setText("")
            btnSaveMotivation.isEnabled = false
        }

        btnLogout?.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Wylogowano!", Toast.LENGTH_SHORT).show()
            // Przekierowanie do AuthFragment i czyszczenie stosu
            requireActivity().run {
                val navController = androidx.navigation.fragment.NavHostFragment.findNavController(
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
                )
                navController.navigate(R.id.authFragment, null, androidx.navigation.NavOptions.Builder().setPopUpTo(R.id.navigation_home, true).build())
            }
        }
    }
} 