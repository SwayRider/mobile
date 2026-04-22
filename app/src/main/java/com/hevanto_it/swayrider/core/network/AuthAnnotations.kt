package com.hevanto_it.swayrider.core.network

/**
 * An annotation to mark a Retrofit API method as requiring authentication.
 *
 * When a function is annotated with `@AuthRequired`, the [AuthInterceptor] will automatically
 * attach the user's JSON Web Token (JWT) to the request's `Authorization` header.
 *
 * This provides a declarative way to specify which endpoints are protected.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthRequired
