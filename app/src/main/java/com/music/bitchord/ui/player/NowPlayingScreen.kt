package com.music.bitchord.ui.player

import com.music.bitchord.R
import com.music.bitchord.ui.components.ExplicitSongTitle

import android.database.ContentObserver
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.LruCache
import android.view.View
import android.widget.Toast
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.animation.core.animateDpAsState
import kotlinx.coroutines.delay
import kotlin.math.abs
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.media3.common.Player
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.music.bitchord.ui.theme.SystemBarIcons
import com.music.bitchord.ui.rememberIsForeground
import com.music.bitchord.ui.components.thumbnailBorder
import com.music.bitchord.ui.components.optimizedHazeEffect
import com.music.bitchord.ui.components.AudioPipelineDialog
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import com.music.bitchord.ui.icons.BitChordIcons
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.NerdStats
import com.music.bitchord.data.listentogether.ListenTogether
import com.music.bitchord.data.listentogether.PartyMember
import com.music.bitchord.data.settings.TrackAnalysisState
import com.music.bitchord.data.canvas.CanvasArtwork
import com.music.bitchord.data.canvas.CanvasRepository
import com.music.bitchord.data.lyrics.CharGrowth
import com.music.bitchord.data.lyrics.Genius
import com.music.bitchord.data.lyrics.GrowingWord
import com.music.bitchord.data.lyrics.LyricAlignment
import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.lyrics.LyricsSource
import com.music.bitchord.data.lyrics.LyricsTranslation
import com.music.bitchord.data.lyrics.translationLanguageName
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.settings.AudioQuality
import com.music.bitchord.data.model.LikeStatus
import com.music.bitchord.data.model.PlaybackSourceType
import com.music.bitchord.data.model.PLAYER_ART_PX
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.artworkAt
import com.music.bitchord.playback.BACK_RESTARTS_AFTER_MS
import com.music.bitchord.playback.autoplaySectionStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val ART_PX = PLAYER_ART_PX
private const val ART_RETRIES = 3
private const val ART_RETRY_DELAY_MS = 1_500L
private const val ALBUM_SETTLE_MS = 700L
private const val REVERT_CUE_MS = 2_600L
private const val SEEK_SETTLE_TOLERANCE_MS = 1_500L
private const val SEEK_SETTLE_TIMEOUT_MS = 4_000L

private val THUMB_SIZE = 54.dp
private val HEADER_HEIGHT = 60.dp
private val ART_TITLE_GAP = 20.dp
private const val QUEUE_TRAVEL_MS = 420
private const val QUEUE_CARRY_FRACTION = 0.3f
private const val QUEUE_FLICK_VELOCITY = 450f
private val DISMISS_STRIP_HEIGHT = 32.dp
private val ART_BOX_TOP_PAD = 8.dp
private val VERSION_PILL_ART_INSET = 12.dp
private const val HERO_FADE_FRACTION = 0.42f
private const val MESH_REFRESH_MS = 3_000L
private val PLAYER_GUTTER = 30.dp
private val PLAYER_MAX_WIDTH = 560.dp
private const val DOCKED_PLAYER_FRACTION = 0.42f
private val DOCKED_PLAYER_MIN_WIDTH = 340.dp
private val DOCKED_PLAYER_MAX_WIDTH = 420.dp
private val DOCKED_PAGE_MIN_WIDTH = 360.dp
private val WIDE_LYRICS_PLAYER_MIN_WIDTH = 700.dp
private val WIDE_LYRICS_MAX_WIDTH = 1000.dp
private const val WIDE_SPLIT_MS = 420

private const val ARTWORK_EXPANDED_SCALE = 1f
private const val ARTWORK_PAUSE_SHRINK_SCALE = 0.82f
private const val ARTWORK_DRAG_SHRINK_SCALE = 0.94f

private val ArtworkScaleEasing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
private const val ARTWORK_SCALE_DURATION_MS = 500
private val DOCKED_TOP_PAD = 12.dp
private val CONTROL_GAP_SPREAD_MAX = 48.dp
private var lastControlSpread: Dp = 0.dp

private const val SHUFFLE_TAP_WINDOW_MS = 400L
private const val AUTOPLAY_TAP_WINDOW_MS = 700L

fun fullBleedArtworkAvailable(windowWidth: Dp): Boolean =
    playerFillsWindow(windowWidth) || dockedPlayerAvailable(windowWidth)

private fun playerFillsWindow(windowWidth: Dp): Boolean =
    windowWidth <= PLAYER_MAX_WIDTH + PLAYER_GUTTER * 2

fun dockedPlayerAvailable(windowWidth: Dp): Boolean =
    windowWidth >= DOCKED_PAGE_MIN_WIDTH + DOCKED_PLAYER_MIN_WIDTH

fun wideLyricsLayoutAvailable(windowWidth: Dp, windowHeight: Dp): Boolean =
    windowWidth > windowHeight && windowWidth >= WIDE_LYRICS_PLAYER_MIN_WIDTH

fun dockedPlayerWidth(windowWidth: Dp): Dp =
    (windowWidth * DOCKED_PLAYER_FRACTION)
        .coerceIn(DOCKED_PLAYER_MIN_WIDTH, DOCKED_PLAYER_MAX_WIDTH)
        .coerceAtMost(windowWidth - DOCKED_PAGE_MIN_WIDTH)

private const val UNSUNG_ALPHA = 0.45f
private const val UNSUNG_ALPHA_STRIP = 0.55f
private const val GLOW_ALPHA = 0.62f
private val GLOW_RADIUS = 6.dp
private val GLOW_ROOM = 10.dp
private val BACKING_FONT_SIZE = 23.sp
private val BACKING_LINE_HEIGHT = 29.sp
private const val BACKING_ALPHA = 0.72f
private val WIPE_FEATHER = 30.dp
private val WORD_RISE = 2.dp
private const val GROW_HEADROOM = 3f
private val DUET_LANE = 44.dp
private val GAP_ROW_HEIGHT = 40.dp
private val GAP_ROW_SPACING = 16.dp
private val LINE_FALLOFF_ALPHA = floatArrayOf(1f, 0.8f, 0.7f, 0.58f, 0.46f)
private val LINE_FALLOFF_BLUR = arrayOf(0.dp, 1.dp, 1.dp, 1.7.dp, 2.4.dp)

private val SKELETON_BLOCKS = listOf(
    floatArrayOf(0.97f, 0.54f),
    floatArrayOf(0.92f, 0.99f, 0.41f),
    floatArrayOf(0.68f),
    floatArrayOf(0.95f, 0.73f),
    floatArrayOf(0.89f, 0.96f, 0.37f),
)

private val SKELETON_BAR = 26.dp
private val SKELETON_LEADING = 15.dp
private val SKELETON_BLOCK_GAP = 35.dp
private const val SKELETON_PERIOD_MS = 1_400
private const val BROWSING_ALPHA = 0.8f
private const val INACTIVE_SCALE = 0.98f
private const val PRESSED_SCALE = 0.96f
private const val GAP_DOTS = 3
private val GAP_DOT_SIZE = 13.dp
private val GAP_DOT_GAP = 5.dp
private const val GAP_DOT_REST = 0.25f
private const val GAP_REST_SCALE = 0.76f
private const val SCROLL_LEAD_MIN_MS = 350L
private const val SCROLL_LEAD_MAX_MS = 500L
private val LYRIC_EASING = CubicBezierEasing(0.41f, 0f, 0.12f, 0.99f)
private const val LYRIC_SETTLE_MS = 400
private const val STAGGER_STEPS = 3
private const val STAGGER_FRACTION = 0.06f

private class ScrollRun(val id: Int, val delta: Float, val durationMs: Int) {
    val spanMs: Float get() = durationMs * (1f + STAGGER_FRACTION * STAGGER_STEPS)
}

private fun scrollLead(lines: List<LyricLine>, positionMs: Long): Long {
    val current = lines.indexOfLast { it.timeMs <= positionMs }
    if (current < 0) return SCROLL_LEAD_MIN_MS
    val next = lines.getOrNull(current + 1) ?: return SCROLL_LEAD_MIN_MS
    val gap = next.timeMs - lines[current].endMs
    return gap.coerceIn(SCROLL_LEAD_MIN_MS, SCROLL_LEAD_MAX_MS)
}

private val CONTROLS_SCROLL_SLOP = 20.dp
private const val LYRICS_CONTROLS_IDLE_MS = 5_000L
private const val LYRICS_UNAVAILABLE_HOLD_MS = 5_000L
private const val LYRICS_UNAVAILABLE_FADE_MS = 900
private const val LIGHT_ARTWORK_LUMINANCE_THRESHOLD = 0.45f

private val artworkLuminanceCache = LruCache<String, Float>(20)

@Composable
private fun rememberArtworkLuminance(imageUrl: String?): Float? {
    val context = LocalContext.current
    var luminance by remember(imageUrl) { mutableStateOf<Float?>(null) }

    LaunchedEffect(imageUrl) {
        luminance = null
        if (imageUrl == null) return@LaunchedEffect

        artworkLuminanceCache.get(imageUrl)?.let { cached ->
            luminance = cached
            return@LaunchedEffect
        }

        val request = ImageRequest.Builder(context)
            .data(imageUrl.artworkAt(ART_PX))
            .size(128)
            .allowHardware(false)
            .build()
        val result = SingletonImageLoader.get(context).execute(request)
        val bitmap = (result as? SuccessResult)?.image?.toBitmap()
        if (bitmap != null) {
            val lum = withContext(Dispatchers.Default) {
                bitmap.topAreaLuminance()
            }
            artworkLuminanceCache.put(imageUrl, lum)
            luminance = lum
        } else {
            luminance = 0f
        }
    }
    return luminance
}

private fun Bitmap.topAreaLuminance(): Float {
    val sampleHeight = (height * 0.35f).toInt().coerceIn(1, height)
    val sampleWidth = width.coerceAtLeast(1)
    val pixels = IntArray(sampleWidth * sampleHeight)
    getPixels(pixels, 0, sampleWidth, 0, 0, sampleWidth, sampleHeight)

    var totalLuminance = 0.0
    val count = pixels.size.coerceAtLeast(1)
    for (pixel in pixels) {
        val r = ((pixel shr 16) and 0xFF) / 255.0f
        val g = ((pixel shr 8) and 0xFF) / 255.0f
        val b = (pixel and 0xFF) / 255.0f
        val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
        totalLuminance += lum
    }
    return (totalLuminance / count).toFloat()
}

private sealed interface LyricsTranslationUiState {
    data object Idle : LyricsTranslationUiState
    data object Loading : LyricsTranslationUiState
    data class Ready(val lines: List<LyricLine>) : LyricsTranslationUiState
    data object SameLanguage : LyricsTranslationUiState
}

private const val TRANSLATION_MOTION_MS = 540
private const val PARTICLES_PER_VOICE = 18

private data class TranslationParticle(
    val anchor: Offset,
    val drift: Offset,
    val radius: Float,
    val delay: Float,
)

