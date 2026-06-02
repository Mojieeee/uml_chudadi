package com.example.uml_chudadi.transport

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

private const val SERVICE_NAME = "ChudadiRoom"
private const val BT_LOG = "ChudadiBt"
private val SERVICE_UUID: UUID = UUID.fromString("3d61b2f0-f63b-4f89-9f2f-9f56df442488")

fun requiredBluetoothPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}

fun hasBluetoothPermissions(context: Context): Boolean {
    return requiredBluetoothPermissions().all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@SuppressLint("MissingPermission")
fun bondedBluetoothDevices(context: Context): List<BluetoothDeviceInfo> {
    val adapter = bluetoothAdapter(context) ?: return emptyList()
    return adapter.bondedDevices.map { BluetoothDeviceInfo(it.name ?: "未知设备", it.address) }
}

data class BluetoothDeviceInfo(val name: String, val address: String)

private class BluetoothDiscoveryHandle(private val stopAction: () -> Unit) : AutoCloseable {
    override fun close() = stopAction()
}

@SuppressLint("MissingPermission")
fun bluetoothDiscoverableIntent(durationSeconds: Int = 300): Intent {
    return Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationSeconds)
    }
}

private fun bluetoothAdapter(context: Context): BluetoothAdapter? {
    return context.getSystemService(BluetoothManager::class.java)?.adapter
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
fun discoverBluetoothDevices(
    context: Context,
    onDevice: (BluetoothDeviceInfo) -> Unit,
    onStatus: (String) -> Unit
): AutoCloseable {
    val adapter = bluetoothAdapter(context)
    if (adapter == null) {
        onStatus("当前设备不支持蓝牙")
        return BluetoothDiscoveryHandle {}
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    } ?: return
                    val name = runCatching { device.name }.getOrNull().orEmpty().ifBlank { "附近设备" }
                    onDevice(BluetoothDeviceInfo(name, device.address))
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onStatus("搜索完成，选择好友加入房间")
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        context.registerReceiver(receiver, filter)
    }

    adapter.cancelDiscovery()
    val started = adapter.startDiscovery()
    if (!started) onStatus("搜索未启动，请确认蓝牙已开启")

    var closed = false
    return BluetoothDiscoveryHandle {
        if (!closed) {
            closed = true
            runCatching { adapter.cancelDiscovery() }
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
}

abstract class SocketGameTransport : GameTransport {
    private val sockets = CopyOnWriteArrayList<BluetoothSocket>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val sendLock = Any()
    private var listener: ((String) -> Unit)? = null

    override fun observe(listener: (String) -> Unit) {
        this.listener = listener
    }

    protected fun addSocket(socket: BluetoothSocket) {
        sockets += socket
        Log.d(BT_LOG, "socket added: ${socket.safeRemoteLabel()}, total=${sockets.size}")
        thread(name = "bt-read-${socket.hashCode()}", isDaemon = true) {
            runCatching {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                while (true) {
                    val line = reader.readLine() ?: break
                    Log.d(BT_LOG, "read ${socket.safeRemoteLabel()}: ${line.take(120)}")
                    emit(line)
                }
                Log.d(BT_LOG, "socket closed by remote: ${socket.safeRemoteLabel()}")
                sockets.remove(socket)
            }.onFailure {
                Log.e(BT_LOG, "read failed ${socket.safeRemoteLabel()}", it)
                sockets.remove(socket)
                emit(GameMessageCodec.encode(GameMessage.Error(it.message ?: "蓝牙读取失败")))
            }
        }
    }

    protected fun emit(message: String) {
        mainHandler.post { listener?.invoke(message) }
    }

    override fun send(message: String) {
        Log.d(BT_LOG, "broadcast to ${sockets.size}: ${message.take(120)}")
        sockets.forEach { socket ->
            runCatching {
                synchronized(sendLock) {
                    val writer = BufferedWriter(OutputStreamWriter(socket.outputStream))
                    writer.write(message)
                    writer.newLine()
                    writer.flush()
                }
            }.onFailure {
                Log.e(BT_LOG, "send failed ${socket.safeRemoteLabel()}", it)
                sockets.remove(socket)
                runCatching { socket.close() }
                emit(GameMessageCodec.encode(GameMessage.Error(it.message ?: "蓝牙发送失败")))
            }
        }
    }

    override fun close() {
        Log.d(BT_LOG, "close transport, sockets=${sockets.size}")
        sockets.forEach { runCatching { it.close() } }
        sockets.clear()
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothSocket.safeRemoteLabel(): String {
        return runCatching {
            val device = remoteDevice
            "${device.name ?: "unknown"}(${device.address})"
        }.getOrElse { "unknown" }
    }
}

class BluetoothHostTransport(context: Context) : SocketGameTransport() {
    private val appContext = context.applicationContext
    private var serverSocket: BluetoothServerSocket? = null

    @SuppressLint("MissingPermission")
    override fun start(role: TransportRole) {
        val adapter = bluetoothAdapter(appContext) ?: return
        thread(name = "bt-host", isDaemon = true) {
            runCatching {
                adapter.cancelDiscovery()
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                Log.d(BT_LOG, "host listening")
                repeat(3) {
                    val socket = serverSocket?.accept() ?: return@thread
                    Log.d(BT_LOG, "host accepted ${it + 1}/3")
                    addSocket(socket)
                    emit(GameMessageCodec.encode(GameMessage.State("好友已连接")))
                }
            }.onFailure { emit(GameMessageCodec.encode(GameMessage.Error(it.message ?: "创建房间失败"))) }
        }
    }

    override fun close() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        super.close()
    }
}

class BluetoothClientTransport(context: Context, private val deviceAddress: String) : SocketGameTransport() {
    private val appContext = context.applicationContext

    @SuppressLint("MissingPermission")
    override fun start(role: TransportRole) {
        val adapter = bluetoothAdapter(appContext) ?: return
        val device = adapter.getRemoteDevice(deviceAddress)
        thread(name = "bt-client", isDaemon = true) {
            runCatching {
                val socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                adapter.cancelDiscovery()
                Log.d(BT_LOG, "client connecting to $deviceAddress")
                socket.connect()
                addSocket(socket)
                emit(GameMessageCodec.encode(GameMessage.State("已加入房间")))
                send(
                    GameMessageCodec.encode(
                        GameMessage.Hello(
                            playerName = (role as? TransportRole.Client)?.playerName ?: "好友",
                            ruleName = "默认规则"
                        )
                    )
                )
            }.onFailure { emit(GameMessageCodec.encode(GameMessage.Error(it.message ?: "加入房间失败"))) }
        }
    }
}
