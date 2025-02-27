package breez_sdk_liquid_notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import breez_sdk_liquid_notification.Constants.DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.DEFAULT_DISMISSIBLE_WORKGROUP_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DEFAULT_DISMISSIBLE_WORKGROUP_NAME
import breez_sdk_liquid_notification.Constants.DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_TITLE
import breez_sdk_liquid_notification.Constants.DEFAULT_NOTIFICATION_COLOR
import breez_sdk_liquid_notification.Constants.DEFAULT_REPLACEABLE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DEFAULT_REPLACEABLE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.DEFAULT_REPLACEABLE_WORKGROUP_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DEFAULT_REPLACEABLE_WORKGROUP_NAME
import breez_sdk_liquid_notification.Constants.DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DISMISSIBLE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.DISMISSIBLE_WORKGROUP_DESCRIPTION
import breez_sdk_liquid_notification.Constants.DISMISSIBLE_WORKGROUP_ID
import breez_sdk_liquid_notification.Constants.DISMISSIBLE_WORKGROUP_NAME
import breez_sdk_liquid_notification.Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.FOREGROUND_SERVICE_NOTIFICATION_TITLE
import breez_sdk_liquid_notification.Constants.NOTIFICATION_CHANNEL_DISMISSIBLE
import breez_sdk_liquid_notification.Constants.NOTIFICATION_CHANNEL_FOREGROUND_SERVICE
import breez_sdk_liquid_notification.Constants.NOTIFICATION_CHANNEL_REPLACEABLE
import breez_sdk_liquid_notification.Constants.NOTIFICATION_COLOR
import breez_sdk_liquid_notification.Constants.NOTIFICATION_ICON
import breez_sdk_liquid_notification.Constants.NOTIFICATION_ID_FOREGROUND_SERVICE
import breez_sdk_liquid_notification.Constants.NOTIFICATION_ID_REPLACEABLE
import breez_sdk_liquid_notification.Constants.REPLACEABLE_NOTIFICATION_CHANNEL_DESCRIPTION
import breez_sdk_liquid_notification.Constants.REPLACEABLE_NOTIFICATION_CHANNEL_NAME
import breez_sdk_liquid_notification.Constants.REPLACEABLE_WORKGROUP_DESCRIPTION
import breez_sdk_liquid_notification.Constants.REPLACEABLE_WORKGROUP_ID
import breez_sdk_liquid_notification.Constants.REPLACEABLE_WORKGROUP_NAME
import breez_sdk_liquid_notification.ResourceHelper.Companion.getColor
import breez_sdk_liquid_notification.ResourceHelper.Companion.getDrawable
import breez_sdk_liquid_notification.ResourceHelper.Companion.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("unused")
class NotificationHelper {
    companion object {
        private const val TAG = "NotificationHelper"
        private var defaultClickAction: String? = null

        fun getNotificationManager(context: Context): NotificationManager? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                if (notificationManager.areNotificationsEnabled()) {
                    return notificationManager
                }
            }
            return null
        }

        @SuppressLint("NewApi")
        fun createNotificationChannelGroup(
            context: Context,
            groupId: String,
            groupName: String,
            groupDescription: String,
        ) {
            getNotificationManager(context)?.also { manager ->
                val channelGroup =
                    NotificationChannelGroup(
                        groupId,
                        groupName,
                    )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    channelGroup.description = groupDescription
                }

                manager.createNotificationChannelGroup(channelGroup)
            }
        }

        @SuppressLint("NewApi")
        fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String,
            channelDescription: String,
            groupId: String,
            importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
        ) {
            getNotificationManager(context)?.also { manager ->
                val applicationId = context.applicationContext.packageName
                val notificationChannel =
                    NotificationChannel(
                        "$applicationId.$channelId",
                        channelName,
                        importance,
                    ).apply {
                        description = channelDescription
                        group = groupId
                    }

                manager.createNotificationChannel(notificationChannel)
            }
        }

        @SuppressLint("NewApi")
        fun registerNotificationChannels(
            context: Context,
            defaultClickAction: String? = null,
        ) {
            this.defaultClickAction = defaultClickAction

            getNotificationManager(context)?.also { manager ->
                createNotificationChannelGroups(context, manager)
                createNotificationChannels(context, manager)
                Log.d(TAG, "Registered notification channels")
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannels(
            context: Context,
            notificationManager: NotificationManager,
        ) {
            val applicationId = context.applicationContext.packageName
            val foregroundServiceNotificationChannel =
                NotificationChannel(
                    "$applicationId.${NOTIFICATION_CHANNEL_FOREGROUND_SERVICE}",
                    getString(
                        context,
                        FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME,
                        DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME,
                    ),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description =
                        getString(
                            context,
                            FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION,
                            DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION,
                        )
                }
            val replaceableNotificationChannel =
                NotificationChannel(
                    "$applicationId.${NOTIFICATION_CHANNEL_REPLACEABLE}",
                    getString(
                        context,
                        DISMISSIBLE_NOTIFICATION_CHANNEL_NAME,
                        DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_NAME,
                    ),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description =
                        getString(
                            context,
                            DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION,
                            DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION,
                        )
                    group = REPLACEABLE_WORKGROUP_ID
                }
            val dismissibleNotificationChannel =
                NotificationChannel(
                    "$applicationId.${NOTIFICATION_CHANNEL_DISMISSIBLE}",
                    getString(
                        context,
                        DISMISSIBLE_NOTIFICATION_CHANNEL_NAME,
                        DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_NAME,
                    ),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description =
                        getString(
                            context,
                            DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION,
                            DEFAULT_DISMISSIBLE_NOTIFICATION_CHANNEL_DESCRIPTION,
                        )
                    group = DISMISSIBLE_WORKGROUP_ID
                }
            notificationManager.createNotificationChannels(
                listOf(
                    foregroundServiceNotificationChannel,
                    replaceableNotificationChannel,
                    dismissibleNotificationChannel,
                ),
            )
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannelGroups(
            context: Context,
            notificationManager: NotificationManager,
        ) {
            val replaceableNotificationChannelGroup =
                NotificationChannelGroup(
                    REPLACEABLE_WORKGROUP_ID,
                    getString(
                        context,
                        REPLACEABLE_WORKGROUP_NAME,
                        DEFAULT_REPLACEABLE_WORKGROUP_NAME,
                    ),
                )
            val dismissibleNotificationChannelGroup =
                NotificationChannelGroup(
                    DISMISSIBLE_WORKGROUP_ID,
                    getString(
                        context,
                        DISMISSIBLE_WORKGROUP_NAME,
                        DEFAULT_DISMISSIBLE_WORKGROUP_NAME,
                    ),
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                replaceableNotificationChannelGroup.description =
                    getString(
                        context,
                        REPLACEABLE_WORKGROUP_DESCRIPTION,
                        DEFAULT_REPLACEABLE_WORKGROUP_DESCRIPTION,
                    )
                dismissibleNotificationChannelGroup.description =
                    getString(
                        context,
                        DISMISSIBLE_WORKGROUP_DESCRIPTION,
                        DEFAULT_DISMISSIBLE_WORKGROUP_DESCRIPTION,
                    )
            }

            notificationManager.createNotificationChannelGroups(
                listOf(
                    replaceableNotificationChannelGroup,
                    dismissibleNotificationChannelGroup,
                ),
            )
        }

        @SuppressLint("MissingPermission")
        fun notifyForegroundService(context: Context): Notification {
            val notificationColor =
                getColor(
                    context,
                    NOTIFICATION_COLOR,
                    DEFAULT_NOTIFICATION_COLOR,
                )

            return NotificationCompat
                .Builder(
                    context,
                    "${context.applicationInfo.packageName}.$NOTIFICATION_CHANNEL_FOREGROUND_SERVICE",
                ).apply {
                    setContentTitle(
                        getString(
                            context,
                            FOREGROUND_SERVICE_NOTIFICATION_TITLE,
                            DEFAULT_FOREGROUND_SERVICE_NOTIFICATION_TITLE,
                        ),
                    )
                    setSmallIcon(
                        getDrawable(
                            context,
                            NOTIFICATION_ICON,
                            android.R.drawable.sym_def_app_icon,
                        ),
                    )
                    setColorized(true)
                    setOngoing(true)
                    color = notificationColor
                }.build()
                .also {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        NotificationManagerCompat
                            .from(context)
                            .notify(NOTIFICATION_ID_FOREGROUND_SERVICE, it)
                    }
                }
        }

        @SuppressLint("NewApi")
        fun cancelNotification(
            context: Context,
            notificationID: Int,
        ) {
            getNotificationManager(context)?.also { manager ->
                manager.cancel(notificationID)
            }
        }

        @SuppressLint("MissingPermission")
        fun notifyChannel(
            context: Context,
            channelId: String,
            contentTitle: String,
            contentText: String? = null,
            clickAction: String? = defaultClickAction,
        ): Notification {
            val notificationID: Int =
                if (channelId ==
                    NOTIFICATION_CHANNEL_DISMISSIBLE
                ) {
                    System.currentTimeMillis().toInt() / 1000
                } else {
                    NOTIFICATION_ID_REPLACEABLE
                }
            val notificationColor =
                getColor(
                    context,
                    NOTIFICATION_COLOR,
                    DEFAULT_NOTIFICATION_COLOR,
                )

            val notificationIntent =
                context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
            notificationIntent.putExtra("click_action", clickAction)

            val flags =
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            val approvePendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    flags,
                )

            val buttonTitle = "Open"
            val notificationAction =
                NotificationCompat.Action
                    .Builder(
                        android.R.drawable.ic_delete,
                        buttonTitle,
                        approvePendingIntent,
                    ).build()

            val contentIntent =
                TaskStackBuilder.create(context).run {
                    addNextIntentWithParentStack(notificationIntent)
                    approvePendingIntent
                }

            // Cancel any current replaceable notification
            cancelNotification(context, NOTIFICATION_ID_REPLACEABLE)

            return NotificationCompat
                .Builder(
                    context,
                    "${context.applicationInfo.packageName}.$channelId",
                ).apply {
                    setContentTitle(contentTitle)
                    setContentText(contentText)
                    setSmallIcon(
                        getDrawable(
                            context,
                            NOTIFICATION_ICON,
                            android.R.drawable.sym_def_app_icon,
                        ),
                    )
                    setContentIntent(contentIntent)
                    addAction(notificationAction)
                    setLights(notificationColor, 1000, 300)
                    // Dismiss on click
                    setOngoing(false)
                    setAutoCancel(true)
                }.build()
                .also {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Required for notification to persist after work is complete
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(200)
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Use notificationID
                                NotificationManagerCompat
                                    .from(context)
                                    .notify(notificationID, it)
                            }
                        }
                    }
                }
        }
    }
}
