package com.mgneg.fakecall.presets

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.mgneg.fakecall.R
import com.mgneg.fakecall.data.db.Preset

/** Publishes the saved presets as launcher long-press (dynamic) shortcuts. */
object PresetShortcuts {

    fun refresh(context: Context, presets: List<Preset>) {
        val shortcuts = presets.take(5).map { preset ->
            val intent = Intent(context, PresetTriggerActivity::class.java)
                .setAction("com.mgneg.fakecall.action.PRESET_${preset.slot}")
                .putExtra(PresetTriggerActivity.EXTRA_SLOT, preset.slot)
            ShortcutInfoCompat.Builder(context, "preset_${preset.slot}")
                .setShortLabel(preset.name)
                .setLongLabel(preset.name)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_tile_phone))
                .setIntent(intent)
                .build()
        }
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
}
