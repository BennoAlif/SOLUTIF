package com.sabeno.solutif.core.data.source

data class User(
    var id: String? = null,
    var email: String? = null,
    var name: String? = null,
    @field:JvmField
    var isPetugas: Boolean? = null,
)
