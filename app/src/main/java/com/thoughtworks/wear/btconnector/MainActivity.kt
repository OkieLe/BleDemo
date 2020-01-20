package com.thoughtworks.wear.btconnector

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.Toast
import com.thoughtworks.wear.btconnector.bt.GattServerManager
import com.thoughtworks.wear.btconnector.utils.BTConstants
import com.thoughtworks.wear.btconnector.utils.RIGHT
import io.github.boopited.droidbt.PeripheralManager
import io.github.boopited.droidbt.common.BluetoothUtils
import io.github.boopited.droidbt.common.BluetoothUtils.REQUEST_ENABLE_BT

class MainActivity : WearableActivity() {

    private var gattServer: GattServerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        if (!BluetoothUtils.hasPermissions(this)) {
            requestPermissions(BluetoothUtils.permissionsToAsk(this), BluetoothUtils.REQUEST_PERMISSION)
        } else {
            checkBluetoothState()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                makeDeviceVisible()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun makeDeviceVisible() {
        gattServer = GattServerManager(this)
        gattServer?.start()
        findViewById<View>(R.id.gesture_detector).setOnClickListener {
            val data = Bundle().apply {
                putInt(BTConstants.EXTRA_DATA, RIGHT)
                putLong(BTConstants.EXTRA_TIME_STAMP, System.currentTimeMillis())
            }
            gattServer?.notifyGesture(BTConstants.CHARACTERISTIC_GESTURE, data)
        }
    }

    private fun checkBluetoothState() {
        if (!BluetoothUtils.isBluetoothEnabled()) {
            BluetoothUtils.openBluetooth(this, REQUEST_ENABLE_BT)
        } else {
            makeDeviceVisible()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BluetoothUtils.REQUEST_PERMISSION) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this,
                    R.string.missing_permission_revoked_by_user, Toast.LENGTH_LONG).show()
                finish()
            } else {
                checkBluetoothState()
            }
        }
    }

    override fun onDestroy() {
        gattServer?.stop()
        super.onDestroy()
    }
}
