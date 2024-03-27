package com.lorick.chatterbox.googleUtils

import android.content.Context
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

interface GoogleSignInInterface {

    /**
     * Google sign-in
     * @param context
     * */
    fun googleSignIn(context: Context): GoogleSignInClient?

    /**
     * Getting google sign-in result
     * @param activityResult
     * @param firebaseAuth
     * @return GoogleSignInAccount
     * */
    fun googleSignInResult(
        activityResult: ActivityResult?,
        firebaseAuth: FirebaseAuth?,
        result: (data: AuthResult?) -> Unit
    )

    /**
     * SignOut from the device clearing the data
     * @param firebaseAuth
     * @param context
     * */
    fun signOut(
        firebaseAuth: FirebaseAuth?,
        context: Context
    )
}