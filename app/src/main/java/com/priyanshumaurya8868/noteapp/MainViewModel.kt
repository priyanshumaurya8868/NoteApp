package com.priyanshumaurya8868.noteapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
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

    fun addNote(note: Note, ref: DatabaseReference) = viewModelScope.launch {
        try {
          val docRef =  ref.child(note.id).setValue(note).await()
              docID =   note.id
        } catch (e: Exception) {
            _uiState.value = Event(e.localizedMessage ?: "Something went wring!")
        }
    }

     fun updateListOfNotes(list : List<Note>)= viewModelScope.launch{
        _notes.postValue( list)
    }

    fun deleteNote(note: Note, ref: DatabaseReference) = viewModelScope.launch {
        try {
            val querySnapshot = ref.child(note.id).get().await()

            if (querySnapshot.exists()) {
                deletedNote = note
                for (doc in querySnapshot.children) {
                    doc.ref.removeValue().await() // del whole doc at once
                }
            } else
                _uiState.value = Event("No persons matched the query.")

        } catch (e: Exception) {
            _uiState.value = Event(e.localizedMessage ?: "Something went wrong!")
        }
    }

    fun updateNote(oldNote: Note, newNote: Note, ref: DatabaseReference) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                        ref.child(oldNote.id).setValue(newNote.copyVal(oldNote.id)).await()
                        docID = oldNote.id

            } catch (e: Exception) {
                _uiState.value = Event(e.localizedMessage ?: "Something went wrong!")
            }
        }

    fun undoDelteNote(ref: DatabaseReference) {
      addNote(deletedNote!!,ref)
    }



    fun resetInsertedImageUri() = viewModelScope.launch{
        _insertedImage.emit("")
    }



    fun setImageUri(strUri: String) = viewModelScope.launch{
        _insertedImage.emit(strUri)
    }

}