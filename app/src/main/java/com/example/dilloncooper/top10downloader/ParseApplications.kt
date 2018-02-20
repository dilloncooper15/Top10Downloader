package com.example.dilloncooper.top10downloader

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Created by dilloncooper on 2/19/18.
 */
class ParseApplications {
    private val TAG = "ParseApplications"
    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean {
        var status = true
        var inEntry = false
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xpp.name?.toLowerCase()
                when (eventType) {

                    XmlPullParser.START_TAG -> {
                        if (tagName == "entry") {
                            inEntry = true
                        }
                    }

                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {   //Check the HTML end tag, EX) "</name>"
                        if (inEntry) {   //Check to make sure we are still in an Entry tag.
                            when (tagName) {   // When the end tag is "entry", we can append the record (that's between the entry tags) to the ArrayList, applications (line 45).
                                "entry" -> {
                                    applications.add(currentRecord)  // Append the feedEntry, from MainActivity, to the ArrayList.
                                    inEntry = false  //Since this is the end tag for entry, set "inEntry" to false to move onto the next "entry" tag, if one exists.
                                    currentRecord = FeedEntry()  // create a new object
                                }
                                //If the end tag is not "</entry>", but we are still within an entry, then perform the following for each corresponding tag.
                                "name" -> currentRecord.name = textValue
                                "artist" -> currentRecord.artist = textValue
                                "releasedate" -> currentRecord.releaseDate = textValue  // Has to be "releasedate" and not "releaseDate" since we are converting the tag names to lowercase in line 28
                                "summary" -> currentRecord.summary = textValue
                                "image" -> currentRecord.imageURL = textValue
                            }
                        }
                    }
                }

                // Nothing else to do.
                eventType = xpp.next()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }

        return status
    }
}