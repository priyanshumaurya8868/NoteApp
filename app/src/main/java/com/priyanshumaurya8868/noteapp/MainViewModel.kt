package com.priyanshumaurya8868.noteapp

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.priyanshumaurya8868.noteapp.model.Note
import com.priyanshumaurya8868.noteapp.utils.Event
import com.priyanshumaurya8868.noteapp.utils.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {

    private val _insertedImage = MutableStateFlow("")
    val insertedImage: StateFlow<String> = _insertedImage

    private val _notes = MutableLiveData<List<Note>>()
    val notes :LiveData<List<Note>> = _notes

    private val _uiState = MutableStateFlow(Event(""))
    val uiState: StateFlow<Event<String>> = _uiState

    private var deletedNote : Note? = null

    private val _loadingState = MutableLiveData(LoadingState.IDLE)
    var loadingState: LiveData<LoadingState> = _loadingState

    var docID : String? = null

    fun addNote(note: Note, ref: CollectionReference) = viewModelScope.launch {
        try {
          val docRef =  ref.add(note).await()
              docID = docRef.id
        } catch (e: Exception) {
            _uiState.value = Event(e.localizedMessage ?: "Something went wring!")
        }
    }

     fun updateListOfNotes(list : List<Note>)= viewModelScope.launch{
        _notes.postValue( list)
    }

    fun deleteNote(note: Note, ref: CollectionReference) = viewModelScope.launch {
        try {
            val querySnapshot = ref
                .whereEqualTo("title", note.title)
                .whereEqualTo("content", note.content)
                .whereEqualTo("image", note.image)
                .get()
                .await()

            //personCollectionRef.document(document.id).update("age", newAge).await()

            if (querySnapshot.documents.isNotEmpty()) {
                deletedNote = note
                for (doc in querySnapshot) {
                    ref.document(doc.id).delete().await() // del whole doc at once
                    //del specific field
//                       personsCollectionRef.document(doc.id).update(mapOf("firstName" to FieldValue.delete())).await()
                }
            } else
                _uiState.value = Event("No persons matched the query.")

        } catch (e: Exception) {
            _uiState.value = Event(e.localizedMessage ?: "Something went wrong!")
        }
    }

    fun updateNote(oldNote : Note, newNote: Note, ref: CollectionReference) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = ref
                    .whereEqualTo("title", oldNote.title)
                    .whereEqualTo("content", oldNote.content)
                    .whereEqualTo("image", oldNote.image)
                    .get()
                    .await()

                //personCollectionRef.document(document.id).update("age", newAge).await()

                if (querySnapshot.documents.isNotEmpty()) {
                    for (doc in querySnapshot) {
                        ref.document(doc.id).set(
                            newNote.toMap(),
                            SetOptions.merge()
                        ).await()
                      docID =  doc.id
                    }
                }else
                    _uiState.value = Event("No persons matched the query.")

            } catch (e: Exception) {
                _uiState.value = Event(e.localizedMessage ?: "Something went wrong!")
            }
        }

    fun undoDelteNote(ref: CollectionReference) {
      addNote(deletedNote!!,ref)
    }



    fun resetInsertedImageUri() = viewModelScope.launch{
        _insertedImage.emit("")
    }



    fun setImageUri(strUri: String) = viewModelScope.launch{
        _insertedImage.emit(strUri)
    }

}