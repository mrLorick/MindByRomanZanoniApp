package com.lorick.chatterbox.validators

import android.app.Activity
import android.util.Log
import android.util.Patterns
import com.lorick.chatterbox.R
import com.lorick.chatterbox.databinding.ActivityChangePasswordBinding
import com.lorick.chatterbox.databinding.ActivityContactUsBinding
import com.lorick.chatterbox.databinding.ActivityCreatePasswordBinding
import com.lorick.chatterbox.databinding.ActivityEditProfileBinding
import com.lorick.chatterbox.databinding.ActivityForgotPasswordBinding
import com.lorick.chatterbox.databinding.ActivityLoginBinding
import com.lorick.chatterbox.databinding.ActivitySignupBinding
import com.lorick.chatterbox.databinding.ActivityUserChatListBinding
import com.lorick.chatterbox.databinding.ActivityVerificationCodeBinding
import com.lorick.chatterbox.databinding.BottomsheetCommentsBinding
import com.lorick.chatterbox.utils.showErrorBarAlert
import java.util.regex.Pattern
import javax.inject.Inject


class Validator @Inject constructor() {
    fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z0-9])" +      //any letter
                    "(?=.*[@#$%^&+='s~<`.>*/()-])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,20}" +               //at least 8 characters
                    "$"
        )
        return passwordREGEX.matcher(password).matches()
    }

    /**
     * this function use to validate the signUp data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateRegistration(
        activity: Activity,
        binding: ActivitySignupBinding
    ): Boolean {
        when {
            binding.etName.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_name))
                return false
            }
            binding.etEmail.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_email))
                return false
            }
            !isValidEmailId(binding.etEmail.text?.trim().toString()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_email))
                return false
            }
            binding.etContactNumber.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_contact_number))
                return false
            }
            binding.etContactNumber.text.toString().length != 10 -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.number_should_be_valid))
                return false
            }
            !isValidPhoneNumber(binding.etContactNumber.text.toString() ) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.number_should_be_valid))
                return false
            }
            binding.etPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_password))
                return false
            }
            binding.etConfirmPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_c_password))
                return false
            }
            !isValidPasswordFormat(binding.etPassword.text?.trim().toString()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.password_should_contain_special_character))
                return false
            }
            binding.etPassword.text?.toString()?.trim() != binding.etConfirmPassword.text?.toString()?.trim() -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_password_not_match))
                return false
            }

            else -> return true
        }
    }


    /**
     * this function use to validate the signUp data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateUpdateProfile(
        activity: Activity,
        binding: ActivityEditProfileBinding
    ): Boolean {
        return when {
            binding.etFullName.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_name))
                false
            }

            binding.etPhoneNumber.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_contact_number))
                false
            }

            else -> true
        }
    }


    /**
     * this function use to validate the Login data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateLogin(
        activity: Activity,
        binding: ActivityLoginBinding
    ): Boolean {
        return when {
            binding.etEmail.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_email))
                false
            }

            !isValidEmailId(binding.etEmail.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_email))
                false
            }

            binding.etPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_password))
                false
            }
            !isValidPasswordFormat(binding.etPassword.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_password))
                 false
            }
            else -> true
        }
    }

    /**
     * this function use to validate the Login data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateComment(
        activity: Activity,
        binding: BottomsheetCommentsBinding?
    ): Boolean {
        return when {
            binding?.etComment?.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_comment))
                false
            }

            else -> true
        }
    }




    /**
     * this function use to validate the Verification data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateOtpVerification(
        activity: Activity,
        binding: ActivityVerificationCodeBinding
    ): Boolean {
        return when {
            binding.firstPinView.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_otp))
                false
            }

            binding.firstPinView.text?.toString()?.trim()!!.length != 4  -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_otp))
                false
            }
            binding.firstPinView.text?.toString()?.trim()!!.length >= 4 -> {
                true
            }
            else -> true
        }
    }

    /**
     * this function use to validate the Contact us data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateContactUs(
        activity: Activity,
        binding: ActivityContactUsBinding
    ): Boolean {
        return when {
            binding.etFullName.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_name))
                false
            }
            binding.etEmailAddress.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_email))
                false
            }
            !isValidEmailId(binding.etEmailAddress.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_email))
                return false
            }
            binding.etSubject.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_subject))
                false
            }
            binding.etMessage.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_message))
                false
            }
            else -> true
        }
    }


    /**
     * this function use to validate the signUp data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateChangePassword(
        activity: Activity,
        binding: ActivityChangePasswordBinding
    ): Boolean {
        when {
            binding.etOldPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_old_password))
                return false
            }
            !isValidPasswordFormat(binding.etOldPassword.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_password))
                return false
            }
            binding.etNewPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_new_password))
                return false
            }
            !isValidPasswordFormat(binding.etNewPassword.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_password))
                return false
            }
            binding.etConfirmPassword.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_c_password))
                return false
            }
            binding.etNewPassword.text?.toString()?.trim() != binding.etConfirmPassword.text?.toString()?.trim() -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_password_not_match))
                return false
            }
            else -> return true
        }
    }



    /**
     * this function use to validate the Login data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateForgotPassword(
        activity: Activity,
        binding: ActivityForgotPasswordBinding
    ): Boolean {
        return when {
            binding.etEmail.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_email))
                false
            }

            !isValidEmailId(binding.etEmail.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_email))
                false
            }

            else -> true
        }
    }


    /**
     * this function use to validate the Login data
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun validateResetPassword(
        activity: Activity,
        binding: ActivityCreatePasswordBinding
    ): Boolean {
        return when {
            binding.passwordET.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_password))
                false
            }
            binding.cPasswordET.text?.toString()?.trim()!!.isBlank() -> {
                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_c_password))
                false
            }
            !isValidPasswordFormat(binding.cPasswordET.text.toString().trim()) -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_valid_password))
                return false
            }
            binding.passwordET.text?.toString()?.trim() != binding.cPasswordET.text?.toString()?.trim() -> {
                showErrorBarAlert(activity,activity.getString(R.string.invalid),activity.getString(R.string.please_enter_password_not_match))
                return false
            }

            else -> true
        }
    }


    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val pattern = Pattern.compile(  "^[6-9]{1}[0-9]{9}\\\$")
        val matcher = pattern.matcher(phoneNumber.replace("\\s".toRegex(), ""))
        Log.d("dmfdmfsfsdfsdf",matcher.matches().toString())
        return Patterns.PHONE.matcher(phoneNumber).matches()
    }

    /** to Check the email is valid or not **/
     fun isValidEmailId(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * this function use to validate the Chat Message
     * @param binding
     * @param activity
     * @return [Boolean]
     * */
    fun isValidChatMessage(
        activity: Activity,
        binding: ActivityUserChatListBinding
    ): Boolean {
        return when {
            binding.etMessage.text?.toString()?.trim()!!.isBlank() -> {
//                showErrorBarAlert(activity,activity.getString(R.string.empty),activity.getString(R.string.please_enter_message))
                false
            }
            else -> true
        }
    }
}