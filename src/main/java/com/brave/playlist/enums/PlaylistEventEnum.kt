package com.brave.playlist.enums

enum class PlaylistEventEnum {
    kNone,
    kItemAdded,             // a new playlist item added but not ready state
    kItemThumbnailReady,    // Thumbnail ready to use for playlist
    kItemThumbnailFailed,   // Failed to fetch thumbnail
    kItemCached,            // The item is cached in local storage
    kItemDeleted,           // An item deleted
    kItemUpdated,           // An item's properties have been changed
    kItemMoved,             // An item moved
    kItemAborted,           // Aborted during the creation process
    kItemLocalDataRemoved,  // Local data removed

    kListCreated,  // A list is created
    kListRemoved,  // A list is removed
    kAllDeleted,   // All playlist are deleted
    kUpdated,
}