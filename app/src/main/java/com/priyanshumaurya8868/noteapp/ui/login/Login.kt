package com.priyanshumaurya8868.noteapp.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.priyanshumaurya8868.noteapp.R
import com.priyanshumaurya8868.noteapp.databinding.FragmentLoginBinding
import com.priyanshumaurya8868.noteapp.utils.BaseFragment
import com.priyanshumaurya8868.noteapp.utils.LoadingState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect

@InternalCoroutinesApi
class Login : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()
    override fun getViewBinding() = FragmentLoginBinding.inflate(layoutInflater)
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            viewModel.signWithCredential(credential)
        } catch (e: ApiException) {
            Log.w("TAG", "Google sign in failed ${e.message}")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        
        binding.btnSignIn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requireContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            launcher.launch(googleSignInClient.signInIntent)
        }
        
    }


    private fun setupViews() = lifecycleScope.launchWhenStarted {
        viewModel.loadingState.collect { state ->
            when (state.status) {
                LoadingState.Status.SUCCESS -> {
                    binding.progressCircular.isVisible = false
                    binding.btnSignIn.isVisible = true
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.action_global_home)
                }
                LoadingState.Status.FAILED -> {
                    viewModel.uiState.collect { event ->
                        if (event.peekContent().isNotBlank()) {
                            Toast.makeText(
                                this@Login.requireContext(),
                                event.getContentIfNotHandled(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    binding.progressCircular.isVisible = false
                    binding.btnSignIn.isVisible = true
                }
                LoadingState.Status.RUNNING -> {
                    binding.progressCircular.isVisible = true
                    binding.btnSignIn.isVisible = false
                }
                else -> {
                }
            }
        }
    }


}