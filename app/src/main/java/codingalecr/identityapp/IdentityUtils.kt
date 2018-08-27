package codingalecr.identityapp

import kotlin.experimental.xor

/** -*- coding: utf-8 -*-
 * This file was created by
 * @Author: aulate
 * @Date:   6/7/18
 */
object IdentityUtils {

    data class Person(
            var cedula: String? = null,
            var nombre: String? = null,
            var apellido1: String? = null,
            var apellido2: String? = null,
            var genero: Char = ' ',
            var fechaNacimiento: String? = null,
            var fechaVencimiento: String? = null
    ) {

        override fun toString(): String {
            return this.cedula + " " +
                    this.apellido1 + " " +
                    this.apellido2 + " " +
                    this.nombre + " " +
                    this.fechaNacimiento + " " +
                    this.fechaVencimiento
        }
    }

    private val keysArray = byteArrayOf(0x27.toByte(), 0x30.toByte(), 0x04.toByte(), 0xA0.toByte(), 0x00.toByte(), 0x0F.toByte(), 0x93.toByte(), 0x12.toByte(), 0xA0.toByte(), 0xD1.toByte(), 0x33.toByte(), 0xE0.toByte(), 0x03.toByte(), 0xD0.toByte(), 0x00.toByte(), 0xDf.toByte(), 0x00.toByte())

    fun parse(raw: ByteArray): Person? {
        var d = ""
        var j = 0
        for (i in raw.indices) {
            if (j == 17) {
                j = 0
            }
            val c = (keysArray[j] xor raw[i]).toChar()
            d += if ((c + "").matches("^[a-zA-Z0-9]*$".toRegex())) {
                c
            } else {
                ' '.toString()
            }
            j++
        }
        var p: Person? = Person()
        try {
            p?.run {
                cedula = d.substring(0, 9).trim { it <= ' ' }
                apellido1 = d.substring(9, 35).trim { it <= ' ' }
                apellido2 = d.substring(35, 61).trim { it <= ' ' }
                nombre = d.substring(61, 91).trim { it <= ' ' }
                genero = d[91]
                fechaNacimiento = d.substring(92, 96) + "-" + d.substring(96, 98) + "-" + d.substring(98, 100)
                fechaVencimiento = d.substring(100, 104) + "-" + d.substring(104, 106) + "-" + d.substring(106, 108)
            }
        } catch (e: Exception) {
            p = null
        }

        return p
    }
}