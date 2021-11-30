package com.priyanshumaurya8868.noteapp.ui.editNotes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.priyanshumaurya8868.noteapp.MainViewModel
import com.priyanshumaurya8868.noteapp.databinding.FragmentCreateNotesBinding
import com.priyanshumaurya8868.noteapp.model.Note
import com.priyanshumaurya8868.noteapp.utils.BaseFragment
import com.priyanshumaurya8868.noteapp.utils.Constant.KEY_DOC_ID
import com.priyanshumaurya8868.noteapp.utils.Constant.KEY_INPUT_TASK
import com.priyanshumaurya8868.noteapp.utils.LoadingState
import com.priyanshumaurya8868.noteapp.utils.MyWorker
import com.priyanshumaurya8868.noteapp.utils.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EditNotes : BaseFragment<FragmentCreateNotesBinding>() {
    private var canExit: Boolean = true
    lateinit var viewModel: MainViewModel

    private var oldNote: Note? = null
    private val ref =
        Firebase.firestore.collection(FirebaseAuth.getInstance().currentUser?.displayName ?: "user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }


    override fun getViewBinding(): FragmentCreateNotesBinding =
        FragmentCreateNotesBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialSetUp()
        setUpListener()
        setUpObserver()
        viewModel.loadingState.observe({ lifecycle }) { state ->
            when (state.status) {
                LoadingState.Status.RUNNING -> {
                    canExit = false
                    binding.progressCircular.isVisible = true
                }
                else -> {
                    canExit = true
                    binding.progressCircular.isVisible = false
                    Log.d("omegaRanger", "tap tap tapta ${state.status}")
                }
            }
        }
    }

    private fun initialSetUp() {
        binding.tvDate.text = getCurrentDate()
        arguments?.apply {
            oldNote = getSerializable("KEY") as Note?
            oldNote?.let { note ->
                binding.apply {
                    etTitle.setText(note.title)
                    tvDate.setText(note.date)
                    etContent.setText(note.content)
                    containerImg.isVisible = note.image.isNotBlank()
                    ivInsertedImg.load(note.image)
                    viewModel.setImageUri(note.image)
                }
            }


        }
    }

    private fun setUpObserver() = lifecycleScope.launchWhenStarted {
        viewModel.uiState.collect { event ->
            if (event.hasBeenHandled)
                Toast.makeText(
                    this@EditNotes.requireContext(),
                    event.getContentIfNotHandled(),
                    Toast.LENGTH_LONG
                ).show()
        }

        viewModel.insertedImage.collect { imgUri ->
            withContext(Dispatchers.Main) {
                binding.ivInsertedImg.isVisible = imgUri.isNotBlank()
                Log.d("omegaRanger", "ImageContainer  Should Visible ${imgUri.isNotBlank()}")
            }
        }

    }


    private fun setUpListener() = binding.apply {

        btnDelInsertedImg.setOnClickListener {
            viewModel.setImageUri("")
            containerImg.isVisible = false
        }

        btnAddImage.setOnClickListener {
            selectImage()
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        btnSaveNote.setOnClickListener {
            if (validateInputs()) {
                saveNote()

            }
        }
    }

    private fun validateInputs(): Boolean {
        return if (binding.etTitle.text.toString().isNotBlank())
            true
        else {
            binding.etTitle.error = "Field Required!"
            binding.etTitle.requestFocus()
            false
        }
    }

    private fun saveNote() = lifecycleScope.launch{
        val note = getUpdateNote()
       val job = if (oldNote == null) {
            viewModel.addNote(note, ref)
        } else {
            viewModel.updateNote(oldNote!!, note, ref)
        }

        job.join()
        val inputData = Data.Builder()
            .putString(KEY_DOC_ID, viewModel.docID)
            .putString(KEY_INPUT_TASK, note.image)
            .build()

        val request = OneTimeWorkRequestBuilder<MyWorker>()
            .setInputData(inputData)
            .build()

        val workManager= WorkManager.getInstance(requireContext())
        workManager .enqueue(request)
        withContext(Dispatchers.Main){ findNavController().popBackStack() }
    }

    private fun getUpdateNote() = Note(
        title = binding.etTitle.text.toString(),
        content = binding.etContent.text.toString(),
        date = binding.tvDate.text.toString(),
        image = viewModel.insertedImage.value
    )

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return sdf.format(Date())
    }


    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcher.launch(intent)
    }


    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                val imagUri = it.data?.data
                imagUri?.let { uri ->
                    setImagePreview(uri)
//                    viewModel.uploadImg(uri, "Img${UUID.randomUUID()}.jpg")
                    viewModel.setImageUri(uri.toString())
                }

                Log.d("omegaRanger", "Selected uri $imagUri?: mila nhi")

            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }

    private fun setImagePreview(uri: Uri) = binding.apply {
        containerImg.isVisible = true
        ivInsertedImg.load(uri.toString())

    }


}

