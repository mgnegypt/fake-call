package com.mgneg.fakecall.presets

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.mgneg.fakecall.FakeCallApp
import com.mgneg.fakecall.R
import kotlinx.coroutines.launch

/** Invisible entry point used by launcher shortcuts and QS tiles. */
class PresetTriggerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val slot = intent.getIntExtra(EXTRA_SLOT, -1)
        val app = application as FakeCallApp
        app.appScope.launch {
            val preset = app.database.presetDao().bySlot(slot)
            if (preset != null) {
                app.callRepository.triggerCall(
                    preset.callerName, preset.callerNumber, preset.audioUri, preset.delaySec
                )
                val label = preset.callerName.ifBlank { preset.name }
                runOnUiThread {
                    Toast.makeText(
                        this@PresetTriggerActivity,
                        getString(R.string.preset_triggered, label),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_SLOT = "preset_slot"
    }
}
