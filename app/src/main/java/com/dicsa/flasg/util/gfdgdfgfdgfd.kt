package com.dicsa.flasg.util

import android.content.Context

fun Context.getId(name: String, resourceType: String) = resources.getIdentifier(name, resourceType, packageName)

fun Context.getDrawableId(name: String) = getId(name, "drawable")