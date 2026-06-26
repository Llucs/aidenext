package com.aidenext.db

import io.realm.Realm
import io.realm.RealmConfiguration

interface IRealmProvider {
  fun get(name: String, configurationBlock: RealmConfiguration.Builder.() -> Unit = {}): Realm

  companion object {
    const val PATH_SEPARATOR: Char = '/'

    fun instance(): IRealmProvider = DefaultRealmProvider()
  }
}

private class DefaultRealmProvider : IRealmProvider {
  override fun get(
    name: String,
    configurationBlock: RealmConfiguration.Builder.() -> Unit,
  ): Realm {
    val config = RealmConfiguration.Builder()
      .name(name)
      .apply(configurationBlock)
      .build()
    return Realm.getInstance(config)
  }
}
