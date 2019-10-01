package com.example.bukbot.data.database.Dao

import org.springframework.security.core.GrantedAuthority

enum class Roles: GrantedAuthority {
    USER;

    override fun getAuthority(): String {
        return name
    }
}