package com.oscarliang.gitfinder.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.test.runTest
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
            .build()
            .create(GithubService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testSearchRepos() = runTest {
        enqueueResponse("search.json")
        val response = service.searchRepos(
            "android",
            10
        )

        val request = mockWebServer.takeRequest()
        assertEquals(
            request.path,
            "/search/repositories?q=android&per_page=10"
        )

        assertNotNull(response)
        assertEquals(response.items.size, 10)

        val repo1 = response.items[0]
        assertEquals(repo1.id, 82128465)
        assertEquals(repo1.owner.name, "open-android")
        assertEquals(repo1.url, "https://github.com/open-android/Android")

        val repo2 = response.items[1]
        assertEquals(repo2.id, 12544093)
        assertEquals(repo2.owner.name, "hmkcode")
        assertEquals(repo2.url, "https://github.com/hmkcode/Android")
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