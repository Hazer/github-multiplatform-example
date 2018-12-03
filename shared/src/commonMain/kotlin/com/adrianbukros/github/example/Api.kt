package com.adrianbukros.github.example

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import kotlinx.serialization.list
import org.kodein.di.erased.instance
import timber.log.Timber
import timber.log.debug
import timber.log.info

internal expect val ApplicationDispatcher: CoroutineDispatcher

@ImplicitReflectionSerializer
class GitHubApiClient(private val githubUserName: String, private val githubPassword: String) {

    private val httpClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                setMapper(GitHubRepo::class, GitHubRepo.serializer())
            }
        }
    }

    private val gitHubHost: String by kodein.instance("GitHubHost")

    fun repos(successCallback: (List<GitHubRepo>) -> Unit, errorCallback: (String?, Exception) -> Unit) {
        GlobalScope.apply {
            launch(ApplicationDispatcher) {
                var responseString: String? = null // TODO: Remove later, just using to debug in iOS a faulty behaviour
                try {
                    val reposString = httpClient.get<String> {
                        url {
                            protocol = URLProtocol.HTTPS
                            port = 443
                            host = gitHubHost
                            encodedPath = "user/repos"
                            header("Authorization", "Basic " + "$githubUserName:$githubPassword".toBase64())
                            Timber.info { "Sending request to: ${buildString()}" }
                        }
                    }
                    responseString = reposString
                    Timber.debug { "Received string:\n$reposString" }
                    val repos = JSON(strictMode = false).parse(GitHubRepo.serializer().list, reposString)

                    successCallback(repos)
                } catch (ex: Exception) {
                    errorCallback(responseString, ex)
                }
            }
        }
    }

    suspend fun repos(): List<GitHubRepo> = withContext(ApplicationDispatcher) {
        val reposString = httpClient.get<String> {
            url {
                protocol = URLProtocol.HTTPS
                port = 443
                host = gitHubHost
                encodedPath = "user/repos"
                header("Authorization", "Basic " + "$githubUserName:$githubPassword".toBase64())
                Timber.info { "Sending request to: ${buildString()}" }
            }
        }

        return@withContext JSON(strictMode = false).parse(GitHubRepo.serializer().list, reposString)
    }
}

@Serializable
data class GitHubRepo(
        val name: String,
        @SerialName("html_url")
        val htmlUrl: String
)
