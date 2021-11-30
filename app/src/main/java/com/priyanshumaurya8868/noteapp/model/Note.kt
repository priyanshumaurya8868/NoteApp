package com.priyanshumaurya8868.noteapp.model

import java.io.Serializable

data class Note(
    val title : String = "",
    val content : String = "",
    val date : String = "",
    val image : String =""
):Serializable{
    fun toMap() : Map<String, Any?>{
      val map = mutableMapOf<String, Any?>()
        map["title"] = title
        map["content"]= content
        map["date"]=date
        map["image"]=image

        return map
    }
}
