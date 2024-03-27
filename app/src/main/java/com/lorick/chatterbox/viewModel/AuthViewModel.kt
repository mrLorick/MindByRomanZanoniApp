package com.lorick.chatterbox.viewModel

import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import com.lorick.chatterbox.base.BaseViewModel
import com.lorick.chatterbox.data.repository.AuthDataSourceImp
import com.lorick.chatterbox.data.response.CommonResponse
import com.lorick.chatterbox.data.response.SignUpResponse
import com.lorick.chatterbox.data.response.userData.UserDataResponse
import com.lorick.chatterbox.genrics.Resource
import com.lorick.chatterbox.googleUtils.GoogleSignInImpl
import com.lorick.chatterbox.googleUtils.GoogleSignInInterface
import com.lorick.chatterbox.sharedPreference.SharedPrefs
import com.lorick.chatterbox.utils.constant.ApiConstants
import com.lorick.chatterbox.utils.createPartFromString
import com.lorick.chatterbox.utils.getFireBaseToken
import com.lorick.chatterbox.utils.toRequestBodyProfileNullImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authDataSourceImp: AuthDataSourceImp) : BaseViewModel() {

    var googleSignIn: GoogleSignInInterface? = null


    init {
        getFireBaseToken { token ->
            deviceToken.set(token)
            Log.d("FCM",  deviceToken.get().toString())
        }
    }



    @Inject
    lateinit var sharedPrefs: SharedPrefs

    var email = ObservableField("")
    var emailGmail = ObservableField("")
    var otp = ObservableField("")
    var verificationType = ObservableField("")
    var image: File? = null
    var password = ObservableField("")
    var location = ObservableField("Mohali")
    var name = ObservableField("")
    private var deviceToken = ObservableField("")
    var contactNumber = ObservableField("")
    var countryCode = ObservableField("")
    var confirmPassword = ObservableField("")
    private var deviceType =  ObservableField("Android")
    var imageUri : Uri? = null
    var tempImageUri : Uri? = null
    var applicationID = ObservableField("")


    private val signUpResponse: MutableSharedFlow<Resource<SignUpResponse>> = MutableSharedFlow()
    val signUpResponseSharedFlow = signUpResponse.asSharedFlow()

    private val updateProfileResponse: MutableSharedFlow<Resource<CommonResponse>> = MutableSharedFlow()
    val updateProfileSharedFlow = updateProfileResponse.asSharedFlow()

    private val loginResponse: MutableSharedFlow<Resource<UserDataResponse>> = MutableSharedFlow()
    val loginResponseSharedFlow = loginResponse.asSharedFlow()

    private val verificationOtpResponse: MutableSharedFlow<Resource<UserDataResponse>> = MutableSharedFlow()
    val verificationOtpSharedFlow = verificationOtpResponse.asSharedFlow()

    private val forgotPasswordResponse: MutableSharedFlow<Resource<CommonResponse>> = MutableSharedFlow()
    val forgotPasswordSharedFlow = forgotPasswordResponse.asSharedFlow()

    private val resetPasswordResponse: MutableSharedFlow<Resource<CommonResponse>> = MutableSharedFlow()
    val resetPasswordSharedFlow = resetPasswordResponse.asSharedFlow()

    private val resendOtpResponse: MutableSharedFlow<Resource<CommonResponse>> = MutableSharedFlow()
    val resendOtpSharedFlow = resetPasswordResponse.asSharedFlow()


    /**
     * this function is use to hit Login Api
     * */
    suspend fun hitLoginCuimsApi() {
        showLoading.postValue(true)
        val param = HashMap<String, String?>()
        param["user_id"] = "22BCA10952"
        param["password"] = "Yadav@1202"

        authDataSourceImp.executeLoginCuimsApi(param, apiType = ApiConstants.LOGIN).catch { e ->
            loginResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            loginResponse.emit(isResponse)
        }
    }

    /**
     * this function is use to hit Register Api
     * */
    suspend fun hitRegisterApi() {
        showLoading.postValue(true)

        val imageParam: MultipartBody.Part =
            if (image?.name.isNullOrEmpty()) {
                toRequestBodyProfileNullImage(image.toString(), ApiConstants.ApiParams.IMAGE.value)
            } else {
                MultipartBody.Part.createFormData(ApiConstants.ApiParams.IMAGE.value, image?.name, image!!.asRequestBody("*/*".toMediaTypeOrNull()))
            }
        val param = HashMap<String, RequestBody?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.NAME.value] = name.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.LOCATION.value] = location.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.CONTACT_NUMBER.value] = contactNumber.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.COUNTRY_CODE.value] = countryCode.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.PASSWORD.value] = password.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.DEVICE_TOKEN.value] = deviceToken.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.DEVICE_TYPE.value] = deviceType.get()?.trim()?.createPartFromString()

        authDataSourceImp.executeRegistrationApi(param, imageParam, apiType = ApiConstants.REGISTRATION).catch { e ->
            signUpResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            signUpResponse.emit(isResponse)
        }
    }

    /**
     * this function is use to hit Register Api
     * */
    suspend fun hitUpdateProfileApi() {
        showLoading.postValue(true)

        val imageParam: MultipartBody.Part =
            if (image?.name.isNullOrEmpty()) {
                toRequestBodyProfileNullImage(image.toString(), ApiConstants.ApiParams.IMAGE.value)
            } else {
                MultipartBody.Part.createFormData(ApiConstants.ApiParams.IMAGE.value, image?.name, image!!.asRequestBody("*/*".toMediaTypeOrNull()))
            }
        val param = HashMap<String, RequestBody?>()
        param[ApiConstants.ApiParams.NAME.value] = name.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.CONTACT_NUMBER.value] = contactNumber.get()?.trim()?.createPartFromString()
        param[ApiConstants.ApiParams.COUNTRY_CODE.value] = countryCode.get()?.trim()?.createPartFromString()

        authDataSourceImp.executeUpdateProfileApi(param, imageParam, apiType = ApiConstants.UPDATE_PROFILE).catch { e ->
            updateProfileResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            updateProfileResponse.emit(isResponse)
        }
    }


    /**
     * this function is use to hit Login Api
     * */
    suspend fun hitLoginApi() {
        showLoading.postValue(true)
        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.PASSWORD.value] = password.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.DEVICE_TOKEN.value] = deviceToken.get()?.trim()?: ""
        param[ApiConstants.ApiParams.DEVICE_TYPE.value] = deviceType.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.APPLICATION_ID.value] = "123456789" ?: ""
        param[ApiConstants.ApiParams.LOGIN_MODE.value] = "Manual"

        authDataSourceImp.executeLoginApi(param, apiType = ApiConstants.LOGIN).catch { e ->
            loginResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            loginResponse.emit(isResponse)
        }
    }

    /**
     * this function is use to hit Login Api
     * */
    suspend fun hitLoginGmailApi() {
        showLoading.postValue(true)
        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = emailGmail.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.PASSWORD.value] = "" ?: ""
        param[ApiConstants.ApiParams.DEVICE_TOKEN.value] = deviceToken.get()?.trim()?: ""
        param[ApiConstants.ApiParams.DEVICE_TYPE.value] = deviceType.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.APPLICATION_ID.value] = applicationID.get().toString().trim() ?: ""
        param[ApiConstants.ApiParams.LOGIN_MODE.value] = "Gmail"
        param[ApiConstants.ApiParams.NAME.value] = name.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.CONTACT_NUMBER.value] = contactNumber.get()?.trim()?: ""

        authDataSourceImp.executeLoginApi(param, apiType = ApiConstants.LOGIN).catch { e ->
            loginResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            loginResponse.emit(isResponse)
        }
    }


    /**
     * this function is use to hit Register Api
     * */
    suspend fun hitVerificationOtpApi() {
        showLoading.postValue(true)

        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.OTP.value] = otp.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.VERIFICATION_TYPE.value] = verificationType.get()?.trim() ?: ""

        authDataSourceImp.executeVerificationOtpApi(param, apiType = ApiConstants.VERIFICATION_OTP).catch { e ->
            verificationOtpResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            verificationOtpResponse.emit(isResponse)
        }
    }


    /**
     * this function is use to hit Forgot Password Api
     * */
    suspend fun hitForgotPasswordApi() {
        showLoading.postValue(true)
        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim() ?: ""
        authDataSourceImp.executeForgotPasswordApi(param, apiType = ApiConstants.VERIFICATION_OTP).catch { e ->
            forgotPasswordResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            forgotPasswordResponse.emit(isResponse)
        }
    }

    /**
     * this function is use to hit reset Password Api
     * */
    suspend fun hitResetPasswordApi() {
        showLoading.postValue(true)

        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim() ?: ""
        param[ApiConstants.ApiParams.PASSWORD.value] = password.get()?.trim() ?: ""

        authDataSourceImp.executeResetPasswordApi(param, apiType = ApiConstants.RESET_PASSWORD).catch { e ->
            resetPasswordResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            resetPasswordResponse.emit(isResponse)
        }
    }

    /**
     * this function is use to hit Resend Otp Api
     * */
    suspend fun hitResendOtpApi() {
        showLoading.postValue(true)

        val param = HashMap<String, String?>()
        param[ApiConstants.ApiParams.EMAIL.value] = email.get()?.trim() ?: ""

        authDataSourceImp.executeResendOtpApi(param, apiType = ApiConstants.RESEND_OTP).catch { e ->
            resendOtpResponse.emit(Resource.Error(e.message.toString()))
            showLoading.postValue(false)
        }.collect { isResponse ->
            showLoading.postValue(false)
            resendOtpResponse.emit(isResponse)
        }
    }

    fun initializeGoogleSignIn() {
        if (googleSignIn == null) {
            googleSignIn = getGoogleSignInInstance()
        }
    }

    private fun getGoogleSignInInstance(): GoogleSignInInterface {
        return GoogleSignInImpl()
    }

}



