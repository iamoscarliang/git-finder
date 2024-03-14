package com.oscarliang.gitfinder.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.oscarliang.gitfinder.util.LiveDataCallAdapterFactory
import com.oscarliang.gitfinder.util.getOrAwaitValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class NewsServiceTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: GithubService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(GithubService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun search() {
        enqueueResponse("search.json")
        val response = service.searchRepos(
            "android",
            10
        ).getOrAwaitValue() as ApiSuccessResponse

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/search/repositories?q=android&per_page=10"
        )

        assertNotNull(response)
        assertEquals(response.body.items.size, 10)

        val news1 = response.body.items.get(0)
        assertEquals(news1.id, 82128465)
        assertEquals(news1.owner.name, "open-android")
        assertEquals(news1.url, "https://github.com/open-android/Android")

        val news2 = response.body.items.get(1)
        assertEquals(news2.id, 12544093)
        assertEquals(news2.owner.name, "hmkcode")
        assertEquals(news2.url, "https://github.com/hmkcode/Android")
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader!!
            .getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }

}