package com.mediaflow.ai.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.mediaflow.ai.R
import com.mediaflow.ai.domain.usecase.RunAiOrganizationUseCase
import com.mediaflow.ai.domain.usecase.SyncMediaStoreUseCase
import com.mediaflow.ai.notification.MediaNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaObserverService : Service() {

    @Inject
    lateinit var syncMediaStoreUseCase: SyncMediaStoreUseCase

    @Inject
    lateinit var runAiOrganizationUseCase: RunAiOrganizationUseCase

    @Inject
    lateinit var notificationManager: MediaNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mediaObserver: ContentObserver? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1001, createServiceNotification())
        registerMediaObserver()
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, MediaNotificationManager.CHANNEL_ID)
            .setContentTitle("MediaFlow AI Active")
            .setContentText("Monitoring media library for real-time organization")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun registerMediaObserver() {
        val handler = Handler(Looper.getMainLooper())
        mediaObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                serviceScope.launch {
                    val synced = syncMediaStoreUseCase()
                    if (synced > 0) {
                        val processed = runAiOrganizationUseCase.runBatchClassification()
                        if (processed > 0) {
                            notificationManager.showOrganizedNotification(
                                "$processed new photos organized",
                                "Sorted with AI classification & smart albums"
                            )
                        }
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver!!
        )
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver!!
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaObserver?.let { contentResolver.unregisterContentObserver(it) }
        serviceScope.cancel()
    }
}
