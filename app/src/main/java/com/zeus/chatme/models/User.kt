package com.zeus.chatme.models

import androidx.annotation.Keep

@Keep
data class User(var id : String? = "", var username : String? = "", var imageURL : String? = "" , var imageURL2 : String? = "",
                var email: String? = "", var about: String? = "", var password: String? = "", var oporokpo: String? = "",
                var search: String? = "", var location: String? = "", var bio: String? = "",  var verified: String? = "",
                var dateJoined: String? = "")
