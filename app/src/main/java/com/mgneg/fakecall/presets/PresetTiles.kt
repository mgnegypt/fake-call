package com.mgneg.fakecall.presets

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Base class for the five preset Quick Settings tiles. */
abstract class BasePresetTile : TileService() {

    abstract val slot: Int

    override fun onStartListening() {
        super.onStartListening()
        val app = application as FakeCallApp
        val preset = runBlocking { app.database.presetDao().bySlot(slot) }
        qsTile?.apply {
            label = preset?.name ?: getString(com.mgneg.fakecall.R.string.preset_generic_label)
            state = if (preset != null) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, PresetTriggerActivity::class.java)
            .putExtra(PresetTriggerActivity.EXTRA_SLOT, slot)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }
}

class PresetTileService1 : BasePresetTile() { override val slot = 1 }
class PresetTileService2 : BasePresetTile() { override val slot = 2 }
class PresetTileService3 : BasePresetTile() { override val slot = 3 }
class PresetTileService4 : BasePresetTile() { override val slot = 4 }
class PresetTileService5 : BasePresetTile() { override val slot = 5 }
