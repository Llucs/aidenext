/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.aidenext.app;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import com.blankj.utilcode.util.ThrowableUtils;
import com.aidenext.buildinfo.BuildInfo;
import com.aidenext.common.R;
import com.aidenext.managers.PreferenceManager;
import com.aidenext.managers.ToolsManager;
import com.aidenext.utils.Environment;
import com.aidenext.utils.FileUtil;
import com.aidenext.utils.FlashbarUtilsKt;
import com.aidenext.utils.JavaCharacter;
import com.aidenext.utils.VMUtils;
import java.io.File;

public class BaseApplication extends Application {

  public static final String NOTIFICATION_GRADLE_BUILD_SERVICE = "17571";
  public static final String TELEGRAM_GROUP_URL = "https://t.me/androidide_discussions";
  public static final String TELEGRAM_CHANNEL_URL = "https://t.me/AndroidIDEOfficial";
  public static final String SPONSOR_URL = BuildInfo.PROJECT_SITE + "/donate";
  public static final String DOCS_URL = "https://docs.androidide.com";
  public static final String CONTRIBUTOR_GUIDE_URL =
      BuildInfo.REPO_URL + "/blob/dev/CONTRIBUTING.md";
  public static final String EMAIL = "contact@androidide.com";
  private static BaseApplication instance;
  private PreferenceManager mPrefsManager;

  public static BaseApplication getBaseInstance() {
    return instance;
  }

  @Override
  public void onCreate() {
    instance = this;
    try {
      Environment.init(this);
    } catch (Exception e) {
      android.util.Log.e("BaseApplication", "Environment.init failed", e);
    }
    super.onCreate();

    try {
      mPrefsManager = new PreferenceManager(this);
    } catch (Exception e) {
      android.util.Log.e("BaseApplication", "PreferenceManager init failed", e);
    }

    try {
      JavaCharacter.initMap();
    } catch (Exception e) {
      android.util.Log.e("BaseApplication", "JavaCharacter.initMap failed", e);
    }

    if (!VMUtils.isJvm()) {
      try {
        ToolsManager.init(this, null);
      } catch (Exception e) {
        android.util.Log.e("BaseApplication", "ToolsManager.init failed", e);
      }
    }
  }

  public void writeException(Throwable th) {
    FileUtil.writeFile(new File(FileUtil.getExternalStorageDir(), "idelog.txt").getAbsolutePath(),
        ThrowableUtils.getFullStackTrace(th));
  }

  public PreferenceManager getPrefManager() {
    return mPrefsManager;
  }

  public File getProjectsDir() {
    return Environment.PROJECTS_DIR;
  }

  public void openTelegramGroup() {
    openTelegram(BaseApplication.TELEGRAM_GROUP_URL);
  }

  public void openTelegramChannel() {
    openTelegram(BaseApplication.TELEGRAM_CHANNEL_URL);
  }

  public void openGitHub() {
    openUrl(BuildInfo.REPO_URL);
  }

  public void openWebsite() {
    openUrl(BuildInfo.PROJECT_SITE);
  }

  public void openDonationsPage() {
    openUrl(SPONSOR_URL);
  }

  public void openDocs() {
    openUrl(DOCS_URL);
  }

  public void emailUs() {
    openUrl("mailto:" + EMAIL);
  }

  public void openUrl(String url) {
    openUrl(url, null);
  }

  public void openTelegram(String url) {
    openUrl(url, "org.telegram.messenger");
  }

  public void openUrl(String url, String pkg) {
    try {
      Intent open = new Intent();
      open.setAction(Intent.ACTION_VIEW);
      open.setData(Uri.parse(url));
      open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      if (pkg != null) {
        open.setPackage(pkg);
      }
      startActivity(open);
    } catch (Throwable th) {
      if (pkg != null) {
        openUrl(url);
      } else if (th instanceof ActivityNotFoundException) {
        FlashbarUtilsKt.flashError(R.string.msg_app_unavailable_for_intent);
      } else {
        FlashbarUtilsKt.flashError(th.getMessage());
      }
    }
  }
}
