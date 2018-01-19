package me.snoty.mobile.server.connection

/**
 * Created by Stefan on 19.01.2018.
 */
enum class ConnectionError {
    NO_SERVER_SET,
    FINGERPRINT_NO_MATCH,
    CONNECTION_CLOSED,
    CONNECTION_REFUSED
}