@Composable
fun NowPlayingScreen(
    song: Song,
    playedBy: String? = null,
    isPlaying: Boolean,
    isLoading: Boolean,
    positionMs: Long,
    durationMs: Long,
    isAudioVersion: Boolean,
    audioVersionSwitching: Boolean,
    qualityUpgraded: Boolean,
    queue: List<Song>,
    queueIndex: Int,
    hasPrevious: Boolean,
    hasNext: Boolean,
    repeatMode: Int,
    shuffleEnabled: Boolean,
    autoplayEnabled: Boolean,
    signedIn: Boolean,
    accountName: String?,
    likeStatus: LikeStatus,
    onToggleLike: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekFraction: (Float) -> Unit,
    onToggleAudioVersion: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveInQueue: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenPlaybackSource: () -> Unit,
    onListenTogether: () -> Unit,
    lyrics: List<LyricLine>?,
    lyricsSource: LyricsSource?,
    lyricsUnavailable: Boolean,
    lyricsOffsetOpen: Boolean,
    onDismissLyricsOffset: () -> Unit,
    windowWidth: Dp,
    windowHeight: Dp,
    docked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptics = rememberHaptics()

    val artLuminance = rememberArtworkLuminance(song.thumbnailUrl)
    val isLightArtwork = artLuminance?.let { it > LIGHT_ARTWORK_LUMINANCE_THRESHOLD } ?: false

    if (!docked) {
        SystemBarIcons(dark = isLightArtwork)
    }

    val playerHaze = remember { HazeState() }
    var showAudioPipeline by remember { mutableStateOf(false) }
    var showAudioOutput by remember { mutableStateOf(false) }
    val openAudioOutput = rememberOutputPicker { showAudioOutput = true }

    val syncedLyricsEnabled by AppSettings.syncedLyrics.collectAsStateWithLifecycle()
    val lyricsOffsetMs by AppSettings.lyricsOffsetMs.collectAsStateWithLifecycle()
    val lyricsPositionMs = adjustedLyricsPosition(positionMs, lyricsOffsetMs)
    val seekToLyric: (Long) -> Unit = { lineTimeMs ->
        onSeek(adjustedLyricsSeekTarget(lineTimeMs, lyricsOffsetMs))
    }
    val hideVolumeBar by AppSettings.hideVolumeBar.collectAsStateWithLifecycle()

    val canvasEnabled by AppSettings.animatedCanvas.collectAsStateWithLifecycle()
    val canvasOverCellular by AppSettings.canvasOverCellular.collectAsStateWithLifecycle()
    val meteredConnection by AppSettings.meteredConnection.collectAsStateWithLifecycle()
    val canvasAllowedNow = canvasEnabled && (meteredConnection != true || canvasOverCellular)
    var canvas by remember(song.videoId) { mutableStateOf<CanvasArtwork?>(null) }
    var canvasRendered by remember(song.videoId) { mutableStateOf(false) }
    var canvasFrame by remember(song.videoId) { mutableStateOf<Bitmap?>(null) }
    val canvasCover = remember(song.videoId) { mutableFloatStateOf(0f) }
    val stillCovered by remember(song.videoId) {
        derivedStateOf { canvasCover.floatValue > 0.999f }
    }
    val legacyMesh by AppSettings.legacyMeshGradient.collectAsStateWithLifecycle()
    val artMesh = if (legacyMesh) null else rememberArtworkMesh(song.thumbnailUrl, canvasFrame, ART_PX)
    val meshRefreshMs = MESH_REFRESH_MS

    LaunchedEffect(song.videoId, song.albumName, canvasAllowedNow) {
        if (!canvasAllowedNow) {
            canvas = null
            return@LaunchedEffect
        }
        canvas = CanvasRepository.cached(song) ?: canvas
        if (canvas == null && song.albumName == null) delay(ALBUM_SETTLE_MS)
        canvas = CanvasRepository.canvasFor(song) ?: canvas
    }

    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    var queueOpen by remember { mutableStateOf(false) }
    var lyricsOpen by remember { mutableStateOf(false) }
    var lyricsScrolling by remember { mutableStateOf(false) }
    var queueScrolling by remember { mutableStateOf(false) }
    LaunchedEffect(lyricsOpen) { if (!lyricsOpen) lyricsScrolling = false }
    LaunchedEffect(queueOpen) { if (!queueOpen) queueScrolling = false }
    val panelScrolling = lyricsScrolling || queueScrolling
    var lyricsControlsOpen by remember { mutableStateOf(false) }

    val openLyrics: () -> Unit = {
        lyricsControlsOpen = true
        lyricsOpen = true
        queueOpen = false
    }
    val closeLyrics: () -> Unit = {
        lyricsControlsOpen = false
        lyricsOpen = false
    }
    val toggleLyrics: () -> Unit = {
        if (lyricsOpen) closeLyrics() else openLyrics()
    }
    val reduceTranslationMotion by AppSettings.reduceAnimation.collectAsStateWithLifecycle()
    val configuredLocale = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
        ?.takeIf { it.isNotBlank() }
        ?: context.resources.configuration.locales.get(0).toLanguageTag()
    val preferredTranslation by AppSettings.translationLanguage.collectAsStateWithLifecycle()
    val translationLanguage = remember(configuredLocale, preferredTranslation) {
        preferredTranslation.ifBlank {
            Locale.forLanguageTag(configuredLocale).language.ifBlank { "en" }
        }
    }
    val translationLanguageName = remember(configuredLocale, translationLanguage) {
        translationLanguageName(translationLanguage, Locale.forLanguageTag(configuredLocale))
    }
    var translationState by remember(song.videoId, translationLanguage, lyrics) {
        mutableStateOf<LyricsTranslationUiState>(LyricsTranslationUiState.Idle)
    }
    var showingTranslation by remember(song.videoId, translationLanguage, lyrics) {
        mutableStateOf(false)
    }
    var translationTransition by remember(song.videoId) { mutableIntStateOf(0) }
    var translationJob by remember(song.videoId, translationLanguage, lyrics) {
        mutableStateOf<Job?>(null)
    }
    DisposableEffect(song.videoId, translationLanguage, lyrics) {
        onDispose { translationJob?.cancel() }
    }
    val displayedLyrics = if (showingTranslation) {
        (translationState as? LyricsTranslationUiState.Ready)?.lines ?: lyrics.orEmpty()
    } else {
        lyrics.orEmpty()
    }
    val lyricsLoadingLines = stringArrayResource(R.array.lyrics_loading_lines)
    val lyricsLoadingText = remember(song.videoId) { lyricsLoadingLines.random() }
    val translationScope = rememberCoroutineScope()
    val toggleTranslation: () -> Unit = toggleTranslation@{
        when (val state = translationState) {
            is LyricsTranslationUiState.Ready -> {
                showingTranslation = !showingTranslation
                translationTransition++
                haptics.play(Haptic.Select)
            }
            LyricsTranslationUiState.Loading -> Unit
            LyricsTranslationUiState.SameLanguage -> {
                haptics.play(Haptic.Tap)
                Toast.makeText(
                    context,
                    context.getString(R.string.lyrics_already_in_language, translationLanguageName),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            LyricsTranslationUiState.Idle -> {
                val source = lyrics.orEmpty()
                if (source.isEmpty()) return@toggleTranslation
                haptics.play(Haptic.Tap)
                translationState = LyricsTranslationUiState.Loading
                translationJob?.cancel()
                translationJob = translationScope.launch {
                    when (
                        val result = LyricsTranslation.translate(
                            context = context.applicationContext,
                            trackId = song.videoId,
                            lines = source,
                            targetLanguageTag = translationLanguage,
                        )
                    ) {
                        is LyricsTranslation.Result.Translated -> {
                            translationState = LyricsTranslationUiState.Ready(result.lines)
                            showingTranslation = true
                            translationTransition++
                            haptics.play(Haptic.ToggleOn)
                        }
                        is LyricsTranslation.Result.SameLanguage -> {
                            translationState = LyricsTranslationUiState.SameLanguage
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.lyrics_already_in_language,
                                    translationLanguageName,
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        LyricsTranslation.Result.Unavailable -> {
                            translationState = LyricsTranslationUiState.Idle
                            Toast.makeText(
                                context,
                                context.getString(R.string.lyrics_translation_unavailable),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }
    }

    var showRevertCue by remember(song.videoId) { mutableStateOf(false) }
    LaunchedEffect(song.videoId, qualityUpgraded) {
        if (!qualityUpgraded) {
            showRevertCue = false
            return@LaunchedEffect
        }
        showRevertCue = true
        delay(REVERT_CUE_MS)
        showRevertCue = false
    }

    val playerView = LocalView.current
    DisposableEffect(playerView, lyricsOpen) {
        playerView.keepScreenOn = lyricsOpen
        onDispose { playerView.keepScreenOn = false }
    }

    BackHandler(enabled = lyricsOpen, onBack = closeLyrics)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val view = LocalView.current
        DisposableEffect(view, lyricsOpen) {
            val callback = if (lyricsOpen) OverlayBack.register(view, closeLyrics) else null
            onDispose { OverlayBack.unregister(view, callback) }
        }
    }

    BackHandler(enabled = queueOpen) { queueOpen = false }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val view = LocalView.current
        DisposableEffect(view, queueOpen) {
            val callback = if (queueOpen) OverlayBack.register(view) { queueOpen = false } else null
            onDispose { OverlayBack.unregister(view, callback) }
        }
    }

    BackHandler(enabled = showAudioPipeline) { showAudioPipeline = false }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val view = LocalView.current
        DisposableEffect(view, showAudioPipeline) {
            val callback = if (showAudioPipeline) OverlayBack.register(view) { showAudioPipeline = false } else null
            onDispose { OverlayBack.unregister(view, callback) }
        }
    }

    BackHandler(enabled = showAudioOutput) { showAudioOutput = false }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val view = LocalView.current
        DisposableEffect(view, showAudioOutput) {
            val callback = if (showAudioOutput) OverlayBack.register(view) { showAudioOutput = false } else null
            onDispose { OverlayBack.unregister(view, callback) }
        }
    }

    BackHandler(enabled = lyricsOffsetOpen) { onDismissLyricsOffset() }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val view = LocalView.current
        DisposableEffect(view, lyricsOffsetOpen) {
            val callback = if (lyricsOffsetOpen) OverlayBack.register(view, onDismissLyricsOffset) else null
            onDispose { OverlayBack.unregister(view, callback) }
        }
    }

    val queueSlide = remember { mutableFloatStateOf(0f) }
    val queueProgress = queueSlide.floatValue
    var queueDragging by remember { mutableStateOf(false) }
    var queueReleased by remember { mutableIntStateOf(0) }
    LaunchedEffect(queueOpen, queueDragging, queueReleased) {
        if (queueDragging) return@LaunchedEffect
        val target = if (queueOpen) 1f else 0f
        val from = queueSlide.floatValue
        if (from == target) return@LaunchedEffect
        animate(
            initialValue = from,
            targetValue = target,
            animationSpec = tween(
                durationMillis = (QUEUE_TRAVEL_MS * abs(target - from)).roundToInt(),
                easing = FastOutSlowInEasing,
            ),
        ) { value, _ -> queueSlide.floatValue = value }
    }

    val swipeThreshold = with(density) { 72.dp.toPx() }
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    val swipeSettle by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "swipeOffset",
    )

    var pendingSeek by remember { mutableStateOf<Float?>(null) }
    val fraction = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    val shown = when {
        scrubbing -> scrubValue
        pendingSeek != null -> pendingSeek!!
        else -> fraction.coerceIn(0f, 1f)
    }

    LaunchedEffect(positionMs, durationMs, pendingSeek) {
        val target = pendingSeek ?: return@LaunchedEffect
        if (durationMs > 0 && abs(positionMs - (target * durationMs).toLong()) < SEEK_SETTLE_TOLERANCE_MS) {
            pendingSeek = null
        }
    }
    LaunchedEffect(pendingSeek) {
        if (pendingSeek == null) return@LaunchedEffect
        delay(SEEK_SETTLE_TIMEOUT_MS)
        pendingSeek = null
    }
    LaunchedEffect(song.videoId) { pendingSeek = null }

    // Apple Music signature spring bounce scale on pause/play
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "artScale",
    )

    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }
    val maxVolume = remember(audioManager) {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 15
    }
    val scope = rememberCoroutineScope()
    val volume = remember {
        Animatable(
            (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0).toFloat() / maxVolume,
        )
    }
    var volumeDragging by remember { mutableStateOf(false) }

    LaunchedEffect(lyricsOpen, lyricsControlsOpen, scrubbing, volumeDragging) {
        if (lyricsOpen && lyricsControlsOpen && !scrubbing && !volumeDragging) {
            delay(LYRICS_CONTROLS_IDLE_MS)
            lyricsControlsOpen = false
        }
    }
    var systemVolume by remember { mutableFloatStateOf(volume.value) }

    LaunchedEffect(systemVolume) {
        if (!volumeDragging) {
            volume.animateTo(systemVolume, tween(durationMillis = 220, easing = FastOutSlowInEasing))
        }
    }

    DisposableEffect(audioManager) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: return
                systemVolume = current.toFloat() / maxVolume
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            observer,
        )
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }

    val p by animateFloatAsState(
        targetValue = if (lyricsOpen || queueOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "sleeveCollapse",
    )

    val panelsSettled = p >= 1f
    val panelFade by animateFloatAsState(
        targetValue = if (panelsSettled) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "panelFade",
    )
    val fullBleedArt by AppSettings.fullBleedArtwork.collectAsStateWithLifecycle()
    val heroMode = fullBleedArt && (docked || playerFillsWindow(windowWidth))

    val artUrl = song.artworkAt(ART_PX)
    var artLoaded by remember(artUrl) { mutableStateOf(false) }
    var artAttempt by remember(artUrl) { mutableIntStateOf(0) }
    val artRequest = remember(artUrl, artAttempt) {
        ImageRequest.Builder(context)
            .data(artUrl)
            .size(ART_PX)
            .apply { if (artAttempt > 0) memoryCacheKeyExtra("attempt", artAttempt.toString()) }
            .build()
    }
    var artFailed by remember(artUrl) { mutableStateOf(false) }
    LaunchedEffect(artUrl, artFailed) {
        if (artUrl == null || !artFailed || artAttempt >= ART_RETRIES) return@LaunchedEffect
        delay(ART_RETRY_DELAY_MS)
        artFailed = false
        artAttempt++
    }

    var heroSettled by remember { mutableStateOf(false) }
    var heroArtLoaded by remember(artUrl, artAttempt, heroMode) { mutableStateOf(false) }
    LaunchedEffect(artLoaded, canvasRendered) {
        if (artLoaded || canvasRendered) heroSettled = true
    }
    val heroClip = canvas?.takeIf { heroMode && p < 0.5f }
    val heroT by animateFloatAsState(
        targetValue = if (heroMode && (canvasRendered || artLoaded || heroSettled)) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "heroCanvas",
    )
    val heroVisible = heroT * (1f - p)
    var heroHeight by remember { mutableStateOf(0.dp) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topStrip = if (docked) DOCKED_TOP_PAD else DISMISS_STRIP_HEIGHT

    var dismissBandTop by remember { mutableFloatStateOf(0f) }
    var dismissBandBottom by remember { mutableFloatStateOf(0f) }
    var dismissBandSpace by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val wideLyricsContent: @Composable (Modifier) -> Unit = { lyricsModifier ->
        Box(modifier = lyricsModifier) {
            if (displayedLyrics.isNotEmpty()) {
                LyricsTranslationMotion(
                    trigger = translationTransition,
                    reduceMotion = reduceTranslationMotion,
                    modifier = Modifier.fillMaxSize(),
                ) { particleProgress ->
                    LyricsPanel(
                        lines = displayedLyrics,
                        trackKey = song.videoId,
                        positionMs = lyricsPositionMs,
                        looking = !lyricsUnavailable,
                        isPlaying = isPlaying,
                        onSeekToLine = seekToLyric,
                        controlsOpen = true,
                        onRevealControls = {},
                        onHideControls = {},
                        translationProgress = particleProgress,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    TranslationToggleButton(
                        state = translationState,
                        showingTranslation = showingTranslation,
                        enabled = !lyrics.isNullOrEmpty(),
                        onClick = toggleTranslation,
                    )
                }
            } else {
                Text(
                    text = if (lyricsUnavailable) {
                        stringResource(R.string.lyrics_not_available)
                    } else {
                        lyricsLoadingText
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    val wideSplitAvailable = wideLyricsLayoutAvailable(windowWidth, windowHeight)
    val wideSplitOpen = (lyricsOpen || queueOpen) && wideSplitAvailable
    val wideSplit by animateFloatAsState(
        targetValue = if (wideSplitOpen) 1f else 0f,
        animationSpec = tween(WIDE_SPLIT_MS, easing = FastOutSlowInEasing),
        label = "wideSplit",
    )
    var widePanelIsQueue by remember { mutableStateOf(false) }
    LaunchedEffect(lyricsOpen, queueOpen) {
        if (queueOpen) widePanelIsQueue = true else if (lyricsOpen) widePanelIsQueue = false
    }

    if (wideSplitAvailable && (wideSplitOpen || wideSplit > 0.001f)) {
        val wideLyricsStatus = when {
            translationState is LyricsTranslationUiState.Loading ->
                stringResource(R.string.translating_lyrics_to, translationLanguageName)
            showingTranslation ->
                stringResource(R.string.lyrics_translated_to, translationLanguageName)
            translationState is LyricsTranslationUiState.SameLanguage ->
                stringResource(R.string.lyrics_already_in_language, translationLanguageName)
            lyricsSource != null -> stringResource(R.string.lyrics_by, lyricsSource.label)
            lyricsUnavailable -> stringResource(R.string.no_lyrics_found)
            lyrics.isNullOrEmpty() -> lyricsLoadingText
            else -> stringResource(R.string.lyrics_saved_with_download)
        }

        val wideStatusContent: @Composable (Modifier) -> Unit = { statusModifier ->
            if (lyricsOpen) {
                Text(
                    text = wideLyricsStatus,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = statusModifier.padding(vertical = 4.dp),
                )
            } else if (syncedLyricsEnabled) {
                Box(modifier = statusModifier) {
                    if (displayedLyrics.isNotEmpty()) {
                        CurrentLyricLine(
                            lines = displayedLyrics,
                            trackKey = song.videoId,
                            positionMs = lyricsPositionMs,
                            isPlaying = isPlaying,
                            durationMs = durationMs,
                            onClick = openLyrics,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else if (lyricsUnavailable) {
                        LyricsUnavailableLine(
                            trackKey = song.videoId,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LyricsLoadingLine(
                            text = lyricsLoadingText,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        val wideQueueContent: @Composable (Modifier) -> Unit = { queueModifier ->
            Column(modifier = queueModifier) {
                InlineQueue(
                    queue = queue,
                    currentIndex = queueIndex,
                    autoplayEnabled = autoplayEnabled,
                    onJumpTo = onJumpTo,
                    onRemove = onRemoveFromQueue,
                    onMove = onMoveInQueue,
                    onClear = onClearQueue,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            WidePlayerControls(
                song = song,
                isPlaying = isPlaying,
                isLoading = isLoading || audioVersionSwitching,
                positionMs = positionMs,
                durationMs = durationMs,
                hasPrevious = hasPrevious,
                hasNext = hasNext,
                repeatMode = repeatMode,
                shuffleEnabled = shuffleEnabled,
                autoplayEnabled = autoplayEnabled,
                signedIn = signedIn,
                accountName = accountName,
                likeStatus = likeStatus,
                hideVolumeBar = hideVolumeBar,
                volume = volume,
                maxVolume = maxVolume,
                audioManager = audioManager,
                onVolumeDragging = { volumeDragging = it },
                scrubbing = scrubbing,
                onScrubbingChange = { scrubbing = it },
                scrubValue = scrubValue,
                onScrubValueChange = { scrubValue = it },
                onToggleLike = onToggleLike,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onPrevious = onPrevious,
                onSeekFraction = onSeekFraction,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                onToggleAutoplay = onToggleAutoplay,
                onOpenMenu = onOpenMenu,
                onOpenAlbum = onOpenAlbum,
                onOpenArtist = onOpenArtist,
                onOpenOutput = openAudioOutput,
                onListenTogether = onListenTogether,
                lyricsOpen = lyricsOpen,
                queueOpen = queueOpen,
                onToggleLyrics = toggleLyrics,
                onToggleQueue = {
                    queueOpen = !queueOpen
                    if (queueOpen) closeLyrics()
                },
                showQueue = widePanelIsQueue,
                statusContent = wideStatusContent,
                lyricsContent = wideLyricsContent,
                queueContent = wideQueueContent,
                legacyMesh = legacyMesh,
                canvasFrame = canvasFrame,
                artMesh = artMesh,
                progress = wideSplit,
            )
            if (showAudioPipeline) {
                AudioPipelineDialog(
                    hazeState = playerHaze,
                    onDismiss = { showAudioPipeline = false },
                )
            }
            if (showAudioOutput) {
                AudioOutputSheet(
                    hazeState = playerHaze,
                    accountName = accountName,
                    onDismiss = { showAudioOutput = false },
                )
            }
            if (lyricsOffsetOpen) {
                LyricsOffsetSheet(
                    hazeState = playerHaze,
                    onDismiss = onDismissLyricsOffset,
                )
            }
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (legacyMesh) {
            MeshGradientBackground(
                palette = rememberArtworkColors(song.thumbnailUrl, canvasFrame),
                trackKey = song.videoId,
            )
        } else {
            ArtworkMeshBackdrop(
                mesh = artMesh,
                seam = if (heroMode) heroHeight else 0.dp,
            )
        }

        if (heroHeight > 0.dp) {
            if (heroMode && !(stillCovered && heroClip != null) &&
                (p < 0.5f || heroVisible > 0.001f)
            ) {
                DisposableEffect(artRequest) {
                    onDispose { heroArtLoaded = false }
                }
                AsyncImage(
                    model = artRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onState = { heroArtLoaded = it is AsyncImagePainter.State.Success },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(heroHeight)
                        .hazeSource(playerHaze)
                        .graphicsLayer {
                            alpha = heroVisible * (1f - if (heroClip != null) canvasCover.floatValue else 0f)
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black, Color.Transparent),
                                    startY = size.height * (1f - HERO_FADE_FRACTION),
                                    endY = size.height,
                                ),
                                blendMode = BlendMode.DstIn,
                            )
                        },
                )
            }

            if (heroMode) {
                heroClip?.let { clip ->
                    CanvasArtworkPlayer(
                        canvas = clip,
                        isPlaying = isPlaying,
                        onRenderedChanged = { canvasRendered = it },
                        onFrameCaptured = { canvasFrame = it },
                        refreshFrameEveryMs = meshRefreshMs,
                        onCoverChanged = { canvasCover.floatValue = it },
                        bottomFade = HERO_FADE_FRACTION,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .height(heroHeight)
                            .hazeSource(playerHaze),
                    )
                }
            }

            if (heroVisible > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(statusBarTop + topStrip)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.38f * heroVisible),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .pointerInput(showAudioPipeline, panelScrolling) {
                    if (showAudioPipeline || panelScrolling) return@pointerInput
                    var total = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { total = 0f },
                        onDragCancel = { swipeOffset = 0f },
                        onDragEnd = {
                            when {
                                total <= -swipeThreshold -> {
                                    haptics.play(Haptic.SkipNext)
                                    onNext()
                                }
                                total >= swipeThreshold -> {
                                    haptics.play(Haptic.SkipPrevious)
                                    onPrevious()
                                }
                            }
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, delta ->
                            total += delta
                            swipeOffset = total * 0.35f
                        },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topStrip),
                contentAlignment = Alignment.Center,
            ) {
                if (!docked) {
                    // Sleek iOS grab bar pill
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(
                                y = lerp(
                                    6.dp,
                                    (topStrip - 5.dp).coerceAtLeast(0.dp) / 2,
                                    p,
                                ),
                            )
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.32f)),
                    )
                }
                if (p < 0.999f) {
                    Text(
                        text = playedBy?.let {
                            stringResource(R.string.played_by, it)
                        } ?: song.radioName?.let {
                            stringResource(R.string.playing_radio, it)
                        } ?: stringResource(
                            R.string.playing_from,
                            song.playbackSource ?: song.albumName ?: stringResource(R.string.queue),
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.2.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = if (isLightArtwork) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(if (docked) Alignment.Center else Alignment.BottomCenter)
                            .graphicsLayer { alpha = 1f - p }
                            .clickable {
                                if (playedBy != null) {
                                    onListenTogether()
                                } else if (song.playbackSourceType == PlaybackSourceType.QUEUE) {
                                    queueOpen = true
                                    closeLyrics()
                                } else {
                                    onOpenPlaybackSource()
                                }
                            }
                            .padding(start = PLAYER_GUTTER, end = PLAYER_GUTTER, bottom = 1.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { dismissBandSpace = it }
                    .pointerInput(showAudioPipeline, panelScrolling) {
                        if (showAudioPipeline || panelScrolling) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val space = dismissBandSpace
                            val y = space?.localToRoot(down.position)?.y ?: down.position.y
                            val panelUp = queueOpen || lyricsOpen || queueSlide.floatValue > 0.01f
                            val bandTop: Float
                            val bandBottom: Float
                            if (panelUp) {
                                bandTop = space?.positionInRoot()?.y ?: 0f
                                bandBottom = bandTop + (ART_BOX_TOP_PAD + HEADER_HEIGHT).toPx()
                            } else {
                                bandTop = dismissBandTop
                                bandBottom = dismissBandBottom
                            }
                            if (y >= bandTop && y <= bandBottom) {
                                if (!panelUp) {
                                    dragQueueIn(
                                        down = down,
                                        travel = bandBottom - bandTop - HEADER_HEIGHT.toPx(),
                                        slide = queueSlide,
                                        onHold = { queueDragging = it },
                                        onSettle = { open ->
                                            if (open != queueOpen) {
                                                haptics.play(if (open) Haptic.Expand else Haptic.Tap)
                                                queueOpen = open
                                            }
                                            queueReleased++
                                        },
                                    )
                                }
                                return@awaitEachGesture
                            }
                            val drag = awaitVerticalTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                            }
                            if (drag != null) verticalDrag(drag.id) { it.consume() }
                        }
                    }
                    .padding(horizontal = PLAYER_GUTTER),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val showNerdStats by AppSettings.showNerdStats.collectAsStateWithLifecycle()
                val nerdStats by NerdStats.current.collectAsStateWithLifecycle()
                val smartFadeOn by AppSettings.smartFadeEnabled.collectAsStateWithLifecycle()
                val mixing by AppSettings.smartMixInProgress.collectAsStateWithLifecycle()
                val smartAnalysis by AppSettings.smartAnalysis.collectAsStateWithLifecycle()
                var controlSpread by remember { mutableStateOf(lastControlSpread) }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = PLAYER_MAX_WIDTH)
                        .fillMaxWidth()
                        .padding(top = ART_BOX_TOP_PAD, bottom = 18.dp),
                ) {
                    val roomy = maxHeight + controlSpread
                    val wantArt = minOf(maxWidth, roomy - ART_TITLE_GAP - HEADER_HEIGHT)
                    val fullArt = minOf(wantArt, maxHeight - ART_TITLE_GAP - HEADER_HEIGHT)
                        .coerceAtLeast(THUMB_SIZE)
                    val slack = (roomy - wantArt - ART_TITLE_GAP - HEADER_HEIGHT).coerceAtLeast(0.dp)

                    if (!lyricsOpen && p == 0f) {
                        val target = with(density) {
                            val half = slack.coerceAtMost(CONTROL_GAP_SPREAD_MAX * 2).toPx().div(2f).roundToInt()
                            (half * 2).toDp()
                        }
                        val granted = with(density) {
                            val steppedPx = (controlSpread.toPx() + (target.toPx() - controlSpread.toPx()) * 0.4f).roundToInt()
                            steppedPx.toDp()
                        }
                        if (granted != controlSpread) {
                            SideEffect {
                                controlSpread = granted
                                lastControlSpread = granted
                            }
                        }
                    }

                    val groupTop = (maxHeight - fullArt - ART_TITLE_GAP - HEADER_HEIGHT).coerceAtLeast(0.dp) / 2
                    val artSize = lerp(fullArt, THUMB_SIZE, p)
                    val artTop = lerp(groupTop, 0.dp, p)
                    val artStart = lerp((maxWidth - fullArt) / 2, 0.dp, p)
                    val titleTop = lerp(groupTop + fullArt + ART_TITLE_GAP, 0.dp, p)
                    val titleStart = lerp(0.dp, THUMB_SIZE + 12.dp, p)

                    val bannerBottom = statusBarTop + topStrip + ART_BOX_TOP_PAD + groupTop + fullArt + ART_TITLE_GAP / 2
                    val bannerSettled = !lyricsOpen || heroHeight == 0.dp
                    if (bannerSettled && bannerBottom != heroHeight) {
                        SideEffect { heroHeight = bannerBottom }
                    }

                    // Authentic Apple Music Squircle Radius
                    val cornerRadius = lerp(22.dp, 8.dp, p)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(artStart.roundToPx(), artTop.roundToPx()) }
                            .size(artSize)
                            .onGloballyPositioned { dismissBandTop = it.boundsInRoot().top }
                            .graphicsLayer {
                                val idle = artScale + (1f - artScale) * p
                                scaleX = idle
                                scaleY = idle
                                translationX = swipeSettle * (1f - p)
                            }
                            .then(
                                if (queueOpen || lyricsOpen) {
                                    Modifier.clickable {
                                        queueOpen = false
                                        closeLyrics()
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // iOS Dynamic Ambient Shadow / Glow
                        if (artLoaded && p < 0.25f) {
                            AsyncImage(
                                model = artRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize(0.94f)
                                    .graphicsLayer {
                                        translationY = 22.dp.toPx()
                                        alpha = (1f - p * 4f).coerceIn(0f, 1f) * 0.58f
                                    }
                                    .blur(32.dp)
                                    .clip(RoundedCornerShape(22.dp)),
                            )
                        }

                        // Main Artwork Card
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(playerHaze)
                                .graphicsLayer {
                                    alpha = if (heroArtLoaded || canvasRendered) 1f - heroVisible else 1f
                                }
                                .shadow(
                                    if (artLoaded) lerp(18.dp, 6.dp, p) else 0.dp,
                                    RoundedCornerShape(cornerRadius),
                                    spotColor = Color.Black.copy(alpha = 0.45f),
                                )
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color.Black.copy(alpha = 0.24f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!artLoaded && !canvasRendered) {
                                Icon(
                                    imageVector = BitChordIcons.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(lerp(40.dp, 20.dp, p)),
                                )
                            }
                            AsyncImage(
                                model = artRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                onState = {
                                    artLoaded = it is AsyncImagePainter.State.Success
                                    if (it is AsyncImagePainter.State.Success) artFailed = false
                                    if (it is AsyncImagePainter.State.Error) artFailed = true
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithContent { if (artLoaded || !canvasRendered) drawContent() },
                            )

                            if (!heroMode) {
                                canvas?.takeIf { p < 0.5f }?.let { clip ->
                                    CanvasArtworkPlayer(
                                        canvas = clip,
                                        isPlaying = isPlaying,
                                        onRenderedChanged = { canvasRendered = it },
                                        onFrameCaptured = { canvasFrame = it },
                                        refreshFrameEveryMs = meshRefreshMs,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        if (showNerdStats && p < 0.5f) {
                            val nerdStyle = MaterialTheme.typography.labelSmall.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 4f,
                                ),
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .graphicsLayer { alpha = 1f - p * 2f },
                            ) {
                                nerdStats?.describe(context)?.let { stats ->
                                    Text(
                                        text = stats,
                                        style = nerdStyle,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                if (smartFadeOn) {
                                    Text(
                                        text = if (song.isVideoOrigin) {
                                            stringResource(R.string.automix_not_supported_video)
                                        } else {
                                            stringResource(
                                                R.string.automix_analysis_status,
                                                smartAnalysis.current.localizedLabel(),
                                                smartAnalysis.next.localizedLabel(),
                                            )
                                        },
                                        style = nerdStyle,
                                        color = Color.White.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    if ((song.isVideo || isAudioVersion) && !lyricsOpen && p < 0.5f) {
                        VideoAudioVersionButton(
                            audioVersion = isAudioVersion,
                            loading = audioVersionSwitching,
                            onClick = onToggleAudioVersion,
                            hazeState = playerHaze,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = artTop + VERSION_PILL_ART_INSET),
                        )
                    }

                    val swipeHintProgress = (abs(swipeSettle) / swipeThreshold).coerceIn(0f, 1f) * (1f - p)
                    if (swipeHintProgress > 0.01f) {
                        val showNext = swipeSettle > 0f
                        val enabled = if (showNext) hasNext else hasPrevious
                        Icon(
                            imageVector = if (showNext) Icons.Rounded.FastForward else Icons.Rounded.FastRewind,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = swipeHintProgress * if (enabled) 0.85f else 0.3f),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = artTop + artSize + (ART_TITLE_GAP - 16.dp) / 2)
                                .size(16.dp),
                        )
                    }

                    // Cupertino Titles & Credits Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = titleTop - lerp(0.dp, (HEADER_HEIGHT - THUMB_SIZE) / 2, p))
                            .padding(start = titleStart)
                            .height(HEADER_HEIGHT)
                            .onGloballyPositioned { dismissBandBottom = it.boundsInRoot().bottom },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            val titleSize = lerp(21.sp, 16.sp, p)
                            var titleOverflowing by remember { mutableStateOf(false) }
                            val scrolls = p < 0.01f
                            MarqueeText(
                                text = song.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = titleSize,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp,
                                ),
                                color = Color.White,
                                enabled = scrolls,
                                leading = if (song.isExplicit == true) {
                                    { ExplicitBadge(color = Color.White) }
                                } else {
                                    null
                                },
                                onOverflowChange = { titleOverflowing = it },
                                modifier = Modifier.opensPage(song.albumId, onOpenAlbum),
                            )
                            MarqueeText(
                                text = song.artist,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = titleSize * 0.95f,
                                    letterSpacing = (-0.2).sp,
                                ),
                                color = Color.White.copy(alpha = 0.55f),
                                enabled = scrolls,
                                startDelayMillis = if (titleOverflowing) MARQUEE_ARTIST_STAGGER_MS else 0L,
                                modifier = Modifier.opensPage(song.artistId, onOpenArtist),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        if (signedIn && song.localUri == null) {
                            val liked = likeStatus == LikeStatus.LIKE
                            CircleGlyph(
                                icon = if (liked) BitChordIcons.HeartFilled else BitChordIcons.Heart,
                                contentDescription = stringResource(
                                    if (liked) R.string.remove_from_liked else R.string.like,
                                ),
                                onClick = onToggleLike,
                                active = liked,
                                haptic = if (liked) Haptic.ToggleOff else Haptic.ToggleOn,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        CircleGlyph(
                            icon = if (showRevertCue) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.MoreHoriz,
                            contentDescription = stringResource(R.string.more),
                            onClick = onOpenMenu,
                        )
                    }

                    if (lyricsOpen && panelsSettled) {
                        LyricsTranslationMotion(
                            trigger = translationTransition,
                            reduceMotion = reduceTranslationMotion,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = HEADER_HEIGHT)
                                .graphicsLayer { alpha = panelFade },
                        ) { particleProgress ->
                            LyricsPanel(
                                lines = displayedLyrics,
                                trackKey = song.videoId,
                                positionMs = lyricsPositionMs,
                                looking = !lyricsUnavailable,
                                isPlaying = isPlaying,
                                onSeekToLine = seekToLyric,
                                controlsOpen = lyricsControlsOpen,
                                onRevealControls = { lyricsControlsOpen = true },
                                onHideControls = { lyricsControlsOpen = false },
                                translationProgress = particleProgress,
                                onScrollingChange = { lyricsScrolling = it },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        val translateShown = lyricsControlsOpen
                        val translateFade by animateFloatAsState(
                            targetValue = if (translateShown) 1f else 0f,
                            animationSpec = tween(if (translateShown) 220 else 160),
                            label = "translateFade",
                        )
                        if (translateFade > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .graphicsLayer { alpha = translateFade },
                            ) {
                                TranslationToggleButton(
                                    state = translationState,
                                    showingTranslation = showingTranslation,
                                    enabled = translateShown && !lyrics.isNullOrEmpty(),
                                    onClick = toggleTranslation,
                                )
                            }
                        }
                    }

                    if (!lyricsOpen && (queueDragging || (queueProgress > 0.01f && panelsSettled))) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = HEADER_HEIGHT)
                                .graphicsLayer {
                                    alpha = if (queueDragging) {
                                        ((queueProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                                    } else {
                                        panelFade
                                    }
                                    translationY = (1f - queueProgress) * 26.dp.toPx()
                                },
                        ) {
                            InlineQueue(
                                queue = queue,
                                currentIndex = queueIndex,
                                autoplayEnabled = autoplayEnabled,
                                onJumpTo = onJumpTo,
                                onRemove = onRemoveFromQueue,
                                onMove = onMoveInQueue,
                                onClear = onClearQueue,
                                onScrollingChange = { queueScrolling = it },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ---- Bottom: Scrubber, Controls, Volume & Actions ----
                AnimatedVisibility(
                    visible = !lyricsOpen || lyricsControlsOpen,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(160)),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = PLAYER_MAX_WIDTH)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (!lyricsOpen && syncedLyricsEnabled) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = 6.dp),
                                ) {
                                    if (displayedLyrics.isNotEmpty()) {
                                        CurrentLyricLine(
                                            lines = displayedLyrics,
                                            trackKey = song.videoId,
                                            positionMs = lyricsPositionMs,
                                            isPlaying = isPlaying,
                                            durationMs = durationMs,
                                            onClick = openLyrics,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    } else if (lyricsUnavailable) {
                                        LyricsUnavailableLine(
                                            trackKey = song.videoId,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    } else {
                                        LyricsLoadingLine(
                                            text = lyricsLoadingText,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                            if (!lyricsOpen && !syncedLyricsEnabled) {
                                Text(
                                    text = "\u00A0",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = 6.dp)
                                        .padding(vertical = 4.dp),
                                )
                            }
                            if (lyricsOpen) {
                                Text(
                                    text = when {
                                        translationState is LyricsTranslationUiState.Loading ->
                                            stringResource(R.string.translating_lyrics_to, translationLanguageName)
                                        showingTranslation ->
                                            stringResource(R.string.lyrics_translated_to, translationLanguageName)
                                        translationState is LyricsTranslationUiState.SameLanguage ->
                                            stringResource(R.string.lyrics_already_in_language, translationLanguageName)
                                        lyricsSource != null -> stringResource(R.string.lyrics_by, lyricsSource.label)
                                        lyricsUnavailable -> stringResource(R.string.no_lyrics_found)
                                        lyrics.isNullOrEmpty() -> lyricsLoadingText
                                        else -> stringResource(R.string.lyrics_saved_with_download)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.55f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = 6.dp)
                                        .padding(vertical = 4.dp),
                                )
                            }

                            val transitionWindow by AppSettings.smartTransitionWindow.collectAsStateWithLifecycle()

                            // iOS Capsule Interactive Scrubber
                            IOSCupertinoScrubber(
                                progress = shown,
                                onScrub = {
                                    scrubbing = true
                                    scrubValue = it
                                },
                                onScrubEnd = {
                                    haptics.play(Haptic.Select)
                                    pendingSeek = scrubValue
                                    onSeekFraction(scrubValue)
                                    scrubbing = false
                                },
                                mixing = mixing && !scrubbing,
                                transitionWindow = transitionWindow
                                    ?.takeIf { !scrubbing && it.end > it.start }
                                    ?.let { it.start..it.end },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            )

                            val wifiQuality by AppSettings.audioQualityWifi.collectAsStateWithLifecycle()
                            val cellularQuality by AppSettings.audioQualityCellular.collectAsStateWithLifecycle()
                            val metered by AppSettings.meteredConnection.collectAsStateWithLifecycle()
                            val effectiveQuality = if (metered == true) cellularQuality else wifiQuality
                            val losslessRequested = effectiveQuality == AudioQuality.LOSSLESS
                            val racingLossless by NerdStats.racingLossless.collectAsStateWithLifecycle()
                            val stillRacing = song.videoId in racingLossless

                            // Timestamps & Lossless Capsule Badge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-4).dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = formatTime((shown * durationMs).toLong()),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = (-0.2).sp,
                                        ),
                                        color = Color.White.copy(alpha = 0.50f),
                                    )
                                    Text(
                                        text = "-" + formatTime(durationMs - (shown * durationMs).toLong()),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = (-0.2).sp,
                                        ),
                                        color = Color.White.copy(alpha = 0.50f),
                                    )
                                }
                                LosslessOrStats(
                                    isLoading = isLoading,
                                    stillRacing = stillRacing,
                                    losslessRequested = losslessRequested,
                                    effectiveQuality = effectiveQuality,
                                    nerdStats = nerdStats,
                                    onBadgeClick = { showAudioPipeline = true },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 8.dp),
                                )
                            }

                            Spacer(Modifier.height(14.dp + controlSpread / 2))

                            // Transport Controls with iOS tactile spring feedback
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TransportGlyph(
                                    icon = R.drawable.ic_player_previous,
                                    contentDescription = stringResource(R.string.widget_previous),
                                    size = 46.dp,
                                    onClick = onPrevious,
                                    enabled = hasPrevious || positionMs > BACK_RESTARTS_AFTER_MS,
                                    haptic = Haptic.SkipPrevious,
                                )
                                if (isLoading || audioVersionSwitching) {
                                    Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(38.dp),
                                        )
                                    }
                                } else {
                                    TransportGlyph(
                                        icon = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play,
                                        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                                        size = 72.dp,
                                        touchSize = 100.dp,
                                        onClick = onPlayPause,
                                        haptic = if (isPlaying) Haptic.Pause else Haptic.Resume,
                                    )
                                }
                                TransportGlyph(
                                    icon = R.drawable.ic_player_next,
                                    contentDescription = stringResource(R.string.widget_next),
                                    size = 46.dp,
                                    onClick = onNext,
                                    enabled = hasNext,
                                    haptic = Haptic.SkipNext,
                                )
                            }

                            Spacer(Modifier.height(18.dp + controlSpread / 2))

                            // Cupertino Volume Bar
                            if (!hideVolumeBar) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.VolumeDown,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    IOSCupertinoScrubber(
                                        progress = volume.value,
                                        onScrub = {
                                            volumeDragging = true
                                            scope.launch { volume.snapTo(it) }
                                            audioManager?.setStreamVolume(
                                                AudioManager.STREAM_MUSIC,
                                                (it * maxVolume).roundToInt(),
                                                0,
                                            )
                                        },
                                        onScrubEnd = { volumeDragging = false },
                                        idleHeight = 5.dp,
                                        activeHeight = 9.dp,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(32.dp))
                            }

                            Spacer(Modifier.height(10.dp))

                            // Bottom Cupertino Pill & Action Matrix
                            BoxWithConstraints(Modifier.fillMaxWidth()) {
                                val widestRow = BOTTOM_ACTION_SIZE * 2 + pillWidth(3)
                                val edgeInset = ((maxWidth - widestRow) / 4).coerceAtLeast(0.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = edgeInset),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BottomGlyph(
                                        icon = BitChordIcons.LyricsQuote,
                                        contentDescription = stringResource(if (lyricsOpen) R.string.close_lyrics else R.string.open_lyrics),
                                        onClick = toggleLyrics,
                                        highlighted = lyricsOpen,
                                    )
                                    AnimatedContent(
                                        targetState = queueOpen,
                                        transitionSpec = {
                                            (fadeIn(tween(180, delayMillis = 140)) togetherWith fadeOut(tween(140)))
                                                .using(SizeTransform(clip = false) { _, _ -> tween(220) })
                                        },
                                        label = "playerBottomPill",
                                    ) { showQueueModes ->
                                        if (showQueueModes) {
                                            Pill {
                                                PillSegment(
                                                    icon = BitChordIcons.Shuffle,
                                                    contentDescription = stringResource(
                                                        if (shuffleEnabled) R.string.shuffle_on else R.string.shuffle_off,
                                                    ),
                                                    onClick = onToggleShuffle,
                                                    highlighted = shuffleEnabled,
                                                    haptic = if (shuffleEnabled) Haptic.ToggleOff else Haptic.ToggleOn,
                                                    tapWindowMs = SHUFFLE_TAP_WINDOW_MS,
                                                )
                                                PillDivider()
                                                PillSegment(
                                                    icon = if (repeatMode == Player.REPEAT_MODE_ONE) null else BitChordIcons.Repeat,
                                                    label = if (repeatMode == Player.REPEAT_MODE_ONE) "1" else null,
                                                    contentDescription = when (repeatMode) {
                                                        Player.REPEAT_MODE_ONE -> stringResource(R.string.repeat_one)
                                                        Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_all)
                                                        else -> stringResource(R.string.repeat_off)
                                                    },
                                                    onClick = onCycleRepeat,
                                                    highlighted = repeatMode != Player.REPEAT_MODE_OFF,
                                                    haptic = when (repeatMode) {
                                                        Player.REPEAT_MODE_OFF -> Haptic.ToggleOn
                                                        Player.REPEAT_MODE_ONE -> Haptic.ToggleOff
                                                        else -> Haptic.Select
                                                    },
                                                )
                                                PillDivider()
                                                PillSegment(
                                                    icon = BitChordIcons.Infinity,
                                                    contentDescription = stringResource(
                                                        if (autoplayEnabled) R.string.autoplay_on else R.string.autoplay_off,
                                                    ),
                                                    onClick = onToggleAutoplay,
                                                    highlighted = autoplayEnabled,
                                                    haptic = if (autoplayEnabled) Haptic.ToggleOff else Haptic.ToggleOn,
                                                    tapWindowMs = AUTOPLAY_TAP_WINDOW_MS,
                                                )
                                            }
                                        } else {
                                            OutputPartyPill(
                                                onOutput = openAudioOutput,
                                                onParty = onListenTogether,
                                            )
                                        }
                                    }
                                    BottomGlyph(
                                        icon = BitChordIcons.Queue,
                                        contentDescription = stringResource(R.string.up_next),
                                        onClick = {
                                            closeLyrics()
                                            queueOpen = !queueOpen
                                        },
                                        highlighted = queueOpen,
                                        haptic = if (queueOpen) Haptic.Tap else Haptic.Expand,
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(20.dp),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                OutputCaption(
                                    accountName = accountName,
                                    onOpenOutput = openAudioOutput,
                                    onOpenParty = onListenTogether,
                                )
                            }
                            Spacer(Modifier.height(18.dp))
                        }
                    }
                }
            }
        }
        if (showAudioPipeline) {
            AudioPipelineDialog(
                hazeState = playerHaze,
                onDismiss = { showAudioPipeline = false },
            )
        }
        if (showAudioOutput) {
            AudioOutputSheet(
                hazeState = playerHaze,
                accountName = accountName,
                onDismiss = { showAudioOutput = false },
            )
        }
        if (lyricsOffsetOpen) {
            LyricsOffsetSheet(
                hazeState = playerHaze,
                onDismiss = onDismissLyricsOffset,
            )
        }
    }
}

/**
 * Authentic Apple Music interactive expanding capsule scrubber.
 */
@Composable
private fun IOSCupertinoScrubber(
    progress: Float,
    onScrub: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    modifier: Modifier = Modifier,
    idleHeight: Dp = 4.dp,
    activeHeight: Dp = 9.dp,
    mixing: Boolean = false,
    transitionWindow: ClosedFloatingPointRange<Float>? = null,
) {
    var isDragging by remember { mutableStateOf(false) }
    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) activeHeight else idleHeight,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "scrubberHeight",
    )

    Box(
        modifier = modifier
            .height(28.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onScrub(fraction)
                    onScrubEnd()
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrub(fraction)
                    },
                    onDragEnd = {
                        isDragging = false
                        onScrubEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        onScrubEnd()
                    },
                    onHorizontalDrag = { change, _ ->
                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        onScrub(fraction)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight),
        ) {
            val corner = CornerRadius(size.height / 2, size.height / 2)
            // Translucent frosted base track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.16f),
                size = size,
                cornerRadius = corner,
            )

            // Automix transition indicator
            if (transitionWindow != null) {
                val startX = size.width * transitionWindow.start
                val endX = size.width * transitionWindow.end
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.28f),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, size.height),
                    cornerRadius = corner,
                )
            }

            // Active progress fill
            drawRoundRect(
                color = Color.White.copy(alpha = 0.90f),
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = corner,
            )
        }
    }
}

internal fun adjustedLyricsPosition(positionMs: Long, offsetMs: Int): Long =
    (positionMs - offsetMs.toLong()).coerceAtLeast(0L)

internal fun adjustedLyricsSeekTarget(lineTimeMs: Long, offsetMs: Int): Long =
    (lineTimeMs + offsetMs.toLong()).coerceAtLeast(0L)

private suspend fun AwaitPointerEventScope.dragQueueIn(
    down: PointerInputChange,
    travel: Float,
    slide: MutableFloatState,
    onHold: (Boolean) -> Unit,
    onSettle: (Boolean) -> Unit,
) {
    if (travel < 1f) return
    var pulled = 0f
    val drag = awaitVerticalTouchSlopOrCancellation(down.id) { change, overSlop ->
        if (overSlop < 0f) {
            pulled = -overSlop
            change.consume()
        }
    }
    if (drag == null || pulled <= 0f) return

    onHold(true)
    val velocity = VelocityTracker()
    velocity.addPointerInputChange(drag)
    slide.floatValue = (pulled / travel).coerceIn(0f, 1f)
    verticalDrag(drag.id) { change ->
        velocity.addPointerInputChange(change)
        pulled -= change.positionChange().y
        slide.floatValue = (pulled / travel).coerceIn(0f, 1f)
        change.consume()
    }

    val flick = -velocity.calculateVelocity().y
    val open = when {
        flick >= QUEUE_FLICK_VELOCITY -> true
        flick <= -QUEUE_FLICK_VELOCITY -> false
        else -> slide.floatValue >= QUEUE_CARRY_FRACTION
    }
    onHold(false)
    onSettle(open)
}

@Composable
private fun WidePlayerControls(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    positionMs: Long,
    durationMs: Long,
    hasPrevious: Boolean,
    hasNext: Boolean,
    repeatMode: Int,
    shuffleEnabled: Boolean,
    autoplayEnabled: Boolean,
    signedIn: Boolean,
    accountName: String?,
    likeStatus: LikeStatus,
    hideVolumeBar: Boolean,
    volume: Animatable<Float, AnimationVector1D>,
    maxVolume: Int,
    audioManager: AudioManager?,
    onVolumeDragging: (Boolean) -> Unit,
    scrubbing: Boolean,
    onScrubbingChange: (Boolean) -> Unit,
    scrubValue: Float,
    onScrubValueChange: (Float) -> Unit,
    onToggleLike: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleAutoplay: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenOutput: () -> Unit,
    onListenTogether: () -> Unit,
    lyricsOpen: Boolean,
    queueOpen: Boolean,
    onToggleLyrics: () -> Unit,
    onToggleQueue: () -> Unit,
    showQueue: Boolean,
    statusContent: @Composable (Modifier) -> Unit,
    lyricsContent: @Composable (Modifier) -> Unit,
    queueContent: @Composable (Modifier) -> Unit,
    legacyMesh: Boolean,
    canvasFrame: Bitmap?,
    artMesh: ArtworkMesh?,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val liveFraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val shown = if (scrubbing) scrubValue else liveFraction

    val artworkScale by animateFloatAsState(
        targetValue = when {
            !isPlaying -> ARTWORK_PAUSE_SHRINK_SCALE
            scrubbing -> ARTWORK_DRAG_SHRINK_SCALE
            else -> ARTWORK_EXPANDED_SCALE
        },
        animationSpec = tween(durationMillis = ARTWORK_SCALE_DURATION_MS, easing = ArtworkScaleEasing),
        label = "wideArtworkScale",
    )

    val panelReady = progress >= 1f
    val panelFade by animateFloatAsState(
        targetValue = if (panelReady) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "widePanelFade",
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (legacyMesh) {
            MeshGradientBackground(
                palette = rememberArtworkColors(song.thumbnailUrl, canvasFrame),
                trackKey = song.videoId,
            )
        } else {
            ArtworkMeshBackdrop(mesh = artMesh, seam = 0.dp)
        }

        val controls: @Composable ColumnScope.() -> Unit = {
            statusContent(Modifier.fillMaxWidth().offset(y = 6.dp))
            IOSCupertinoScrubber(
                progress = shown,
                onScrub = {
                    onScrubbingChange(true)
                    onScrubValueChange(it)
                },
                onScrubEnd = {
                    haptics.play(Haptic.Select)
                    onSeekFraction(scrubValue)
                    onScrubbingChange(false)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().offset(y = (-4).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatTime((shown * durationMs).toLong()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White.copy(alpha = 0.50f),
                )
                Text(
                    text = "-" + formatTime(durationMs - (shown * durationMs).toLong()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White.copy(alpha = 0.50f),
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransportGlyph(
                    icon = R.drawable.ic_player_previous,
                    contentDescription = stringResource(R.string.widget_previous),
                    size = 46.dp,
                    onClick = onPrevious,
                    enabled = hasPrevious || positionMs > BACK_RESTARTS_AFTER_MS,
                    haptic = Haptic.SkipPrevious,
                )
                if (isLoading) {
                    Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                } else {
                    TransportGlyph(
                        icon = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play,
                        contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                        size = 72.dp,
                        touchSize = 100.dp,
                        onClick = onPlayPause,
                        haptic = if (isPlaying) Haptic.Pause else Haptic.Resume,
                    )
                }
                TransportGlyph(
                    icon = R.drawable.ic_player_next,
                    contentDescription = stringResource(R.string.widget_next),
                    size = 46.dp,
                    onClick = onNext,
                    enabled = hasNext,
                    haptic = Haptic.SkipNext,
                )
            }

            Spacer(Modifier.height(18.dp))
            if (!hideVolumeBar) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.VolumeDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    IOSCupertinoScrubber(
                        progress = volume.value,
                        onScrub = { v ->
                            onVolumeDragging(true)
                            scope.launch { volume.snapTo(v) }
                            audioManager?.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                (v * maxVolume).roundToInt(),
                                0,
                            )
                        },
                        onScrubEnd = { onVolumeDragging(false) },
                        idleHeight = 5.dp,
                        activeHeight = 9.dp,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                Spacer(Modifier.height(31.dp))
            }

            Spacer(Modifier.height(18.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val widestRow = BOTTOM_ACTION_SIZE * 2 + pillWidth(3)
                val edgeInset = ((maxWidth - widestRow) / 4).coerceAtLeast(0.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = edgeInset),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BottomGlyph(
                        icon = BitChordIcons.LyricsQuote,
                        contentDescription = stringResource(if (lyricsOpen) R.string.close_lyrics else R.string.open_lyrics),
                        onClick = onToggleLyrics,
                        highlighted = lyricsOpen,
                    )
                    AnimatedContent(
                        targetState = queueOpen,
                        transitionSpec = {
                            (fadeIn(tween(180, delayMillis = 140)) togetherWith fadeOut(tween(140)))
                                .using(SizeTransform(clip = false) { _, _ -> tween(220) })
                        },
                        label = "widePlayerBottomPill",
                    ) { showQueueModes ->
                        if (showQueueModes) {
                            Pill {
                                PillSegment(
                                    icon = BitChordIcons.Shuffle,
                                    contentDescription = stringResource(
                                        if (shuffleEnabled) R.string.shuffle_on else R.string.shuffle_off,
                                    ),
                                    onClick = onToggleShuffle,
                                    highlighted = shuffleEnabled,
                                    haptic = if (shuffleEnabled) Haptic.ToggleOff else Haptic.ToggleOn,
                                    tapWindowMs = SHUFFLE_TAP_WINDOW_MS,
                                )
                                PillDivider()
                                PillSegment(
                                    icon = if (repeatMode == Player.REPEAT_MODE_ONE) null else BitChordIcons.Repeat,
                                    label = if (repeatMode == Player.REPEAT_MODE_ONE) "1" else null,
                                    contentDescription = when (repeatMode) {
                                        Player.REPEAT_MODE_ONE -> stringResource(R.string.repeat_one)
                                        Player.REPEAT_MODE_ALL -> stringResource(R.string.repeat_all)
                                        else -> stringResource(R.string.repeat_off)
                                    },
                                    onClick = onCycleRepeat,
                                    haptic = when (repeatMode) {
                                        Player.REPEAT_MODE_OFF -> Haptic.ToggleOn
                                        Player.REPEAT_MODE_ONE -> Haptic.ToggleOff
                                        else -> Haptic.Select
                                    },
                                    highlighted = repeatMode != Player.REPEAT_MODE_OFF,
                                )
                                PillDivider()
                                PillSegment(
                                    icon = BitChordIcons.Infinity,
                                    contentDescription = stringResource(
                                        if (autoplayEnabled) R.string.autoplay_on else R.string.autoplay_off,
                                    ),
                                    onClick = onToggleAutoplay,
                                    highlighted = autoplayEnabled,
                                    haptic = if (autoplayEnabled) Haptic.ToggleOff else Haptic.ToggleOn,
                                    tapWindowMs = AUTOPLAY_TAP_WINDOW_MS,
                                )
                            }
                        } else {
                            OutputPartyPill(onOutput = onOpenOutput, onParty = onListenTogether)
                        }
                    }
                    BottomGlyph(
                        icon = BitChordIcons.Queue,
                        contentDescription = stringResource(R.string.up_next),
                        onClick = onToggleQueue,
                        highlighted = queueOpen,
                        haptic = if (queueOpen) Haptic.Tap else Haptic.Expand,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                OutputCaption(
                    accountName = accountName,
                    onOpenOutput = onOpenOutput,
                    onOpenParty = onListenTogether,
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            val contentWidth = maxWidth.coerceAtMost(WIDE_LYRICS_MAX_WIDTH)
            val halfLane = contentWidth / 2
            val openLaneWidth = halfLane - PLAYER_GUTTER * 2
            val restLaneWidth = PLAYER_MAX_WIDTH.coerceAtMost(contentWidth - PLAYER_GUTTER * 2)
            val laneWidth = lerp(restLaneWidth, openLaneWidth, progress)
            val laneShift = lerp(0.dp, -(contentWidth / 4), progress)

            Box(
                modifier = Modifier
                    .width(contentWidth)
                    .fillMaxHeight(),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = laneShift)
                        .width(laneWidth)
                        .fillMaxHeight()
                        .padding(vertical = DOCKED_TOP_PAD),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Spacer(Modifier.height(12.dp))
                    WideArtwork(
                        song = song,
                        scale = artworkScale,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .aspectRatio(1f)
                            .align(Alignment.CenterHorizontally),
                    )
                    Spacer(Modifier.height(20.dp))
                    WideCredits(
                        song = song,
                        signedIn = signedIn,
                        likeStatus = likeStatus,
                        onToggleLike = onToggleLike,
                        onOpenMenu = onOpenMenu,
                        onOpenAlbum = onOpenAlbum,
                        onOpenArtist = onOpenArtist,
                    )
                    Spacer(Modifier.height(18.dp))
                    controls()
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(halfLane)
                        .fillMaxHeight()
                        .graphicsLayer { alpha = panelFade }
                        .padding(vertical = DOCKED_TOP_PAD),
                ) {
                    if (panelReady || panelFade > 0.01f) {
                        AnimatedContent(
                            targetState = showQueue,
                            transitionSpec = {
                                fadeIn(tween(200, delayMillis = 90)) togetherWith fadeOut(tween(140))
                            },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            label = "widePanel",
                        ) { queue ->
                            if (queue) {
                                queueContent(Modifier.fillMaxSize())
                            } else {
                                lyricsContent(Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WideArtwork(song: Song, scale: Float, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val artUrl = song.artworkAt(ART_PX)
    var artLoaded by remember(artUrl) { mutableStateOf(false) }
    val request = remember(context, artUrl) {
        ImageRequest.Builder(context)
            .data(artUrl)
            .size(ART_PX)
            .build()
    }
    Box(
        modifier = modifier
            .scale(scale)
            .shadow(if (artLoaded) 18.dp else 0.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Black.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!artLoaded) {
            Icon(
                imageVector = BitChordIcons.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxSize(0.36f),
            )
        }
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = { artLoaded = it is AsyncImagePainter.State.Success },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun WideCredits(
    song: Song,
    signedIn: Boolean,
    likeStatus: LikeStatus,
    onToggleLike: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            var titleOverflowing by remember { mutableStateOf(false) }
            MarqueeText(
                text = song.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
                onOverflowChange = { titleOverflowing = it },
                leading = if (song.isExplicit == true) {
                    { ExplicitBadge(Color.White) }
                } else {
                    null
                },
                modifier = Modifier.opensPage(song.albumId, onOpenAlbum),
            )
            Spacer(Modifier.height(2.dp))
            MarqueeText(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.2).sp,
                ),
                color = Color.White.copy(alpha = 0.55f),
                startDelayMillis = if (titleOverflowing) MARQUEE_ARTIST_STAGGER_MS else 0L,
                modifier = Modifier.opensPage(song.artistId, onOpenArtist),
            )
        }
        Spacer(Modifier.width(10.dp))
        if (signedIn && song.localUri == null) {
            val liked = likeStatus == LikeStatus.LIKE
            CircleGlyph(
                icon = if (liked) BitChordIcons.HeartFilled else BitChordIcons.Heart,
                contentDescription = stringResource(
                    if (liked) R.string.remove_from_liked else R.string.like,
                ),
                onClick = onToggleLike,
                active = liked,
                haptic = if (liked) Haptic.ToggleOff else Haptic.ToggleOn,
            )
            Spacer(Modifier.width(8.dp))
        }
        CircleGlyph(
            icon = Icons.Rounded.MoreHoriz,
            contentDescription = stringResource(R.string.more),
            onClick = onOpenMenu,
        )
    }
}

@Composable
private fun rememberLyricClock(positionMs: Long, isPlaying: Boolean): MutableLongState {
    val clock = remember { mutableLongStateOf(positionMs) }
    val foreground = rememberIsForeground()
    LaunchedEffect(positionMs, isPlaying, foreground) {
        clock.longValue = reconcileLyricPosition(clock.longValue, positionMs)
        if (!isPlaying || !foreground) return@LaunchedEffect
        val firstFrame = withFrameMillis { it }
        while (true) {
            withFrameMillis { frame ->
                clock.longValue = maxOf(clock.longValue, positionMs + frame - firstFrame)
            }
        }
    }
    return clock
}

@Composable
private fun SweptLyricLine(
    line: LyricLine,
    clock: MutableLongState,
    style: TextStyle,
    dimAlpha: Float,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    glowAlpha: Float = 0f,
    glowRadius: Dp = GLOW_RADIUS,
    glowRoom: Dp = 0.dp,
    feather: Boolean = false,
    rise: Boolean = true,
    alignEnd: Boolean = false,
    translationProgress: State<Float>? = null,
) {
    var layout by remember(line) { mutableStateOf<TextLayoutResult?>(null) }
    val growth = remember { CharGrowth() }
    val room = if (glowRoom > 0.dp) Modifier.padding(glowRoom) else Modifier

    val riseAgainst: (Modifier) -> Modifier = { inner ->
        if (!rise) {
            inner
        } else {
            Modifier
                .drawWithContent {
                    val measured = layout
                    if (measured == null || line.words.isEmpty()) {
                        drawContent()
                    } else {
                        riseWith(
                            layout = measured,
                            line = line,
                            positionMs = clock.longValue,
                            inset = glowRoom.toPx(),
                            peak = WORD_RISE.toPx(),
                            growth = growth,
                        )
                    }
                }
                .then(inner)
        }
    }

    val sweep = Modifier.drawWithContent {
        val position = clock.longValue
        when {
            position >= line.endMs -> drawContent()
            position <= line.timeMs -> Unit
            else -> layout?.let { sweepTo(it, line.revealedChars(position), feather) }
        }
    }

    Box(
        modifier.lyricParticles(layout, translationProgress, glowRoom),
        contentAlignment = if (alignEnd) Alignment.TopEnd else Alignment.TopStart,
    ) {
        Text(
            text = line.text,
            style = style,
            color = Color.White.copy(alpha = dimAlpha),
            maxLines = maxLines,
            overflow = overflow,
            onTextLayout = { layout = it },
            modifier = riseAgainst(room),
        )
        if (glowAlpha > 0.01f) {
            Text(
                text = line.text,
                style = style,
                color = Color.White,
                maxLines = maxLines,
                overflow = overflow,
                modifier = Modifier
                    .graphicsLayer { alpha = glowAlpha }
                    .blur(glowRadius, BlurredEdgeTreatment.Unbounded)
                    .then(room)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        val measured = layout ?: return@drawWithContent
                        glowGrown(
                            layout = measured,
                            line = line,
                            positionMs = clock.longValue,
                            inset = glowRoom.toPx(),
                            peak = WORD_RISE.toPx(),
                            growth = growth,
                        )
                    },
            )
        }
        Text(
            text = line.text,
            style = style,
            color = Color.White,
            maxLines = maxLines,
            overflow = overflow,
            modifier = riseAgainst(
                Modifier
                    .graphicsLayer {
                        compositingStrategy = if (feather) CompositingStrategy.Offscreen else CompositingStrategy.Auto
                    }
                    .then(room)
                    .then(sweep),
            ),
        )
    }
}

private fun ContentDrawScope.glowGrown(
    layout: TextLayoutResult,
    line: LyricLine,
    positionMs: Long,
    inset: Float,
    peak: Float,
    growth: CharGrowth,
) {
    if (!line.isGrowing(positionMs)) return
    val em = layout.layoutInput.style.fontSize.toPx()
    val length = layout.layoutInput.text.length
    for (word in line.growingWords) {
        if (positionMs < word.startMs || positionMs > word.restsAtMs) continue
        val span = line.wordSpans[word.index]
        val fall = line.wordFall(word.index, positionMs)
        for (char in span.first..minOf(span.last, length - 1)) {
            word.sampleInto(char - span.first, positionMs, growth)
            if (growth.bloom <= 0.01f) continue
            val visualLine = layout.getLineForOffset(char)
            val from = layout.xOn(char, visualLine, inset)
            val to = layout.xOn(char + 1, visualLine, inset)
            if (to <= from) continue
            val dx = growth.shift * em
            val dy = -growth.rise * peak * fall
            val rowTop = layout.getLineTop(visualLine) + inset
            val bottom = layout.getLineBottom(visualLine) + inset
            val overhang = (to - from) * (growth.scale - 1f) / 2f
            clipRect(
                left = from - overhang + dx,
                top = rowTop - peak * GROW_HEADROOM,
                right = to + overhang + dx,
                bottom = bottom,
            ) {
                translate(left = dx, top = dy) {
                    scale(
                        growth.scale,
                        growth.scale,
                        Offset((from + to) / 2f, (rowTop + bottom) / 2f),
                    ) {
                        this@glowGrown.drawContent()
                    }
                }
                drawRect(
                    color = Color.White.copy(alpha = growth.bloom),
                    blendMode = BlendMode.DstIn,
                )
            }
        }
    }
}

private fun ContentDrawScope.riseWith(
    layout: TextLayoutResult,
    line: LyricLine,
    positionMs: Long,
    inset: Float,
    peak: Float,
    growth: CharGrowth,
) {
    if (!line.isLifted(positionMs)) {
        drawContent()
        return
    }
    val em = layout.layoutInput.style.fontSize.toPx()
    for (visualLine in 0 until layout.lineCount) {
        val lineStart = layout.getLineStart(visualLine)
        val lineEnd = layout.getLineEnd(visualLine, visibleEnd = true)
        val top = layout.getLineTop(visualLine) + inset
        val bottom = layout.getLineBottom(visualLine) + inset
        var at = lineStart
        var edge = layout.getLineLeft(visualLine) + inset
        for (index in line.words.indices) {
            val span = line.wordSpans[index]
            val start = maxOf(span.first, lineStart)
            val end = minOf(span.last + 1, lineEnd)
            if (start >= end) continue
            val held = line.growingAt(index)?.takeIf { positionMs in it.startMs..it.restsAtMs }
            val lift = line.wordLift(index, positionMs)
            if (held == null && lift <= 0.01f) continue
            val from = layout.xOn(start, visualLine, inset)
            val to = layout.xOn(end, visualLine, inset)
            if (to <= from) continue
            if (start > at) sliceRisen(edge, top, from, bottom, 0f)
            if (held != null) {
                growEach(
                    layout, held, line, positionMs, visualLine,
                    start, end, top, bottom, inset, peak, em, growth,
                )
            } else {
                sliceRisen(from, top - peak, to, bottom, -lift * peak)
            }
            at = end
            edge = to
        }
        if (at < lineEnd) {
            sliceRisen(edge, top, layout.getLineRight(visualLine) + inset, bottom, 0f)
        }
    }
}

@Suppress("LongParameterList")
private fun ContentDrawScope.growEach(
    layout: TextLayoutResult,
    word: GrowingWord,
    line: LyricLine,
    positionMs: Long,
    visualLine: Int,
    start: Int,
    end: Int,
    top: Float,
    bottom: Float,
    inset: Float,
    peak: Float,
    em: Float,
    growth: CharGrowth,
) {
    val fall = line.wordFall(word.index, positionMs)
    val first = line.wordSpans[word.index].first
    val ceiling = top - peak * GROW_HEADROOM
    val middle = (top + bottom) / 2f
    for (char in start until end) {
        word.sampleInto(char - first, positionMs, growth)
        val from = layout.xOn(char, visualLine, inset)
        val to = layout.xOn(char + 1, visualLine, inset)
        if (to <= from) continue
        val dx = growth.shift * em
        val dy = -growth.rise * peak * fall
        val overhang = (to - from) * (growth.scale - 1f) / 2f
        clipRect(
            left = from - overhang + dx,
            top = ceiling,
            right = to + overhang + dx,
            bottom = bottom,
        ) {
            translate(left = dx, top = dy) {
                scale(growth.scale, growth.scale, Offset((from + to) / 2f, middle)) {
                    this@growEach.drawContent()
                }
            }
        }
    }
}

private fun TextLayoutResult.xOn(offset: Int, visualLine: Int, inset: Float): Float {
    val left = getLineLeft(visualLine) + inset
    val right = getLineRight(visualLine) + inset
    return when {
        offset <= getLineStart(visualLine) -> left
        offset >= getLineEnd(visualLine, visibleEnd = true) -> right
        else -> (getHorizontalPosition(offset, usePrimaryDirection = true) + inset).coerceIn(left, right)
    }
}

private fun ContentDrawScope.sliceRisen(
    from: Float,
    top: Float,
    to: Float,
    bottom: Float,
    dy: Float,
) {
    if (to <= from) return
    clipRect(left = from, top = top, right = to, bottom = bottom) {
        translate(top = dy) { this@sliceRisen.drawContent() }
    }
}

private fun horizontalAt(
    layout: TextLayoutResult,
    chars: Float,
    visualLine: Int,
): Float {
    val lineStart = layout.getLineStart(visualLine)
    val lineEnd = layout.getLineEnd(visualLine, visibleEnd = true)
    val index = chars.toInt().coerceIn(lineStart, lineEnd)
    val here = layout.xOn(index, visualLine, 0f)
    val next = layout.xOn((index + 1).coerceAtMost(lineEnd), visualLine, 0f)
    return here + (next - here) * (chars - index)
}

private fun ContentDrawScope.sweepTo(
    layout: TextLayoutResult,
    revealedChars: Float,
    feather: Boolean,
) {
    if (revealedChars <= 0f) return
    if (revealedChars >= layout.layoutInput.text.length) {
        drawContent()
        return
    }
    for (visualLine in 0 until layout.lineCount) {
        val start = layout.getLineStart(visualLine)
        if (revealedChars <= start) return
        val end = layout.getLineEnd(visualLine, visibleEnd = true)
        val cut = revealedChars < end
        val right = if (cut) horizontalAt(layout, revealedChars, visualLine) else layout.getLineRight(visualLine)
        val top = layout.getLineTop(visualLine)
        val bottom = layout.getLineBottom(visualLine)
        clipRect(
            left = layout.getLineLeft(visualLine),
            top = top,
            right = right,
            bottom = bottom,
        ) {
            this@sweepTo.drawContent()
        }
        if (!feather || !cut) continue
        clipRect(top = top, bottom = bottom) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.White,
                    1f to Color.Transparent,
                    startX = (right - WIPE_FEATHER.toPx()).coerceAtLeast(layout.getLineLeft(visualLine)),
                    endX = right,
                ),
                blendMode = BlendMode.DstIn,
            )
        }
    }
}

@Composable
private fun TranslationToggleButton(
    state: LyricsTranslationUiState,
    showingTranslation: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val active = showingTranslation || state is LyricsTranslationUiState.Loading
    val tint = when {
        !enabled || state is LyricsTranslationUiState.SameLanguage -> Color.White.copy(alpha = 0.42f)
        active -> Color.White
        else -> Color.White.copy(alpha = 0.78f)
    }
    val discAlpha by animateFloatAsState(
        targetValue = if (active) 0.34f else 0.18f,
        label = "translateDisc",
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = discAlpha))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (state is LyricsTranslationUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = tint,
                strokeWidth = 1.7.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = stringResource(
                    if (showingTranslation) R.string.show_original_lyrics else R.string.translate_lyrics,
                ),
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun LyricsTranslationMotion(
    trigger: Int,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (State<Float>?) -> Unit,
) {
    val progress = remember { Animatable(1f) }
    val foreground = rememberIsForeground()
    var consumedTrigger by remember { mutableIntStateOf(trigger) }
    LaunchedEffect(trigger, reduceMotion, foreground) {
        val changed = trigger != consumedTrigger
        consumedTrigger = trigger
        if (!changed || trigger <= 0 || reduceMotion || !foreground) {
            progress.snapTo(1f)
        } else {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = TRANSLATION_MOTION_MS, easing = LinearEasing),
            )
        }
    }

    Box(modifier = modifier) {
        content(progress.asState().takeIf { !reduceMotion && foreground && trigger > 0 })
    }
}

private fun Modifier.lyricParticles(
    layout: TextLayoutResult?,
    progress: State<Float>?,
    room: Dp,
): Modifier {
    if (layout == null || progress == null) return this
    return drawWithCache {
        val text = layout.layoutInput.text.text
        val candidates = text.indices.filter { text[it].isLetterOrDigit() }
        val random = Random(text.hashCode())
        val inset = room.toPx()
        val particles = candidates.shuffled(random).take(PARTICLES_PER_VOICE).map { index ->
            val glyph = layout.getBoundingBox(index)
            TranslationParticle(
                anchor = glyph.center + Offset(inset, inset),
                drift = Offset(
                    (random.nextFloat() - 0.5f) * 12.dp.toPx(),
                    -(5f + random.nextFloat() * 11f).dp.toPx(),
                ),
                radius = (0.65f + random.nextFloat() * 0.65f).dp.toPx(),
                delay = 0.16f * index / text.length.coerceAtLeast(1),
            )
        }
        onDrawWithContent {
            drawContent()
            val value = progress.value
            if (value > 0f && value < 1f) {
                particles.forEach { particle ->
                    val t = ((value - particle.delay) / 0.84f).coerceIn(0f, 1f)
                    val envelope = sin(PI * t).toFloat()
                    val ease = 1f - (1f - t) * (1f - t)
                    val center = particle.anchor + Offset(
                        particle.drift.x * ease,
                        particle.drift.y * ease + 3.dp.toPx() * t * t,
                    )
                    drawCircle(Color.White, particle.radius * 2.7f, center, alpha = envelope * 0.07f)
                    drawCircle(Color.White, particle.radius, center, alpha = envelope * 0.58f)
                }
            }
        }
    }
}

@Composable
private fun LyricsSkeleton(modifier: Modifier = Modifier) {
    val sweep = rememberInfiniteTransition(label = "lyricsSkeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(SKELETON_PERIOD_MS, easing = LinearEasing),
        ),
        label = "sweep",
    )
    BoxWithConstraints(modifier.padding(top = 40.dp)) {
        val column = maxWidth
        Column(verticalArrangement = Arrangement.spacedBy(SKELETON_BLOCK_GAP)) {
            SKELETON_BLOCKS.forEach { rows ->
                Column(verticalArrangement = Arrangement.spacedBy(SKELETON_LEADING)) {
                    rows.forEach { fraction ->
                        Box(
                            Modifier
                                .fillMaxWidth(fraction)
                                .height(SKELETON_BAR)
                                .clip(RoundedCornerShape(4.dp))
                                .drawWithCache {
                                    val full = column.toPx()
                                    val band = full * 0.45f
                                    val startX = -band + sweep.value * (full + band * 2)
                                    val brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.10f),
                                            Color.White.copy(alpha = 0.26f),
                                            Color.White.copy(alpha = 0.10f),
                                        ),
                                        startX = startX,
                                        endX = startX + band,
                                    )
                                    onDrawBehind { drawRect(brush) }
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsPanel(
    lines: List<LyricLine>,
    trackKey: String,
    positionMs: Long,
    looking: Boolean,
    isPlaying: Boolean,
    onSeekToLine: (Long) -> Unit,
    controlsOpen: Boolean,
    onRevealControls: () -> Unit,
    onHideControls: () -> Unit,
    translationProgress: State<Float>? = null,
    onScrollingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val clock = rememberLyricClock(positionMs, isPlaying)
    val isSynced = remember(lines) { lines.any { it.timeMs > 0L } }
    val duet = remember(lines) { lines.any { it.alignment == LyricAlignment.End } }

    val activeRows by remember(lines, isSynced) {
        derivedStateOf {
            if (!isSynced) emptyList() else activeLyricRows(lines, clock.longValue)
        }
    }
    val scrollLine = activeRows.firstOrNull() ?: -1
    val leadLine by remember(lines, isSynced) {
        derivedStateOf {
            if (!isSynced) {
                -1
            } else {
                val now = clock.longValue
                activeLyricRows(lines, now + scrollLead(lines, now)).firstOrNull() ?: -1
            }
        }
    }
    val focusLine = if (leadLine >= 0) leadLine else scrollLine
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect(onScrollingChange)
    }
    val viewportHeight by remember(listState) {
        derivedStateOf { listState.layoutInfo.viewportSize.height }
    }
    val keepScroll = remember(listState) { keepScrollInList(listState) }
    var browsing by remember { mutableStateOf(false) }
    val onBottomHalfTap: () -> Unit = {
        if (!listState.isScrollInProgress) {
            onRevealControls()
        }
    }

    val reduceDynamicBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val lyricsBlur by AppSettings.lyricsBlur.collectAsStateWithLifecycle()
    val reduceAnimation by AppSettings.reduceAnimation.collectAsStateWithLifecycle()

    val glowing = !reduceAnimation && !reduceDynamicBlur && lyricsBlur &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val hideControls by rememberUpdatedState(onHideControls)
    val revealControls by rememberUpdatedState(onRevealControls)
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                browsing = true
            }
        }
    }

    val controlsSlopPx = with(LocalDensity.current) { CONTROLS_SCROLL_SLOP.toPx() }
    val controlsOnScroll = remember(listState, controlsSlopPx) {
        object : NestedScrollConnection {
            private var travel = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    if (travel != 0f && (travel > 0f) != (available.y > 0f)) travel = 0f
                    travel += available.y
                    if (travel <= -controlsSlopPx) {
                        travel = 0f
                        hideControls()
                    } else if (travel >= controlsSlopPx) {
                        travel = 0f
                        revealControls()
                    }
                }
                return Offset.Zero
            }
        }
    }

    val currentLine by rememberUpdatedState(focusLine)
    val activeOnScreen by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any { it.index == currentLine }
        }
    }
    LaunchedEffect(browsing, activeOnScreen, listState.isScrollInProgress) {
        if (browsing && activeOnScreen && !listState.isScrollInProgress) {
            delay(600)
            browsing = false
        }
    }

    LaunchedEffect(browsing, listState.isScrollInProgress) {
        if (browsing && !listState.isScrollInProgress) {
            delay(5_000)
            browsing = false
        }
    }

    var run by remember(lines) { mutableStateOf(ScrollRun(0, 0f, LYRIC_SETTLE_MS)) }
    val since = remember(lines) { mutableFloatStateOf(0f) }
    LaunchedEffect(run.id) {
        if (run.id == 0) return@LaunchedEffect
        animate(
            initialValue = 0f,
            targetValue = run.spanMs,
            animationSpec = tween(run.spanMs.toInt(), easing = LinearEasing),
        ) { value, _ -> since.floatValue = value }
    }
    var placed by remember(trackKey) { mutableStateOf(false) }

    LaunchedEffect(focusLine, browsing, controlsOpen) {
        if (isSynced && !browsing && focusLine >= 0 && focusLine in lines.indices) {
            snapshotFlow { listState.layoutInfo.viewportSize.height }.first { it > 0 }
            val visible = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == focusLine }
            when {
                !placed -> {
                    listState.scrollToItem(focusLine, scrollOffset = 0)
                    placed = true
                }
                visible != null -> {
                    val span = scrollLead(lines, clock.longValue).toInt()
                    run = ScrollRun(run.id + 1, visible.offset.toFloat(), span)
                    listState.animateScrollBy(
                        value = visible.offset.toFloat(),
                        animationSpec = tween(durationMillis = span, easing = LYRIC_EASING),
                    )
                }
                else -> listState.animateScrollToItem(focusLine, scrollOffset = 0)
            }
        }
    }

    if (lines.isEmpty()) {
        val empty = modifier.revealLyricsControlsOnTap(!controlsOpen, onBottomHalfTap)
        if (looking) {
            LyricsSkeleton(empty)
        } else {
            Box(empty, contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_lyrics_for_track),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .bleedHorizontally(PLAYER_GUTTER)
            .nestedScroll(controlsOnScroll)
            .nestedScroll(keepScroll)
            .revealLyricsControlsOnTap(!controlsOpen, onBottomHalfTap)
            .fadingEdges(),
        contentPadding = PaddingValues(
            top = 40.dp - GLOW_ROOM,
            bottom = with(LocalDensity.current) { viewportHeight.toDp() } * 0.8f,
            start = PLAYER_GUTTER - GLOW_ROOM,
            end = PLAYER_GUTTER - GLOW_ROOM,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            if (!isSynced && Genius.isSectionHeader(line.text)) {
                val sectionTitle = line.text.removePrefix("[").removeSuffix("]").trim()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) 6.dp else 24.dp, bottom = 8.dp)
                        .padding(horizontal = GLOW_ROOM),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 11.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = sectionTitle.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.3.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
                return@itemsIndexed
            }

            if (!isSynced && line.isGap) {
                Spacer(Modifier.height(14.dp))
                return@itemsIndexed
            }

            val offset = if (scrollLine < 0) 0 else index - scrollLine
            val distance = abs(offset)
            val isActive = isSynced && index in activeRows
            val step = distance.coerceAtMost(LINE_FALLOFF_ALPHA.lastIndex)
            val blur by animateDpAsState(
                targetValue = when {
                    !isSynced || reduceDynamicBlur || !lyricsBlur || browsing || isActive -> 0.dp
                    else -> LINE_FALLOFF_BLUR[step]
                },
                animationSpec = tween(LYRIC_SETTLE_MS, easing = LYRIC_EASING),
                label = "lyricBlur",
            )
            val lineAlpha by animateFloatAsState(
                targetValue = when {
                    !isSynced -> 0.95f
                    isActive -> 1f
                    browsing -> BROWSING_ALPHA
                    else -> LINE_FALLOFF_ALPHA[step]
                },
                animationSpec = tween(LYRIC_SETTLE_MS, easing = LYRIC_EASING),
                label = "lyricAlpha",
            )
            if (line.isGap) {
                val until = lines.getOrNull(index + 1)?.timeMs ?: line.endMs
                val swell by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = if (isActive) 400 else 350,
                        easing = LYRIC_EASING,
                    ),
                    label = "gapSwell",
                )
                val instrumental = stringResource(R.string.instrumental)
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .height((GAP_ROW_HEIGHT + GAP_ROW_SPACING) * swell)
                        .clipToBounds(),
                ) {
                    Box(
                        modifier = Modifier
                            .blur(blur, BlurredEdgeTreatment.Unbounded)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = isSynced) { onSeekToLine(line.timeMs) }
                            .padding(GLOW_ROOM)
                            .size(
                                width = GAP_DOT_SIZE * 3 + GAP_DOT_GAP * 2,
                                height = GAP_DOT_SIZE,
                            )
                            .graphicsLayer {
                                val grow = GAP_REST_SCALE + (1f - GAP_REST_SCALE) * swell
                                scaleX = grow
                                scaleY = grow
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                alpha = lineAlpha * swell
                            }
                            .drawBehind {
                                val span = (until - line.timeMs).coerceAtLeast(1L)
                                val through = ((clock.longValue - line.timeMs).toFloat() / span).coerceIn(0f, 1f)
                                val radius = GAP_DOT_SIZE.toPx() / 2f
                                val stride = (GAP_DOT_SIZE + GAP_DOT_GAP).toPx()
                                repeat(GAP_DOTS) { dot ->
                                    val lit = (through * GAP_DOTS - dot).coerceIn(0f, 1f)
                                    drawCircle(
                                        color = Color.White.copy(
                                            alpha = GAP_DOT_REST + (1f - GAP_DOT_REST) * lit,
                                        ),
                                        radius = radius,
                                        center = Offset(radius + dot * stride, size.height / 2f),
                                    )
                                }
                            }
                            .semantics { contentDescription = instrumental },
                    )
                }
            } else {
                val alignEnd = duet && line.alignment == LyricAlignment.End
                val style = if (isSynced) {
                    MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 34.sp,
                        lineHeight = 41.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                    )
                } else {
                    MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                    )
                }
                val sung = offset < 0
                val behind = if (run.delta >= 0f) index - focusLine else focusLine - index
                val staggerDelay = behind.coerceIn(0, STAGGER_STEPS) * STAGGER_FRACTION * run.durationMs
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = when {
                        pressed -> PRESSED_SCALE
                        isActive -> 1f
                        else -> INACTIVE_SCALE
                    },
                    animationSpec = tween(
                        durationMillis = if (pressed) 120 else LYRIC_SETTLE_MS,
                        easing = LYRIC_EASING,
                    ),
                    label = "lyricScale",
                )
                val glow by animateFloatAsState(
                    targetValue = if (isActive && glowing) GLOW_ALPHA else 0f,
                    animationSpec = tween(durationMillis = 420),
                    label = "lyricGlow",
                )
                val shape = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (duet && alignEnd) DUET_LANE else 0.dp,
                        end = if (duet && !alignEnd) DUET_LANE else 0.dp,
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(if (alignEnd) 1f else 0f, 0.5f)
                        alpha = lineAlpha
                        translationY = if (staggerDelay <= 0f) {
                            0f
                        } else {
                            val elapsed = since.floatValue
                            run.delta * (
                                LYRIC_EASING.transform((elapsed / run.durationMs).coerceIn(0f, 1f)) -
                                    LYRIC_EASING.transform(((elapsed - staggerDelay) / run.durationMs).coerceIn(0f, 1f))
                            )
                        }
                    }
                    .blur(blur, BlurredEdgeTreatment.Unbounded)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        enabled = isSynced,
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                    ) { onSeekToLine(line.timeMs) }

                AnimatedContent(
                    targetState = line,
                    transitionSpec = {
                        val duration = if (reduceAnimation) 0 else 380
                        val fadeSpec = if (reduceAnimation) snap() else tween<Float>(duration, easing = FastOutSlowInEasing)
                        (fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec)).using(
                            SizeTransform(
                                clip = false,
                                sizeAnimationSpec = { _, _ ->
                                    if (reduceAnimation) snap() else tween(duration, easing = FastOutSlowInEasing)
                                },
                            ),
                        )
                    },
                    label = "lyricsTranslationLine",
                    modifier = shape,
                ) { renderedLine ->
                    Column {
                        PanelVoice(
                            line = renderedLine,
                            clock = clock,
                            style = style,
                            isActive = isActive,
                            sung = sung,
                            synced = isSynced,
                            browsing = browsing,
                            glowAlpha = glow,
                            room = GLOW_ROOM,
                            alignEnd = alignEnd,
                            translationProgress = translationProgress.takeIf {
                                if (isSynced) abs(index - focusLine) <= 1 else index < 4
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        renderedLine.background?.let { backing ->
                            PanelVoice(
                                line = backing.withoutBracketPunctuation(),
                                clock = clock,
                                style = style.copy(
                                    fontSize = BACKING_FONT_SIZE,
                                    lineHeight = BACKING_LINE_HEIGHT,
                                ),
                                isActive = isActive,
                                sung = sung,
                                synced = isSynced,
                                browsing = browsing,
                                glowAlpha = 0f,
                                room = 0.dp,
                                alignEnd = alignEnd,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = GLOW_ROOM, end = GLOW_ROOM, bottom = GLOW_ROOM)
                                    .graphicsLayer { alpha = BACKING_ALPHA },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelVoice(
    line: LyricLine,
    clock: MutableLongState,
    style: TextStyle,
    isActive: Boolean,
    sung: Boolean,
    synced: Boolean,
    browsing: Boolean,
    glowAlpha: Float,
    room: Dp,
    alignEnd: Boolean,
    translationProgress: State<Float>? = null,
    modifier: Modifier = Modifier,
) {
    if (line.isWordSynced && !browsing) {
        val tail by animateFloatAsState(
            targetValue = if (sung) 1f else UNSUNG_ALPHA,
            label = "lyricTail",
        )
        SweptLyricLine(
            line = line,
            clock = clock,
            style = style,
            dimAlpha = tail,
            modifier = modifier,
            glowAlpha = glowAlpha,
            glowRoom = room,
            feather = isActive,
            alignEnd = alignEnd,
            translationProgress = translationProgress,
        )
    } else if (line.isWordSynced) {
        val tail by animateFloatAsState(
            targetValue = if (sung) 1f else UNSUNG_ALPHA,
            label = "lyricTail",
        )
        SweptLyricLine(
            line = line,
            clock = clock,
            style = style,
            dimAlpha = tail,
            modifier = modifier,
            glowAlpha = 0f,
            glowRoom = room,
            alignEnd = alignEnd,
            translationProgress = translationProgress,
        )
    } else {
        val lit by animateFloatAsState(
            targetValue = if (!synced || sung || isActive) 1f else UNSUNG_ALPHA,
            label = "lyricLit",
        )
        var layout by remember(line.text) { mutableStateOf<TextLayoutResult?>(null) }
        Text(
            text = line.text,
            style = style,
            color = Color.White.copy(alpha = lit),
            onTextLayout = { layout = it },
            modifier = modifier.lyricParticles(layout, translationProgress, room).padding(room),
        )
    }
}

private fun LyricLine.withoutBracketPunctuation(): LyricLine = copy(
    text = text.stripParens(),
    words = words.mapNotNull { word ->
        word.text.stripParens().takeIf { it.isNotEmpty() }?.let { word.copy(text = it) }
    },
)

private fun String.stripParens(): String = replace("(", "").replace(")", "").trim()

@Composable
private fun CurrentLyricLine(
    lines: List<LyricLine>,
    trackKey: Any,
    positionMs: Long,
    isPlaying: Boolean,
    durationMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSynced = remember(lines) { lines.any { it.timeMs > 0L } }
    if (!isSynced) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
        ) {
            Icon(
                imageVector = BitChordIcons.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.open_lyrics),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = BitChordIcons.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp),
            )
        }
        return
    }

    val clock = rememberLyricClock(positionMs, isPlaying)
    val index by remember(lines) {
        derivedStateOf { lines.indexOfLast { it.timeMs <= clock.longValue } }
    }
    val current = lines.getOrNull(index)
    val instrumental = current == null || current.isGap
    val firstSung = remember(lines) { lines.indexOfFirst { !it.isGap } }
    val intro = instrumental && firstSung >= 0 && index < firstSung
    val introLines = stringArrayResource(R.array.lyrics_intro_lines)
    val introLine = remember(trackKey) { introLines.random() }
    val text = when {
        intro -> introLine
        instrumental -> stringResource(R.string.instrumental)
        else -> current.text
    }

    val reduceAnimation by AppSettings.reduceAnimation.collectAsStateWithLifecycle()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        if (instrumental) {
            Icon(
                imageVector = BitChordIcons.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        AnimatedContent(
            targetState = Triple(index, current, text),
            transitionSpec = {
                val duration = if (reduceAnimation) 0 else 340
                if (reduceAnimation) {
                    (fadeIn(snap()) togetherWith fadeOut(snap())).using(
                        SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> snap() }),
                    )
                } else {
                    (fadeIn(animationSpec = tween(duration, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { height -> (height * 0.35f).toInt() })
                        .togetherWith(
                            fadeOut(animationSpec = tween(duration, easing = FastOutSlowInEasing)) +
                                slideOutVertically(animationSpec = tween(duration, easing = FastOutSlowInEasing)) { height -> -(height * 0.35f).toInt() },
                        ).using(
                            SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(duration, easing = FastOutSlowInEasing) }),
                        )
                }
            },
            label = "currentLyricTransition",
            modifier = Modifier.weight(1f, fill = false),
        ) { (_, lineItem, lineText) ->
            val itemInstrumental = lineItem == null || lineItem.isGap
            val swept = lineItem?.takeIf { !itemInstrumental && it.isWordSynced }
            if (swept != null) {
                SweptLyricLine(
                    line = swept,
                    clock = clock,
                    style = MaterialTheme.typography.titleMedium,
                    dimAlpha = UNSUNG_ALPHA_STRIP,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    rise = false,
                )
            } else {
                Text(
                    text = lineText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (itemInstrumental) Color.White.copy(alpha = 0.5f) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = BitChordIcons.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun LyricsUnavailableLine(trackKey: Any, modifier: Modifier = Modifier) {
    var visible by remember(trackKey) { mutableStateOf(true) }
    LaunchedEffect(trackKey) {
        delay(LYRICS_UNAVAILABLE_HOLD_MS)
        visible = false
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.55f else 0f,
        animationSpec = tween(durationMillis = LYRICS_UNAVAILABLE_FADE_MS),
        label = "lyricsUnavailableAlpha",
    )
    Text(
        text = stringResource(R.string.lyrics_not_available),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .padding(vertical = 4.dp)
            .graphicsLayer { this.alpha = alpha },
    )
}

@Composable
private fun LyricsLoadingLine(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White.copy(alpha = 0.55f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun VideoAudioVersionButton(
    audioVersion: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .optimizedHazeEffect(
                state = hazeState,
                style = HazeMaterials.regular(MaterialTheme.colorScheme.surface.copy(alpha = 0.16f)),
            )
            .background(Color.White.copy(alpha = 0.04f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VideoAudioTab(
                icon = Icons.Rounded.Videocam,
                contentDescription = stringResource(R.string.revert_to_original),
                selected = !audioVersion,
                enabled = audioVersion && !loading,
                onClick = {
                    haptics.play(Haptic.Tap)
                    onClick()
                },
            )
            VideoAudioTab(
                icon = BitChordIcons.MusicNote,
                contentDescription = stringResource(R.string.convert_to_audio),
                selected = audioVersion,
                enabled = !audioVersion && !loading,
                onClick = {
                    haptics.play(Haptic.Tap)
                    onClick()
                },
                loading = loading,
            )
        }
    }
}

@Composable
private fun VideoAudioTab(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    loading: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (selected) 0.20f else 0f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(17.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = if (selected) 1f else 0.58f),
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun CircleGlyph(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
    haptic: Haptic = Haptic.Tap,
) {
    val haptics = rememberHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "circleGlyphScale",
    )
    val discAlpha by animateFloatAsState(
        targetValue = if (active) 0.34f else 0.18f,
        label = "glyphDisc",
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(Color.White.copy(alpha = discAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.play(haptic)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = icon,
            animationSpec = tween(durationMillis = 180),
            label = "playerMenuGlyph",
        ) { glyph ->
            Icon(
                imageVector = glyph,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun TransportGlyph(
    @DrawableRes icon: Int,
    contentDescription: String,
    size: Dp,
    touchSize: Dp = size,
    onClick: () -> Unit,
    enabled: Boolean = true,
    haptic: Haptic = Haptic.Tap,
) {
    val haptics = rememberHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "transportScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.3f,
        label = "transportAlpha",
    )
    Box(
        modifier = Modifier
            .size(touchSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
            ) {
                haptics.play(haptic)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = alpha),
            modifier = Modifier.size(size),
        )
    }
}

private val BOTTOM_ACTION_SIZE = 44.dp
private val PILL_SEGMENT_WIDTH = 54.dp
private val PILL_HEADPHONES_SIZE = 23.dp
private val PILL_PARTY_SIZE = 22.dp
private val PILL_ICON_SIZE = 24.dp

private fun pillWidth(segments: Int): Dp =
    PILL_SEGMENT_WIDTH * segments + 1.dp * (segments - 1)

@Composable
private fun Pill(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .height(BOTTOM_ACTION_SIZE)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun PillDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(18.dp)
            .background(Color.White.copy(alpha = 0.18f)),
    )
}

@Composable
private fun OutputPartyPill(
    onOutput: () -> Unit,
    onParty: () -> Unit,
) {
    val badge = rememberPartyBadge()
    Pill {
        PillSegment(
            icon = Icons.Rounded.Headphones,
            iconSize = PILL_HEADPHONES_SIZE,
            contentDescription = stringResource(R.string.audio_output),
            onClick = onOutput,
        )
        PillDivider()
        PillSegment(
            icon = Icons.Rounded.Person,
            iconSize = PILL_PARTY_SIZE,
            contentDescription = if (badge.inParty) {
                stringResource(R.string.listen_together_open_count, badge.members)
            } else {
                stringResource(R.string.listen_together_open)
            },
            onClick = onParty,
            highlighted = badge.inParty,
        )
    }
}

@Composable
private fun PillSegment(
    contentDescription: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconSize: Dp = PILL_ICON_SIZE,
    label: String? = null,
    highlighted: Boolean = false,
    haptic: Haptic = Haptic.Tap,
    tapWindowMs: Long = 0L,
) {
    val haptics = rememberHaptics()
    val lastTap = remember { mutableLongStateOf(-tapWindowMs) }
    Box(
        modifier = Modifier
            .width(PILL_SEGMENT_WIDTH)
            .height(BOTTOM_ACTION_SIZE)
            .background(if (highlighted) Color.White.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                val now = SystemClock.uptimeMillis()
                if (now - lastTap.longValue >= tapWindowMs) {
                    lastTap.longValue = now
                    haptics.play(haptic)
                    onClick()
                }
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val tint = Color.White.copy(alpha = if (highlighted) 1f else 0.75f)
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        } else if (label != null) {
            Text(
                text = label,
                color = tint,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun OutputCaption(
    accountName: String?,
    onOpenOutput: () -> Unit,
    onOpenParty: () -> Unit,
) {
    val badge = rememberPartyBadge()
    val outputName = rememberAudioOutputName(accountName)
    val jamName = badge.hostFirstName
        ?.let { stringResource(R.string.listen_together_jam, it) }
        ?: stringResource(R.string.listen_together_jam_unnamed)
    Text(
        text = if (badge.inParty) jamName else outputName,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.2).sp,
        ),
        color = Color.White.copy(alpha = 0.55f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .clickable { if (badge.inParty) onOpenParty() else onOpenOutput() },
    )
}

private data class PartyBadge(
    val inParty: Boolean,
    val members: Int,
    val hostFirstName: String?,
)

private fun ListenTogether.State.badge(): PartyBadge = PartyBadge(
    inParty = inParty,
    members = members.size,
    hostFirstName = members.firstOrNull(PartyMember::isHost)
        ?.displayName
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() },
)

@Composable
private fun rememberPartyBadge(): PartyBadge {
    val badges = remember {
        ListenTogether.state.map { it.badge() }.distinctUntilChanged()
    }
    return badges
        .collectAsStateWithLifecycle(initialValue = ListenTogether.state.value.badge())
        .value
}

@Composable
private fun BottomGlyph(
    icon: ImageVector?,
    contentDescription: String,
    onClick: () -> Unit,
    highlighted: Boolean = false,
    haptic: Haptic = Haptic.Tap,
    label: String? = null,
    tapWindowMs: Long = 0L,
) {
    val haptics = rememberHaptics()
    val lastTap = remember { mutableLongStateOf(-tapWindowMs) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "bottomGlyphScale",
    )
    Box(
        modifier = Modifier
            .size(BOTTOM_ACTION_SIZE)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (highlighted) Color.White.copy(alpha = 0.22f) else Color.Transparent,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                val now = SystemClock.uptimeMillis()
                if (now - lastTap.longValue >= tapWindowMs) {
                    lastTap.longValue = now
                    haptics.play(haptic)
                    onClick()
                }
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val tint = Color.White.copy(alpha = if (highlighted) 1f else 0.75f)
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        } else if (label != null) {
            Text(
                text = label,
                color = tint,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun keepScrollInList(listState: LazyListState) = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = available

    override suspend fun onPreFling(available: Velocity): Velocity =
        if (available.y > 0f && !listState.canScrollBackward) available else Velocity.Zero

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

private fun Modifier.opensPage(browseId: String?, onOpen: (String) -> Unit): Modifier =
    if (browseId == null) {
        this
    } else {
        clip(RoundedCornerShape(6.dp)).clickable { onOpen(browseId) }
    }

private const val MARQUEE_DP_PER_SEC = 26f
private val MARQUEE_GAP = 48.dp
private const val MARQUEE_REST_MS = 5_000L
private const val MARQUEE_ARTIST_STAGGER_MS = 3_000L

@Composable
private fun MarqueeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startDelayMillis: Long = 0L,
    leading: (@Composable () -> Unit)? = null,
    onOverflowChange: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(6.dp))
        }
        if (!enabled) {
            Text(
                text = text,
                style = style,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            return@Row
        }
        BoxWithConstraints(Modifier.weight(1f, fill = false).clipToBounds()) {
            val maxWidthPx = constraints.maxWidth
            val layout = remember(text, style, maxWidthPx) {
                textMeasurer.measure(text = text, style = style, maxLines = 1, softWrap = false)
            }
            val overflowing = layout.size.width > maxWidthPx
            LaunchedEffect(overflowing) { onOverflowChange(overflowing) }

            val travelPx = if (overflowing) {
                layout.size.width + with(density) { MARQUEE_GAP.roundToPx() }
            } else {
                0
            }

            val offsetX = remember { Animatable(0f) }
            LaunchedEffect(text, travelPx, startDelayMillis) {
                offsetX.snapTo(0f)
                if (travelPx <= 0) return@LaunchedEffect
                val pxPerMs = with(density) { MARQUEE_DP_PER_SEC.dp.toPx() } / 1000f
                val scrollMs = (travelPx / pxPerMs).roundToInt().coerceAtLeast(400)
                delay(startDelayMillis)
                while (true) {
                    offsetX.animateTo(-travelPx.toFloat(), tween(scrollMs, easing = LinearEasing))
                    offsetX.snapTo(0f)
                    delay(MARQUEE_REST_MS)
                }
            }

            Row(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarqueeLine(text = text, style = style, color = color)
                if (overflowing) {
                    Spacer(Modifier.width(MARQUEE_GAP))
                    MarqueeLine(text = text, style = style, color = color)
                }
            }
        }
    }
}

@Composable
private fun MarqueeLine(text: String, style: TextStyle, color: Color) {
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
    )
}

@Composable
private fun ExplicitBadge(color: Color) {
    Text(
        text = "E",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
        color = color,
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(3.dp))
            .padding(horizontal = 3.dp, vertical = 0.5.dp),
    )
}

private fun Modifier.bleedHorizontally(gutter: Dp): Modifier = layout { measurable, constraints ->
    val extra = gutter.roundToPx() * 2
    val widened = if (constraints.hasBoundedWidth) {
        constraints.copy(
            minWidth = constraints.minWidth + extra,
            maxWidth = constraints.maxWidth + extra,
        )
    } else {
        constraints
    }
    val placeable = measurable.measure(widened)
    val width = (placeable.width - extra).coerceAtLeast(0)
    layout(width, placeable.height) {
        placeable.place(-(placeable.width - width) / 2, 0)
    }
}

private fun Modifier.fadingEdges(): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fade = 28.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startY = 0f,
                endY = fade,
            ),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startY = size.height - fade,
                endY = size.height,
            ),
            blendMode = BlendMode.DstIn,
        )
    }

@Composable
private fun InlineQueue(
    queue: List<Song>,
    currentIndex: Int,
    autoplayEnabled: Boolean,
    onJumpTo: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onClear: () -> Unit,
    onScrollingChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect(onScrollingChange)
    }
    val keepScroll = remember(listState) { keepScrollInList(listState) }
    val autoplayStart = remember(queue, currentIndex) {
        autoplaySectionStart(queue.map { it.fromAutoplay }, currentIndex)
    }

    val manualRows = queue.subList(0, autoplayStart)
    val autoplayRows = queue.subList(autoplayStart, queue.size)
    val manualKeys = remember(manualRows) { manualRows.stableQueueKeys() }
    val autoplayKeys = remember(autoplayRows) { autoplayRows.stableQueueKeys("autoplay/") }

    val headingShown = autoplayEnabled || autoplayStart < queue.size
    val headingCount = if (headingShown) 1 else 0
    val firstMovable = (currentIndex + 1).coerceIn(0, autoplayStart)
    val manualDrag = rememberQueueDragState(
        listState = listState,
        lazyRange = firstMovable until autoplayStart,
        lazyOffset = 0,
        onMove = onMove,
    )
    val autoplayDrag = rememberQueueDragState(
        listState = listState,
        lazyRange = (autoplayStart + headingCount) until (autoplayStart + headingCount + autoplayRows.size),
        lazyOffset = headingCount,
        onMove = onMove,
    )

    LaunchedEffect(currentIndex) {
        val holding = manualDrag.draggedKey != null || autoplayDrag.draggedKey != null
        if (!holding && currentIndex in queue.indices) {
            listState.scrollToItem(currentIndex + if (currentIndex >= autoplayStart) 1 else 0)
        }
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.queue),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.clear),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .clickable(onClick = onClear)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .bleedHorizontally(PLAYER_GUTTER)
                .nestedScroll(keepScroll)
                .fadingEdges(),
            contentPadding = PaddingValues(horizontal = PLAYER_GUTTER),
        ) {
            itemsIndexed(
                items = manualRows,
                key = { index, _ -> manualKeys[index] },
            ) { index, song ->
                val key = manualKeys[index]
                val dragging = manualDrag.draggedKey == key
                InlineQueueRow(
                    song = song,
                    isCurrent = index == currentIndex,
                    onClick = { onJumpTo(index) },
                    onRemove = { onRemove(index) },
                    draggable = index >= firstMovable,
                    dragging = dragging,
                    onDragStart = { manualDrag.onDragStart(key) },
                    onDrag = manualDrag::onDrag,
                    onDragEnd = manualDrag::onDragEnd,
                    modifier = Modifier
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging) manualDrag.renderOffset else 0f }
                        .then(if (manualDrag.draggedKey != null) Modifier else Modifier.animateItem()),
                )
            }
            if (autoplayEnabled || autoplayStart < queue.size) {
                item(key = "autoplay-heading") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            BitChordIcons.Infinity,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.autoplay),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                            Text(
                                text = if (autoplayStart < queue.size) {
                                    stringResource(R.string.autoplay_queue_description)
                                } else {
                                    stringResource(R.string.autoplay_empty_description)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.55f),
                            )
                        }
                    }
                }
            }
            itemsIndexed(
                items = autoplayRows,
                key = { index, _ -> autoplayKeys[index] },
            ) { index, song ->
                val at = autoplayStart + index
                val key = autoplayKeys[index]
                val dragging = autoplayDrag.draggedKey == key
                InlineQueueRow(
                    song = song,
                    isCurrent = at == currentIndex,
                    onClick = { onJumpTo(at) },
                    onRemove = { onRemove(at) },
                    draggable = true,
                    dragging = dragging,
                    onDragStart = { autoplayDrag.onDragStart(key) },
                    onDrag = autoplayDrag::onDrag,
                    onDragEnd = autoplayDrag::onDragEnd,
                    modifier = Modifier
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (dragging) autoplayDrag.renderOffset else 0f }
                        .then(if (autoplayDrag.draggedKey != null) Modifier else Modifier.animateItem()),
                )
            }
        }
    }
}