fun main() {
    System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver") // Set the path to your chromedriver
    val driver: WebDriver = ChromeDriver()
    val wait = WebDriverWait(driver, 10)
    driver.get("https://students.cuchd.in/")
    driver.manage().window().maximize()

    val userIdInput: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtUserId")))
    userIdInput.click()
    userIdInput.sendKeys("22BCA10952")
    println("userid entered")

    val nextButton: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnNext")))
    nextButton.click()

    val passwordInput: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtLoginPassword")))
    passwordInput.click()
    passwordInput.sendKeys("Yadav@1202")
    println("password send")

    try {
        val element: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("imgCaptcha")))
        (element as TakesScreenshot).getScreenshotAs(OutputType.FILE).let { screenshot ->
            val destFile = File("specific_screenshot.jpg")
            org.apache.commons.io.FileUtils.copyFile(screenshot, destFile)
            println("Screenshot saved successfully!")
        }
    } catch (e: Exception) {
        println("An error occurred: $e")
    }

    // You need to replace this part with appropriate code to read text from the captcha image
    // using an OCR library in Kotlin
    // val captchaText = readCaptchaText("specific_screenshot.jpg")

    // Dummy value, replace with actual OCR code
    val captchaText = "dummy_captcha_text"

    println("Extracted Text: $captchaText")

    val inputField: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtcaptcha")))
    inputField.click()
    inputField.sendKeys(captchaText)

    val loginButton: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")))
    loginButton.click()

    Thread.sleep(7000)
    driver.quit()
}

