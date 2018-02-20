package com.example.dilloncooper.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name : String = ""
    var artist : String = ""
    var releaseDate : String = ""
    var summary : String = ""
    var imageURL : String = ""

    override fun toString(): String {  // Override toString so the Recycler View does not display the full class name and hashcode of each instance of the FeedEntry class.
        return """
            name = $name
            artist = $artist
            releaseDate = $releaseDate
            imageURL = $imageURL
            """.trimIndent()
    }
}


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val downloadData by lazy { DownloadData(this, xmlListView) }  // Have to add by lazy, since none of the widgets exist until after the call to setContentView in the onCreate Method.
    // NOTE: With a lazy function, initialization does not occur until the first time we need to use the DownloadData object.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData.cancel(true)
    }

    // Companion Object = Kotlin's version of a Java Static Class
    companion object {  // Created companion object to avoid memory leaks
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propContext : Context by Delegates.notNull()
            var propListView : ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

//                New Adapter:
                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications) //parseApplications.applications = list of feed entry objects
                propListView.adapter = feedAdapter
            }

            override fun doInBackground(vararg url: String?): String {
                // Anything within doInBackground (including method calls) will be ran on a different thread.
                val rssFeed = downloadXML(url[0])
                return rssFeed
            }

            private fun downloadXML(urlPath: String?): String {
               return URL(urlPath).readText()
            }
        }
    }


}
