package com.dhsdevelopments.potato.clientapi.search

import com.google.gson.annotations.SerializedName

class SearchResult {
    @SerializedName("num_found")
    var numFound: Int = 0

    @SerializedName("start")
    var start: Int = 0

    @SerializedName("messages")
    lateinit var messages: Array<SearchResultMessage>
}
