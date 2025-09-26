package com.malmungchi.feature.friend

import androidx.annotation.DrawableRes
import com.malmungchi.feature.friend.R

@DrawableRes
fun mapAvatarNameToRes(name: String?): Int? = when (name) {
    "img_malchi" -> R.drawable.img_malchi
    "img_mungchi" -> R.drawable.img_mungchi
    "img_glass_malchi"   -> R.drawable.img_glass_malchi
    "img_glass_mungchi"   -> R.drawable.img_glass_mungchi
    // TODO: 실제 리소스에 맞춰 확장
    else -> null
}