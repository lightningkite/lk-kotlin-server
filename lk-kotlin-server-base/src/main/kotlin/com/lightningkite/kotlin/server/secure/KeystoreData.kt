package com.lightningkite.kotlin.server.secure


//class KeystoreData(
//        var keystoreLocation:String,
//        var keystorePassword:CharArray,
//        var keystoreAlias:String,
//        var keystoreAliasPassword:CharArray,
//        var truststoreLocation:String?,
//        var truststorePassword:CharArray?
//){
//    val keystore by lazy{
//        KeyStore.getInstance(File(keystoreLocation), keystorePassword)
//    }
//
//    val algorithm by lazy{
//        Algorithm.RSA512(keystore.getCertificate())
//    }
//}