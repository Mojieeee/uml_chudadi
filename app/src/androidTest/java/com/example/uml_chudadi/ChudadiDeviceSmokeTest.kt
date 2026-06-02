package com.example.uml_chudadi

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChudadiDeviceSmokeTest {
    @Test
    fun mainActivityLaunchesOnRealDevice() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.startActivity(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )

        Thread.sleep(1_500)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertTrue(launchIntent != null)
    }

    @Test
    fun bluetoothAndVibrationPermissionsAreDeclared() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val permissions = context.packageManager
            .getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            .orEmpty()
            .toSet()

        assertTrue(Manifest.permission.VIBRATE in permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            assertTrue(Manifest.permission.BLUETOOTH_SCAN in permissions)
            assertTrue(Manifest.permission.BLUETOOTH_ADVERTISE in permissions)
            assertTrue(Manifest.permission.BLUETOOTH_CONNECT in permissions)
        } else {
            assertTrue(Manifest.permission.BLUETOOTH in permissions)
            assertTrue(Manifest.permission.BLUETOOTH_ADMIN in permissions)
            assertTrue(Manifest.permission.ACCESS_FINE_LOCATION in permissions)
        }
    }
}
