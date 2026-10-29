package com.example.e_wallet.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Self-contained vector icons for PocketPay.
 * Built with native Compose ImageVector to avoid any external icon dependencies.
 */
object PocketPayIcons {

    val ArrowUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4f)
                lineTo(5f, 11f)
                lineTo(6.41f, 12.41f)
                lineTo(11f, 7.83f)
                lineTo(11f, 20f)
                lineTo(13f, 20f)
                lineTo(13f, 7.83f)
                lineTo(17.59f, 12.41f)
                lineTo(19f, 11f)
                close()
            }
        }.build()
    }

    val ArrowDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowDown",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 20f)
                lineTo(19f, 13f)
                lineTo(17.59f, 11.59f)
                lineTo(13f, 16.17f)
                lineTo(13f, 4f)
                lineTo(11f, 4f)
                lineTo(11f, 16.17f)
                lineTo(6.41f, 11.59f)
                lineTo(5f, 13f)
                close()
            }
        }.build()
    }

    val Wallet: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 18f)
                curveTo(21f, 19.1f, 20.1f, 20f, 19f, 20f)
                lineTo(5f, 20f)
                curveTo(3.9f, 20f, 3f, 19.1f, 3f, 18f)
                lineTo(3f, 6f)
                curveTo(3f, 4.9f, 3.9f, 4f, 5f, 4f)
                lineTo(19f, 4f)
                curveTo(20.1f, 4f, 21f, 4.9f, 21f, 6f)
                lineTo(21f, 9f)
                lineTo(12f, 9f)
                curveTo(10.9f, 9f, 10f, 9.9f, 10f, 11f)
                lineTo(10f, 13f)
                curveTo(10f, 14.1f, 10.9f, 15f, 12f, 15f)
                lineTo(21f, 15f)
                lineTo(21f, 18f)
                close()
                moveTo(16f, 12f)
                curveTo(16f, 11.45f, 16.45f, 11f, 17f, 11f)
                curveTo(17.55f, 11f, 18f, 11.45f, 18f, 12f)
                curveTo(18f, 12.55f, 17.55f, 13f, 17f, 13f)
                curveTo(16.45f, 13f, 16f, 12.55f, 16f, 12f)
                close()
            }
        }.build()
    }

    val QrCode: ImageVector by lazy {
        ImageVector.Builder(
            name = "QrCode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 3f)
                lineTo(9f, 3f)
                lineTo(9f, 9f)
                lineTo(3f, 9f)
                close()
                moveTo(15f, 3f)
                lineTo(21f, 3f)
                lineTo(21f, 9f)
                lineTo(15f, 9f)
                close()
                moveTo(3f, 15f)
                lineTo(9f, 15f)
                lineTo(9f, 21f)
                lineTo(3f, 21f)
                close()
                moveTo(15f, 15f)
                lineTo(21f, 15f)
                lineTo(21f, 21f)
                lineTo(15f, 21f)
                close()
                moveTo(5f, 5f)
                lineTo(7f, 5f)
                lineTo(7f, 7f)
                lineTo(5f, 7f)
                close()
                moveTo(17f, 5f)
                lineTo(19f, 5f)
                lineTo(19f, 7f)
                lineTo(17f, 7f)
                close()
                moveTo(5f, 17f)
                lineTo(7f, 17f)
                lineTo(7f, 19f)
                lineTo(5f, 19f)
                close()
            }
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f)
                curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
                lineTo(1f, 12f)
                lineTo(4.89f, 15.89f)
                lineTo(5f, 16f)
                lineTo(9f, 12f)
                lineTo(6f, 12f)
                curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
                curveTo(16.87f, 5f, 20f, 8.13f, 20f, 12f)
                curveTo(20f, 15.87f, 16.87f, 19f, 13f, 19f)
                curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
                lineTo(6.64f, 18.36f)
                curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
                curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f)
                curveTo(22f, 7.03f, 17.97f, 3f, 13f, 3f)
                close()
                moveTo(12f, 8f)
                lineTo(12f, 13f)
                lineTo(16.2f, 15.5f)
                lineTo(17f, 14.2f)
                lineTo(13.5f, 12.1f)
                lineTo(13.5f, 8f)
                close()
            }
        }.build()
    }

    val Visibility: ImageVector by lazy {
        ImageVector.Builder(
            name = "Visibility",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
                curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
                moveTo(12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                close()
            }
        }.build()
    }

    val VisibilityOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "VisibilityOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 12.65f, 16.87f, 13.26f, 16.65f, 13.82f)
                lineTo(19.46f, 16.63f)
                curveTo(21.11f, 15.35f, 22.34f, 13.78f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                curveTo(10.59f, 4.5f, 9.25f, 4.79f, 8.01f, 5.31f)
                lineTo(10.18f, 7.48f)
                curveTo(10.74f, 7.18f, 11.35f, 7f, 12f, 7f)
                close()
                moveTo(2.71f, 3.86f)
                lineTo(4.12f, 2.45f)
                lineTo(21.55f, 19.88f)
                lineTo(20.14f, 21.29f)
                close()
            }
        }.build()
    }

    val CheckCircle: ImageVector by lazy {
        ImageVector.Builder(
            name = "CheckCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(10f, 17f)
                lineTo(5f, 12f)
                lineTo(6.41f, 10.59f)
                lineTo(10f, 14.17f)
                lineTo(17.59f, 6.58f)
                lineTo(19f, 8f)
                close()
            }
        }.build()
    }

    val Receipt: ImageVector by lazy {
        ImageVector.Builder(
            name = "Receipt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 17f)
                lineTo(6f, 17f)
                lineTo(6f, 15f)
                lineTo(18f, 15f)
                close()
                moveTo(18f, 13f)
                lineTo(6f, 13f)
                lineTo(6f, 11f)
                lineTo(18f, 11f)
                close()
                moveTo(18f, 9f)
                lineTo(6f, 9f)
                lineTo(6f, 7f)
                lineTo(18f, 7f)
                close()
                moveTo(3f, 22f)
                lineTo(4.5f, 20.5f)
                lineTo(6f, 22f)
                lineTo(7.5f, 20.5f)
                lineTo(9f, 22f)
                lineTo(10.5f, 20.5f)
                lineTo(12f, 22f)
                lineTo(13.5f, 20.5f)
                lineTo(15f, 22f)
                lineTo(16.5f, 20.5f)
                lineTo(18f, 22f)
                lineTo(19.5f, 20.5f)
                lineTo(21f, 22f)
                lineTo(21f, 2f)
                lineTo(19.5f, 3.5f)
                lineTo(18f, 2f)
                lineTo(16.5f, 3.5f)
                lineTo(15f, 2f)
                lineTo(13.5f, 3.5f)
                lineTo(12f, 2f)
                lineTo(10.5f, 3.5f)
                lineTo(9f, 2f)
                lineTo(7.5f, 3.5f)
                lineTo(6f, 2f)
                lineTo(4.5f, 3.5f)
                lineTo(3f, 2f)
                close()
            }
        }.build()
    }

    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 20f)
                lineTo(10f, 14f)
                lineTo(14f, 14f)
                lineTo(14f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 12f)
                lineTo(22f, 12f)
                lineTo(12f, 3f)
                lineTo(2f, 12f)
                lineTo(5f, 12f)
                lineTo(5f, 20f)
                close()
            }
        }.build()
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "Person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f)
                curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
                curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
                curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
                curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
                close()
                moveTo(12f, 14f)
                curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
                lineTo(4f, 20f)
                lineTo(20f, 20f)
                lineTo(20f, 18f)
                curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
                close()
            }
        }.build()
    }

    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                lineTo(13f, 13f)
                lineTo(13f, 19f)
                lineTo(11f, 19f)
                lineTo(11f, 13f)
                lineTo(5f, 13f)
                lineTo(5f, 11f)
                lineTo(11f, 11f)
                lineTo(11f, 5f)
                lineTo(13f, 5f)
                lineTo(13f, 11f)
                lineTo(19f, 11f)
                close()
            }
        }.build()
    }

    val Send: ImageVector by lazy {
        ImageVector.Builder(
            name = "Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.01f, 21f)
                lineTo(23f, 12f)
                lineTo(2.01f, 3f)
                lineTo(2f, 10f)
                lineTo(17f, 12f)
                lineTo(2f, 14f)
                close()
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(13f, 17f)
                lineTo(11f, 17f)
                lineTo(11f, 11f)
                lineTo(13f, 11f)
                close()
                moveTo(13f, 9f)
                lineTo(11f, 9f)
                lineTo(11f, 7f)
                lineTo(13f, 7f)
                close()
            }
        }.build()
    }

    val Star: ImageVector by lazy {
        ImageVector.Builder(
            name = "Star",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineTo(16.54f, 13.97f)
                lineTo(22f, 9.24f)
                lineTo(14.81f, 8.63f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.63f)
                lineTo(2f, 9.24f)
                lineTo(7.46f, 13.97f)
                lineTo(5.82f, 21f)
                close()
            }
        }.build()
    }

    val Notifications: ImageVector by lazy {
        ImageVector.Builder(
            name = "Notifications",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 22f)
                curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
                lineTo(10f, 20f)
                curveTo(10f, 21.1f, 10.9f, 22f, 12f, 22f)
                close()
                moveTo(18f, 16f)
                lineTo(18f, 11f)
                curveTo(18f, 7.93f, 16.37f, 5.36f, 13.5f, 4.68f)
                lineTo(13.5f, 4f)
                curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
                curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
                lineTo(10.5f, 4.68f)
                curveTo(7.64f, 5.36f, 6f, 7.92f, 6f, 11f)
                lineTo(6f, 16f)
                lineTo(4f, 18f)
                lineTo(4f, 19f)
                lineTo(20f, 19f)
                lineTo(20f, 18f)
                close()
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                lineTo(17.65f, 14f)
                curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
                lineTo(13f, 11f)
                lineTo(20f, 11f)
                lineTo(20f, 4f)
                close()
            }
        }.build()
    }

    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "Close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 6.41f)
                lineTo(17.59f, 5f)
                lineTo(12f, 10.59f)
                lineTo(6.41f, 5f)
                lineTo(5f, 6.41f)
                lineTo(10.59f, 12f)
                lineTo(5f, 17.59f)
                lineTo(6.41f, 19f)
                lineTo(12f, 13.41f)
                lineTo(17.59f, 19f)
                lineTo(19f, 17.59f)
                lineTo(13.41f, 12f)
                close()
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                lineTo(7.83f, 11f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                lineTo(20f, 13f)
                close()
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f)
                lineTo(14.71f, 14f)
                lineTo(14.43f, 13.73f)
                curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                curveTo(5.91f, 3f, 3f, 5.91f, 3f, 9.5f)
                curveTo(3f, 13.09f, 5.91f, 16f, 9.5f, 16f)
                curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
                lineTo(14f, 14.71f)
                lineTo(14f, 15.5f)
                lineTo(19f, 20.49f)
                lineTo(20.49f, 19f)
                lineTo(15.5f, 14f)
                close()
                moveTo(9.5f, 14f)
                curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
                curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
                curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 8f)
                lineTo(17f, 8f)
                lineTo(17f, 6f)
                curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
                curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
                lineTo(7f, 8f)
                lineTo(6f, 8f)
                curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
                lineTo(4f, 20f)
                curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                lineTo(18f, 22f)
                curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                lineTo(20f, 10f)
                curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
                close()
                moveTo(12f, 17f)
                curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
                curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
                curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
                curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
                close()
                moveTo(15.1f, 8f)
                lineTo(8.9f, 8f)
                lineTo(8.9f, 6f)
                curveTo(8.9f, 4.29f, 10.29f, 2.9f, 12f, 2.9f)
                curveTo(13.71f, 2.9f, 15.1f, 4.29f, 15.1f, 6f)
                lineTo(15.1f, 8f)
                close()
            }
        }.build()
    }

    val Backspace: ImageVector by lazy {
        ImageVector.Builder(
            name = "Backspace",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(22f, 3f)
                lineTo(7f, 3f)
                curveTo(6.31f, 3f, 5.7f, 3.43f, 5.42f, 4.07f)
                lineTo(0f, 12f)
                lineTo(5.42f, 19.93f)
                curveTo(5.7f, 20.57f, 6.31f, 21f, 7f, 21f)
                lineTo(22f, 21f)
                curveTo(23.1f, 21f, 24f, 20.1f, 24f, 19f)
                lineTo(24f, 5f)
                curveTo(24f, 3.9f, 23.1f, 3f, 22f, 3f)
                close()
                moveTo(19f, 15.59f)
                lineTo(17.59f, 17f)
                lineTo(14f, 13.41f)
                lineTo(10.41f, 17f)
                lineTo(9f, 15.59f)
                lineTo(12.59f, 12f)
                lineTo(9f, 8.41f)
                lineTo(10.41f, 7f)
                lineTo(14f, 10.59f)
                lineTo(17.59f, 7f)
                lineTo(19f, 8.41f)
                lineTo(15.41f, 12f)
                lineTo(19f, 15.59f)
                close()
            }
        }.build()
    }

    val FlashOn: ImageVector by lazy {
        ImageVector.Builder(
            name = "FlashOn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 2f)
                verticalLineTo(13f)
                horizontalLineTo(10f)
                verticalLineTo(22f)
                lineTo(17f, 10f)
                horizontalLineTo(13f)
                lineTo(17f, 2f)
                horizontalLineTo(7f)
                close()
            }
        }.build()
    }

    val FlashOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "FlashOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.27f, 3f)
                lineTo(2f, 4.27f)
                lineTo(7.73f, 10f)
                horizontalLineTo(7f)
                verticalLineTo(13f)
                horizontalLineTo(10f)
                verticalLineTo(18.27f)
                lineTo(13.73f, 16f)
                lineTo(19.73f, 22f)
                lineTo(21f, 20.73f)
                lineTo(3.27f, 3f)
                close()
                moveTo(17f, 10f)
                lineTo(14.09f, 4.54f)
                lineTo(17f, 2f)
                horizontalLineTo(7f)
                verticalLineTo(3.18f)
                lineTo(13.82f, 10f)
                horizontalLineTo(17f)
                close()
            }
        }.build()
    }

    val FlipCamera: ImageVector by lazy {
        ImageVector.Builder(
            name = "FlipCamera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 4f)
                horizontalLineTo(16.83f)
                lineTo(15f, 2f)
                horizontalLineTo(9f)
                lineTo(7.17f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                verticalLineTo(18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
                moveTo(12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                close()
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 6f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12f)
                lineTo(8.59f, 16.59f)
                lineTo(10f, 18f)
                lineTo(16f, 12f)
                close()
            }
        }.build()
    }

    val CreditCard: ImageVector by lazy {
        ImageVector.Builder(
            name = "CreditCard",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 4f)
                lineTo(4f, 4f)
                curveTo(2.89f, 4f, 2.01f, 4.89f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.89f, 20f, 4f, 20f)
                lineTo(20f, 20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                lineTo(22f, 6f)
                curveTo(22f, 4.89f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(20f, 18f)
                lineTo(4f, 18f)
                lineTo(4f, 12f)
                lineTo(20f, 12f)
                lineTo(20f, 18f)
                close()
                moveTo(20f, 8f)
                lineTo(4f, 8f)
                lineTo(4f, 6f)
                lineTo(20f, 6f)
                lineTo(20f, 8f)
                close()
            }
        }.build()
    }

    val Shield: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                lineTo(3f, 5f)
                lineTo(3f, 11f)
                curveTo(3f, 16.55f, 6.84f, 21.74f, 12f, 23f)
                curveTo(17.16f, 21.74f, 21f, 16.55f, 21f, 11f)
                lineTo(21f, 5f)
                lineTo(12f, 1f)
                close()
                moveTo(12f, 11.99f)
                lineTo(19f, 11.99f)
                curveTo(18.47f, 16.11f, 15.72f, 19.78f, 12f, 20.93f)
                lineTo(12f, 12f)
                lineTo(5f, 12f)
                curveTo(5f, 6.3f, 12f, 3.19f, 12f, 3.19f)
                lineTo(12f, 11.99f)
                close()
            }
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder(
            name = "Edit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17.25f)
                lineTo(3f, 21f)
                lineTo(6.75f, 21f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }.build()
    }

    val ContentCopy: ImageVector by lazy {
        ImageVector.Builder(
            name = "ContentCopy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f)
                lineTo(4f, 1f)
                curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
                lineTo(2f, 17f)
                lineTo(4f, 17f)
                lineTo(4f, 3f)
                lineTo(16f, 3f)
                lineTo(16f, 1f)
                close()
                moveTo(19f, 5f)
                lineTo(8f, 5f)
                curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
                lineTo(6f, 21f)
                curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
                lineTo(19f, 23f)
                curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
                lineTo(21f, 7f)
                curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(19f, 21f)
                lineTo(8f, 21f)
                lineTo(8f, 7f)
                lineTo(19f, 7f)
                lineTo(19f, 21f)
                close()
            }
        }.build()
    }

    val Logout: ImageVector by lazy {
        ImageVector.Builder(
            name = "Logout",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 7f)
                lineTo(15.59f, 8.41f)
                lineTo(17.17f, 10f)
                lineTo(9f, 10f)
                lineTo(9f, 12f)
                lineTo(17.17f, 12f)
                lineTo(15.59f, 13.59f)
                lineTo(17f, 15f)
                lineTo(21f, 11f)
                lineTo(17f, 7f)
                close()
                moveTo(5f, 5f)
                lineTo(12f, 5f)
                lineTo(12f, 3f)
                lineTo(5f, 3f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                lineTo(3f, 19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                lineTo(12f, 21f)
                lineTo(12f, 19f)
                lineTo(5f, 19f)
                lineTo(5f, 5f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.63f, 19.21f, 12.32f, 19.21f, 12f)
                curveTo(19.21f, 11.68f, 19.18f, 11.37f, 19.14f, 11.06f)
                lineTo(21.41f, 9.28f)
                curveTo(21.61f, 9.12f, 21.67f, 8.84f, 21.53f, 8.62f)
                lineTo(19.39f, 4.91f)
                curveTo(19.26f, 4.69f, 18.99f, 4.61f, 18.75f, 4.7f)
                lineTo(16.08f, 5.77f)
                curveTo(15.52f, 5.34f, 14.91f, 5f, 14.24f, 4.73f)
                lineTo(13.84f, 1.89f)
                curveTo(13.8f, 1.64f, 13.58f, 1.45f, 13.33f, 1.45f)
                lineTo(9.07f, 1.45f)
                curveTo(8.82f, 1.45f, 8.61f, 1.64f, 8.56f, 1.89f)
                lineTo(8.16f, 4.73f)
                curveTo(7.49f, 5f, 6.88f, 5.34f, 6.32f, 5.77f)
                lineTo(3.65f, 4.7f)
                curveTo(3.42f, 4.6f, 3.14f, 4.69f, 3.01f, 4.91f)
                lineTo(0.87f, 8.62f)
                curveTo(0.74f, 8.84f, 0.79f, 9.12f, 1f, 9.28f)
                lineTo(3.26f, 11.06f)
                curveTo(3.22f, 11.37f, 3.19f, 11.69f, 3.19f, 12f)
                curveTo(3.19f, 12.31f, 3.22f, 12.63f, 3.26f, 12.94f)
                lineTo(1f, 14.72f)
                curveTo(0.79f, 14.88f, 0.73f, 15.16f, 0.87f, 15.38f)
                lineTo(3.01f, 19.09f)
                curveTo(3.15f, 19.31f, 3.42f, 19.39f, 3.65f, 19.3f)
                lineTo(6.32f, 18.23f)
                curveTo(6.88f, 18.66f, 7.49f, 19f, 8.16f, 19.27f)
                lineTo(8.56f, 22.11f)
                curveTo(8.61f, 22.36f, 8.82f, 22.55f, 9.07f, 22.55f)
                lineTo(13.33f, 22.55f)
                curveTo(13.58f, 22.55f, 13.8f, 22.36f, 13.84f, 22.11f)
                lineTo(14.24f, 19.27f)
                curveTo(14.91f, 19f, 15.52f, 18.66f, 16.08f, 18.23f)
                lineTo(18.75f, 19.3f)
                curveTo(18.99f, 19.4f, 19.26f, 19.31f, 19.39f, 19.09f)
                lineTo(21.53f, 15.38f)
                curveTo(21.67f, 15.16f, 21.61f, 14.88f, 21.41f, 14.72f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(11.2f, 15.5f)
                curveTo(9.27f, 15.5f, 7.7f, 13.93f, 7.7f, 12f)
                curveTo(7.7f, 10.07f, 9.27f, 8.5f, 11.2f, 8.5f)
                curveTo(13.13f, 8.5f, 14.7f, 10.07f, 14.7f, 12f)
                curveTo(14.7f, 13.93f, 13.13f, 15.5f, 11.2f, 15.5f)
                close()
            }
        }.build()
    }

    val Water: ImageVector by lazy {
        ImageVector.Builder(
            name = "Water",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2.69f)
                lineTo(7.05f, 7.64f)
                curveTo(4.32f, 10.37f, 4.32f, 14.8f, 7.05f, 17.53f)
                curveTo(8.41f, 18.9f, 10.21f, 19.58f, 12f, 19.58f)
                curveTo(13.79f, 19.58f, 15.59f, 18.9f, 16.95f, 17.53f)
                curveTo(19.68f, 14.8f, 19.68f, 10.37f, 16.95f, 7.64f)
                lineTo(12f, 2.69f)
                close()
            }
        }.build()
    }

    val Wifi: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wifi",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.01f, 21.49f)
                lineTo(23.64f, 7f)
                curveTo(23.18f, 6.65f, 18.79f, 3.22f, 12.01f, 3.22f)
                curveTo(5.21f, 3.22f, 0.83f, 6.65f, 0.36f, 7f)
                lineTo(12.01f, 21.49f)
                close()
            }
        }.build()
    }

    val Smartphone: ImageVector by lazy {
        ImageVector.Builder(
            name = "Smartphone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 1.01f)
                lineTo(7f, 1f)
                curveTo(5.9f, 1f, 5f, 1.9f, 5f, 3f)
                lineTo(5f, 21f)
                curveTo(5f, 22.1f, 5.9f, 23f, 7f, 23f)
                lineTo(17f, 23f)
                curveTo(18.1f, 23f, 19f, 22.1f, 19f, 21f)
                lineTo(19f, 3f)
                curveTo(19f, 1.9f, 18.1f, 1.01f, 17f, 1.01f)
                close()
                moveTo(17f, 19f)
                lineTo(7f, 19f)
                lineTo(7f, 5f)
                lineTo(17f, 5f)
                lineTo(17f, 19f)
                close()
            }
        }.build()
    }

    val Tv: ImageVector by lazy {
        ImageVector.Builder(
            name = "Tv",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 3f)
                lineTo(3f, 3f)
                curveTo(1.9f, 3f, 1f, 3.9f, 1f, 5f)
                lineTo(1f, 17f)
                curveTo(1f, 18.1f, 1.9f, 19f, 3f, 19f)
                lineTo(8f, 19f)
                lineTo(8f, 21f)
                lineTo(16f, 21f)
                lineTo(16f, 19f)
                lineTo(21f, 19f)
                curveTo(22.1f, 19f, 23f, 18.1f, 23f, 17f)
                lineTo(23f, 5f)
                curveTo(23f, 3.9f, 22.1f, 3f, 21f, 3f)
                close()
                moveTo(21f, 17f)
                lineTo(3f, 17f)
                lineTo(3f, 5f)
                lineTo(21f, 5f)
                lineTo(21f, 17f)
                close()
            }
        }.build()
    }

    val School: ImageVector by lazy {
        ImageVector.Builder(
            name = "School",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 13.18f)
                lineTo(5f, 17.18f)
                lineTo(12f, 21f)
                lineTo(19f, 17.18f)
                lineTo(19f, 13.18f)
                lineTo(12f, 17f)
                lineTo(5f, 13.18f)
                close()
                moveTo(12f, 3f)
                lineTo(1f, 9f)
                lineTo(12f, 15f)
                lineTo(21f, 10.09f)
                lineTo(21f, 17f)
                lineTo(23f, 17f)
                lineTo(23f, 9f)
                lineTo(12f, 3f)
                close()
            }
        }.build()
    }

    val AccountBalance: ImageVector by lazy {
        ImageVector.Builder(
            name = "AccountBalance",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 10f)
                lineTo(6.5f, 10f)
                lineTo(6.5f, 17f)
                lineTo(4f, 17f)
                close()
                moveTo(10.75f, 10f)
                lineTo(13.25f, 10f)
                lineTo(13.25f, 17f)
                lineTo(10.75f, 17f)
                close()
                moveTo(2f, 22f)
                lineTo(22f, 22f)
                lineTo(22f, 19f)
                lineTo(2f, 19f)
                close()
                moveTo(17.5f, 10f)
                lineTo(20f, 10f)
                lineTo(20f, 17f)
                lineTo(17.5f, 17f)
                close()
                moveTo(12f, 1f)
                lineTo(2f, 6f)
                lineTo(2f, 8f)
                lineTo(22f, 8f)
                lineTo(22f, 6f)
                close()
            }
        }.build()
    }

    val ReceiptLong: ImageVector by lazy {
        ImageVector.Builder(
            name = "ReceiptLong",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.5f, 3.5f)
                lineTo(18f, 2f)
                lineTo(16.5f, 3.5f)
                lineTo(15f, 2f)
                lineTo(13.5f, 3.5f)
                lineTo(12f, 2f)
                lineTo(10.5f, 3.5f)
                lineTo(9f, 2f)
                lineTo(7.5f, 3.5f)
                lineTo(6f, 2f)
                lineTo(4.5f, 3.5f)
                lineTo(3f, 2f)
                lineTo(3f, 22f)
                lineTo(4.5f, 20.5f)
                lineTo(6f, 22f)
                lineTo(7.5f, 20.5f)
                lineTo(9f, 22f)
                lineTo(10.5f, 20.5f)
                lineTo(12f, 22f)
                lineTo(13.5f, 20.5f)
                lineTo(15f, 22f)
                lineTo(16.5f, 20.5f)
                lineTo(18f, 22f)
                lineTo(19.5f, 20.5f)
                lineTo(21f, 22f)
                lineTo(21f, 2f)
                lineTo(19.5f, 3.5f)
                close()
                moveTo(15f, 7f)
                lineTo(9f, 7f)
                lineTo(9f, 5f)
                lineTo(15f, 5f)
                lineTo(15f, 7f)
                close()
                moveTo(15f, 11f)
                lineTo(9f, 11f)
                lineTo(9f, 9f)
                lineTo(15f, 9f)
                lineTo(15f, 11f)
                close()
                moveTo(15f, 15f)
                lineTo(9f, 15f)
                lineTo(9f, 13f)
                lineTo(15f, 13f)
                lineTo(15f, 15f)
                close()
            }
        }.build()
    }

    val Email: ImageVector by lazy {
        ImageVector.Builder(
            name = "Email",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 4f)
                lineTo(4f, 4f)
                curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                lineTo(20f, 20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                lineTo(22f, 6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(20f, 8f)
                lineTo(12f, 13f)
                lineTo(4f, 8f)
                lineTo(4f, 6f)
                lineTo(12f, 11f)
                lineTo(20f, 6f)
                lineTo(20f, 8f)
                close()
            }
        }.build()
    }

    val Phone: ImageVector by lazy {
        ImageVector.Builder(
            name = "Phone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.62f, 10.79f)
                curveTo(8.06f, 13.62f, 10.38f, 15.93f, 13.21f, 17.38f)
                lineTo(15.41f, 15.18f)
                curveTo(15.68f, 14.91f, 16.08f, 14.82f, 16.43f, 14.94f)
                curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20f, 15.5f)
                curveTo(20.55f, 15.5f, 21f, 15.95f, 21f, 16.5f)
                lineTo(21f, 20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
                curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
                lineTo(7.5f, 3f)
                curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
                curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.06f, 7.57f)
                curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
                lineTo(6.62f, 10.79f)
                close()
            }
        }.build()
    }

    val Badge: ImageVector by lazy {
        ImageVector.Builder(
            name = "Badge",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 7f)
                lineTo(15.5f, 7f)
                curveTo(15.5f, 5.07f, 13.93f, 3.5f, 12f, 3.5f)
                curveTo(10.07f, 3.5f, 8.5f, 5.07f, 8.5f, 7f)
                lineTo(4f, 7f)
                curveTo(2.9f, 7f, 2f, 7.9f, 2f, 9f)
                lineTo(2f, 20f)
                curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
                lineTo(20f, 22f)
                curveTo(21.1f, 22f, 22f, 21.1f, 22f, 20f)
                lineTo(22f, 9f)
                curveTo(22f, 7.9f, 21.1f, 7f, 20f, 7f)
                close()
                moveTo(12f, 5.5f)
                curveTo(12.83f, 5.5f, 13.5f, 6.17f, 13.5f, 7f)
                curveTo(13.5f, 7.83f, 12.83f, 8.5f, 12f, 8.5f)
                curveTo(11.17f, 8.5f, 10.5f, 7.83f, 10.5f, 7f)
                curveTo(10.5f, 6.17f, 11.17f, 5.5f, 12f, 5.5f)
                close()
                moveTo(12f, 11f)
                curveTo(13.66f, 11f, 15f, 12.34f, 15f, 14f)
                curveTo(15f, 15.66f, 13.66f, 17f, 12f, 17f)
                curveTo(10.34f, 17f, 9f, 15.66f, 9f, 14f)
                curveTo(9f, 12.34f, 10.34f, 11f, 12f, 11f)
                close()
                moveTo(18f, 20f)
                lineTo(6f, 20f)
                lineTo(6f, 18.93f)
                curveTo(6f, 17.31f, 8.69f, 16f, 12f, 16f)
                curveTo(15.31f, 16f, 18f, 17.31f, 18f, 18.93f)
                lineTo(18f, 20f)
                close()
            }
        }.build()
    }

    val Fingerprint: ImageVector by lazy {
        ImageVector.Builder(
            name = "Fingerprint",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.81f, 4.47f)
                curveTo(16.14f, 3.23f, 14.14f, 2.5f, 12f, 2.5f)
                curveTo(9.86f, 2.5f, 7.86f, 3.23f, 6.19f, 4.47f)
                lineTo(7.33f, 6.01f)
                curveTo(8.68f, 5.01f, 10.28f, 4.5f, 12f, 4.5f)
                curveTo(13.72f, 4.5f, 15.32f, 5.01f, 16.67f, 6.01f)
                lineTo(17.81f, 4.47f)
                close()
                moveTo(12f, 6.5f)
                curveTo(8.41f, 6.5f, 5.5f, 9.41f, 5.5f, 13f)
                curveTo(5.5f, 14.73f, 6.17f, 16.31f, 7.27f, 17.48f)
                lineTo(8.69f, 16.06f)
                curveTo(7.96f, 15.28f, 7.5f, 14.19f, 7.5f, 13f)
                curveTo(7.5f, 10.51f, 9.51f, 8.5f, 12f, 8.5f)
                curveTo(14.49f, 8.5f, 16.5f, 10.51f, 16.5f, 13f)
                curveTo(16.5f, 15.54f, 15.26f, 17.78f, 13.38f, 19.14f)
                lineTo(14.54f, 20.76f)
                curveTo(16.89f, 19.06f, 18.5f, 16.22f, 18.5f, 13f)
                curveTo(18.5f, 9.41f, 15.59f, 6.5f, 12f, 6.5f)
                close()
                moveTo(12f, 10.5f)
                curveTo(10.62f, 10.5f, 9.5f, 11.62f, 9.5f, 13f)
                curveTo(9.5f, 14.88f, 10.56f, 16.51f, 12.12f, 17.33f)
                lineTo(13.06f, 15.57f)
                curveTo(12.12f, 15.07f, 11.5f, 14.11f, 11.5f, 13f)
                curveTo(11.5f, 12.72f, 11.72f, 12.5f, 12f, 12.5f)
                curveTo(12.28f, 12.5f, 12.5f, 12.72f, 12.5f, 13f)
                curveTo(12.5f, 14.85f, 13.88f, 16.4f, 15.71f, 16.71f)
                lineTo(16.05f, 14.74f)
                curveTo(15.15f, 14.59f, 14.5f, 13.84f, 14.5f, 13f)
                curveTo(14.5f, 11.62f, 13.38f, 10.5f, 12f, 10.5f)
                close()
            }
        }.build()
    }

    val Savings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Savings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.83f, 7.5f)
                lineTo(18.17f, 5.84f)
                curveTo(17.78f, 5.45f, 17.25f, 5.25f, 16.71f, 5.29f)
                lineTo(15f, 5.42f)
                curveTo(14.07f, 4.54f, 12.87f, 4f, 11.53f, 4f)
                curveTo(8.47f, 4f, 6f, 6.47f, 6f, 9.53f)
                curveTo(6f, 9.87f, 6.03f, 10.21f, 6.09f, 10.53f)
                lineTo(4f, 12.63f)
                lineTo(4f, 16f)
                lineTo(6f, 16f)
                lineTo(6f, 19f)
                lineTo(8f, 19f)
                lineTo(8f, 16f)
                lineTo(14f, 16f)
                lineTo(14f, 19f)
                lineTo(16f, 19f)
                lineTo(16f, 16.89f)
                curveTo(18.36f, 15.74f, 20f, 13.27f, 20f, 10.42f)
                curveTo(20f, 9.4f, 19.94f, 8.44f, 19.83f, 7.5f)
                close()
                moveTo(13f, 9f)
                lineTo(9f, 9f)
                curveTo(8.45f, 9f, 8f, 8.55f, 8f, 8f)
                curveTo(8f, 7.45f, 8.45f, 7f, 9f, 7f)
                lineTo(13f, 7f)
                curveTo(13.55f, 7f, 14f, 7.45f, 14f, 8f)
                curveTo(14f, 8.55f, 13.55f, 9f, 13f, 9f)
                close()
            }
        }.build()
    }

    val Flight: ImageVector by lazy {
        ImageVector.Builder(
            name = "Flight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 16f)
                lineTo(21f, 14f)
                lineTo(13f, 9f)
                lineTo(13f, 3.5f)
                curveTo(13f, 2.67f, 12.33f, 2f, 11.5f, 2f)
                curveTo(10.67f, 2f, 10f, 2.67f, 10f, 3.5f)
                lineTo(10f, 9f)
                lineTo(2f, 14f)
                lineTo(2f, 16f)
                lineTo(10f, 13.5f)
                lineTo(10f, 19f)
                lineTo(8f, 20.5f)
                lineTo(8f, 22f)
                lineTo(11.5f, 21f)
                lineTo(15f, 22f)
                lineTo(15f, 20.5f)
                lineTo(13f, 19f)
                lineTo(13f, 13.5f)
                lineTo(21f, 16f)
                close()
            }
        }.build()
    }

    val DirectionsCar: ImageVector by lazy {
        ImageVector.Builder(
            name = "DirectionsCar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5f, 17.5f, 5f)
                lineTo(6.5f, 5f)
                curveTo(5.84f, 5f, 5.28f, 5.42f, 5.08f, 6.01f)
                lineTo(3f, 12f)
                lineTo(3f, 20f)
                curveTo(3f, 20.55f, 3.45f, 21f, 4f, 21f)
                lineTo(5f, 21f)
                curveTo(5.55f, 21f, 6f, 20.55f, 6f, 20f)
                lineTo(6f, 19f)
                lineTo(18f, 19f)
                lineTo(18f, 20f)
                curveTo(18f, 20.55f, 18.45f, 21f, 19f, 21f)
                lineTo(20f, 21f)
                curveTo(20.55f, 21f, 21f, 20.55f, 21f, 20f)
                lineTo(21f, 12f)
                lineTo(18.92f, 6.01f)
                close()
                moveTo(6.5f, 16f)
                curveTo(5.67f, 16f, 5f, 15.33f, 5f, 14.5f)
                curveTo(5f, 13.67f, 5.67f, 13f, 6.5f, 13f)
                curveTo(7.33f, 13f, 8f, 13.67f, 8f, 14.5f)
                curveTo(8f, 15.33f, 7.33f, 16f, 6.5f, 16f)
                close()
                moveTo(17.5f, 16f)
                curveTo(16.67f, 16f, 16f, 15.33f, 16f, 14.5f)
                curveTo(16f, 13.67f, 16.67f, 13f, 17.5f, 13f)
                curveTo(18.33f, 13f, 19f, 13.67f, 19f, 14.5f)
                curveTo(19f, 15.33f, 18.33f, 16f, 17.5f, 16f)
                close()
                moveTo(5f, 11f)
                lineTo(6.5f, 6.5f)
                lineTo(17.5f, 6.5f)
                lineTo(19f, 11f)
                lineTo(5f, 11f)
                close()
            }
        }.build()
    }

    val TrendingUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "TrendingUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 6f)
                lineTo(18.29f, 8.29f)
                lineTo(13.41f, 13.17f)
                lineTo(9.41f, 9.17f)
                lineTo(2f, 16.59f)
                lineTo(3.41f, 18f)
                lineTo(9.41f, 12f)
                lineTo(13.41f, 16f)
                lineTo(19.71f, 9.71f)
                lineTo(22f, 12f)
                lineTo(22f, 6f)
                close()
            }
        }.build()
    }

    val Event: ImageVector by lazy {
        ImageVector.Builder(
            name = "Event",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 4f)
                lineTo(18f, 4f)
                lineTo(18f, 2f)
                lineTo(16f, 2f)
                lineTo(16f, 4f)
                lineTo(8f, 4f)
                lineTo(8f, 2f)
                lineTo(6f, 2f)
                lineTo(6f, 4f)
                lineTo(5f, 4f)
                curveTo(3.89f, 4f, 3.01f, 4.9f, 3.01f, 6f)
                lineTo(3f, 20f)
                curveTo(3f, 21.1f, 3.89f, 22f, 5f, 22f)
                lineTo(19f, 22f)
                curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                lineTo(21f, 6f)
                curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
                close()
                moveTo(19f, 20f)
                lineTo(5f, 20f)
                lineTo(5f, 9f)
                lineTo(19f, 9f)
                lineTo(19f, 20f)
                close()
            }
        }.build()
    }
}
