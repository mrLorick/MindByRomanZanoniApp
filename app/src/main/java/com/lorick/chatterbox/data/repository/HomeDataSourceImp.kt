package com.lorick.chatterbox.data.repository

import android.content.Context
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.retrofit.ApiService
import com.lorick.chatterbox.retrofit.NetworkErrorHandlerImpl
import com.lorick.chatterbox.retrofit.responseToResourceFromServer
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject

/**
 *
 * [HomeDataSourceImp]
 * it stands for Implementation.
 * [HomeDataSourceImp] is use to handle param Implementation and hit Api from here
 * and filter the api response using this function [response To ResourceFromServer]
 * [NetworkErrorHandlerImpl] is use to handle Api Catch Errors and show the Generic
 * */

class HomeDataSourceImp @Inject constructor(
    private val apiService: ApiService,
    private val networkErrorHandlerImpl: NetworkErrorHandlerImpl,
    @ApplicationContext val context: Context,
    private val sharedPrefs: SharedPrefs
) {


    /**
     * this function is use to hit Contact us Otp APi
     * */
    suspend fun executeContactUsApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.contactUsApi(param),
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
     * this function is use to hit Change Password APi
     * */
    suspend fun executeChangePasswordApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.changePasswordApi(param),
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
     * this function is use to hit Meditation type List APi
     * */
    suspend fun executeMeditationTypeListApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.meditationTypeListApi(param),
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
     * this function is use to hit Meditation Cat List APi
     * */
    suspend fun executeMeditationCatListApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.meditationCatListApi(),
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
     * this function is use to hit Meditation Cat List APi
     * */
    suspend fun executeSearchCatOrSubCatApi(param: HashMap<String, Any?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.searchSubCatOrCatApi(param),
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
     * this function is use to hit Edification List APi
     * */
    suspend fun executeEdificationListApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.edificationListApi(param),
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
     * this function is use to hit Edification Category List APi
     * */
    suspend fun executeEdificationCategoryListApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.edificationCategoryListApi(),
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
     * this function is use to hit Journal List APi
     * */
    suspend fun executeJournalListApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.journalListApi(),
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
     * this function is use to hit add Journal  APi
     * */
    suspend fun executeAddJournalApi(param: RequestBody,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.addJournalApi(param),
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
     * this function is use to hit Resources List APi
     * */
    suspend fun executeResourcesCategoryApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.resourceCategoryApi(),
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
     * this function is use to hit Event List APi
     * */
    suspend fun executeEventListApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.eventListApi(),
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
     * this function is use to hit Event List APi
     * */
    suspend fun executeSearchMediationApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.searchMeditationApi(param),
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
     * this function is use to hit Update Notification Setting APi
     * */
    suspend fun executeUpdateNotificationSettingApi(param: HashMap<String, Boolean?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.updateNotificationSettingApi(param),
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
     * this function is use to hit Update Notification Setting APi
     * */
    suspend fun executeUpdateBiometricSettingApi(param: HashMap<String, Boolean?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.updateBiometricSettingApi(param),
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
     * this function is use to hit Comment List APi
     * */
    suspend fun executeCommentListApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.commentListApi(param),
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
     * this function is use to hit Like List APi
     * */
    suspend fun executeLikeListApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.likeListApi(param),
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
     * this function is use to hit Event Details APi
     * */
    suspend fun executeEventDetailsApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.eventDetailsApi(param),
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
     * this function is use to hit Update Journal APi
     * */
    suspend fun executeUpdateJournalApi(param: RequestBody, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.updateJournalApi(param),
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
     * this function is use to hit Notification List APi
     * */
    suspend fun executeNotificationListApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.notificationListApi(),
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
     * this function is use to hit Event Details APi
     * */
    suspend fun executeAddCommentApi(param: HashMap<String, String?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.addCommentApi(param),
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
     * this function is use to hit Update Favourite Status API
     * */
    suspend fun executeUpdateFavouriteEventStatusApi(param: HashMap<String, Any?>,apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.updateFavouriteEventStatusApi(param),
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
     * this function is use to hit User Profile API
     * */
    suspend fun executeUserProfileApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.userProfileApi(),
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
     * this function is use to hit Logout API
     * */
    suspend fun executeLogoutApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.logoutApi(param),
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
     * this function is use to hit Get Journal Detail API
     * */
    suspend fun executeGetJournalDetailApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.getJournalDetailApi(param),
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
     * this function is use to hit Resource List By Type API
     * */
    suspend fun executeResourceListByTypeApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.resourceListByTypeApi(param),
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
     * this function is use to hit Delete Journal API
     * */
    suspend fun executeDeleteJournalApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.deleteJournalApi(param),
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
     * this function is use to hit Notification Status API
     * */
    suspend fun executeNotificationStatusApi(apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.notificationStatusApi(),
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
     * this function is use to hit Chat Users API
     * */
    suspend fun executeChatUsersApi(param: HashMap<String, String?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.chatUsersApi(param),
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
     * this function is use to hit Notification Reminder Users API
     * */
    suspend fun executeNotificationReminderApi(param: HashMap<String, Any?>, apiType: String) = flow {
        try {
            emit(
                responseToResourceFromServer(
                    response = apiService.notificationReminderApi(param),
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


