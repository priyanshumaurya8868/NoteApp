package com.priyanshumaurya8868.noteapp.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.priyanshumaurya8868.noteapp.MainViewModel
import com.priyanshumaurya8868.noteapp.R
import com.priyanshumaurya8868.noteapp.databinding.FragmentHomeBinding
import com.priyanshumaurya8868.noteapp.databinding.ItemRvBinding
import com.priyanshumaurya8868.noteapp.model.Note
import com.priyanshumaurya8868.noteapp.utils.BaseFragment
import com.priyanshumaurya8868.noteapp.utils.GenericListAdapter
import com.priyanshumaurya8868.noteapp.utils.load

class Home : BaseFragment<FragmentHomeBinding>() {

    private lateinit var viewModel: MainViewModel

    private val ref =
        FirebaseDatabase.getInstance()
            .getReference(FirebaseAuth.getInstance().currentUser?.displayName ?: "user")

    override fun getViewBinding(): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeRealtimeUpdates()
        setUpViews()
        enableSignOutFeature()
    }

    private fun setUpViews() = binding.apply {

        //user's dp
        FirebaseAuth.getInstance().currentUser?.photoUrl?.let {
            ivCurrentUser.load(it.toString())
        }

        floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_editNotes)
        }
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.notes.observe({ lifecycle }) {
            recyclerView.adapter = getAdapter(it)
        }
    }

    private fun getAdapter(list: List<Note>) =
        object : GenericListAdapter<Note>(
            layoutId = R.layout.item_rv,
            bind = { item, holder, itemCount ->
                val itemBinding = ItemRvBinding.bind(holder.itemView)
                itemBinding.apply {
                    ivPreviewImg.isVisible = item.image.isNotBlank()
                    ivPreviewImg.load(item.image)
                    tvTittleName.text = item.title
                    tvDateTime.text = item.date
                    tvContent.text = item.content
                    root.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putSerializable("KEY", item)
                        findNavController().navigate(
                            R.id.action_global_editNotes,
                            bundle
                        )
                        viewModel.setImageUri("")
                    }
                    btnDelete.setOnClickListener {
                        deleteNote(item)
                    }
                }

            }
        ) {}.apply {
            submitList(list)
        }


    private fun subscribeRealtimeUpdates() {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val list = mutableListOf<Note>()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue<Note>()
                        list.add(note!!)
                    }
                    viewModel.updateListOfNotes(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }

        })


    }

    fun deleteNote(note: Note) {
        viewModel.deleteNote(note, ref)

        Snackbar.make(binding.root, "Note deleted!", Snackbar.LENGTH_LONG).apply {
            setAction("Undo") {
                undoDelete()
            }
        }.show()
    }

    private fun undoDelete() {
        viewModel.undoDelteNote(ref)
    }

    private fun enableSignOutFeature() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        binding.btnSignOut.setOnClickListener {
            //firebase sign out
            FirebaseAuth.getInstance().signOut()
            //google sign out
            googleSignInClient.signOut()
            findNavController().apply {
                popBackStack()
                navigate(R.id.action_global_login)
            }
        }
    }
}