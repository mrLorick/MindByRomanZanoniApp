package com.lorick.chatterbox.data.repository

import android.content.Context
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.retrofit.ApiService
import com.lorick.chatterbox.retrofit.NetworkErrorHandlerImpl
import com.lorick.chatterbox.retrofit.responseToResourceFromServer
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

/**
 *
 * [AuthDataSourceImp]
 * it stands for Implementation.
 * [AuthDataSourceImp] is use to handle param Implementation and hit Api from here
 * and filter the api response using this function [response To ResourceFromServer]
 * [NetworkErrorHandlerImpl] is use to handle Api Catch Errors and show the Generic
 * */

class AuthDataSourceImp @Inject constructor(
    private val apiService: ApiService,
    private val networkErrorHandlerImpl: NetworkErrorHandlerImpl,
    @ApplicationContext val context: Context,
    private val sharedPrefs: SharedPrefs
) {

    /**
     * this function is use to hit Login APi
     * */
    suspend fun executeLoginCuimsApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.loginCuApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }


    /**
     * this function is use to hit Registration APi
     * */
    suspend fun executeRegistrationApi(param: HashMap<String, RequestBody?>,image:MultipartBody.Part?,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.registrationApi(param,image),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }

    /**
     * this function is use to hit Update Profile APi
     * */
    suspend fun executeUpdateProfileApi(param: HashMap<String, RequestBody?>,image:MultipartBody.Part?,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.updateProfileApi(param,image),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }




    /**
     * this function is use to hit Verification Otp APi
     * */
    suspend fun executeVerificationOtpApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.verificationOtpApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }


    /**
     * this function is use to hit Login APi
     * */
    suspend fun executeLoginApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.loginApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }


    /**
     * this function is use to hit Login APi
     * */
    suspend fun executeForgotPasswordApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.forgotPasswordApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }

    /**
     * this function is use to hit Reset password APi
     * */
    suspend fun executeResetPasswordApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.resetPasswordApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }

    /**
     * this function is use to hit Resend Otp APi
     * */
    suspend fun executeResendOtpApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.resendOtpApi(param),
                    sharedPrefs = sharedPrefs,
                    context = context,
                    apiType = apiType
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(networkErrorHandlerImpl.getErrorMessage(e), apiType = apiType))
        }
    }



}


