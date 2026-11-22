package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DocumentFormat
import com.example.model.PresentationMode
import com.example.model.SubmissionStatus
import com.example.ui.theme.*
import java.io.File

@Composable
fun PelajariaLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Brush.linearGradient(listOf(PelajariaBlue, PelajariaBlueBright))),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_pelajaria_icon_fg),
            contentDescription = "Logo Pelajaria",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun PresentationModeBadge(
    mode: PresentationMode,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = when (mode) {
        PresentationMode.ORIGINAL_MATERIAL -> Triple(PelajariaBlueSoft, PelajariaBlue, Icons.Rounded.MenuBook)
        PresentationMode.ORIGINAL_WORKSHEET -> Triple(PelajariaOrangeSoft, Color(0xFFC65102), Icons.Rounded.Assignment)
        PresentationMode.INTERACTIVE_MATERIAL -> Triple(PelajariaTealSoft, PelajariaTeal, Icons.Rounded.AutoAwesome)
        PresentationMode.DIGITAL_QUESTIONS -> Triple(PelajariaPurpleSoft, PelajariaPurple, Icons.Rounded.FactCheck)
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
            Text(
                text = mode.label,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DocumentFormatBadge(
    format: DocumentFormat,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = when (format) {
        DocumentFormat.IMAGE -> Triple(PelajariaAmberSoft, Color(0xFF9A5B00), Icons.Rounded.Image)
        DocumentFormat.PDF -> Triple(PelajariaCoralSoft, PelajariaCoral, Icons.Rounded.PictureAsPdf)
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
            Text(format.label, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SubmissionStatusBadge(
    status: SubmissionStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        SubmissionStatus.NOT_STARTED -> PelajariaAmberSoft to Color(0xFF9A5B00)
        SubmissionStatus.WAITING_GRADE -> PelajariaBlueSoft to PelajariaBlue
        SubmissionStatus.GRADED -> PelajariaGreenSoft to PelajariaGreen
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChildAvatarCircle(
    emoji: String,
    colorHex: Long,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(colorHex).copy(alpha = 0.16f))
            .border(2.dp, Color(colorHex).copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size.value * 0.48f).sp)
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PelajariaBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PelajariaBlueBright, modifier = Modifier.size(28.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun loadUriBitmap(uriString: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        if (uriString.isBlank()) return@remember null
        runCatching {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path ?: return@runCatching null
                BitmapFactory.decodeFile(path)?.asImageBitmap()
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }
        }.getOrNull()
    }
}

/**
 * Interactive Zoomable Photo / Worksheet Viewer supporting pinch-to-zoom,
 * double-tap zoom, and accessible zoom controls (+ / - / reset).
 */
@Composable
fun ZoomableDocumentViewer(
    uriString: String,
    samplePresetKey: String,
    title: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val loadedBitmap = loadUriBitmap(uriString)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, OutlineSubtle, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val nextScale = (scale * zoom).coerceIn(1f, 3.2f)
                        scale = nextScale
                        offset = if (nextScale > 1f) {
                            Offset(
                                x = (offset.x + pan.x).coerceIn(-220f, 220f),
                                y = (offset.y + pan.y).coerceIn(-180f, 180f)
                            )
                        } else Offset.Zero
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.2f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (loadedBitmap != null) {
                    Image(
                        bitmap = loadedBitmap,
                        contentDescription = "Preview foto $title",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    SimulatedWorksheetPhotoCard(samplePresetKey = samplePresetKey, title = title)
                }
            }

            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            ) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Zoom controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cubitan / ketuk 2x untuk memperbesar foto",
                style = MaterialTheme.typography.labelSmall,
                color = InkSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = {
                        scale = (scale - 0.4f).coerceAtLeast(1f)
                        if (scale == 1f) offset = Offset.Zero
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Rounded.ZoomOut, contentDescription = "Perkecil")
                }
                FilledTonalIconButton(
                    onClick = { scale = (scale + 0.4f).coerceAtMost(3.2f) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Rounded.ZoomIn, contentDescription = "Perbesar")
                }
                FilledTonalIconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset Zoom")
                }
            }
        }
    }
}

@Composable
private fun SimulatedWorksheetPhotoCard(
    samplePresetKey: String,
    title: String
) {
    val lines = when (samplePresetKey) {
        "math_worksheet" -> listOf(
            "LEMBAR KERJA MATEMATIKA • KELAS 2 SD",
            "1. Ibu membeli 14 apel merah dan 5 apel hijau.",
            "   Berapa jumlah seluruh apel Ibu?",
            "2. Hitunglah: 12 + 15 = ....",
            "3. Tuliskan cara berhitungmu di buku tulis!"
        )
        "indo_handwritten" -> listOf(
            "CATATAN LATIHAN BAHASA INDONESIA",
            "1. Sebutkan huruf vokal pada kata: B U K U",
            "2. Tulislah 1 kalimat sapaan santun di pagi hari!",
            "   (Tulisan tangan guru pada papan tulis)"
        )
        "tk_fruit_count" -> listOf(
            "KARTU BERHITUNG TK",
            "🍎 🍎  +  🍊  =  ...",
            "Hitung semua buah pada gambar!"
        )
        "handwritten_math_sample" -> listOf(
            "FOTO JAWABAN MURID (TULISAN TANGAN)",
            "Nama: Alya • Kelas 2 SD",
            "1) 14 + 5 = 19 apel",
            "2) 12 + 15 = 27"
        )
        else -> listOf(
            title.uppercase(),
            "Dokumen foto asli yang diunggah oleh guru."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(lines.first(), style = MaterialTheme.typography.labelMedium, color = PelajariaBlueDeep, fontWeight = FontWeight.ExtraBold)
                Icon(Icons.Rounded.Description, contentDescription = null, tint = PelajariaBlueBright, modifier = Modifier.size(18.dp))
            }
            HorizontalDivider(color = PelajariaBlueSoft)
            lines.drop(1).forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = InkPrimary)
            }
        }
    }
}

