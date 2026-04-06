package com.orchestradashboard.shared.ui.commandcenter

object ShellCommandConstants {
    val dangerousPatterns = listOf("rm -rf", "drop ", "shutdown", "reboot", "mkfs", "dd if=")
}
