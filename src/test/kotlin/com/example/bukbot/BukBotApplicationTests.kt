package com.example.bukbot

import com.example.bukbot.utils.getAccessToken
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.Request
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.io.IOException
import okhttp3.OkHttpClient
import org.springframework.util.Assert


@SpringBootTest
class BukBotApplicationTests {

	@Test
	fun contextLoads() {
	}

//	@Test
//	@Throws(IOException::class)
//	fun whenSendPostRequest_thenCorrect() {
//		val formBody = FormBody.Builder()
//				.add("username", "test")
//				.add("password", "test")
//				.build()
//
//		val request = Request.Builder()
//				.url("$BASE_URL/users")
//				.post(formBody)
//				.build()
//
//		client.newCall(request).execute().use({ response -> return response.body().string() })
//	}

	val JSON = MediaType.get("application/json; charset=utf-8")

	var client = OkHttpClient()

	@Test
	@Throws(IOException::class)
	fun post(url: String, json: String) {
		val body = FormBody.Builder()
				.add("username", "unity_group153")
				.add("accessToken", getAccessToken()!!)
				.build()
		val request = Request.Builder()
				.url("http://biweb-unity-test.olesportsresearch.com/getusercredit")
				.post(body)
				.build()
		try {
			val response = client.newCall(request).execute()
			val j = response.body()!!.string()
			val k = 1
		} catch (e: Exception){

		}
//		client.newCall(request).execute().use { response -> return response.body()!!.string() }
	}

	@Test
	fun token() {
		val k = getAccessToken()!!
	}
}
