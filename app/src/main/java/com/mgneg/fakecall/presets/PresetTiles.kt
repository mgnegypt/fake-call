package com.mgneg.fakecall.presets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mgneg.fakecall.FakeCallApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Base class for the five preset Quick Settings tiles. */
abstract class BasePresetTile : TileService() {

    abstract val slot: Int

    private var scope: CoroutineScope? = null

    override fun onStartListening() {
        super.onStartListening()
        val listeningScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope = listeningScope
        val app = application as FakeCallApp
        listeningScope.launch {
            val preset = withContext(Dispatchers.IO) { app.database.presetDao().bySlot(slot) }
            qsTile?.apply {
                label = preset?.name ?: getString(com.mgneg.fakecall.R.string.preset_generic_label)
                state = if (preset != null) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
                updateTile()
            }
        }
    }

    override fun onStopListening() {
        scope?.cancel()
        scope = null
        super.onStopListening()
    }

    // The Intent overload is used only below API 34, where it is the
    // correct (non-deprecated) form; 34+ uses the PendingIntent overload.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, PresetTriggerActivity::class.java)
            .putExtra(PresetTriggerActivity.EXTRA_SLOT, slot)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this, slot, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

class PresetTileService1 : BasePresetTile() { override val slot = 1 }
class PresetTileService2 : BasePresetTile() { override val slot = 2 }
class PresetTileService3 : BasePresetTile() { override val slot = 3 }
class PresetTileService4 : BasePresetTile() { override val slot = 4 }
class PresetTileService5 : BasePresetTile() { override val slot = 5 }
