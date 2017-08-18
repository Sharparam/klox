package com.sharparam.klox

class RuntimeError(val token: Token, message: String): RuntimeException(message)
