package com.example.bukbot.config

import com.example.bukbot.data.database.persistance.approvedusers.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.NoOpPasswordEncoder






@Configuration
@EnableWebSecurity
class WebSecurityConfig: WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var userService: UserService

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .authorizeRequests()
                .antMatchers(
                        "/login",
                        "/whait_auth/**",
                        "/assets/**"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .and()
                .logout()
                .permitAll()
    }

    @Bean
    fun passwordEncoder(): NoOpPasswordEncoder {
        return NoOpPasswordEncoder.getInstance() as NoOpPasswordEncoder
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder){
        auth.userDetailsService(userService)
    }
}