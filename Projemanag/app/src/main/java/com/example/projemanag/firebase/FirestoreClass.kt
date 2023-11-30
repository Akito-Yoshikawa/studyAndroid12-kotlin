package com.example.projemanag.firebase

import android.util.Log
import com.example.projemanag.activitys.SignUpActivity
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFireStore.collection(Constans.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnCompleteListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName, "$it")
            }
    }


    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

}