private fun List<Song>.stableQueueKeys(prefix: String = ""): List<String> {
    val seen = HashMap<String, Int>()
    return map { song ->
        val n = seen.getOrDefault(song.videoId, 0)
        seen[song.videoId] = n + 1
        if (n == 0) "$prefix${song.videoId}" else "$prefix${song.videoId}#$n"
    }
}

private val QUEUE_EDGE_SCROLL_ZONE = 40.dp
private val QUEUE_EDGE_SCROLL_SPEED = 340.dp

internal fun edgeScrollSpeed(
    top: Float,
    bottom: Float,
    viewportStart: Int,
    viewportEnd: Int,
    zone: Float,
    speed: Float,
): Float {
    if (zone <= 0f) return 0f
    val intoStart = (viewportStart + zone) - top
    val intoEnd = bottom - (viewportEnd - zone)
    val reach = when {
        intoStart > 0f && intoEnd <= 0f -> -intoStart
        intoEnd > 0f && intoStart <= 0f -> intoEnd
        else -> return 0f
    }
    val ramp = speed * (0.2f + 0.8f * (abs(reach) / zone).coerceAtMost(1f))
    return if (reach < 0f) -ramp else ramp
}

@Composable
private fun rememberQueueDragState(
    listState: LazyListState,
    lazyRange: IntRange,
    lazyOffset: Int,
    onMove: (Int, Int) -> Unit,
): QueueDragState {
    val state = remember(listState) { QueueDragState(listState) }
    state.lazyRange = lazyRange
    state.lazyOffset = lazyOffset
    state.onMove = onMove
    with(LocalDensity.current) {
        state.edgeZone = QUEUE_EDGE_SCROLL_ZONE.toPx()
        state.edgeSpeed = QUEUE_EDGE_SCROLL_SPEED.toPx()
    }

    val direction = state.autoScrollDir
    LaunchedEffect(state, direction) {
        if (direction == 0) return@LaunchedEffect
        listState.scroll {
            var previous = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val seconds = ((now - previous) / 1_000_000_000f).coerceAtMost(1f / 30f)
                previous = now
                val scrolled = scrollBy(state.autoScrollSpeed * seconds)
                if (scrolled == 0f) break
                state.onScrolled()
            }
        }
    }
    return state
}

