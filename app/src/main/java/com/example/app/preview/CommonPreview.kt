package com.example.app.preview

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)


// 🔥 Covers 99% devices
//@Preview(name = "Small Phone", device = Devices.NEXUS_ONE, showBackground = true)
@Preview(name = "Normal Phone", device = Devices.PIXEL_4, showBackground = true)
@Preview(name = "Large Phone", device = Devices.PIXEL_7_PRO, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class AllDevicesPreview