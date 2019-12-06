package com.thoughtworks.wear.btconnector

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.*

object BTConstants {

    val SERVICE_CHAT: UUID = UUID.fromString("703ecd36-825e-4d0f-b200-ae901ff8decb")
    val CHARACTERISTIC_GESTURE: UUID = UUID.fromString("a8089ebe-8d56-4b04-8896-b925c3bf7c18")
    val CONTENT_NOTIFY: UUID = UUID.fromString("385aa50f-81a4-4fc2-a663-d5cc22d72c84")

    const val ACTION_GATT_CONNECTED = "com.thoughtworks.hmi.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.thoughtworks.hmi.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.thoughtworks.hmi.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_DATA_AVAILABLE = "com.thoughtworks.hmi.ACTION_DATA_AVAILABLE"

    const val EXTRA_DATA = "com.thoughtworks.hmi.EXTRA_DATA"
    const val EXTRA_TIME_STAMP = "com.thoughtworks.hmi.EXTRA_TIME"

    const val ACTION_MESSAGE_SENT = "com.thoughtworks.hmi.ACTION_MESSAGE_SENT"

    fun createChatService(): BluetoothGattService {
        val service = BluetoothGattService(
            SERVICE_CHAT,
            BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val message = BluetoothGattCharacteristic(
            CHARACTERISTIC_GESTURE,
            //Read-only characteristic, supports notifications
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ)
        val content = BluetoothGattDescriptor(
            CONTENT_NOTIFY,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        message.addDescriptor(content)
        service.addCharacteristic(message)

        return service
    }

    fun wrapMessage(gesture: Gesture): ByteArray {
        return byteArrayOf(gesture.type.toByte())
            .plus(longToUInt32ByteArray(gesture.date / 1000))
    }

    private fun longToUInt32ByteArray(value: Long): ByteArray {
        val bytes = ByteArray(4)
        bytes[0] = (value and 0xFF).toByte()
        bytes[1] = (value shr 8 and 0xFF).toByte()
        bytes[2] = (value shr 16 and 0xFF).toByte()
        bytes[3] = (value shr 24 and 0xFF).toByte()
        return bytes
    }
}