private class QueueDragState(private val listState: LazyListState) {
    var lazyRange: IntRange = IntRange.EMPTY
    var lazyOffset: Int = 0
    var onMove: (Int, Int) -> Unit = { _, _ -> }
    var edgeZone: Float = 0f
    var edgeSpeed: Float = 0f

    var draggedKey by mutableStateOf<Any?>(null)
        private set
    var renderOffset by mutableFloatStateOf(0f)
        private set
    var autoScrollDir by mutableIntStateOf(0)
        private set
    var autoScrollSpeed: Float = 0f
        private set

    private var heldCenter: Float = Float.NaN
    private var awaiting: Int? = null

    fun onDragStart(key: Any) {
        draggedKey = key
        heldCenter = Float.NaN
        renderOffset = 0f
        awaiting = null
        setAutoScroll(0f)
    }

    fun onDrag(deltaY: Float) = settle(deltaY)
    fun onScrolled() = settle(0f)

    fun onDragEnd() {
        draggedKey = null
        heldCenter = Float.NaN
        renderOffset = 0f
        awaiting = null
        setAutoScroll(0f)
    }

    private fun settle(deltaY: Float) {
        val key = draggedKey ?: return
        val items = listState.layoutInfo.visibleItemsInfo
        val dragged = items.find { it.key == key } ?: run {
            setAutoScroll(0f)
            return
        }
        val half = dragged.size / 2f
        if (heldCenter.isNaN()) heldCenter = dragged.offset + half
        heldCenter += deltaY
        holdToSection(items, dragged)

        val top = heldCenter - half
        aimAutoScroll(top, dragged)
        renderOffset = insideViewport(top, dragged.size) - dragged.offset

        awaiting?.let {
            if (dragged.index != it) return
            awaiting = null
        }
        val target = swapTarget(items, dragged) ?: return
        onMove(dragged.index - lazyOffset, target.index - lazyOffset)
        awaiting = target.index
    }

