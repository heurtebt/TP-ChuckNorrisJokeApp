package com.example.chucknorrisjokeapp
import kotlinx.serialization.*
import kotlinx.serialization.json.*
@Serializable
data class Joke(val categories:List<String>,
                @SerialName("created_at")val createdAt:String,
                @SerialName("icon_url")val iconUrl:String,
                val id:String,
                @SerialName("updated_at")val updatedAt:String,
                val url:String,
                val value:String)

fun main() {
    val json = Json(JsonConfiguration.Stable)
    val jsonData = json.stringify(Joke.serializer(), Joke(listOf(""),"created_at","icon_url", "id","updated_at","url", "value"))
    val jsonList = json.stringify(Joke.serializer().list, listOf(Joke(listOf(""),"created_at","icon_url", "id","updated_at","url", "value")))

    // parsing data back
    val obj = json.parse(
        Joke.serializer(),
        """{
          "categories": [
            
          ],
          "created_at": "2020-01-05 13:42:26.766831",
          "icon_url": "https://assets.chucknorris.host/img/avatar/chuck-norris.png",
          "id": "pyNXTV7WThiNLRykGsQmrg",
          "updated_at": "2020-01-05 13:42:26.766831",
          "url": "https://api.chucknorris.io/jokes/pyNXTV7WThiNLRykGsQmrg",
          "value": "The hills are alive with the sound of Chuck Norris' dong slapping against his legs while he walks."
        }"""
    )
}