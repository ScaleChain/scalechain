package io.scalechain.blockchain.api

/*
object JsonWriter {
  implicit val anyValWriter = Writes[Any] (a => a match {
    case v:String => Json.toJson(v)
    case v:Int => Json.toJson(v)
    case v:Any => Json.toJson(v.toString)
    // or, if you don't care about the value
    case _ => throw new RuntimeException("unserializeable type")
  })
}
*/