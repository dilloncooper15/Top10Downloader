package com.example.dilloncooper.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

//    private val downloadData by lazy { DownloadData(this, xmlListView) }  // Have to add by lazy, since none of the widgets exist until after the call to setContentView in the onCreate Method.
    private var downloadData: DownloadData? = null  //Have to change downloadData to allow nulls since we will be referring to Async multiple times via the user selecting an option from the menu.
    // NOTE: With a lazy function, initialization does not occur until the first time we need to use the DownloadData object.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(TAG,"onCreate: done")
    }

    private fun downloadUrl(feedUrl: String) {
        Log.d(TAG, "downloadURL starting AsyncTask")
        downloadData = DownloadData(this, xmlListView)
        downloadData?.execute(feedUrl)
        Log.d(TAG, "downloadUrl done")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //Gives us the ellipses within the menu and displays the three options depicted in feeds_menu.xml
        menuInflater.inflate(R.menu.feeds_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //After prompting the menu, decides where to go when the user selects one of the options from within the menu.
        val feedUrl: String

        when (item.itemId) {  //If item was nullable (item: MenuItem?), we would use the null-safe operator here. However, we removed the null-safe operator since we know it will exist.
            R.id.mnuFree ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"
            R.id.mnuPaid ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml"
            R.id.mnuSongs ->
                    feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml"
            else ->
                    return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
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
