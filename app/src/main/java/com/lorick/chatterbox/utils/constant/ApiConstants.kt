package com.lorick.chatterbox.utils.constant


/**
 * [ApiConstants]
 * this class contains All Constant variable of the app
 * In Kotlin you also have the #object keyword.
 * It is used to obtain a data type with a single implementation.
 * If you are a Java user and want to understand what "single" means, you can think of the Singleton pattern:
 * it ensures you that only one instance of that class is created even if 2 threads try to create it.
 * */


object ApiConstants {
    const val REGISTRATION = "User/SignUp"
    const val VERIFICATION_OTP = "User/VerifyOTP"
    const val LOGIN = "User/Authenticate"
    const val CONTACT_US = "User/ContactUs"
    const val CHANGE_PASSWORD = "User/ChangePassword"
    const val MEDITATION_TYPE_LIST = "Dashboard/MeditationTypesList"
    const val MEDITATION_CAT_LIST = "Dashboard/MeditationCategoryList"
    const val FORGOT_PASSWORD = "User/ForgotPassword"
    const val EDIFICATION_LIST = "Dashboard/EditificationList"
    const val EDIFICATION_CATEGORY_LIST = "Dashboard/EditificationCategoryList"
    const val RESET_PASSWORD = "User/ResetPassword"
    const val JOURNAL_LIST = "Dashboard/JournalList"
    const val ADD_JOURNAL = "Dashboard/AddJournal"
    const val RESOURCE_CATEGORY = "Dashboard/ResourceCategory"
    const val EVENT_LIST = "Dashboard/EventList"
    const val SEARCH_MEDITATION = "Dashboard/SearchMeditation"
    const val UPDATE_NOTIFICATION_SETTING = "User/UpdateNotificationSetting"
    const val UPDATE_BIOMETRIC_SETTING = "User/UpdateBioMetricSetting"
    const val COMMENT_LIST = "Dashboard/CommentList"
    const val EVENT_DETAILS = "Dashboard/EventDetails"
    const val UPDATE_JOURNAL = "Dashboard/UpdateJournal"
    const val NOTIFICATION_LIST = "Notification/NotificationList"
    const val NOTIFICATION_ADD_REMINDER = "Dashboard/AddReminder"
    const val RESEND_OTP = "User/ResendOTP"
    const val ADD_COMMENT = "Dashboard/AddComment"
    const val UPDATE_PROFILE = "User/AddUpdateProfile"
    const val UPDATE_FAVOURITE_EVENT_STATUS = "Dashboard/UpdateFavouriteEventStatus"
    const val USER_PROFILE = "User/UserProfile"
    const val LIKE_LIST = "Dashboard/LikesList"
    const val LOGOUT = "User/Logout"
    const val GET_JOURNAL_DETAIL =  "Dashboard/EditJournal"
    const val RESOURCE_LIST_BY_TYPE =  "Dashboard/ResourceListByType"
    const val DELETE_JOURNAL =  "Dashboard/DeleteJournal"
    const val NOTIFICATION_STATUS = "Notification/NotificationStatus"
    const val CHAT_USERS = "Chat/ChatUsers"
    const val SEARCH_API_DASHBOARD = "Dashboard/Search"







    /**
     * APi Params
     * ************************************************
     * */
    enum class ApiParams(val value: String) {
        NAME("Name"),
        EMAIL("Email"),
        LOCATION("Location"),
        CONTACT_NUMBER("ContactNumber"),
        COUNTRY_CODE("countryCode"),
        PASSWORD("Password"),
        DEVICE_TYPE("DeviceType"),
        DEVICE_TOKEN("DeviceToken"),
        IMAGE("Image"),
        USER_IMAGE("UserImage"),
        OTP("Otp"),
        VERIFICATION_TYPE("VerificationType"),
        APPLICATION_ID("AuthenticationId"),
        LOGIN_MODE("LoginMode"),
        SUBJECT("Subject"),
        MEDITATION_SEARCH("Meditationsearch"),
        MESSAGE("Message"),
        OLD_PASSWORD("OldPassword"),
        CATEGORY_ID("CategoryId"),
        ENTRY_TYPE_ID("TypeId"),
        NOTES("Notes"),
        JOURNAL_DATE("JournalDate"),
        EDIFICATION_ID("EdificationId"),
        IS_NOTIFICATION_ON("IsNotificationOn"),
        IS_BIOMETRIC_ON("IsBiometricOn"),
        EVENT_ID("EventId"),
        TYPE_ID("TypeId"),
        JOURNAL_ID("JournalId"),
        COMMENT_DESC("CommentDesc"),
        IS_FAVOURITE("IsFavourite"),
        USER_ID("UserId"),
        USER_NAME("UserName"),
        RESOURCE_TYPE_ID("ResourcesTypeId"),
        WEIGHT_JOURNAL_NOTES_AS("WeightJournalNotesAs"),
        WEIGHT_JOURNAL_NOTES_BS("WeightJournalNotesBs"),
        WEIGHT_JOURNAL_NOTES("WeightJournalNotes"),
        REMINDER_DATE("ReminderDate"),
        REMINDER_TYPE_ID("ReminderTypeId"),
        SEARCH_KEYWORD("SearchKeyword"),

    }
}