    private fun swapTarget(
        items: List<LazyListItemInfo>,
        dragged: LazyListItemInfo,
    ): LazyListItemInfo? {
        val target = items
            .filter { it.index in lazyRange && it.index != dragged.index }
            .minByOrNull { abs((it.offset + it.size / 2f) - heldCenter) }
            ?: return null
        if (abs(heldCenter - (target.offset + target.size / 2f)) > target.size / 2f) return null
        if (target.index == listState.firstVisibleItemIndex && listState.canScrollBackward) {
            return null
        }
        return target
    }

    private fun aimAutoScroll(top: Float, dragged: LazyListItemInfo) {
        val info = listState.layoutInfo
        val speed = edgeScrollSpeed(
            top = top,
            bottom = top + dragged.size,
            viewportStart = info.viewportStartOffset,
            viewportEnd = info.viewportEndOffset,
            zone = edgeZone,
            speed = edgeSpeed,
        )
        val blocked = when {
            speed < 0f -> dragged.index <= lazyRange.first || !listState.canScrollBackward
            speed > 0f -> dragged.index >= lazyRange.last || !listState.canScrollForward
            else -> true
        }
        setAutoScroll(if (blocked) 0f else speed)
    }

    private fun holdToSection(items: List<LazyListItemInfo>, dragged: LazyListItemInfo) {
        val half = dragged.size / 2f
        items.firstOrNull { it.index == lazyRange.first }?.let {
            heldCenter = heldCenter.coerceAtLeast(it.offset + half)
        }
        items.firstOrNull { it.index == lazyRange.last }?.let {
            heldCenter = heldCenter.coerceAtMost(it.offset + it.size - half)
        }
    }

