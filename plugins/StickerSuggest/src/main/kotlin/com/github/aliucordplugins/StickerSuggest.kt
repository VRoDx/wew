package com.github.aliucordplugins

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.discord.stores.StoreStream
import com.discord.widgets.chat.input.WidgetChatInput

/**
 * StickerSuggest
 *
 * Watches the chat input box. When the user types "::" followed by at least
 * one character, it fetches stickers from all guilds the user is in whose
 * name contains that text, and shows a small horizontal suggestion strip
 * above the keyboard (like emoji autocomplete).
 *
 * Tapping a suggestion sends that sticker immediately to the channel.
 *
 * Trigger: "::<query>" (e.g. "::haha" suggests stickers with "haha" in the name)
 */
@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class StickerSuggest : Plugin() {

    companion object {
        private const val TRIGGER = "::"
        private const val MAX_SUGGESTIONS = 10
    }

    private var suggestPopup: PopupWindow? = null

    override fun start(context: Context) {
        patcher.after<WidgetChatInput>(
            "onViewBound",
            View::class.java,
        ) { _ ->
            val inputEditText: EditText = this.`chatInputEditText` ?: return@after

            inputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: run {
                        hideSuggestions()
                        return
                    }
                    val idx = text.lastIndexOf(TRIGGER)
                    if (idx < 0) {
                        hideSuggestions()
                        return
                    }
                    val query = text.substring(idx + TRIGGER.length).trimStart()
                    if (query.isEmpty()) {
                        hideSuggestions()
                        return
                    }
                    showSuggestions(inputEditText, query, s)
                }
            })
        }
    }

    private fun showSuggestions(anchor: EditText, query: String, editable: Editable) {
        val context = anchor.context
        val stickers = findStickers(query)
        if (stickers.isEmpty()) {
            hideSuggestions()
            return
        }

        // Dismiss any previous popup
        suggestPopup?.dismiss()

        // Build a horizontal strip with sticker previews
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF2B2D31.toInt())
        }

        val dp8 = DimenUtils.dpToPx(8)
        val dp64 = DimenUtils.dpToPx(64)

        for (sticker in stickers.take(MAX_SUGGESTIONS)) {
            val itemView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp8, dp8, dp8, dp8)
                gravity = android.view.Gravity.CENTER
            }

            val stickerImage = ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(dp64, dp64)
                scaleType = ImageView.ScaleType.FIT_CENTER
                // Load sticker image via Aliucord's image loader
                com.aliucord.utils.GifUtils.setImage(
                    this,
                    getStickerUrl(sticker),
                )
            }

            val nameLabel = TextView(context).apply {
                text = sticker.name
                setTextColor(0xFFDCDEE1.toInt())
                textSize = 10f
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            itemView.addView(stickerImage)
            itemView.addView(nameLabel)

            itemView.setOnClickListener {
                sendSticker(sticker, anchor)
                hideSuggestions()
            }

            container.addView(itemView)
        }

        val dp80 = DimenUtils.dpToPx(80)

        val popup = PopupWindow(
            container,
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp80,
            false,
        )
        popup.isOutsideTouchable = false
        popup.showAsDropDown(anchor, 0, -dp80 * 2)
        suggestPopup = popup
    }

    private fun hideSuggestions() {
        suggestPopup?.dismiss()
        suggestPopup = null
    }

    private fun findStickers(query: String): List<com.discord.api.sticker.Sticker> {
        val result = mutableListOf<com.discord.api.sticker.Sticker>()
        val lowerQuery = query.lowercase()
        val guilds = StoreStream.getGuilds().guilds ?: return result

        for ((_, guild) in guilds) {
            val stickers = guild.stickers ?: continue
            for (sticker in stickers) {
                val name = sticker.name?.lowercase() ?: continue
                if (name.contains(lowerQuery)) {
                    result.add(sticker)
                }
                if (result.size >= MAX_SUGGESTIONS) return result
            }
        }
        return result
    }

    private fun getStickerUrl(sticker: com.discord.api.sticker.Sticker): String {
        val ext = when (sticker.formatType) {
            2 -> "apng"
            3 -> "lottie"
            else -> "png"
        }
        return "https://media.discordapp.net/stickers/${sticker.id}.$ext?size=128"
    }

    private fun sendSticker(
        sticker: com.discord.api.sticker.Sticker,
        anchor: EditText,
    ) {
        // Clear the :: query from the input
        val text = anchor.text?.toString() ?: return
        val idx = text.lastIndexOf(TRIGGER)
        if (idx >= 0) {
            anchor.setText(text.substring(0, idx))
            anchor.setSelection(anchor.text?.length ?: 0)
        }

        val channelId = StoreStream.getChannelsSelected().selectedChannelId
        if (channelId <= 0L) return

        // Send using Aliucord's message sender utility
        com.aliucord.utils.MiscUtils.sendStickerMessage(channelId, sticker.id)
    }

    override fun stop(context: Context) {
        hideSuggestions()
        patcher.unpatchAll()
    }
}
