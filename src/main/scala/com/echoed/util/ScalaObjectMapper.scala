package com.echoed.util


import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.{JavaType, SerializationFeature, DeserializationFeature, ObjectMapper}
import com.echoed.chamber.domain.{TwitterUser, EchoedUser}


object ScalaObjectMapper {
    def apply[T](
            value: String,
            valueType: Class[T],
            unwrap: Boolean = false) = new ScalaObjectMapper(unwrap).readValue(value, valueType)
}

class ScalaObjectMapper(unwrap: Boolean = false) extends ObjectMapper {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
    configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    configure(DeserializationFeature.UNWRAP_ROOT_VALUE, unwrap)

//    configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)

    registerModule(DefaultScalaModule)

    def this() = this(false)
}

object ScalaObjectMapperText extends App {
    val echoedUser = new EchoedUser(
            "Matthew Pflueger",
            "matthew.pflueger@gmail.com",
            null,
            "11012812034012",
            "19832892383908230",
            null,
            null)
    println(echoedUser)
    val echoedUserString = new ScalaObjectMapper().writeValueAsString(echoedUser)
    println(echoedUserString)
    val echoedUserMap = new ScalaObjectMapper().writeValueAsString(echoedUser.asPublicMap)
    println(echoedUserMap)

//    val echoedUser = new ScalaObjectMapper().writeValueAsString(ObjectUtils.asMap(new EchoedUser(
//            "Matthew Pflueger",
//            "matthew.pflueger@gmail.com",
//            null,
//            "11012812034012",
//            "19832892383908230",
//            null,
//            null)))


    val encrypter = new Encrypter()
    encrypter.setSecret("PHHabG5MSw6hv4lZJg-Ppg")
    encrypter.init()
    val encryptedEchoedUser = encrypter.encrypt(echoedUserMap)
//    val encryptedEchoedUser = encrypter.encrypt(echoedUserMap)
    println(encryptedEchoedUser)
    val decryptedEchoedUser = encrypter.decrypt(encryptedEchoedUser)
    println(decryptedEchoedUser)
    val echoedUserFinal = new ScalaObjectMapper().readValue(decryptedEchoedUser, classOf[EchoedUser])
//    val echoedUser2 = new ScalaObjectMapper().readTree(decryptedEchoedUser)
    println(echoedUserFinal)
}