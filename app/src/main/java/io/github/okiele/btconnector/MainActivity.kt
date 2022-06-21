package io.github.okiele.btconnector

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.okiele.btconnector.R
import io.github.okiele.btconnector.bt.GattServerManager
import io.github.okiele.btconnector.utils.BTConstants
import io.github.okiele.btconnector.utils.RIGHT
import io.github.boopited.droidbt.common.BluetoothUtils

class MainActivity : AppCompatActivity() {

    private var gattServer: GattServerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!BluetoothUtils.hasPermissions(this)) {
            requestPermissions(
                BluetoothUtils.permissionsToAsk(this),
                BluetoothUtils.REQUEST_PERMISSION
            )
        } else {
            checkBluetoothState()
        }
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
        if (!BluetoothUtils.isBluetoothEnabled(this)) {
            BluetoothUtils.openBluetooth(this) { makeDeviceVisible() }
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
                Toast.makeText(
                    this,
                    R.string.missing_permission_revoked_by_user, Toast.LENGTH_LONG
                ).show()
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
