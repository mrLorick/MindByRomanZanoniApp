package com.lorick.chatterbox.googleUtils
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.lorick.chatterbox.utils.showToast
import com.lorick.chatterbox.R


class GoogleSignInImpl : GoogleSignInInterface {

    private var context: Context? = null

    /**
     * Google sign-in
     * @param context
     * @return GoogleSignInClient
     * */
    override fun googleSignIn(context: Context): GoogleSignInClient? {
        return try {
            this.context = context
            val googleSignInOption =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            GoogleSignIn.getClient(context, googleSignInOption)

        } catch (exception: Exception) {
            exception.printStackTrace()
            Log.d("Google_Sign_In", "Google sing-in: ${exception.message} ")
            (context as Activity).showToast(
                message = exception.message.toString(),
            )
            null
        }
    }

    /**
     * Getting google sign-in result
     * @param activityResult
     * @param result call back of the user data
     * @return AuthResult
     * */
    override fun googleSignInResult(
        activityResult: ActivityResult?,
        firebaseAuth: FirebaseAuth?,
        result: (data: AuthResult?) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult?.data)
        handleSignResult(task, firebaseAuth) { authResult ->
            result.invoke(authResult)
        }
    }

    /**
     * SignOut from the device clearing the data
     * @param firebaseAuth
     * @param context
     * */
    override fun signOut(firebaseAuth: FirebaseAuth?, context: Context) {
        firebaseAuth?.signOut()
        googleSignIn(context)?.signOut()
    }

    /**
     * Handling the sign-in result
     * @param task
     * @param googleSignInResult
     * */
    private fun handleSignResult(
        task: Task<GoogleSignInAccount>,
        firebaseAuth: FirebaseAuth?,
        googleSignInResult: (result: AuthResult?) -> Unit
    ) {
            val result = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(result.idToken, null)

            firebaseAuth?.signInWithCredential(credential)
                ?.addOnSuccessListener { authResult ->

                    googleSignInResult.invoke(authResult)
                }
                ?.addOnFailureListener { exception ->
                    Log.d("Google_Sign_In", "Google sing-in failed: ${exception.message} ")
                    (context as Activity).showToast(
                        message = exception.message.toString(),
                    )
                }


    }
}