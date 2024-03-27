package com.lorick.chatterbox.retrofit

import com.lorick.chatterbox.data.response.CommonResponse
import com.lorick.chatterbox.data.response.SignUpResponse
import com.lorick.chatterbox.data.response.chatUsers.ChatUsersResponse
import com.lorick.chatterbox.data.response.edification.EdificationCatResponse
import com.lorick.chatterbox.data.response.edification.EdificationTypeResponse
import com.lorick.chatterbox.data.response.eventDetails.AddCommentResponse
import com.lorick.chatterbox.data.response.eventDetails.CommentResponse
import com.lorick.chatterbox.data.response.eventDetails.EventDetailsResponse
import com.lorick.chatterbox.data.response.eventDetails.LikesResponse
import com.lorick.chatterbox.data.response.home.EventResponse
import com.lorick.chatterbox.data.response.journal.JournalResponse
import com.lorick.chatterbox.data.response.journal.ViewJournalsResponse
import com.lorick.chatterbox.data.response.meditation.MeditationCatResponse
import com.lorick.chatterbox.data.response.meditation.MeditationTypeResponse
import com.lorick.chatterbox.data.response.notification.NotificationResponse
import com.lorick.chatterbox.data.response.notificationStatus.NotificationStatusResponse
import com.lorick.chatterbox.data.response.resource.ResourceCategoryResponse
import com.lorick.chatterbox.data.response.resource.ResourceTypeResponse
import com.lorick.chatterbox.data.response.search.SearchCatOrSubCatResponse
import com.lorick.chatterbox.data.response.userData.UserDataResponse
import com.lorick.chatterbox.utils.constant.ApiConstants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    /** Login Api*/
    @POST("uims/")
    suspend fun loginCuApi(@Body map: HashMap<String, String?>): Response<UserDataResponse>

    /** Registration Api*/
    @Multipart
    @POST(ApiConstants.REGISTRATION)
    suspend fun registrationApi(
        @PartMap map: HashMap<String, RequestBody?>,
        @Part profileImage: MultipartBody.Part?,
    ): Response<SignUpResponse>


    /** Update Profile*/
    @Multipart
    @POST(ApiConstants.UPDATE_PROFILE)
    suspend fun updateProfileApi(
        @PartMap map: HashMap<String, RequestBody?>,
        @Part profileImage: MultipartBody.Part?,
    ): Response<CommonResponse>

    /** Verification Otp*/
    @POST(ApiConstants.VERIFICATION_OTP)
    suspend fun verificationOtpApi(@Body map: HashMap<String, String?>): Response<UserDataResponse>

    /** Contact us Otp*/
    @POST(ApiConstants.CONTACT_US)
    suspend fun contactUsApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Login Api*/
    @POST(ApiConstants.LOGIN)
    suspend fun loginApi(@Body map: HashMap<String, String?>): Response<UserDataResponse>

    /** Change Password*/
    @POST(ApiConstants.CHANGE_PASSWORD)
    suspend fun changePasswordApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Login Api*/
    @POST(ApiConstants.FORGOT_PASSWORD)
    suspend fun forgotPasswordApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Mediation Type Api*/
    @POST(ApiConstants.MEDITATION_TYPE_LIST)
    suspend fun meditationTypeListApi(@Body map: HashMap<String, String?>): Response<MeditationTypeResponse>

    /** Mediation Cat Api*/
    @GET(ApiConstants.MEDITATION_CAT_LIST)
    suspend fun meditationCatListApi(): Response<MeditationCatResponse>

    /** Edification Type Api*/
    @POST(ApiConstants.EDIFICATION_LIST)
    suspend fun edificationListApi(@Body map: HashMap<String, String?>): Response<EdificationTypeResponse>

    /** Edification Type Api*/
    @POST(ApiConstants.SEARCH_API_DASHBOARD)
    suspend fun searchSubCatOrCatApi(@Body map: HashMap<String, Any?>): Response<SearchCatOrSubCatResponse>

    /** Edification Cat Api*/
    @GET(ApiConstants.EDIFICATION_CATEGORY_LIST)
    suspend fun edificationCategoryListApi(): Response<EdificationCatResponse>

    /** Reset password Api*/
    @POST(ApiConstants.RESET_PASSWORD)
    suspend fun resetPasswordApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Update Notification Setting Api*/
    @POST(ApiConstants.UPDATE_NOTIFICATION_SETTING)
    suspend fun updateNotificationSettingApi(@Body map: HashMap<String, Boolean?>): Response<CommonResponse>

    /** Update Biometric Setting Api*/
    @POST(ApiConstants.UPDATE_BIOMETRIC_SETTING)
    suspend fun updateBiometricSettingApi(@Body map: HashMap<String, Boolean?>): Response<CommonResponse>

    @GET(ApiConstants.JOURNAL_LIST)
    suspend fun journalListApi(): Response<JournalResponse>

    /** Resources List Api*/
    @POST(ApiConstants.RESOURCE_CATEGORY)
    suspend fun resourceCategoryApi(): Response<ResourceCategoryResponse>

    /** Event List Api*/
    @GET(ApiConstants.EVENT_LIST)
    suspend fun eventListApi(): Response<EventResponse>

    /** Add Journal Api*/
    @POST(ApiConstants.ADD_JOURNAL)
    suspend fun addJournalApi(@Body body : RequestBody): Response<CommonResponse>

    /** Search Meditation Api*/
    @POST(ApiConstants.SEARCH_MEDITATION)
    suspend fun searchMeditationApi(@Body map: HashMap<String, String?>): Response<MeditationCatResponse>

    /** Comment List Api*/
    @POST(ApiConstants.COMMENT_LIST)
    suspend fun commentListApi(@Body map: HashMap<String, String?>): Response<CommentResponse>

    /** Like List Api*/
    @POST(ApiConstants.LIKE_LIST)
    suspend fun likeListApi(@Body map: HashMap<String, String?>): Response<LikesResponse>


    /** Event Details Api*/
    @POST(ApiConstants.EVENT_DETAILS)
    suspend fun eventDetailsApi(@Body map: HashMap<String, String?>): Response<EventDetailsResponse>

    /** Update Journal Api*/
    @POST(ApiConstants.UPDATE_JOURNAL)
    suspend fun updateJournalApi(@Body map: RequestBody): Response<CommonResponse>

    /** Notification List Api*/
    @GET(ApiConstants.NOTIFICATION_LIST)
    suspend fun notificationListApi(): Response<NotificationResponse>

    /** Resend Otp Api*/
    @POST(ApiConstants.RESEND_OTP)
    suspend fun resendOtpApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Add Comment Api*/
    @POST(ApiConstants.ADD_COMMENT)
    suspend fun addCommentApi(@Body map: HashMap<String, String?>): Response<AddCommentResponse>

    /** Favourite/Unfavourite Api*/
    @POST(ApiConstants.UPDATE_FAVOURITE_EVENT_STATUS)
    suspend fun updateFavouriteEventStatusApi(@Body map: HashMap<String, Any?>): Response<CommonResponse>

    /** Resources List Api*/
    @GET(ApiConstants.USER_PROFILE)
    suspend fun userProfileApi(): Response<UserDataResponse>

    /** Logout Api*/
    @POST(ApiConstants.LOGOUT)
    suspend fun logoutApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Get Journal Detail Api*/
    @POST(ApiConstants.GET_JOURNAL_DETAIL)
    suspend fun getJournalDetailApi(@Body map: HashMap<String, String?>): Response<ViewJournalsResponse>

    /** Resource List By Type Api*/
    @POST(ApiConstants.RESOURCE_LIST_BY_TYPE)
    suspend fun resourceListByTypeApi(@Body map: HashMap<String, String?>): Response<ResourceTypeResponse>

    /** Delete Journal Api*/
    @POST(ApiConstants.DELETE_JOURNAL)
    suspend fun deleteJournalApi(@Body map: HashMap<String, String?>): Response<CommonResponse>

    /** Notification Status Api*/
    @GET(ApiConstants.NOTIFICATION_STATUS)
    suspend fun notificationStatusApi(): Response<NotificationStatusResponse>

    /** Chat Users Api*/
    @POST(ApiConstants.CHAT_USERS)
    suspend fun chatUsersApi(@Body map: HashMap<String, String?>): Response<ChatUsersResponse>

    /** Notification reminder Users Api*/
    @POST(ApiConstants.NOTIFICATION_ADD_REMINDER)
    suspend fun notificationReminderApi(@Body map: HashMap<String, Any?>): Response<CommonResponse>

}
