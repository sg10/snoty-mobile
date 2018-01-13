package me.snoty.mobile.server.protocol

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.snoty.mobile.notifications.Repository


/**
 * Created by Stefan on 06.01.2018.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = IgnorePackagePacket::class, name = "IgnorePackage"),
        JsonSubTypes.Type(value = NotificationOperationPacket::class, name = "NotificationOperation"),
        JsonSubTypes.Type(value = NotificationPostedPacket::class, name = "NotificationPosted"),
        JsonSubTypes.Type(value = NotificationRemovedPacket::class, name = "NotificationRemoved"))
abstract class NetworkPacket {

}