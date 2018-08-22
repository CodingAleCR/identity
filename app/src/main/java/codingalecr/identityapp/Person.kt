package codingalecr.identityapp

/** -*- coding: utf-8 -*-
 * This file was created by
 * @Author: aulate
 * @Date:   6/7/18
 */
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