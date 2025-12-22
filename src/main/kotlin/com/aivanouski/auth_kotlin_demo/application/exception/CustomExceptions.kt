package com.aivanouski.auth_kotlin_demo.application.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class EmailAlreadyExistsException(message: String) : RuntimeException(message)

class TokenExpiredException(message: String) : RuntimeException(message)

class InvalidTokenException(message: String) : RuntimeException(message)