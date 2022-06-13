package com.github.paylike.kotlin_request.exceptions

/**
 * Describes an exception that happens when the version is invalid
 */
class VersionException(version: Int) : Exception("Version: $version has to be higher than 0.")
