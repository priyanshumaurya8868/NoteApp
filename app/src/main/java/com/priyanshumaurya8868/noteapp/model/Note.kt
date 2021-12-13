package com.priyanshumaurya8868.noteapp.model

import java.io.Serializable

data class Note(
    val id : String ="",
    val title : String = "",
    val content : String = "",
    val date : String = "",
    val image : String =""
):Serializable{
    fun toMap() : Map<String, Any?>{
      val map = mutableMapOf<String, Any?>()
        map["id"]=id
        map["title"] = title
        map["content"]= content
        map["date"]=date
        map["image"]=image
        return map
    }

    fun copyVal(
        id : String =this.id,
        title : String =this.title,
        content : String = this.content,
        date : String = this.date,
        image : String =this.image
    ) = Note(id, title, content, date, image)
}