/**
 * PDF Document Reader supporting both real uploaded PDF files via Android's PdfRenderer
 * and multi-page simulated PDF documents for pre-seeded demo content.
 */
@Composable
fun PdfDocumentViewer(
    uriString: String,
    simulatedPages: List<String>,
    title: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember(uriString, simulatedPages) { mutableIntStateOf(0) }

    val renderedPages: List<ImageBitmap> = remember(uriString) {
        if (uriString.isBlank()) return@remember emptyList()
        runCatching {
            val uri = Uri.parse(uriString)
            val pfd = if (uri.scheme == "file") {
                val f = File(uri.path ?: return@runCatching emptyList())
                android.os.ParcelFileDescriptor.open(f, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                context.contentResolver.openFileDescriptor(uri, "r")
            } ?: return@runCatching emptyList()
            pfd.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    val count = renderer.pageCount.coerceAtMost(8)
                    (0 until count).map { idx ->
                        renderer.openPage(idx).use { page ->
                            val bmp = Bitmap.createBitmap(
                                (page.width * 2).coerceAtLeast(600),
                                (page.height * 2).coerceAtLeast(800),
                                Bitmap.Config.ARGB_8888
                            )
                            bmp.eraseColor(android.graphics.Color.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bmp.asImageBitmap()
                        }
                    }
                }
            }
        }.getOrElse { emptyList() }
    }

    val totalPages = if (renderedPages.isNotEmpty()) renderedPages.size else simulatedPages.size.coerceAtLeast(1)
    val safeIndex = currentPage.coerceIn(0, totalPages - 1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = PelajariaCoral)
                    Text("Pembaca PDF • $title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(50)) {
                    Text(
                        "Hal ${safeIndex + 1}/$totalPages",
                        style = MaterialTheme.typography.labelSmall,
                        color = PelajariaBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (renderedPages.isNotEmpty()) {
                Image(
                    bitmap = renderedPages[safeIndex],
                    contentDescription = "Halaman PDF ${safeIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp, max = 340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Fit
                )
            } else {
                val pageText = simulatedPages.getOrElse(safeIndex) {
                    "Dokumen PDF \"$title\" telah dimuat dan siap dibaca."
                }
                Surface(
                    color = Color(0xFFFAFAFC),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSubtle),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 190.dp)
                ) {
                    Text(
                        text = pageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (totalPages > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { if (safeIndex > 0) currentPage = safeIndex - 1 },
                        enabled = safeIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                        Text("Hal. Sebelumnya")
                    }
                    OutlinedButton(
                        onClick = { if (safeIndex < totalPages - 1) currentPage = safeIndex + 1 },
                        enabled = safeIndex < totalPages - 1
                    ) {
                        Text("Hal. Berikutnya")
                        Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreTrendChart(
    labels: List<String>,
    scores: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = PelajariaBlueBright
) {
    if (scores.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Rounded.Insights,
            title = "Belum Ada Nilai",
            subtitle = "Nilai tugas yang sudah diperiksa guru akan tampil sebagai grafik di sini."
        )
        return
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            val w = size.width
            val h = size.height
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = h - (h * i / gridSteps)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.5f
                )
            }
            val count = scores.size
            val points = scores.mapIndexed { idx, score ->
                val x = if (count == 1) w / 2f else (w * idx) / (count - 1)
                val clamped = score.coerceIn(0, 100)
                val y = h - (h * clamped / 100f)
                Offset(x, y)
            }
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, h)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.02f))
                    )
                )
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
            points.forEach { pt ->
                drawCircle(color = Color.White, radius = 11f, center = pt)
                drawCircle(color = lineColor, radius = 7f, center = pt)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { i, label ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${scores.getOrNull(i) ?: 0}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                    Text(
                        text = label.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectBannerArt(
    subject: String,
    modifier: Modifier = Modifier
) {
    val drawableId = when (subject) {
        "Matematika" -> R.drawable.img_math_banner
        "IPA" -> R.drawable.img_science_nature
        else -> null
    }
    if (drawableId != null) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "Ilustrasi $subject",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val colors = when (subject) {
            "Bahasa Indonesia" -> listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
            "Bahasa Inggris" -> listOf(Color(0xFF5E35B1), Color(0xFF7E57C2))
            else -> listOf(Color(0xFFFB8C00), Color(0xFFFFB300))
        }
        Box(
            modifier = modifier.background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.MenuBook, contentDescription = subject, tint = Color.White, modifier = Modifier.size(28.dp))
                Text(subject, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
