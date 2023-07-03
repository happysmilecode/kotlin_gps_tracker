package com.singularity_code.live_location.util.other

import android.content.Context
import androidx.core.app.NotificationCompat
import com.singularity_code.live_location.util.enums.NetworkMethod
import com.singularity_code.live_location.util.pattern.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * In case you are using alien technologi such Xamarin/Unity, C++, or anything non java platform,
 * you can use this ALienPortal instance to interact with the LiveLocationService.
 */
class AlienPortal {

    // observable
    private var _status: String = "DEAD"
    fun getStatus() = _status

    private var _errorMessage: String? = null
    fun getErrorMessage() = _errorMessage

    private var _latitude: String? = null
    fun getLatitude(): String? = _latitude

    private var _longitude: String? = null
    fun getLongitude(): String? = _longitude

    private var _accuracy: String? = null
    fun getAccuracy(): String? = _accuracy

    private var _updatedTime: String? = null
    fun getUpdatedTime(): String? = _updatedTime

    // gps config
    private var gpsSamplingRate = 10000L

    // notification config
    private lateinit var context: Context
    private lateinit var channelId: String
    private lateinit var channelName: String
    private lateinit var channelDescription: String
    private lateinit var notificationTitle: String
    private lateinit var notificationMessage: String


    // network configuration
    private lateinit var apiOrSocketURL: String
    private lateinit var networkMethod: NetworkMethod
    private lateinit var messageDescriptor: String
    private var headers = hashMapOf<String, String>()

    private var liveLocationServiceInteractor: LiveLocationServiceInteractor? = null

    fun setGPSSamplingRate(samplingRate: Long) {
        gpsSamplingRate = samplingRate
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun setChannelId(id: String) {
        channelId = id
    }

    fun setChannelName(name: String) {
        channelName = name
    }

    fun setChannelDescription(description: String) {
        channelDescription = description
    }

    fun setupAPIorSocketURL(url: String) {
        apiOrSocketURL = url
    }

    fun setNotificationTitle(title: String) {
        notificationTitle = title
    }

    fun setNotificationMessage(message: String) {
        notificationMessage = message
    }

    // 0 -> RESTFUL
    // 1 -> Web Socket
    fun setupNetworkMethod(methodEnumIndex: Int) {
        networkMethod = NetworkMethod.values()[methodEnumIndex]
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    fun clearHeader() {
        headers.clear()
    }

    fun setMessageDescriptor(descriptor: String) {
        messageDescriptor = descriptor
    }

    fun start() {
        liveLocationServiceInteractor = object : LiveLocationServiceInteractorAbs() {
            override val context: Context = this@AlienPortal.context

            override val gpsConfig: GPSConfig =
                object : GPSConfig {
                    override val samplingRate: Long = 10000
                }

            override val notificationConfig: NotificationConfig =
                object : NotificationConfig {
                    override val foregroundServiceID: Int = 1003
                    override val notificationChannelID: String = this@AlienPortal.channelId
                    override val notificationChannelName: String = this@AlienPortal.channelName
                    override val notificationChannelDescription: String =
                        this@AlienPortal.channelDescription
                    override val notificationPriority: Int = NotificationCompat.PRIORITY_DEFAULT
                    override val iconRes: Int? = null
                }

            override val networkConfiguration: LiveLocationNetworkConfiguration =
                object : LiveLocationNetworkConfiguration {
                    override val url: String = this@AlienPortal.apiOrSocketURL
                    override val networkMethod: NetworkMethod = this@AlienPortal.networkMethod
                    override val headers: HashMap<String, String> = this@AlienPortal.headers
                    override val messageDescriptor: String by lazy {
                        this@AlienPortal.messageDescriptor
                    }
                }

            private val coroutine by lazy {
                CoroutineScope(Dispatchers.IO + SupervisorJob())
            }

            override fun onServiceStatusChanged(
                serviceStatus: LiveLocationServiceInteractor.ServiceStatus
            ) {
                coroutine.launch {
                    this@AlienPortal._status = serviceStatus.name
                }
            }

            override fun onError(
                message: String?
            ) {
                coroutine.launch {
                    this@AlienPortal._errorMessage = message
                }
            }

            override fun onReceiveUpdate(
                latitude: Double,
                longitude: Double,
                accuracy: Float,
                updateTime: Long
            ) {
                coroutine.launch {
                    this@AlienPortal._latitude = latitude.toString()
                    this@AlienPortal._longitude = longitude.toString()
                    this@AlienPortal._accuracy = accuracy.toString()
                    this@AlienPortal._updatedTime = updateTime.toString()
                }
            }

        }

        liveLocationServiceInteractor?.startService(
            this@AlienPortal.notificationTitle,
            this@AlienPortal.notificationMessage
        )
    }

    fun stop() {
        liveLocationServiceInteractor?.stopService()
        liveLocationServiceInteractor = null
    }
}