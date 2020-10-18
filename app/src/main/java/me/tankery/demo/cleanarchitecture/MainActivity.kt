package me.tankery.demo.cleanarchitecture

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query


class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var listView: ListView

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
        listView = findViewById<View>(R.id.list_view) as ListView

        listView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        retrofit.create(GitHubService::class.java)
            .searchRepos("cleanarchitecture")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val repositories = response.body()?.string()?.getRepoNames()

                    if (repositories == null || repositories.isEmpty()) return
                    val adapter: ArrayAdapter<*> = ArrayAdapter(
                        this@MainActivity, android.R.layout.simple_list_item_1, android.R.id.text1, repositories
                    )
                    listView.adapter = adapter
                    listView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

            })
    }
}

interface GitHubService {
    @GET("search/repositories")
    fun searchRepos(@Query("q") query: String?): Call<ResponseBody>
}

private fun String.getRepoNames(): List<String>? =
    try {
        val array = JSONObject(this).getJSONArray("items")
        (0 until array.length())
            .map { i ->
                val item = array.getJSONObject(i)
                item.getString("full_name")
            }
    } catch (e: JSONException) {
        e.printStackTrace()
        null
    }
