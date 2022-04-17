package com.spotify.playit.video.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.spotify.playit.video.R

val fontFamily = FontFamily(
    Font(R.font.poppins_light, FontWeight.Normal),
//    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_semi_bold, FontWeight.Medium)
)


private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
//    body1 = defaultTypography.body1.copy(fontFamily = fonts)

//    body1 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp
//    ),
//    body2 = TextStyle(
//        fontFamily = fonts,
//        fontWeight = FontWeight.Light,
//        fontSize = 16.sp
//    ),
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)