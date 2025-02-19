package com.example.bukbot.data.database.Dao

import com.google.common.collect.ImmutableList
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Document
class ApprovedUsers(
        @Id
        val chatId: String,
        var isEnabledProperty: Boolean,
        val passwordProperty: String,
        val usernameProperty: String,
        val isCredentialsNonExpiredProperty: Boolean,
        val isAccountNonExpiredProperty: Boolean,
        val isAccountNonLockedProperty: Boolean,
        val userLastName: String
): UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return ImmutableList.of(Roles.USER)
    }
    override fun isEnabled(): Boolean = isEnabledProperty
    override fun getUsername(): String = chatId
    override fun isCredentialsNonExpired(): Boolean = isCredentialsNonExpiredProperty
    override fun getPassword(): String = passwordProperty
    override fun isAccountNonExpired(): Boolean = isAccountNonExpiredProperty
    override fun isAccountNonLocked(): Boolean = isAccountNonLockedProperty
    fun getLastName(): String = usernameProperty
    fun getFirstName(): String = userLastName
}