package com.priyanshumaurya8868.noteapp.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.priyanshumaurya8868.noteapp.utils.Constant.KEY_DOC_ID
import com.priyanshumaurya8868.noteapp.utils.Constant.KEY_INPUT_TASK
import com.priyanshumaurya8868.noteapp.utils.Constant.KEY_OUTPUT_TASK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*


class MyWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        val inData = inputData
        val strUri = inData.getString(KEY_INPUT_TASK)
        val docID = inData.getString(KEY_DOC_ID)

        if (docID != null) {
            uploadImg(Uri.parse(strUri), "Img ${UUID.randomUUID()}.png", docID)
        } else return Result.failure(workDataOf(Pair(KEY_OUTPUT_TASK, "didnt get img uri  ")))

        val outPutData =
            workDataOf(Pair(KEY_OUTPUT_TASK, "work  has  been completed successfully...!"))

        return Result.success(outPutData)
    }

    private fun updateNote(docId: String, downloadLink: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val ref: CollectionReference = Firebase.firestore.collection(
                FirebaseAuth.getInstance().currentUser?.displayName ?: "user"
            )
            try {
                ref.document(docId).update("image", downloadLink).await()

            } catch (e: Exception) {
                Log.d("omegaRanger", e.message ?: "work manger")
            }
        }

    private fun uploadImg(uri: Uri, fileName: String, docId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val storage = FirebaseStorage.getInstance().reference.child("pics")
            val ref = storage.child(fileName)
            try {
                ref.putFile(uri).await()
                val link = ref.downloadUrl.await()
                updateNote(docId, link.toString())
                Log.d("omegaRanger", "uploaded image $link" + "path ${link.path} => ref $ref")
            } catch (e: Exception) {
                Log.d("OmegaRanger", " uploading Image ${e.message.toString()}")
            }

        }

}