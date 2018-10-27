package codingalecr.identityapp

import kotlin.experimental.xor

/** -*- coding: utf-8 -*-
 * This file was created by
 * @Author: aulate
 * @Date:   6/7/18
 */
object IdentityUtils {

    data class Persona(
            var id: String? = null,
            var name: String? = null,
            var lastname1: String? = null,
            var lastname2: String? = null,
            var gender: Char = ' ',
            var birthdate: String? = null,
            var expirationdate: String? = null
    ) {
        override fun toString(): String {
            return this.id + " " +
                    this.lastname1 + " " +
                    this.lastname2 + " " +
                    this.name + " " +
                    this.birthdate + " " +
                    this.expirationdate
        }

        fun getLastname(): String {
            return this.lastname1 + " " + this.lastname2
        }
    }

    private val keysArray = byteArrayOf(0x27.toByte(), 0x30.toByte(), 0x04.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x0F.toByte(), 0x93.toByte(), 0x12.toByte(), 0xA0.toByte(), 0xD1.toByte(), 0x33.toByte(), 0xE0.toByte(), 0x03.toByte(), 0xD0.toByte(), 0x00.toByte(), 0xDf.toByte(), 0x00.toByte())

    fun parse(raw: ByteArray): Persona? {
        var d = ""
        var j = 0
        for (i in raw.indices) {
            if (j == 17) {
                j = 0
            }
            val c = (keysArray[j] xor raw[i]).toChar()
            if ((c + "").matches("^[a-zA-Z0-9]*$".toRegex())) {
                d += c
            } else {
                d += ' '.toString()
            }
            j++
        }
        var p: Persona? = Persona()
        try {
            p?.run {
                id = d.substring(0, 9).trim { it <= ' ' }
                lastname1 = d.substring(9, 35).trim { it <= ' ' }
                lastname2 = d.substring(35, 61).trim { it <= ' ' }
                name = d.substring(61, 91).trim { it <= ' ' }
                gender = d[91]
                birthdate = d.substring(92, 96) + "-" + d.substring(96, 98) + "-" + d.substring(98, 100)
                expirationdate = d.substring(100, 104) + "-" + d.substring(104, 106) + "-" + d.substring(106, 108)
            }
        } catch (e: Exception) {
            p = null
        }

        return p
    }
}