    private fun insideViewport(top: Float, size: Int): Float {
        val info = listState.layoutInfo
        val minTop = info.viewportStartOffset.toFloat()
        val maxTop = (info.viewportEndOffset - size).toFloat().coerceAtLeast(minTop)
        return top.coerceIn(minTop, maxTop)
    }

    private fun setAutoScroll(speed: Float) {
        autoScrollSpeed = speed
        val direction = when {
            speed > 0f -> 1
            speed < 0f -> -1
            else -> 0
        }
        if (autoScrollDir != direction) autoScrollDir = direction
    }
}

@Composable
private fun InlineQueueRow(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    draggable: Boolean = false,
    dragging: Boolean = false,
    onDragStart: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    val heldOnDispose by rememberUpdatedState(dragging)
    val endDrag by rememberUpdatedState(onDragEnd)
    DisposableEffect(Unit) {
        onDispose { if (heldOnDispose) endDrag() }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (dragging) Color.White.copy(alpha = 0.06f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (draggable) {
            Icon(
                Icons.Rounded.DragHandle,
                contentDescription = stringResource(R.string.drag_to_reorder),
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = (-4).dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                        )
                    },
            )
            Spacer(Modifier.width(4.dp))
        }
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .thumbnailBorder(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            ExplicitSongTitle(
                song = song,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.92f),
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isCurrent) {
            Icon(
                Icons.Rounded.GraphicEq,
                contentDescription = stringResource(R.string.now_playing),
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.remove_from_queue),
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "%d:%02d".format(Locale.ROOT, minutes, seconds)
}

@Composable
private fun LosslessOrStats(
    isLoading: Boolean,
    stillRacing: Boolean,
    losslessRequested: Boolean,
    effectiveQuality: AudioQuality,
    nerdStats: NerdStats.Snapshot?,
    onBadgeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        (stillRacing && nerdStats?.isLossless != true) ||
            (isLoading && losslessRequested && nerdStats == null) -> LosslessLabel(
            text = if (nerdStats?.isHiQuality == true) {
                stringResource(R.string.high_quality_upgrading)
            } else {
                stringResource(R.string.upgrading_quality)
            },
            animated = false,
            onClick = onBadgeClick,
            modifier = modifier,
        )
        nerdStats?.isLossless == true -> LosslessLabel(
            text = stringResource(if (nerdStats.isHiRes) R.string.hi_res_lossless else R.string.lossless),
            animated = true,
            onClick = onBadgeClick,
            modifier = modifier,
        )
        nerdStats?.isDolbyAtmos == true -> LosslessLabel(
            text = "Dolby Atmos",
            animated = true,
            iconPainter = painterResource(R.drawable.ic_dolby_atmos),
            onClick = onBadgeClick,
            modifier = modifier,
        )
        nerdStats?.isHiQuality == true -> LosslessLabel(
            text = stringResource(R.string.high_quality),
            animated = false,
            onClick = onBadgeClick,
            modifier = modifier,
        )
        effectiveQuality == AudioQuality.LOW && nerdStats?.isLowQuality == true -> LosslessLabel(
            text = stringResource(R.string.data_saver),
            animated = false,
            onClick = onBadgeClick,
            modifier = modifier,
        )
        effectiveQuality == AudioQuality.MEDIUM && nerdStats?.isMediumQuality == true -> LosslessLabel(
            text = stringResource(R.string.medium_quality),
            animated = false,
            onClick = onBadgeClick,
            modifier = modifier,
        )
        else -> {}
    }
}

