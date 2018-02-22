package com.example.dilloncooper.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK
import android.widget.ListView
import com.example.dilloncooper.top10downloader.R.id.*
import com.example.dilloncooper.top10downloader.R.string.app_name
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates


class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String = ""

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

    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10  //default to Top 10

    private var feedCachedUrl = "INVALIDATED"
    private val STATE_URL = "feedUrl"
    private val STATE_LIMIT = "feedLimit"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {  //Since onRestore occurs after onCreate in the Android Lifecycle, we have to put the restoration in onCreate

            val saveUrls = savedInstanceState.getString(STATE_URL)
            feedUrl = saveUrls

            val saveFeed = savedInstanceState.getInt(STATE_LIMIT)
            feedLimit = saveFeed
        }

        downloadUrl(feedUrl.format(feedLimit))  //Formats feedUrl to substitute the value specified for "feedLimit" into the url in "feedUrl" for the value of "%d"
        Log.d(TAG, "onCreate: done")
    }

    private fun downloadUrl(feedUrl: String) {
        if (feedUrl != feedCachedUrl) {  //Checks and prevents the user from re-downloading the same URL if they are already viewing that URL
            Log.d(TAG, "downloadURL starting AsyncTask")
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedUrl)
            Log.d(TAG, "downloadUrl done")
            Log.d(TAG, "downloadURL1 $feedCachedUrl")
            feedCachedUrl = feedUrl  // Update feedCachedUrl to contain the currently accessed URL
            Log.d(TAG, "downloadURL2 $feedCachedUrl")
        } else {
            Log.d(TAG, "downloadUrl - URL not changed")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //Gives us the ellipses within the menu and displays the three options depicted in feeds_menu.xml
        menuInflater.inflate(R.menu.feeds_menu, menu)

        if (feedLimit == 10) {
            Log.d(TAG, "$feedLimit")
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        Log.d(TAG, "$feedLimit")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //After prompting the menu, decides where to go when the user selects one of the options from within the menu.

        when (item.itemId) {  //If item was nullable (item: MenuItem?), we would use the null-safe operator here. However, we removed the null-safe operator since we know it will exist.
            R.id.mnuFree ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"

            R.id.mnu10, R.id.mnu25 -> {
                if (!item.isChecked) { //If item is not currently checked
                    item.isChecked = true  //Set isChecked to now true
                    feedLimit = 35 - feedLimit  //Subtract the current value from the sum of the options (To toggle between the two options). Subtract the sum from the last feedLimit value.
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                    app_name
                } else {
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                    return false
                }
            }
            R.id.mnuRefresh ->
                feedCachedUrl = "INVALIDATED"

            else ->
                return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl.format(feedLimit))
        Log.d(TAG, "$feedLimit")

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(STATE_URL, feedUrl)
        outState.putInt(STATE_LIMIT, feedLimit)
    }


    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    // Companion Object = Kotlin's version of a Java Static Class
    companion object {  // Created companion object to avoid memory leaks
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

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
