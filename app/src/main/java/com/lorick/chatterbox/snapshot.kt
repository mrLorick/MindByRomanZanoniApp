package com.lorick.chatterbox


import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ApiLoginFailureError(message: String?) : Exception(message)
class UIMSInternalError(message: String?) : Exception(message)

fun main() {
    SessionUIMS("22BCA10952","Yadav@1202")
}
class SessionUIMS(private val uid: String, private val password: String) {
    private var cookies: HashMap<String, String>? = null
    private var fullAttendance: FullAttendance? = null
    private val fullAttendanceReport: MutableMap<String, Any> = HashMap()
    private var sessionID: String? = null
    private var reportID: String? = null
    private  var AUTHENTICATE_URL = "https://students.cuchd.in"

    private fun login(): HashMap<String, String> {
        val response = Jsoup.connect(AUTHENTICATE_URL).execute()
        val cookies = HashMap(response.cookies())
        val doc: Document = response.parse()
        println("kasnjnsansas" + "ksjfkn+$doc")

        val viewStateTag = doc.selectFirst("input[id=__VIEWSTATE]")
        val data: MutableMap<String, String> = HashMap()
        data["__VIEWSTATE"] = viewStateTag?.attr("value").toString()
        data["txtUserId"] = uid
        data["btnNext"] = "NEXT"
        val postResponse = Jsoup.connect(AUTHENTICATE_URL)
            .data(data)
            .cookies(cookies)
            .method(org.jsoup.Connection.Method.POST)
            .execute()
//        val passwordUrl = AUTHENTICATE_URL + postResponse.header("location")
//        val passwordResponse = Jsoup.connect(passwordUrl).cookies(cookies).execute()
        val loginCookies = postResponse.cookies()
        val loginDoc: Document = postResponse.parse()
        val viewStateTagLogin = loginDoc.selectFirst("input[name=__VIEWSTATE]")
        val viewStateGeneratorTag = loginDoc.selectFirst("input[id=__VIEWSTATEGENERATOR]")
        val captchaImgSourceStr = loginDoc.selectFirst("img[id=imgCaptcha]").absUrl("src")
        val imgResponse =
            Jsoup.connect(captchaImgSourceStr).cookies(loginCookies).ignoreContentType(true)
                .execute()
        val captchaImage = imgResponse.bodyAsBytes()
        println("kasnjnsansas" + "ksjfkn")
      /*  val tessInstance = Tesseract()
        tessInstance.setDatapath("path/to/your/tessdata") // Set path to tessdata directory
        tessInstance.setLanguage("eng") // Set language
        val result = tessInstance.doOCR(imageFile)
*/
//        val captchaAnswer =
//            pytesseract.doOCR(ByteArrayInputStream(captchaImage)).replace("\\s".toRegex(), "")
//        val cleanedCaptchaAnswer = captchaAnswer.filter { it.isLetterOrDigit() }
        val dataLogin: MutableMap<String, String> = HashMap()
        dataLogin["__VIEWSTATE"] = viewStateTagLogin.attr("value")
        dataLogin["__VIEWSTATEGENERATOR"] = viewStateGeneratorTag.attr("value")
        dataLogin["txtLoginPassword"] = password
        dataLogin["txtcaptcha"] = "cleanedCaptchaAnswer"
        dataLogin["btnLogin"] = "LOGIN"
        val loginAndAspnetSessionCookies = HashMap(loginCookies)
        val finalResponse = Jsoup.connect(AUTHENTICATE_URL)
            .data(dataLogin)
            .cookies(loginCookies)
            .method(org.jsoup.Connection.Method.POST)
            .execute()
        if (finalResponse.statusCode() == 200) {
            throw ApiLoginFailureError("Invalid login request sent to UIMS, check credentials or captcha")
        }
        loginAndAspnetSessionCookies.putAll(finalResponse.cookies())
        return loginAndAspnetSessionCookies
    }

    init {
        cookies = login()
    }

    private fun refreshSession() {
        cookies = login()
    }

    private fun extractMarks(response: org.jsoup.Connection.Response): List<Map<String, Any>> {
        val doc: Document = response.parse()
        val accordion = doc.selectFirst("div[id=accordion]")
        val subjectNames = accordion.select("h3").map { it.text().trim() }
        val divs = accordion.select("div")
        val marks: MutableList<Map<String, Any>> = ArrayList()
        if (subjectNames.size == divs.size) {
            for (i in subjectNames.indices) {
                val obj: MutableMap<String, Any> = HashMap()
                obj["name"] = subjectNames[i]
                val tbodyTrs = divs[i].select("tbody tr")
                val subMarks: MutableList<Map<String, String>> = ArrayList()
                tbodyTrs.forEach { tr ->
                    val tds = tr.select("td")
                    val fields: MutableMap<String, String> = HashMap()
                    fields["element"] = tds[0].text().trim()
                    fields["total"] = tds[1].text().trim()
                    fields["obtained"] = tds[2].text().trim()
                    subMarks.add(fields)
                }
                obj["marks"] = subMarks
                marks.add(obj)
            }
        }
        return marks
    }
}

data class FullAttendance(val subjects: List<Subject>) {
    // Add any additional properties or methods as needed
}

data class Subject(val encryptCode: String, var fullAttendanceReport: FullAttendanceReport?) {
    // Add any additional properties or methods as needed
}

data class FullAttendanceReport(val jsonData: String) {
    // Add any additional properties or methods as needed
}