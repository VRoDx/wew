package com.github.aliucordplugins

import android.content.Context
import android.view.View
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.MiscUtils
import com.discord.stores.StoreStream
import com.discord.widgets.stickers.WidgetStickerSheetSticker

/**
 * LongPressFavorite
 *
 * Adds a long-press handler to every sticker item in the sticker picker panel.
 * When the user long-presses a sticker:
 *   1. It is added to the Discord "favorite stickers" list.
 *   2. The UI scrolls / moves the sticker to the top of the favourites row
 *      (Discord re-renders the sticker grid after the store is mutated).
 *   3. A brief toast confirms the action.
 *
 * How it works:
 *   We patch WidgetStickerSheetSticker.onViewBound() (the individual sticker
 *   cell in the sheet) to attach a LongClickListener that calls
 *   StoreStream.getStickers().toggleFavoriteSticker(), which is the same
 *   internal API Discord uses when you long-press a sticker from the profile
 *   reaction picker.
 */
@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class LongPressFavorite : Plugin() {

    override fun start(context: Context) {
        patcher.after<WidgetStickerSheetSticker>(
            "onViewBound",
            View::class.java,
        ) { _ ->
            val sticker = this.sticker ?: return@after
            val itemView: View = this.itemView

            itemView.setOnLongClickListener {
                val stickersStore = StoreStream.getStickers()

                // Toggle favourite — if already favourite, remove; if not, add
                val currentFavs = stickersStore.favoriteStickers ?: emptySet()
                val stickerId = sticker.id

                if (currentFavs.contains(stickerId)) {
                    stickersStore.removeFavoriteSticker(stickerId)
                    MiscUtils.showToast("Removed from favourites")
                } else {
                    stickersStore.addFavoriteSticker(stickerId)
                    MiscUtils.showToast("Added to favourites ⭐")
                }
                true
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
