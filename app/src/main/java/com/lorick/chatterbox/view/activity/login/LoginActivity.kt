package com.lorick.chatterbox.view.activity.login

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.lorick.chatterbox.R
import com.lorick.chatterbox.base.BaseActivity
import com.lorick.chatterbox.databinding.ActivityLoginBinding
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.genrics.RunInScope
import com.lorick.chatterbox.sharedPreference.RememberMeSharedPrefs
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.MyProgressBar
import com.lorick.chatterbox.utils.applyTextWatcherAndFilter
import com.lorick.chatterbox.utils.constant.AppConstants
import com.lorick.chatterbox.utils.constant.AppConstants.SAVE_EMAIL
import com.lorick.chatterbox.utils.constant.AppConstants.SAVE_PASSWORD
import com.lorick.chatterbox.utils.launchActivity
import com.lorick.chatterbox.utils.showErrorSnack
import com.lorick.chatterbox.validators.Validator
import com.lorick.chatterbox.view.activity.dashboard.DashboardActivity
import com.lorick.chatterbox.view.activity.forgotPassword.ForgotPasswordActivity
import com.lorick.chatterbox.view.activity.signup.SignupActivity
import com.lorick.chatterbox.view.activity.verificationCode.VerificationCodeActivity
import com.lorick.chatterbox.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {
    private val viewModal: AuthViewModel by viewModels()
    var activity = this@LoginActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var rememberMeSharedPref: RememberMeSharedPrefs
    private var mCallbackManager: CallbackManager? = null


    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    override fun getLayoutRes() = R.layout.activity_login

    private val signInActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            viewModal.googleSignIn?.googleSignInResult(
                activityResult = activityResult,
                firebaseAuth = auth,
                result = { result ->
                    viewModal.applicationID.set(result?.user?.uid)
                    viewModal.emailGmail.set(result?.user?.email)
                    viewModal.name.set(result?.user?.displayName)
                    viewModal.contactNumber.set(result?.user?.phoneNumber)
                    RunInScope.ioThread {
                        viewModal.hitLoginGmailApi()
                    }
                }
            )
        }

    override fun initView() {
        rememberMeSharedPref = RememberMeSharedPrefs(applicationContext)
        getWatcherEmail()
        auth = FirebaseAuth.getInstance()
        onClickListener()
        setInUiEmailPassword()
        observeDataFromViewModal()

        mCallbackManager = create()

        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Login Cancel", Toast.LENGTH_LONG).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                }

                override fun onSuccess(result: LoginResult) {
                    Log.d("Success", "Login")
                }
            })


        binding.appCompatImageView13.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(activity, mutableListOf("public_profile", "user_friends"))
        }


    }



/** check email is valid or not*/
    private fun getWatcherEmail() {
        binding.etEmail.applyTextWatcherAndFilter(validator, binding.ivTick)
    }

    /**When Remember me enabled or checked save data set in views email or password*/
    private fun setInUiEmailPassword() {
        if (rememberMeSharedPref.getRememberMe()) {
            binding.checkRememberMe.isChecked = true
            viewModal.email.set(rememberMeSharedPref.getString(SAVE_EMAIL))
            viewModal.password.set(rememberMeSharedPref.getString(SAVE_PASSWORD))
        }
    }

    override fun viewModel() {
        binding.viewModel = viewModal
    }

    private fun onClickListener() {
        binding.apply {
            tvForgotPassword.setOnClickListener {
                launchActivity<ForgotPasswordActivity> { }
            }

            btnSignIn.setOnClickListener {
                RunInScope.ioThread {
                    viewModal.hitLoginCuimsApi()
                }
              /*  if (validator.validateLogin(activity, binding)) {
                    RunInScope.ioThread {
                        viewModal.hitLoginCuimsApi()
                    }
                }*/
            }

            tvSignUp.setOnClickListener {
                launchActivity<SignupActivity> { }
            }

            ivGoogle.setOnClickListener {
                if (viewModal.googleSignIn == null) viewModal.initializeGoogleSignIn()
                val googleSignInClient = viewModal.googleSignIn?.googleSignIn(context = activity)
                viewModal.googleSignIn?.signOut(firebaseAuth = auth, context = activity)
                signInActivityResult.launch(googleSignInClient?.signInIntent)
            }
        }
    }


    /** Observer Response via View model*/
    private fun observeDataFromViewModal() {
        lifecycleScope.launch {
            viewModal.loginResponseSharedFlow.collectLatest { isResponse ->
                when (isResponse) {
                    is Resource.Success -> {
                        val data = isResponse.data
                        if (data?.isSuccess() == true) {
                            /**When Remember me enabled or checked save data in shared preference mail or password otherwise clear Data*/
                            rememberMeSharedPref.apply {
                                if (binding.checkRememberMe.isChecked) {
                                    saveRememberMe(true)
                                    save(SAVE_EMAIL, viewModal.email.get().toString())
                                    save(SAVE_PASSWORD, viewModal.password.get().toString())
                                } else {

                                    saveRememberMe(false)
                                    clearPreference()
                                }
                            }
                            if (data.data!!.isEmailVerified) {
                                sharedPrefs.save(AppConstants.USER_AUTH_TOKEN, data.data.token)
                                sharedPrefs.saveUserLogin(true)
                                sharedPrefs.save(AppConstants.SHARED_PREFS_USER_DATA, Gson().toJson(data.data))

                                launchActivity<DashboardActivity> { }
                                finishAffinity()
                            } else {
                                val bundle = bundleOf(
                                    AppConstants.SCREEN_TYPE to AppConstants.SIGN_UP,
                                    AppConstants.USER_EMAIL to data.data.email
                                )
                                launchActivity<VerificationCodeActivity>(0, bundle) { }
                            }
                        } else {
                            showErrorSnack(activity, data?.message ?: "")
                        }
                    }

                    is Resource.Error -> {
                        isResponse.message?.let { msg ->
                            showErrorSnack(activity, msg)
                        }
                    }
                }
            }
        }

        viewModal.showLoading.observe(activity) {
            if (it) {
                MyProgressBar.showProgress(activity)
            } else {
                MyProgressBar.hideProgress()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mCallbackManager!!.onActivityResult(requestCode, resultCode, data)) {
            return
        }
    }




}



