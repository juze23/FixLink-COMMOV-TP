package com.example.fixlink.ui.filters

object FilterConstants {
    // Ownership
    const val OWNERSHIP_ALL = "All"
    const val OWNERSHIP_MY = "My"

    // Priority
    const val PRIORITY_LOW = "Low"
    const val PRIORITY_MEDIUM = "Medium"
    const val PRIORITY_HIGH = "High"

    // State
    const val STATE_PENDING = "Pending"
    const val STATE_ASSIGNED = "Assigned"
    const val STATE_UNDER_REPAIR = "Under Repair"
    const val STATE_RESOLVED = "Resolved"
    const val STATE_ONGOING = "Ongoing"
    const val STATE_COMPLETED = "Completed"

    // Equipment Status
    const val EQUIPMENT_ACTIVE = "Active"
    const val EQUIPMENT_INACTIVE = "Inactive"

    // Helper functions to get localized strings
    fun getLocalizedOwnership(ownership: String): String {
        return when (ownership) {
            OWNERSHIP_ALL -> "text_all_issues"
            OWNERSHIP_MY -> "text_my_issues"
            else -> ""
        }
    }

    fun getLocalizedPriority(priority: String): String {
        return when (priority) {
            PRIORITY_LOW -> "text_priority_low"
            PRIORITY_MEDIUM -> "text_priority_medium"
            PRIORITY_HIGH -> "text_priority_high"
            else -> ""
        }
    }

    fun getLocalizedState(state: String): String {
        return when (state) {
            STATE_PENDING -> "text_state_pending"
            STATE_ASSIGNED -> "text_state_assigned"
            STATE_UNDER_REPAIR -> "text_state_under_repair"
            STATE_RESOLVED -> "text_state_resolved"
            STATE_ONGOING -> "text_state_ongoing"
            STATE_COMPLETED -> "text_state_completed"
            else -> ""
        }
    }

    fun getLocalizedEquipmentStatus(status: String): String {
        return when (status) {
            EQUIPMENT_ACTIVE -> "text_status_active"
            EQUIPMENT_INACTIVE -> "text_status_inactive"
            else -> ""
        }
    }
} 