// Replace this with the appropriate OCR code to read captcha text
//fun readCaptchaText(imagePath: String): String {
// Implement your OCR code here
//}



/*



fun main() {
    System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver") // Set the path to your chromedriver
    val driver: WebDriver = ChromeDriver()
    val wait = WebDriverWait(driver, 10)
    driver.get("https://students.cuchd.in/")
    driver.manage().window().maximize()

    val userIdInput: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtUserId")))
    userIdInput.click()
    userIdInput.sendKeys("22BCA10952")
    println("userid entered")

    val nextButton: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnNext")))
    nextButton.click()

    val passwordInput: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtLoginPassword")))
    passwordInput.click()
    passwordInput.sendKeys("Yadav@1202")
    println("password send")

    try {
        val element: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("imgCaptcha")))
        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView();", element)
        val screenshot = (element as TakesScreenshot).getScreenshotAs(OutputType.BYTES)
        val image = ImageIO.read(ByteArrayInputStream(screenshot))
        ImageIO.write(image, "jpg", File("specific_screenshot.jpg"))
        println("Screenshot saved successfully!")
    } catch (e: Exception) {
        println("An error occurred: $e")
    }

    // You need to replace this part with appropriate code to read text from the captcha image
    // using an OCR library in Kotlin
    val tesseract = Tesseract()
    val captchaText = tesseract.doOCR(File("specific_screenshot.jpg"))
    println("Extracted Text: $captchaText")

    val inputField: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtcaptcha")))
    inputField.click()
    inputField.sendKeys(captchaText)

    val loginButton: WebElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin")))
    loginButton.click()

    Thread.sleep(7000)
    driver.quit()
}*/




