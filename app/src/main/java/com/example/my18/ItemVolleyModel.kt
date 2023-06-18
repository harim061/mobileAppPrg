package com.example.my18

import java.io.Serializable

data class ItemVolleyModel(
    var name: String? = null,
    var address: String? = null,
    var call: String? = null,
    var about: String? = null,
    var lat: String? = null,
    var lon: String? = null,
    var email :String? = null

): Serializable