package io.scalechain.util

import com.google.gson.*
import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import java.lang.reflect.Type

class GsonSpec : FlatSpec(), Matchers {

    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
    }

    data class Person(val name : String, val age : Int?)

    val person1 = Person("Kangmo", 10)
    val person2 = Person("Kangmo", null)

    val json1 = """{"name":"Kangmo","age":10}"""
    val json2 = """{"name":"Kangmo"}"""

    interface SuperType

    data class SubType1(val intValue : Int) : SuperType
    data class SubType2(val stringValue : String) : SuperType
    data class SubType3(val stringValues : Array<String>) : SuperType


    data class ElementListWrapper(val paramValues:List<JsonElement>)

    init {
        "Gson" should "serialize a data class with nullable fields" {
            val gson = Gson()

            val actualJson1 = gson.toJson(person1)
            val actualJson2 = gson.toJson(person2)
            println( "$person1 is converted to $actualJson1" )
            println( "$person2 is converted to $actualJson2" )

            actualJson1 shouldBe json1
            actualJson2 shouldBe json2
        }

        "Gson" should "deserialize a data class with nullable fields" {
            val gson = Gson()

            val actualPerson1 = gson.fromJson(json1, Person::class.java)
            println( "$json1 is converted to ${person1}" )

            val actualPerson2 = gson.fromJson(json2, Person::class.java)
            println( "$json2 is converted to ${person2}" )

            actualPerson1 shouldBe person1
            actualPerson2 shouldBe person2
        }

        "Gson" should "serialize objects based on the base class's serializer" {
            val gson = Gson()


            class TypeSerializer : JsonSerializer<SuperType> {
                override fun serialize(src: SuperType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                    return when {
                        src is SubType1 -> {
                            JsonPrimitive(src.intValue)
                        }
                        src is SubType2 -> {
                            JsonPrimitive(src.stringValue)
                        }
                        src is SubType3 -> {
                            val array = JsonArray()

                            src.stringValues.forEach { string ->
                                array.add(string)
                            }
                            array
                        }
                        else -> {
                            gson.toJsonTree(src, typeOfSrc)
                        }
                    }
                    /*
                    // This method gets involved whenever the parser encounters the Dog
                    // object (for which this serializer is registered)
                    val object : JsonObject = new JsonObject();
                    String name = src . getName ().replaceAll(" ", "_");
                    object.addProperty("name", name);
                    // we create the json object for the dog and send it back to the
                    // Gson serializer
                    return object;
                    */
                }
            }

            val gson2 = GsonBuilder().registerTypeAdapter(SubType1::class.java, TypeSerializer())
                                     .registerTypeAdapter(SubType2::class.java, TypeSerializer())
                                     .registerTypeAdapter(SubType3::class.java, TypeSerializer())
                                     .create();

            println ("${gson2.toJson(SubType1(10))}")
            println ("${gson2.toJson(SubType2("abc"))}")
            println ("${gson2.toJson(SubType3(arrayOf("abc", "def")))}")

            gson2.toJson(SubType1(10)) shouldBe """10"""
            gson2.toJson(SubType2("abc")) shouldBe """"abc""""
            gson2.toJson(SubType3(arrayOf("abc", "def"))) shouldBe """["abc","def"]"""
        }
        "gson" should "parse json array into a list of json element" {
            class TypeDeserializer : JsonDeserializer<ElementListWrapper> {
                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ElementListWrapper {
                    if (json == null)
                        throw IllegalArgumentException()
                    when {
                        json.isJsonArray() -> {
                            val jsonArray = json.getAsJsonArray()
                            return ElementListWrapper(jsonArray.toList())
                        }
                        else -> {
                            throw IllegalArgumentException()
                        }
                    }
                }
            }

            val gson = GsonBuilder().registerTypeAdapter(ElementListWrapper::class.java, TypeDeserializer()).create()

            gson.fromJson("[]", ElementListWrapper::class.java) shouldBe ElementListWrapper(listOf())
            gson.fromJson("""[10,"abc"]""", ElementListWrapper::class.java) shouldBe ElementListWrapper(listOf(JsonPrimitive(10), JsonPrimitive("abc")))
        }
    }
}