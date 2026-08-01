package com.onurkolofficial.spsgame.ui.localization

import android.content.Context
import java.util.Locale
import android.content.res.Configuration

object LocaleHelper {
    fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }
}

fun String.toAppUppercase(): String {
    val locale = Locale.getDefault()
    val finalLocale = if (locale.language == "tr") Locale("tr", "TR") else locale
    return this.uppercase(finalLocale)
}