/**
 * Micro-frosted Apple Lossless Badge
 */
@Composable
private fun LosslessLabel(
    text: String,
    animated: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Headphones,
    iconPainter: Painter? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(0.5.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = Color.White.copy(alpha = if (animated) 0.85f else 0.50f)
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(width = 12.dp, height = 9.dp),
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(11.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            if (animated) {
                ShimmerText(text = text)
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.1.sp,
                    ),
                    color = Color.White.copy(alpha = 0.60f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ShimmerText(text: String) {
    var widthPx by remember { mutableIntStateOf(0) }
    val transition = rememberInfiniteTransition(label = "lossless-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "lossless-shimmer-progress",
    )
    val baseColor = Color.White.copy(alpha = 0.60f)
    val brush = if (widthPx <= 0) {
        Brush.linearGradient(listOf(baseColor, baseColor))
    } else {
        val band = widthPx * 0.6f
        val center = -band + progress * (widthPx + 2 * band)
        Brush.linearGradient(
            colorStops = arrayOf(0f to baseColor, 0.5f to Color.White, 1f to baseColor),
            start = Offset(center - band, 0f),
            end = Offset(center + band, 0f),
        )
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            brush = brush,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.1.sp,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.onSizeChanged { widthPx = it.width },
    )
}

private fun NerdStats.Snapshot.describe(context: android.content.Context): String? {
    val parts = buildList {
        codecLabel(mimeType)?.let(::add)
        bitDepth?.let { add(context.getString(R.string.bit_depth, it)) }
        sampleRateHz?.let { add("%.1f kHz".format(Locale.ROOT, it / 1000f)) }
        bitrateKbps?.let { add("$it kbps") }
        channels?.let {
            add(
                when (it) {
                    1 -> context.getString(R.string.mono)
                    2 -> context.getString(R.string.stereo)
                    else -> context.getString(R.string.channel_count, it)
                },
            )
        }
        if (downgraded) add(context.getString(R.string.downgraded_from, claimed?.summary.orEmpty()))
    }
    return parts.joinToString(" · ").takeIf { it.isNotEmpty() }
}

private fun codecLabel(mimeType: String?): String? = when {
    mimeType == null -> null
    mimeType.endsWith("opus") -> "Opus"
    mimeType.endsWith("mp4a-latm") -> "AAC"
    mimeType.endsWith("vorbis") -> "Vorbis"
    mimeType.endsWith("mpeg") -> "MP3"
    mimeType.endsWith("flac") -> "FLAC"
    mimeType.endsWith("alac") -> "ALAC"
    else -> mimeType.substringAfter('/').uppercase(Locale.ROOT)
}

@Composable
private fun TrackAnalysisState.localizedLabel(): String = when (this) {
    TrackAnalysisState.ANALYSED -> stringResource(R.string.analysis_complete)
    TrackAnalysisState.REFINING -> stringResource(R.string.analysis_refining)
    TrackAnalysisState.ANALYSING -> stringResource(R.string.analysis_in_progress)
    TrackAnalysisState.WAITING -> stringResource(R.string.waiting)
    TrackAnalysisState.FAILED -> stringResource(R.string.failed)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private object OverlayBack {
    fun register(view: View, onBack: () -> Unit): Any? {
        val dispatcher = view.findOnBackInvokedDispatcher() ?: return null
        val callback = OnBackInvokedCallback { onBack() }
        dispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_OVERLAY,
            callback,
        )
        return callback
    }

    fun unregister(view: View, callback: Any?) {
        if (callback !is OnBackInvokedCallback) return
        view.findOnBackInvokedDispatcher()?.unregisterOnBackInvokedCallback(callback)
    }
}