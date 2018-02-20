package com.example.dilloncooper.top10downloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Created by dilloncooper on 2/20/18.
 */

class ViewHolder(v: View) { //Created this class outside FeedAdapter Class, instead of inside, so we can store the three widgets and reference them. Assigning them inside is less efficient and will slow down performance.
    val tvName: TextView = v.findViewById(R.id.tvName)
    val tvArtists: TextView = v.findViewById(R.id.tvArtist)
    val tvSummary: TextView = v.findViewById(R.id.tvSummary)
}
class FeedAdapter(context: Context, private val resource: Int, private val applications: List<FeedEntry>): ArrayAdapter<FeedEntry>(context, resource) {  //Extending class
    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int { //If we didn't override getCount(), our listView wouldn't display any items. Therefore, it needs to know how many exist so it can create a list of rows.
        return applications.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //convertView = If the listView has a view it can reuse, it passes a reference to it in convertView
        //Until a view is scrolled off of the screen, there won't be a view to reuse. Therefore, we have to check to see if convertView is NULL and only create a new view if it is NULL

        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) { //If the convertView is NULL, create a new view. If convertView is not NULL, then reuse the view. We do this so we are not creating a new view for views that already existed, thus eating up memory.
            view = inflater.inflate(resource,parent,false)  //Create a view by inflating the layout resource, uses the layout inflater created from the context in the layout constructor.
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
//        val tvName: TextView = view.findViewById(R.id.tvName)
//        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
//        val tvSummary: TextView = view.findViewById(R.id.tvSummary)

        val currentApp = applications[position]

        viewHolder.tvName.text = currentApp.name
        viewHolder.tvArtists.text = currentApp.artist
        viewHolder.tvSummary.text = currentApp.summary

        return view
    }
}