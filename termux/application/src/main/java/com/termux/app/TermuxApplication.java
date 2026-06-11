package com.termux.app;

import android.content.Context;
import com.aidenext.app.BaseApplication;
import com.termux.BuildConfig;
import com.termux.shared.errors.Error;
import com.termux.shared.logger.Logger;
import com.termux.shared.termux.TermuxBootstrap;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.crash.TermuxCrashUtils;
import com.termux.shared.termux.file.TermuxFileUtils;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import com.termux.shared.termux.settings.properties.TermuxAppSharedProperties;
import com.termux.shared.termux.shell.TermuxShellManager;
import com.termux.shared.termux.shell.am.TermuxAmSocketServer;
import com.termux.shared.termux.shell.command.environment.TermuxShellEnvironment;
import com.termux.shared.termux.theme.TermuxThemeUtils;

public class TermuxApplication extends BaseApplication {

    private static final String LOG_TAG = "TermuxApplication";

    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        try {
            // Set crash handler for the app
            TermuxCrashUtils.setDefaultCrashHandler(this);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to set crash handler: " + e.getMessage());
        }

        try {
            // Set log config for the app
            setLogConfig(context);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to set log config: " + e.getMessage());
        }

        Logger.logDebug("Starting Application");

        try {
            // Set TermuxBootstrap.TERMUX_APP_PACKAGE_MANAGER and TermuxBootstrap.TERMUX_APP_PACKAGE_VARIANT
            TermuxBootstrap.setTermuxPackageManagerAndVariant(BuildConfig.TERMUX_PACKAGE_VARIANT);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to set package manager and variant: " + e.getMessage());
        }

        try {
            // Init app wide SharedProperties loaded from termux.properties
            TermuxAppSharedProperties.init(context);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to init shared properties: " + e.getMessage());
        }

        try {
            // Init app wide shell manager
            TermuxShellManager.init(context);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to init shell manager: " + e.getMessage());
        }

        try {
            // Defer heavy environment initialization to avoid ANR/crashes on newer Android
            Logger.logInfo(LOG_TAG, "Deferring environment setup to background thread");
            deferEnvironmentSetup(context);
        } catch (Exception e) {
            Logger.logError(LOG_TAG, "Failed to defer environment setup: " + e.getMessage());
        }
    }

    private void deferEnvironmentSetup(final Context context) {
        new Thread(() -> {
            try {
                TermuxAppSharedProperties properties = TermuxAppSharedProperties.getProperties();
                if (properties != null) {
                    TermuxThemeUtils.setAppNightMode(properties.getNightMode());
                }
            } catch (Exception e) {
                Logger.logError(LOG_TAG, "Failed to set night mode: " + e.getMessage());
            }

            try {
                // Check and create termux files directory. If failed to access it like in case of secondary
                // user or external sd card installation, then don't run files directory related code
                Error error = TermuxFileUtils.isTermuxFilesDirectoryAccessible(TermuxApplication.this, true, true);
                if (error == null) {
                    Logger.logInfo(LOG_TAG, "Termux files directory is accessible");

                    try {
                        error = TermuxFileUtils.isAppsTermuxAppDirectoryAccessible(true, true);
                        if (error != null) {
                            Logger.logErrorExtended(LOG_TAG, "Create apps/termux-app directory failed\n" + error);
                            return;
                        }
                    } catch (Exception e) {
                        Logger.logError(LOG_TAG, "Failed to create apps directory: " + e.getMessage());
                        return;
                    }

                    try {
                        // Setup termux-am-socket server
                        TermuxAmSocketServer.setupTermuxAmSocketServer(context);
                    } catch (Exception e) {
                        Logger.logError(LOG_TAG, "Failed to setup AM socket server: " + e.getMessage());
                    }
                } else {
                    Logger.logWarn(LOG_TAG, "Termux files directory is not accessible\n" + error);
                }
            } catch (Exception e) {
                Logger.logError(LOG_TAG, "Failed to check termux files directory accessibility: " + e.getMessage());
            }

            try {
                // Init TermuxShellEnvironment constants and caches after everything has been setup
                TermuxShellEnvironment.init(TermuxApplication.this);
            } catch (Exception e) {
                Logger.logError(LOG_TAG, "Failed to init shell environment: " + e.getMessage());
            }

            try {
                TermuxShellEnvironment.writeEnvironmentToFile(TermuxApplication.this);
            } catch (Exception e) {
                Logger.logError(LOG_TAG, "Failed to write environment to file: " + e.getMessage());
            }
        }).start();
    }

    public static void setLogConfig(Context context) {
        Logger.setDefaultLogTag(TermuxConstants.TERMUX_APP_NAME);

        // Load the log level from shared preferences and set it to the {@link Logger.CURRENT_LOG_LEVEL}
        TermuxAppSharedPreferences preferences = TermuxAppSharedPreferences.build(context);
        if (preferences == null) return;
        preferences.setLogLevel(null, preferences.getLogLevel());
    }

}
