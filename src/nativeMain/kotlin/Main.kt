import kotlinx.cinterop.*
import libcurl.*

fun main(args: Array<String>) {
    curl_easy_init()?.apply {
        curl_easy_setopt(this, CURLOPT_URL, "https://example.com")
        curl_easy_setopt(this, CURLOPT_FOLLOWLOCATION, 1L)
        val res = curl_easy_perform(this)
        if (res != CURLE_OK)
            println("curl_easy_perform() failed ${curl_easy_strerror(res)?.toKString()}")
        curl_easy_cleanup(this)
    }
}