package com.chintu.ai.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

class AppLauncherTool(
    private val context: Context
) {

    fun findApp(
        name: String
    ): String? =

        context.packageManager
            .getInstalledApplications(
                PackageManager.GET_META_DATA
            )
            .firstOrNull {

                it.loadLabel(
                    context.packageManager
                )
                    .toString()
                    .equals(
                        name,
                        true
                    ) ||

                it.packageName.equals(
                    name,
                    true
                )
            }
            ?.packageName

    fun launch(
        name: String
    ): Result<String> =

        runCatching {

            val pkg =
                findApp(name)
                    ?: error(
                        "$name installed nathi."
                    )

            val i =
                context.packageManager
                    .getLaunchIntentForPackage(
                        pkg
                    )
                    ?: error(
                        "$name launchable nathi."
                    )

            context.startActivity(
                i.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )

            "✓ Opened $name"
        }
}

class AndroidIntentTools(
    private val context: Context
) {

    fun openBrowser(
        url: String
    ) {

        context.startActivity(

            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
        )
    }

    fun openSettings() {

        context.startActivity(

            Intent(
                Settings.ACTION_SETTINGS
            )
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
        )
    }

    fun openAlarm() {

        context.startActivity(

            Intent(
                android.provider.AlarmClock
                    .ACTION_SET_ALARM
            )
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
        )
    }
}
