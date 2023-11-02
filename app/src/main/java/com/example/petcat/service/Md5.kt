package com.example.petcat.service

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class Md5 {
    companion object {
        fun md5(s: String): String {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(s.toByteArray(StandardCharsets.UTF_8))
            val byteArray = messageDigest.digest()
            val md5StrBuff = StringBuffer()
            for (i in byteArray.indices) {
                if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                }
            }
            return md5StrBuff.toString()
        }
    }
}