@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.uml_chudadi.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uml_chudadi.R
import com.example.uml_chudadi.audio.CardRoomMusicPlayer
import com.example.uml_chudadi.audio.MusicScene
import com.example.uml_chudadi.controller.AiController
import com.example.uml_chudadi.controller.AiStrategy
import com.example.uml_chudadi.controller.GameController
import com.example.uml_chudadi.controller.NeuralAiProfile
import com.example.uml_chudadi.controller.NeuralAiStrategy
import com.example.uml_chudadi.controller.PlayerActionPolicy
import com.example.uml_chudadi.model.AvailableRuleSets
import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.Difficulty
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.PlayedHand
import com.example.uml_chudadi.model.Player
import com.example.uml_chudadi.model.PlayerKind
import com.example.uml_chudadi.model.RuleSet
import com.example.uml_chudadi.model.Suit
import com.example.uml_chudadi.model.ruleSetByIdOrName
import com.example.uml_chudadi.profile.AchievementCatalog
import com.example.uml_chudadi.profile.AchievementDefinition
import com.example.uml_chudadi.profile.AvatarCatalog
import com.example.uml_chudadi.profile.AvatarProfile
import com.example.uml_chudadi.profile.AvatarRarity
import com.example.uml_chudadi.profile.BuiltInAvatar
import com.example.uml_chudadi.profile.GameModeLabel
import com.example.uml_chudadi.profile.MatchRecord
import com.example.uml_chudadi.profile.MatchSettlement
import com.example.uml_chudadi.profile.PlayerProfile
import com.example.uml_chudadi.profile.ProfileChange
import com.example.uml_chudadi.profile.ProfileController
import com.example.uml_chudadi.profile.ProfileProgress
import com.example.uml_chudadi.profile.ProfileStore
import com.example.uml_chudadi.transport.BluetoothClientTransport
import com.example.uml_chudadi.transport.BluetoothHostTransport
import com.example.uml_chudadi.transport.GameMessage
import com.example.uml_chudadi.transport.GameMessageCodec
import com.example.uml_chudadi.transport.GameSnapshot
import com.example.uml_chudadi.transport.GameTransport
import com.example.uml_chudadi.transport.NetworkMoveGuard
import com.example.uml_chudadi.transport.RoomSeat
import com.example.uml_chudadi.transport.RoomSeatKind
import com.example.uml_chudadi.transport.SnapshotPlayer
import com.example.uml_chudadi.transport.TransportEvent
import com.example.uml_chudadi.transport.TransportRole
import com.example.uml_chudadi.transport.addAiToFirstEmpty
import com.example.uml_chudadi.transport.addOrRejoinHuman
import com.example.uml_chudadi.transport.addHumanToFirstEmpty
import com.example.uml_chudadi.transport.bluetoothDiscoverableIntent
import com.example.uml_chudadi.transport.bondedBluetoothDevices
import com.example.uml_chudadi.transport.canStartRoom
import com.example.uml_chudadi.transport.defaultRoomSeats
import com.example.uml_chudadi.transport.discoverBluetoothDevices
import com.example.uml_chudadi.transport.emptyRoomSeats
import com.example.uml_chudadi.transport.hasBluetoothPermissions
import com.example.uml_chudadi.transport.isBluetoothEnabled
import com.example.uml_chudadi.transport.markHumanDisconnected
import com.example.uml_chudadi.transport.normalizedSeats
import com.example.uml_chudadi.transport.removeAi
import com.example.uml_chudadi.transport.resetForRematch
import com.example.uml_chudadi.transport.requiredBluetoothPermissions
import com.example.uml_chudadi.transport.setReady
import com.example.uml_chudadi.transport.setConnected
import com.example.uml_chudadi.transport.toggleAiDifficulty
import com.example.uml_chudadi.transport.uniqueRoomPlayerName
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Felt = Color(0xFF0A5A44)
private val FeltDark = Color(0xFF063527)
private val FeltLight = Color(0xFF138461)
private val Gold = Color(0xFFFFC84A)
private val GoldDeep = Color(0xFFB86F16)
private val Panel = Color(0xFF123E35)
private val Cream = Color(0xFFFFF4D8)
private val Ink = Color(0xFF1A1712)
private val Danger = Color(0xFFC93B31)

private enum class Screen {
    Splash,
    Lobby,
    Profile,
    DifficultySelect,
    Rules,
    Settings,
    Tutorial,
    Nearby,
    Game,
    Result
}

private enum class GameMode {
    HumanVsAi,
    BluetoothRoom
}

private enum class GameStartPhase {
    ReadyToStart,
    Dealing,
    Playing
}

private sealed class PendingProfileSpend {
    data class Rename(val nickname: String) : PendingProfileSpend()
    data class UnlockAvatar(val avatarId: String) : PendingProfileSpend()
    object UnlockCustomAvatar : PendingProfileSpend()
    object ResetStats : PendingProfileSpend()
}

private data class TableMoveAnimation(
    val key: String,
    val playerId: Int,
    val cards: List<Card>
)

private enum class BluetoothEntryMode {
    Choose,
    HostRoom,
    JoinRoom
}

private enum class ConnectionGuidePhase {
    Permission,
    Ready,
    Hosting,
    Searching,
    Joining,
    WaitingStart,
    Error
}

private data class RuleExplanation(
    val title: String,
    val sections: List<RuleSection>
)

private data class RuleSection(
    val title: String,
    val body: String
)

private data class ConnectionGuideStep(
    val index: Int,
    val title: String,
    val body: String,
    val active: Boolean
)

private const val SETTINGS_NAME = "chudadi_settings"
private const val KEY_RULE = "rule"
private const val KEY_DEFAULT_DIFFICULTY = "default_difficulty"
private const val KEY_SOUND = "sound_enabled"
private const val KEY_VIBRATION = "vibration_enabled"
private const val KEY_BLUETOOTH_CLIENT_ID = "bluetooth_client_id"
private const val KEY_COINS = "coins"
private const val KEY_TOTAL_GAMES = "total_games"
private const val KEY_WINS = "wins"
private const val KEY_BLUETOOTH_GAMES = "bluetooth_games"
private const val KEY_WIN_STREAK = "win_streak"
private const val KEY_BEST_WIN_HANDS = "best_win_hands"
private const val DEFAULT_RULE_ID = "north"

@Composable
fun ChudadiApp() {
    val context = LocalContext.current
    val localClientId = remember(context) { stableBluetoothClientId(context) }
    val settings = remember(context) {
        context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    }
    val profileStore = remember(context) { ProfileStore(context.applicationContext) }
    var profile by remember { mutableStateOf(profileStore.load()) }
    var lastProfileChange by remember { mutableStateOf<ProfileChange?>(null) }
    var profileActionPopup by remember { mutableStateOf<ProfileChange?>(null) }
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            copyAvatarToPrivateStorage(context, it)?.let { path ->
                val updated = ProfileController.selectGalleryAvatar(profile, path)
                val change = ProfileChange(profile = updated, message = "自定义头像已更新")
                lastProfileChange = change
                profileActionPopup = change
                profile = updated
                profileStore.save(updated)
            }
        }
    }
    var screen by remember { mutableStateOf(Screen.Splash) }
    var selectedRule by remember {
        mutableStateOf<RuleSet>(ruleSetByIdOrName(settings.getString(KEY_RULE, DEFAULT_RULE_ID) ?: DEFAULT_RULE_ID))
    }
    var difficulty by remember {
        mutableStateOf(
            runCatching { Difficulty.valueOf(settings.getString(KEY_DEFAULT_DIFFICULTY, Difficulty.Easy.name) ?: Difficulty.Easy.name) }
                .getOrDefault(Difficulty.Easy)
        )
    }
    var soundEnabled by remember { mutableStateOf(settings.getBoolean(KEY_SOUND, true)) }
    var vibrationEnabled by remember { mutableStateOf(settings.getBoolean(KEY_VIBRATION, true)) }
    var gameState by remember { mutableStateOf<GameState?>(null) }
    var localPlayerId by remember { mutableIntStateOf(0) }
    var networkTransport by remember { mutableStateOf<GameTransport?>(null) }
    var networkIsHost by remember { mutableStateOf(false) }
    var networkSeats by remember { mutableStateOf(defaultRoomSeats(profile.nickname)) }
    var networkSeed by remember { mutableIntStateOf(0) }
    var networkSequence by remember { mutableIntStateOf(0) }
    var networkRoomId by remember { mutableStateOf("") }
    var networkHostEpoch by remember { mutableIntStateOf(0) }
    var networkLastHostSignalAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var networkStatus by remember { mutableStateOf("") }
    var latestNetworkSnapshot by remember { mutableStateOf<GameSnapshot?>(null) }
    var networkMigrationInProgress by remember { mutableStateOf(false) }
    var waitingForNetworkMove by remember { mutableStateOf(false) }
    var networkAlertMessage by remember { mutableStateOf<String?>(null) }
    var gameMode by remember { mutableStateOf(GameMode.HumanVsAi) }
    var gameStartPhase by remember { mutableStateOf(GameStartPhase.Playing) }
    CardRoomMusicEffect(
        enabled = soundEnabled,
        scene = if (screen == Screen.Game) MusicScene.Game else MusicScene.Lobby
    )
    LaunchedEffect(networkTransport, networkIsHost, networkRoomId, networkHostEpoch, gameMode) {
        val activeTransport = networkTransport ?: return@LaunchedEffect
        if (gameMode != GameMode.BluetoothRoom || networkRoomId.isBlank()) return@LaunchedEffect
        while (true) {
            delay(3_000)
            if (networkIsHost) {
                activeTransport.send(GameMessageCodec.encode(GameMessage.Heartbeat(networkRoomId, networkHostEpoch, localPlayerId)))
            } else if (System.currentTimeMillis() - networkLastHostSignalAt > 8_000 && !networkMigrationInProgress) {
                networkAlertMessage = "自己的蓝牙连接已断开，已退出本局。请重新进入好友蓝牙对局。"
                activeTransport.close()
                networkTransport = null
                networkIsHost = false
                networkSeats = emptyRoomSeats()
                networkSeed = 0
                networkSequence = 0
                networkRoomId = ""
                networkHostEpoch = 0
                networkStatus = networkAlertMessage.orEmpty()
                latestNetworkSnapshot = null
                networkMigrationInProgress = false
                waitingForNetworkMove = false
                gameMode = GameMode.HumanVsAi
                screen = Screen.Lobby
            }
        }
    }

    fun strategy(controller: GameController, level: Difficulty = difficulty): AiStrategy = when (level) {
        Difficulty.Easy -> NeuralAiStrategy(controller, NeuralAiProfile.Easy)
        Difficulty.Normal -> NeuralAiStrategy(controller, NeuralAiProfile.Normal)
        Difficulty.Hard -> NeuralAiStrategy(controller, NeuralAiProfile.Hard)
    }

    fun difficultyForPlayer(playerId: Int): Difficulty {
        return networkSeats.normalizedSeats()
            .firstOrNull { it.index == playerId }
            ?.difficulty
            ?: difficulty
    }

    fun saveRule(ruleSet: RuleSet) {
        selectedRule = ruleSet
        settings.edit()
            .putString(KEY_RULE, ruleSet.profile.id)
            .apply()
        screen = Screen.Lobby
    }

    fun savePreferences() {
        settings.edit()
            .putString(KEY_DEFAULT_DIFFICULTY, difficulty.name)
            .putBoolean(KEY_SOUND, soundEnabled)
            .putBoolean(KEY_VIBRATION, vibrationEnabled)
            .apply()
    }

    fun persistProfile(updated: PlayerProfile) {
        profile = updated
        profileStore.save(updated)
    }

    fun applyProfileChange(change: ProfileChange) {
        lastProfileChange = change
        profileActionPopup = change
        if (change.profile != profile) {
            persistProfile(change.profile)
        }
    }

    fun clearNetworkSession(message: String = "") {
        networkTransport = null
        networkIsHost = false
        networkSeats = emptyRoomSeats()
        networkSeed = 0
        networkSequence = 0
        networkRoomId = ""
        networkHostEpoch = 0
        networkStatus = message
        latestNetworkSnapshot = null
        networkMigrationInProgress = false
        waitingForNetworkMove = false
        gameMode = GameMode.HumanVsAi
    }

    fun showNetworkAlertAndExit(message: String) {
        networkAlertMessage = message
        networkTransport?.close()
        clearNetworkSession(message)
        screen = Screen.Lobby
    }

    fun leaveNetworkToLobby() {
        if (gameMode == GameMode.BluetoothRoom) {
            val message = if (networkIsHost) {
                "房主已返回大厅，本局蓝牙对局已结束。"
            } else {
                "你已离开蓝牙对局。"
            }
            val outgoing = if (networkIsHost) {
                GameMessage.Error(message)
            } else {
                GameMessage.Leave(localPlayerId)
            }
            networkTransport?.send(GameMessageCodec.encode(outgoing))
            networkTransport?.close()
            clearNetworkSession("")
        }
        screen = Screen.Lobby
    }

    fun playerDisplayName(): String = profile.nickname.ifBlank { "你" }

    fun humanPreviewState(): GameState {
        return GameState(
            players = listOf(
                Player(0, playerDisplayName(), PlayerKind.Human),
                Player(1, "小北", PlayerKind.LocalAi),
                Player(2, "阿豪", PlayerKind.LocalAi),
                Player(3, "星河", PlayerKind.LocalAi)
            ),
            ruleSet = selectedRule,
            currentPlayerId = 0,
            message = "准备入座，点击开始游戏后发牌"
        )
    }

    fun prepareHumanGame() {
        localPlayerId = 0
        networkTransport?.close()
        networkTransport = null
        networkIsHost = false
        networkSeats = defaultRoomSeats(playerDisplayName())
        networkSequence = 0
        networkSeed = 0
        networkRoomId = ""
        networkHostEpoch = 0
        networkStatus = ""
        latestNetworkSnapshot = null
        networkMigrationInProgress = false
        waitingForNetworkMove = false
        gameMode = GameMode.HumanVsAi
        gameStartPhase = GameStartPhase.ReadyToStart
        gameState = humanPreviewState()
        screen = Screen.Game
    }

    fun startHumanGame(seed: Int? = null) {
        localPlayerId = 0
        gameMode = GameMode.HumanVsAi
        gameStartPhase = GameStartPhase.Dealing
        val controller = GameController(selectedRule)
        gameState = controller.newGame(
            humanName = playerDisplayName(),
            aiNames = listOf("小北", "阿豪", "星河"),
            seed = seed
        )
        screen = if (gameState?.isFinished == true) Screen.Result else Screen.Game
    }

    fun buildNetworkGame(seed: Int, ruleSet: RuleSet, seats: List<RoomSeat>, playerId: Int, isHost: Boolean, roomId: String = networkRoomId, hostEpoch: Int = networkHostEpoch) {
        val roomSeats = seats.normalizedSeats()
        networkSeats = roomSeats
        networkSeed = seed
        networkSequence = 0
        networkRoomId = roomId.ifBlank { networkRoomId.ifBlank { "room-${System.currentTimeMillis()}" } }
        networkHostEpoch = hostEpoch
        networkLastHostSignalAt = System.currentTimeMillis()
        networkStatus = "蓝牙对局已连接"
        networkMigrationInProgress = false
        waitingForNetworkMove = false
        localPlayerId = playerId
        gameMode = GameMode.BluetoothRoom
        gameStartPhase = GameStartPhase.Playing
        val names = roomSeats.mapIndexed { index, seat ->
            seat.name.ifBlank { "牌友${index + 1}" }
        }
        val controller = GameController(ruleSet)
        val state = controller.newGame(
            humanName = names[0],
            aiNames = names.drop(1),
            seed = seed
        )
        val createdState = state.copy(
            players = state.players.map { player ->
                player.copy(
                    name = names[player.id],
                    kind = when {
                        player.id == playerId -> PlayerKind.Human
                        isHost && roomSeats[player.id].controlledByAi -> PlayerKind.LocalAi
                        else -> PlayerKind.Remote
                    },
                    connected = roomSeats.getOrNull(player.id)?.connected ?: true
                )
            }
        )
        gameState = createdState
        latestNetworkSnapshot = GameSnapshot(
            sequence = networkSequence,
            seed = networkSeed,
            ruleProfileId = createdState.ruleSet.profile.id,
            players = createdState.players.map { player -> SnapshotPlayer(player.id, player.name, player.hand) },
            currentPlayerId = createdState.currentPlayerId,
            lastPlayerId = createdState.lastPlayedHand?.playerId,
            lastCards = createdState.lastPlayedHand?.type?.cards.orEmpty(),
            passCount = createdState.passCount,
            firstTurn = createdState.firstTurn,
            winnerId = createdState.winnerId,
            message = createdState.message,
            roomId = networkRoomId,
            hostEpoch = networkHostEpoch
        )
        screen = Screen.Game
    }

    fun returnToNetworkRoomForRematch() {
        val restoredSeats = networkSeats.resetForRematch()
        networkSeats = restoredSeats
        networkSeed = 0
        networkSequence = 0
        networkStatus = "已回到原房间，等待房主再开一局"
        latestNetworkSnapshot = null
        networkMigrationInProgress = false
        waitingForNetworkMove = false
        gameStartPhase = GameStartPhase.Playing
        gameState = null
        if (networkIsHost) {
            networkTransport?.send(GameMessageCodec.encode(GameMessage.Room(restoredSeats, selectedRule.name, networkRoomId, networkHostEpoch)))
        }
        screen = Screen.Nearby
    }

    fun publishState(previous: GameState, next: GameState) {
        gameState = next
        if (previous.winnerId == null && next.winnerId != null) {
            val ranking = next.players.sortedWith(compareBy<Player> { it.hand.size }.thenBy { it.id })
            val rank = ranking.indexOfFirst { it.id == localPlayerId }.takeIf { it >= 0 }?.plus(1) ?: 4
            val winnerName = next.winnerId?.let { next.player(it).name } ?: "无人胜出"
            val change = ProfileController.settleMatch(
                profile = profile,
                settlement = MatchSettlement(
                    timestamp = System.currentTimeMillis(),
                    mode = if (gameMode == GameMode.BluetoothRoom) GameModeLabel.Bluetooth else GameModeLabel.HumanVsAi,
                    difficulty = if (gameMode == GameMode.HumanVsAi) difficulty else null,
                    ruleName = next.ruleSet.name,
                    rank = rank,
                    winnerName = winnerName,
                    remainingCards = next.player(localPlayerId).hand.size
                )
            )
            lastProfileChange = change
            persistProfile(change.profile)
            screen = Screen.Result
        }
    }

    fun snapshotFromState(state: GameState): GameSnapshot {
        return GameSnapshot(
            sequence = networkSequence,
            seed = networkSeed,
            ruleProfileId = state.ruleSet.profile.id,
            players = state.players.map { player -> SnapshotPlayer(player.id, player.name, player.hand) },
            currentPlayerId = state.currentPlayerId,
            lastPlayerId = state.lastPlayedHand?.playerId,
            lastCards = state.lastPlayedHand?.type?.cards.orEmpty(),
            passCount = state.passCount,
            firstTurn = state.firstTurn,
            winnerId = state.winnerId,
            message = state.message,
            roomId = networkRoomId,
            hostEpoch = networkHostEpoch
        )
    }

    fun stateFromSnapshot(snapshot: GameSnapshot): GameState {
        val ruleSet = ruleSetByIdOrName(snapshot.ruleProfileId)
        val players = snapshot.players.map { snapshotPlayer ->
            val seat = networkSeats.normalizedSeats().firstOrNull { it.index == snapshotPlayer.id }
            Player(
                id = snapshotPlayer.id,
                name = snapshotPlayer.name,
                kind = when {
                    snapshotPlayer.id == localPlayerId -> PlayerKind.Human
                    networkIsHost && seat?.controlledByAi == true -> PlayerKind.LocalAi
                    else -> PlayerKind.Remote
                },
                hand = snapshotPlayer.hand,
                connected = seat?.connected ?: true
            )
        }
        val lastPlayedHand = snapshot.lastPlayerId?.let { playerId ->
            ruleSet.classify(snapshot.lastCards)?.let { handType -> PlayedHand(playerId, handType) }
        }
        return GameState(
            players = players,
            ruleSet = ruleSet,
            currentPlayerId = snapshot.currentPlayerId,
            lastPlayedHand = lastPlayedHand,
            passCount = snapshot.passCount,
            firstTurn = snapshot.firstTurn,
            winnerId = snapshot.winnerId,
            message = snapshot.message
        )
    }

    fun sendAcceptedMove(playerId: Int, cards: List<Card>, pass: Boolean) {
        networkSequence += 1
        networkTransport?.send(GameMessageCodec.encode(GameMessage.MoveAccepted(networkSequence, playerId, cards, pass, networkRoomId, networkHostEpoch)))
        gameState?.let { current ->
            val snapshot = snapshotFromState(current)
            latestNetworkSnapshot = snapshot
            networkTransport?.send(GameMessageCodec.encode(GameMessage.StateSnapshot(snapshot)))
        }
    }

    fun applyAcceptedMove(message: GameMessage.MoveAccepted) {
        val current = gameState ?: return
        if (message.hostEpoch != 0 && message.hostEpoch < networkHostEpoch) return
        if (message.sequence <= networkSequence && !networkIsHost) return
        if (!networkIsHost && message.playerId == localPlayerId) {
            waitingForNetworkMove = false
        }
        networkSequence = maxOf(networkSequence, message.sequence)
        val controller = GameController(current.ruleSet)
        val next = if (message.pass) {
            controller.pass(current, message.playerId)
        } else {
            controller.play(current, message.playerId, message.cards)
        }
        networkStatus = "蓝牙同步正常"
        publishState(current, next)
    }

    fun applyTableMove(move: (GameState) -> GameState) {
        val current = gameState ?: return
        publishState(current, move(current))
    }

    fun playLocalCards(cards: List<Card>) {
        val current = gameState ?: return
        if (networkTransport != null && current.player(localPlayerId).kind == PlayerKind.Human) {
            if (networkIsHost) {
                val next = GameController(current.ruleSet).play(current, localPlayerId, cards)
                publishState(current, next)
                if (NetworkMoveGuard.wasMoveApplied(current, next, localPlayerId, cards, pass = false)) {
                    sendAcceptedMove(localPlayerId, cards, pass = false)
                }
            } else {
                if (waitingForNetworkMove) return
                waitingForNetworkMove = true
                networkTransport?.send(GameMessageCodec.encode(GameMessage.MoveRequest(localPlayerId, cards, pass = false, roomId = networkRoomId, hostEpoch = networkHostEpoch)))
            }
        } else {
            applyTableMove { GameController(it.ruleSet).play(it, localPlayerId, cards) }
        }
    }

    fun passLocalTurn() {
        val current = gameState ?: return
        if (networkTransport != null && current.player(localPlayerId).kind == PlayerKind.Human) {
            if (networkIsHost) {
                val next = GameController(current.ruleSet).pass(current, localPlayerId)
                publishState(current, next)
                if (NetworkMoveGuard.wasMoveApplied(current, next, localPlayerId, emptyList(), pass = true)) {
                    sendAcceptedMove(localPlayerId, emptyList(), pass = true)
                }
            } else {
                if (waitingForNetworkMove) return
                waitingForNetworkMove = true
                networkTransport?.send(GameMessageCodec.encode(GameMessage.MoveRequest(localPlayerId, emptyList(), pass = true, roomId = networkRoomId, hostEpoch = networkHostEpoch)))
            }
        } else {
            applyTableMove { GameController(it.ruleSet).pass(it, localPlayerId) }
        }
    }

    fun applyComputerTurn() {
        val current = gameState ?: return
        val controller = GameController(current.ruleSet)
        val move = AiController(controller, strategy(controller, difficultyForPlayer(current.currentPlayerId))).playTurnIfNeeded(current) ?: return
        val next = controller.applyMove(current, move)
        publishState(current, next)
        if (networkTransport != null && networkIsHost && next != current) {
            when (move) {
                is com.example.uml_chudadi.model.Move.Play -> sendAcceptedMove(move.playerId, move.cards, pass = false)
                is com.example.uml_chudadi.model.Move.Pass -> sendAcceptedMove(move.playerId, emptyList(), pass = true)
            }
        }
    }

    fun canPlayLocalCards(cards: List<Card>): Boolean {
        val current = gameState ?: return false
        if (current.currentPlayerId != localPlayerId || current.player(localPlayerId).kind != PlayerKind.Human) return false
        val controller = GameController(current.ruleSet)
        val legal = controller.legalPlays(current, localPlayerId).any { candidate ->
            candidate.size == cards.size && candidate.toSet() == cards.toSet()
        }
        if (!legal) {
            gameState = controller.play(current, localPlayerId, cards)
        }
        return legal
    }

    fun applyTimeoutMove() {
        val current = gameState ?: return
        if (waitingForNetworkMove) return
        if (current.currentPlayerId != localPlayerId || current.isFinished) return
        val legal = GameController(current.ruleSet).legalPlays(current, localPlayerId).firstOrNull()
        val next = when {
            legal != null -> GameController(current.ruleSet).play(current, localPlayerId, legal)
            current.lastPlayedHand != null -> GameController(current.ruleSet).pass(current, localPlayerId)
            else -> current
        }
        if (legal != null) {
            playLocalCards(legal)
        } else if (current.lastPlayedHand != null) {
            passLocalTurn()
        } else {
            publishState(current, next)
        }
    }

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FeltDark
        ) {
            AnimatedScreenHost(screen = screen) { activeScreen ->
            when (activeScreen) {
                Screen.Splash -> SplashScreen(
                    onStart = { screen = Screen.Lobby }
                )
                Screen.Lobby -> LobbyScreen(
                    profile = profile,
                    onProfile = { screen = Screen.Profile },
                    onHumanGame = { screen = Screen.DifficultySelect },
                    onRules = { screen = Screen.Rules },
                    onNearby = { screen = Screen.Nearby },
                    onSettings = { screen = Screen.Settings },
                    onTutorial = { screen = Screen.Tutorial }
                )
                Screen.Profile -> ProfileScreen(
                    profile = profile,
                    latestChange = lastProfileChange,
                    actionPopup = profileActionPopup,
                    onDismissActionPopup = { profileActionPopup = null },
                    onBack = { screen = Screen.Lobby },
                    onRename = { nickname ->
                        applyProfileChange(ProfileController.renameWithCost(profile, nickname))
                    },
                    onAvatar = { avatarId ->
                        applyProfileChange(ProfileController.unlockAvatar(profile, avatarId))
                    },
                    onGalleryAvatar = {
                        if (profile.customAvatarUnlocked) {
                            avatarLauncher.launch("image/*")
                        } else {
                            applyProfileChange(ProfileController.unlockCustomAvatar(profile))
                        }
                    },
                    onClaimDaily = {
                        val change = ProfileController.claimDailyReward(profile, currentDayKey())
                        if (change.dailyClaimed) {
                            applyProfileChange(change)
                        }
                    },
                    onResetStats = {
                        applyProfileChange(ProfileController.resetStats(profile))
                    }
                )
                Screen.DifficultySelect -> DifficultySelectScreen(
                    selected = difficulty,
                    onDifficulty = {
                        difficulty = it
                        savePreferences()
                        prepareHumanGame()
                    },
                    onBack = { screen = Screen.Lobby }
                )
                Screen.Rules -> RulesScreen(
                    selectedRule = selectedRule,
                    onBack = { screen = Screen.Lobby },
                    onSave = { saveRule(it) }
                )
                Screen.Settings -> SettingsScreen(
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    onSound = {
                        soundEnabled = it
                        savePreferences()
                    },
                    onVibration = {
                        vibrationEnabled = it
                        savePreferences()
                    },
                    onBack = { screen = Screen.Lobby }
                )
                Screen.Tutorial -> TutorialScreen(onBack = { screen = Screen.Lobby })
                Screen.Nearby -> NearbyScreen(
                    ruleSet = selectedRule,
                    localProfileName = playerDisplayName(),
                    restoredTransport = if (gameMode == GameMode.BluetoothRoom) networkTransport else null,
                    restoredSeats = networkSeats,
                    restoredPlayerId = localPlayerId,
                    restoredIsHost = networkIsHost,
                    restoredRoomId = networkRoomId,
                    restoredHostEpoch = networkHostEpoch,
                    restoredStatus = networkStatus,
                    onBack = {
                        networkTransport?.close()
                        networkTransport = null
                        networkStatus = ""
                        screen = Screen.Lobby
                    },
                    onStartNetworkGame = { transport, seed, ruleSet, seats, playerId, isHost, roomId, hostEpoch ->
                        networkTransport = transport
                        networkIsHost = isHost
                        buildNetworkGame(seed, ruleSet, seats, playerId, isHost, roomId, hostEpoch)
                    },
                    onRoomUpdated = { seats, rule ->
                        networkSeats = seats.normalizedSeats()
                        selectedRule = rule
                    },
                    onNetworkStatus = { message ->
                        networkStatus = message
                    },
                    onRoomSessionChanged = { transport, isHost, seats, roomId, hostEpoch, message ->
                        gameMode = GameMode.BluetoothRoom
                        networkTransport = transport
                        networkIsHost = isHost
                        networkSeats = seats.normalizedSeats()
                        networkRoomId = roomId
                        networkHostEpoch = hostEpoch
                        networkLastHostSignalAt = System.currentTimeMillis()
                        networkMigrationInProgress = false
                        networkStatus = message
                    },
                    onRoomSessionCleared = { message ->
                        networkTransport = null
                        networkIsHost = false
                        networkSeats = emptyRoomSeats()
                        networkRoomId = ""
                        networkHostEpoch = 0
                        networkStatus = message
                        gameMode = GameMode.HumanVsAi
                    },
                    onNetworkMessage = { message ->
                        when (message) {
                            is GameMessage.Heartbeat -> {
                                if (message.hostEpoch >= networkHostEpoch) {
                                    networkLastHostSignalAt = System.currentTimeMillis()
                                    networkHostEpoch = maxOf(networkHostEpoch, message.hostEpoch)
                                    networkRoomId = message.roomId.ifBlank { networkRoomId }
                                    networkMigrationInProgress = false
                                    networkStatus = "蓝牙连接正常"
                                }
                            }
                            is GameMessage.StateSnapshot -> {
                                if (message.snapshot.hostEpoch >= networkHostEpoch) {
                                    networkLastHostSignalAt = System.currentTimeMillis()
                                    networkHostEpoch = maxOf(networkHostEpoch, message.snapshot.hostEpoch)
                                    networkRoomId = message.snapshot.roomId.ifBlank { networkRoomId }
                                    latestNetworkSnapshot = message.snapshot
                                }
                            }
                            else -> Unit
                        }
                        val current = gameState
                        if (current != null) {
                            val controller = GameController(current.ruleSet)
                            when (message) {
                                is GameMessage.Heartbeat -> {
                                    if (message.hostEpoch >= networkHostEpoch) {
                                        networkLastHostSignalAt = System.currentTimeMillis()
                                        networkMigrationInProgress = false
                                        networkStatus = "蓝牙连接正常"
                                    }
                                }
                                is GameMessage.DisconnectNotice -> {
                                    if (message.playerId == localPlayerId) {
                                        showNetworkAlertAndExit("自己的蓝牙连接已断开，已退出本局。请重新进入好友蓝牙对局。")
                                    } else {
                                        networkSeats = networkSeats.markHumanDisconnected(message.playerId, takeoverByAi = true)
                                        networkStatus = message.reason
                                        waitingForNetworkMove = false
                                        val next = current.copy(
                                            players = current.players.map { player ->
                                                if (player.id == message.playerId && networkIsHost) {
                                                    player.copy(kind = PlayerKind.LocalAi, connected = false)
                                                } else if (player.id == message.playerId) {
                                                    player.copy(connected = false)
                                                } else {
                                                    player
                                                }
                                            },
                                            message = message.reason
                                        )
                                        publishState(current, next)
                                    }
                                }
                                is GameMessage.Leave -> {
                                    if (networkIsHost) {
                                        val leavingName = networkSeats.getOrNull(message.playerId)?.name?.ifBlank { "好友" } ?: "好友"
                                        val noticeText = "$leavingName 已离开，座位已由人机托管"
                                        networkSeats = networkSeats.markHumanDisconnected(message.playerId, takeoverByAi = true)
                                        networkStatus = noticeText
                                        waitingForNetworkMove = false
                                        val next = current.copy(
                                            players = current.players.map { player ->
                                                if (player.id == message.playerId) {
                                                    player.copy(kind = PlayerKind.LocalAi, connected = false)
                                                } else {
                                                    player
                                                }
                                            },
                                            message = noticeText
                                        )
                                        publishState(current, next)
                                        val notice = GameMessage.DisconnectNotice(message.playerId, noticeText)
                                        networkTransport?.send(GameMessageCodec.encode(notice))
                                        networkTransport?.send(GameMessageCodec.encode(GameMessage.StateSnapshot(snapshotFromState(next))))
                                    } else if (message.playerId == 0) {
                                        showNetworkAlertAndExit("房主已返回大厅，本局蓝牙对局已结束。")
                                    }
                                }
                                is GameMessage.Error -> {
                                    showNetworkAlertAndExit(message.reason.ifBlank { "蓝牙连接已断开，已退出本局。" })
                                }
                                is GameMessage.HostMigration -> {
                                    if (message.hostEpoch >= networkHostEpoch) {
                                        networkHostEpoch = message.hostEpoch
                                        networkRoomId = message.roomId.ifBlank { networkRoomId }
                                        networkSeats = message.seats.normalizedSeats()
                                        networkIsHost = message.newHostPlayerId == localPlayerId
                                        networkMigrationInProgress = false
                                        waitingForNetworkMove = false
                                        networkStatus = if (networkIsHost) "你已成为新房主，牌局继续" else "房主迁移完成，牌局继续"
                                        val next = current.copy(
                                            players = current.players.map { player ->
                                                val seat = networkSeats.getOrNull(player.id)
                                                player.copy(
                                                    kind = when {
                                                        player.id == localPlayerId -> PlayerKind.Human
                                                        networkIsHost && seat?.controlledByAi == true -> PlayerKind.LocalAi
                                                        else -> PlayerKind.Remote
                                                    },
                                                    connected = seat?.connected ?: player.connected
                                                )
                                            },
                                            message = networkStatus
                                        )
                                        latestNetworkSnapshot = snapshotFromState(next)
                                        publishState(current, next)
                                    }
                                }
                                is GameMessage.MoveRequest -> {
                                    if (message.hostEpoch != 0 && message.hostEpoch < networkHostEpoch) {
                                        Unit
                                    } else if (networkIsHost && !networkMigrationInProgress && NetworkMoveGuard.canHostAcceptMove(current, message)) {
                                        val next = if (message.pass) {
                                            controller.pass(current, message.playerId)
                                        } else {
                                            controller.play(current, message.playerId, message.cards)
                                        }
                                        publishState(current, next)
                                        if (NetworkMoveGuard.wasMoveApplied(current, next, message.playerId, message.cards, message.pass)) {
                                            sendAcceptedMove(message.playerId, message.cards, message.pass)
                                        } else {
                                            networkTransport?.send(GameMessageCodec.encode(GameMessage.StateSnapshot(snapshotFromState(next))))
                                        }
                                    } else if (networkIsHost) {
                                        networkTransport?.send(GameMessageCodec.encode(GameMessage.StateSnapshot(snapshotFromState(current))))
                                    }
                                }
                                is GameMessage.MoveAccepted -> applyAcceptedMove(message)
                                is GameMessage.StateSnapshot -> {
                                    if (message.snapshot.hostEpoch >= networkHostEpoch) {
                                        latestNetworkSnapshot = message.snapshot
                                        networkLastHostSignalAt = System.currentTimeMillis()
                                        networkHostEpoch = maxOf(networkHostEpoch, message.snapshot.hostEpoch)
                                        networkRoomId = message.snapshot.roomId.ifBlank { networkRoomId }
                                        networkStatus = message.snapshot.message.ifBlank { "蓝牙状态已同步" }
                                        networkMigrationInProgress = false
                                        if (!networkIsHost) {
                                            waitingForNetworkMove = false
                                            networkSequence = maxOf(networkSequence, message.snapshot.sequence)
                                            val next = stateFromSnapshot(message.snapshot)
                                            publishState(current, next)
                                        }
                                    }
                                }
                                is GameMessage.SyncRequest -> {
                                    if (networkIsHost) {
                                        networkTransport?.send(GameMessageCodec.encode(GameMessage.StateSnapshot(snapshotFromState(current))))
                                    }
                                }
                                is GameMessage.Play -> {
                                    val next = controller.play(current, message.playerId, message.cards)
                                    publishState(current, next)
                                    if (networkIsHost && next != current) {
                                        networkTransport?.send(GameMessageCodec.encode(message))
                                    }
                                }
                                is GameMessage.Pass -> {
                                    val next = controller.pass(current, message.playerId)
                                    publishState(current, next)
                                    if (networkIsHost && next != current) {
                                        networkTransport?.send(GameMessageCodec.encode(message))
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                )
                Screen.Game -> GameScreen(
                    state = gameState,
                    localProfile = profile,
                    difficulty = difficulty,
                    currentTurnDifficulty = gameState?.currentPlayerId?.let { difficultyForPlayer(it) } ?: difficulty,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    gameMode = gameMode,
                    localPlayerId = localPlayerId,
                    inputLocked = waitingForNetworkMove || networkMigrationInProgress,
                    networkStatus = if (gameMode == GameMode.BluetoothRoom) networkStatus else "",
                    onCanPlay = { cards -> canPlayLocalCards(cards) },
                    onPlay = { cards -> playLocalCards(cards) },
                    onPass = { passLocalTurn() },
                    onHint = {
                        val current = gameState ?: return@GameScreen emptyList()
                        GameController(current.ruleSet).legalPlays(current, localPlayerId).firstOrNull().orEmpty()
                    },
                    onComputerTurn = { applyComputerTurn() },
                    onTimeout = { applyTimeoutMove() },
                    startPhase = gameStartPhase,
                    onStartGame = { startHumanGame() },
                    onDealFinished = {
                        if (gameStartPhase == GameStartPhase.Dealing) {
                            gameStartPhase = GameStartPhase.Playing
                        }
                    },
                    onRestart = {
                        if (gameMode == GameMode.HumanVsAi) {
                            prepareHumanGame()
                        } else {
                            networkTransport?.close()
                            networkTransport = null
                            screen = Screen.Nearby
                        }
                    },
                    onLobby = {
                        leaveNetworkToLobby()
                    }
                )
                Screen.Result -> ResultScreen(
                    state = gameState,
                    localPlayerId = localPlayerId,
                    profile = profile,
                    latestChange = lastProfileChange,
                    onAgain = {
                        if (gameMode == GameMode.HumanVsAi) prepareHumanGame() else returnToNetworkRoomForRematch()
                    },
                    onLobby = {
                        leaveNetworkToLobby()
                    }
                )
            }
            }
            networkAlertMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { networkAlertMessage = null },
                    title = { Text("蓝牙连接提示") },
                    text = { Text(message) },
                    confirmButton = {
                        Button(onClick = { networkAlertMessage = null }) {
                            Text("知道了")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AnimatedScreenHost(screen: Screen, content: @Composable (Screen) -> Unit) {
    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            (
                fadeIn(tween(180)) +
                    scaleIn(initialScale = 0.992f, animationSpec = tween(200))
                ) togetherWith (
                fadeOut(tween(120)) +
                    scaleOut(targetScale = 0.992f, animationSpec = tween(120))
                )
        },
        label = "screenHost"
    ) { activeScreen ->
        content(activeScreen)
    }
}

@Composable
private fun GameScaffold(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    animatedBackground: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F7458),
                        Color(0xFF063829),
                        Color(0xFF031A14)
                    )
                )
            )
    ) {
        FeltPattern()
        if (animatedBackground) {
            FloatingCardsBackground()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
private fun FloatingCardsBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "floatingCards")
    val drift = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200), RepeatMode.Reverse),
        label = "cardDrift"
    )
    val cards = remember {
        listOf(
            Triple(0.12f, 0.18f, -13f),
            Triple(0.82f, 0.21f, 11f),
            Triple(0.18f, 0.72f, 8f),
            Triple(0.86f, 0.78f, -9f)
        )
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val driftValue = drift.value
        cards.forEachIndexed { index, (xRatio, yRatio, rotation) ->
            val width = 52f + index * 4f
            val height = 76f + index * 4f
            val center = Offset(
                size.width * xRatio,
                size.height * yRatio + (driftValue - 0.5f) * (18f + index * 4f)
            )
            rotate(rotation + (driftValue - 0.5f) * 4f, center) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.045f),
                    topLeft = Offset(center.x - width / 2, center.y - height / 2),
                    size = Size(width, height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f)
                )
                drawRoundRect(
                    color = Gold.copy(alpha = 0.06f),
                    topLeft = Offset(center.x - width / 2 + 6f, center.y - height / 2 + 6f),
                    size = Size(width - 12f, height - 12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Composable
private fun StaggeredEntry(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        delay(index * 55L)
        visible = true
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(260),
        label = "staggeredEntry-$index"
    )
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            translationY = with(density) { ((1f - progress) * 14f).dp.toPx() }
            val entryScale = 0.985f + progress * 0.015f
            scaleX = entryScale
            scaleY = entryScale
        }
    ) {
        content()
    }
}

@Composable
private fun GamePanel(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val glow by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220),
        label = "panelGlow"
    )
    val shape = RoundedCornerShape(22.dp)
    Card(
        modifier = modifier
            .shadow(if (selected) 16.dp else 8.dp, shape)
            .drawBehind {
                if (glow > 0f) {
                    drawRoundRect(
                        color = Gold.copy(alpha = 0.22f * glow),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
            },
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF1B7658) else Panel.copy(alpha = 0.94f)),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (selected) Gold.copy(alpha = 0.8f) else Gold.copy(alpha = 0.2f), shape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            content = content
        )
    }
}

@Composable
private fun PressScale(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f),
        label = "pressScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        content = content
    )
}

@Composable
private fun CardRoomMusicEffect(enabled: Boolean, scene: MusicScene) {
    val context = LocalContext.current
    val player = remember(context) { CardRoomMusicPlayer(context.applicationContext) }
    DisposableEffect(enabled, scene) {
        if (enabled) player.start(scene) else player.stop()
        onDispose { player.stop() }
    }
}

@Composable
private fun SplashScreen(onStart: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "splashGlow")
    val glow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "splashGlowValue"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_cards),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.76f)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.38f),
                            FeltDark.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Gold.copy(alpha = 0.10f + glow * 0.08f),
                radius = size.minDimension * (0.34f + glow * 0.04f),
                center = Offset(size.width * 0.5f, size.height * 0.36f)
            )
            drawCircle(
                color = FeltLight.copy(alpha = 0.12f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.5f, size.height * 0.42f),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    "锄大地",
                    color = Cream,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(14.dp))
                )
                Text(
                    "四人牌局 · 南北玩法 · 蓝牙开房",
                    color = Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SplashCardMark("♠", glow)
                    SplashCardMark("♥", 1f - glow)
                    SplashCardMark("♣", glow)
                    SplashCardMark("♦", 1f - glow)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldenButton(
                    text = "开始游戏",
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "点击开始，进入牌局大厅",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SplashCardMark(mark: String, progress: Float) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .graphicsLayer {
                rotationZ = (progress - 0.5f) * 7f
                scaleX = 0.96f + progress * 0.06f
                scaleY = 0.96f + progress * 0.06f
            }
            .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(9.dp))
            .border(1.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            mark,
            color = if (mark == "♥" || mark == "♦") Danger else Ink,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LobbyScreen(
    profile: PlayerProfile,
    onProfile: () -> Unit,
    onHumanGame: () -> Unit,
    onRules: () -> Unit,
    onNearby: () -> Unit,
    onSettings: () -> Unit,
    onTutorial: () -> Unit
) {
    GameScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StaggeredEntry(0) { PlayerBar(profile = profile, onClick = onProfile) }
            StaggeredEntry(1, modifier = Modifier.weight(1f)) {
                HeroPanel(modifier = Modifier.fillMaxSize(), onHumanGame = onHumanGame)
            }
            StaggeredEntry(2) {
                StatsStrip(profile = profile)
            }
            StaggeredEntry(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    LobbyModeCard("人机对局", "单人开桌", "♠", Modifier.weight(1f), onHumanGame)
                    LobbyModeCard("好友蓝牙对局", "附近开局", "♥", Modifier.weight(1f), onNearby)
                }
            }
            StaggeredEntry(4) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionButton("♣", "规则", onRules, Modifier.weight(1f))
                    QuickActionButton("⚙", "设置", onSettings, Modifier.weight(1f))
                    QuickActionButton("?", "教程", onTutorial, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatsStrip(profile: PlayerProfile) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        StatChip("总局", profile.stats.totalGames.toString(), Modifier.weight(1f))
        StatChip("胜率", "${profile.stats.winRate}%", Modifier.weight(1f))
        StatChip("连胜", profile.stats.winStreak.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Gold, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Text(label, color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp)
    }
}

@Composable
private fun ProgressLine(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .background(Color.Black.copy(alpha = 0.24f), RoundedCornerShape(6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(5.dp)
                .background(Gold, RoundedCornerShape(6.dp))
        )
    }
}

@Composable
private fun PlayerBar(profile: PlayerProfile, onClick: () -> Unit) {
    val (xpNow, xpNeed) = ProfileProgress.progressToNextLevel(profile.xp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
            .border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Avatar(name = profile.nickname, active = false, size = 48, avatar = profile.avatar)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(profile.nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${profile.title}  Lv.${profile.level}  $xpNow/$xpNeed", color = Gold, fontSize = 12.sp, maxLines = 1)
                ProgressLine(progress = xpNow / xpNeed.toFloat())
            }
        }
        CoinPill(profile.coins)
    }
}

@Composable
private fun HeroPanel(modifier: Modifier = Modifier, onHumanGame: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "heroGlow")
    val glow = transition.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "heroGlowPulse"
    )
    val heroShape = RoundedCornerShape(26.dp)
    val heroBrush = remember { Brush.radialGradient(listOf(FeltLight, Felt)) }
    Box(
        modifier = modifier
            .shadow(18.dp, heroShape)
            .background(heroBrush, heroShape)
            .border(1.dp, Gold.copy(alpha = 0.55f), heroShape)
            .padding(22.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val glowValue = glow.value
            drawCircle(
                color = Gold.copy(alpha = 0.08f),
                radius = size.minDimension * 0.48f * glowValue,
                center = Offset(size.width * 0.76f, size.height * 0.24f)
            )
            val path = Path().apply {
                moveTo(size.width * 0.05f, size.height * 0.82f)
                cubicTo(size.width * 0.36f, size.height * 0.70f, size.width * 0.62f, size.height * 0.93f, size.width * 0.96f, size.height * 0.78f)
            }
            drawPath(path, color = Gold.copy(alpha = 0.12f), style = Stroke(width = 9f, cap = StrokeCap.Round))
        }
        CardFan(Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp))
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("锄大地", color = Cream, fontSize = 44.sp, fontWeight = FontWeight.Black)
            Text("经典四人扑克对战", color = Color.White.copy(alpha = 0.82f), fontSize = 16.sp)
            Text("开局有礼  ·  好友同桌", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))
            GoldenButton(text = "人机对局", onClick = onHumanGame, modifier = Modifier.width(178.dp))
        }
    }
}

@Composable
private fun LobbyModeCard(title: String, subtitle: String, mark: String, modifier: Modifier, onClick: () -> Unit) {
    PressScale(modifier = modifier.height(118.dp), onClick = onClick) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.96f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Gold.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = Color.White.copy(alpha = 0.68f), fontSize = 13.sp, maxLines = 1)
                }
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Black.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mark,
                        color = if (mark == "♥") Danger else Gold,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PressScale(modifier = modifier.height(58.dp), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                .border(1.dp, Gold.copy(alpha = 0.26f), RoundedCornerShape(22.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, color = Gold, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DifficultySelectScreen(
    selected: Difficulty,
    onDifficulty: (Difficulty) -> Unit,
    onBack: () -> Unit
) {
    MenuPage(title = "选择难度", subtitle = "人机场次", onBack = onBack) {
        Difficulty.entries.forEach { level ->
            SelectCard(selected = selected == level, onClick = { onDifficulty(level) }) {
                Text(level.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(level.subtitle, color = Color.White.copy(alpha = 0.76f), fontSize = 14.sp)
                Text(
                    when (level) {
                        Difficulty.Easy -> "适合热身"
                        Difficulty.Normal -> "神经网络均衡控牌"
                        Difficulty.Hard -> "完整模型评分决策"
                    },
                    color = Gold
                )
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RulesScreen(
    selectedRule: RuleSet,
    onBack: () -> Unit,
    onSave: (RuleSet) -> Unit
) {
    var draftRule by remember(selectedRule) { mutableStateOf(selectedRule) }
    val explanation = ruleExplanation(draftRule)
    MenuPage(title = "规则设置", subtitle = "南北玩法", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvailableRuleSets.forEach { ruleSet ->
                RuleCard(ruleSet.name, ruleSet.description, ruleSet, draftRule) { draftRule = it }
            }
            RuleExplanationPanel(explanation)
        }
        GoldenButton(text = "保存设置", onClick = { onSave(draftRule) }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RuleCard(title: String, body: String, ruleSet: RuleSet, selectedRule: RuleSet, onRule: (RuleSet) -> Unit) {
    SelectCard(selected = selectedRule == ruleSet, onClick = { onRule(ruleSet) }) {
        Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(body, color = Color.White.copy(alpha = 0.76f), fontSize = 14.sp)
        Text("关键牌 ${ruleSet.firstCard}", color = Gold, fontWeight = FontWeight.SemiBold)
    }
}

private fun ruleExplanation(ruleSet: RuleSet): RuleExplanation {
    val firstLead = ruleSet.firstCard.toString()
    val pressureSections = if (ruleSet.bombEnhanced) {
        listOf(
            RuleSection("压制规则", "普通牌型通常按同张数比较；四带一和同花顺视为强牌，可以压过单张、对子、三张、顺子、同花和葫芦。"),
            RuleSection("强牌顺序", "同花顺大于四带一；同类强牌按核心点数比较。四带一可用更大的四带一或同花顺接牌，同花顺只能用更大的同花顺接牌。"),
            RuleSection("五张牌等级", "五张牌从小到大为：顺子 < 同花 < 葫芦 < 四带一 < 同花顺。当前玩法中四带一和同花顺额外拥有跨牌型压制能力。")
        )
    } else {
        listOf(
            RuleSection("压制规则", "必须和桌面上一手牌张数一致，并且更大；单张、对子、三张只和同类比较。"),
            RuleSection("五张牌等级", "五张牌之间可按等级压制：顺子 < 同花 < 葫芦 < 四带一 < 同花顺。五张牌不能压单张、对子或三张。")
        )
    }
    return RuleExplanation(
        title = "${ruleSet.name}详解",
        sections = listOf(
            RuleSection("当前房间规则预览", ruleSet.description),
            RuleSection("基础目标", "四人对局，每人13张牌，谁先把手牌全部出完谁获胜。"),
            RuleSection("首手要求", "第一手必须由持有 $firstLead 的玩家先出，并且出的牌里必须包含 $firstLead。"),
            RuleSection("大小顺序", "点数从小到大为 3、4、5、6、7、8、9、10、J、Q、K、A、2；同点数按方块、梅花、红心、黑桃递增。"),
            RuleSection("可出牌型", "可以出单张、对子、三张，以及五张牌型：顺子、同花、葫芦、四带一、同花顺。本局顺子由连续五张组成，不包含2。")
        ) + pressureSections + listOf(
            RuleSection("胜负结算", "有人出完手牌后本局结束，剩余手牌越少排名越靠前。")
        )
    )
}

@Composable
private fun RuleExplanationPanel(explanation: RuleExplanation) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text(explanation.title, color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        explanation.sections.forEachIndexed { index, section ->
            StaggeredEntry(index) {
                RuleSectionRow(section)
            }
        }
    }
}

@Composable
private fun RuleSectionRow(section: RuleSection) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(section.title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            section.body,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun SettingsScreen(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showExitConfirm by remember { mutableStateOf(false) }
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            containerColor = Panel,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text("退出游戏", color = Cream, fontWeight = FontWeight.Black, fontSize = 20.sp)
            },
            text = {
                Text(
                    "确定要退出锄大地吗？当前未保存的对局进度将结束。",
                    color = Color.White.copy(alpha = 0.84f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirm = false
                        context.findActivity()?.finishAndRemoveTask()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("确认退出", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitConfirm = false }, shape = RoundedCornerShape(14.dp)) {
                    Text("取消", color = Cream)
                }
            }
        )
    }
    MenuPage(title = "设置", subtitle = "声音反馈", onBack = onBack) {
        Text("声音与反馈", color = Cream, fontWeight = FontWeight.Black, fontSize = 18.sp)
        SettingToggleRow("背景音乐与音效", soundEnabled) { onSound(!soundEnabled) }
        SettingToggleRow("震动反馈", vibrationEnabled) { onVibration(!vibrationEnabled) }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { showExitConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出游戏", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ProfileScreen(
    profile: PlayerProfile,
    latestChange: ProfileChange?,
    actionPopup: ProfileChange?,
    onDismissActionPopup: () -> Unit,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onAvatar: (String) -> Unit,
    onGalleryAvatar: () -> Unit,
    onClaimDaily: () -> Unit,
    onResetStats: () -> Unit
) {
    var draftName by remember(profile.nickname) { mutableStateOf(profile.nickname) }
    var pendingSpend by remember { mutableStateOf<PendingProfileSpend?>(null) }
    val canClaimDaily = ProfileController.canClaimDaily(profile, currentDayKey())
    val cleanName = draftName.trim().take(10).ifBlank { "牌桌新星" }
    val nameChanged = cleanName != profile.nickname
    val canRename = nameChanged && profile.coins >= ProfileController.RENAME_COST
    pendingSpend?.let { spend ->
        ConfirmProfileSpendDialog(
            spend = spend,
            profile = profile,
            onDismiss = { pendingSpend = null },
            onConfirm = {
                when (spend) {
                    is PendingProfileSpend.Rename -> onRename(spend.nickname)
                    is PendingProfileSpend.UnlockAvatar -> onAvatar(spend.avatarId)
                    PendingProfileSpend.UnlockCustomAvatar -> onGalleryAvatar()
                    PendingProfileSpend.ResetStats -> onResetStats()
                }
                pendingSpend = null
            }
        )
    }
    actionPopup?.let { change ->
        ProfileActionResultDialog(change = change, onDismiss = onDismissActionPopup)
    }
    MenuPage(title = "玩家中心", subtitle = "档案与战绩", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileOverviewPanel(profile, latestChange)
            NicknameSettingsPanel(
                draftName = draftName,
                coins = profile.coins,
                nameChanged = nameChanged,
                canRename = canRename,
                onNameChange = { draftName = it },
                onRename = { pendingSpend = PendingProfileSpend.Rename(cleanName) }
            )
            AvatarPickerPanel(
                profile = profile,
                onAvatar = onAvatar,
                onUnlockAvatar = { pendingSpend = PendingProfileSpend.UnlockAvatar(it) }
            )
            CustomAvatarPanel(
                profile = profile,
                onPickAvatar = onGalleryAvatar,
                onUnlockCustomAvatar = { pendingSpend = PendingProfileSpend.UnlockCustomAvatar }
            )
            DailyRewardPanel(canClaimDaily, onClaimDaily)
            StatsManagementPanel(
                profile = profile,
                onResetStats = { pendingSpend = PendingProfileSpend.ResetStats }
            )
            AchievementPanel(profile)
            HistoryPanel(profile.history)
        }
    }
}

@Composable
private fun ProfileActionResultDialog(change: ProfileChange, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(if (change.failed) "操作未完成" else "操作成功", color = Cream, fontWeight = FontWeight.Black, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    profileChangeDialogText(change),
                    color = if (change.failed) Danger else Color.White.copy(alpha = 0.86f),
                    lineHeight = 20.sp,
                    fontWeight = if (change.failed) FontWeight.Bold else FontWeight.Normal
                )
                if (!change.failed && (change.coinsDelta != 0 || change.xpDelta != 0 || change.leveledUp || change.unlockedAchievements.isNotEmpty())) {
                    RewardSummary(change = change)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("知道了", fontWeight = FontWeight.Black)
            }
        }
    )
}

private fun profileChangeDialogText(change: ProfileChange): String {
    return change.message.ifBlank {
        when {
            change.dailyClaimed -> "每日奖励已领取"
            change.coinsDelta != 0 || change.xpDelta != 0 -> "奖励已到账"
            else -> "操作已完成"
        }
    }
}

@Composable
private fun ConfirmProfileSpendDialog(
    spend: PendingProfileSpend,
    profile: PlayerProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val cost = profileSpendCost(spend)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text("确认消耗金币", color = Cream, fontWeight = FontWeight.Black, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    profileSpendDescription(spend),
                    color = Color.White.copy(alpha = 0.82f),
                    lineHeight = 20.sp
                )
                Text(
                    "当前金币 ${profile.coins}，确认后剩余 ${(profile.coins - cost).coerceAtLeast(0)}",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("确认支付 $cost", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) {
                Text("取消", color = Cream)
            }
        }
    )
}

private fun profileSpendCost(spend: PendingProfileSpend): Int {
    return when (spend) {
        is PendingProfileSpend.Rename -> ProfileController.RENAME_COST
        is PendingProfileSpend.UnlockAvatar -> AvatarCatalog.byId(spend.avatarId).price
        PendingProfileSpend.UnlockCustomAvatar -> ProfileController.CUSTOM_AVATAR_COST
        PendingProfileSpend.ResetStats -> ProfileController.RESET_STATS_COST
    }
}

private fun profileSpendDescription(spend: PendingProfileSpend): String {
    return when (spend) {
        is PendingProfileSpend.Rename -> "花费 ${ProfileController.RENAME_COST} 金币，将昵称改为「${spend.nickname}」？"
        is PendingProfileSpend.UnlockAvatar -> {
            val avatar = AvatarCatalog.byId(spend.avatarId)
            "花费 ${avatar.price} 金币解锁「${avatar.label}」头像？解锁后会立即使用。"
        }
        PendingProfileSpend.UnlockCustomAvatar -> "花费 ${ProfileController.CUSTOM_AVATAR_COST} 金币解锁自定义头像功能？解锁后可从相册选择图片作为头像。"
        PendingProfileSpend.ResetStats -> "花费 ${ProfileController.RESET_STATS_COST} 金币重置战绩？总局、胜率、连胜和历史记录会清空，等级、金币余额、头像和成就会保留。"
    }
}

@Composable
private fun NicknameSettingsPanel(
    draftName: String,
    coins: Int,
    nameChanged: Boolean,
    canRename: Boolean,
    onNameChange: (String) -> Unit,
    onRename: () -> Unit
) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("昵称设置", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text("改名需要确认支付 ${ProfileController.RENAME_COST} 金币，当前金币 $coins", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        OutlinedTextField(
            value = draftName,
            onValueChange = { onNameChange(it.take(10)) },
            singleLine = true,
            label = { Text("昵称", color = Gold) },
            textStyle = TextStyle(color = Cream, fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        GoldenButton(
            text = when {
                !nameChanged -> "昵称未变"
                coins < ProfileController.RENAME_COST -> "金币不足"
                else -> "确认改名 ${ProfileController.RENAME_COST}"
            },
            enabled = canRename,
            onClick = onRename,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ProfileOverviewPanel(profile: PlayerProfile, latestChange: ProfileChange?) {
    val (xpNow, xpNeed) = ProfileProgress.progressToNextLevel(profile.xp)
    GamePanel(modifier = Modifier.fillMaxWidth(), selected = latestChange?.leveledUp == true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(profile.nickname, active = latestChange?.leveledUp == true, size = 70, avatar = profile.avatar)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(profile.nickname, color = Cream, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${profile.title}  Lv.${profile.level}", color = Gold, fontWeight = FontWeight.Bold)
                ProgressLine(progress = xpNow / xpNeed.toFloat())
                Text("经验 $xpNow/$xpNeed", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip("金币", profile.coins.toString(), Modifier.weight(1f))
            StatChip("总局", profile.stats.totalGames.toString(), Modifier.weight(1f))
            StatChip("胜率", "${profile.stats.winRate}%", Modifier.weight(1f))
        }
    }
}

@Composable
private fun RewardSummary(change: ProfileChange) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .border(1.dp, Gold.copy(alpha = 0.26f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            change.message.ifBlank { if (change.dailyClaimed) "每日奖励已领取" else "上一局奖励" },
            color = if (change.failed) Danger else Gold,
            fontWeight = FontWeight.Black
        )
        if (change.coinsDelta != 0 || change.xpDelta != 0) {
            Text(
                "金币 ${signed(change.coinsDelta)}  经验 ${signed(change.xpDelta)}",
                color = Cream,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        if (change.leveledUp) {
            Text("升级到 Lv.${change.newLevel}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        if (change.unlockedAchievements.isNotEmpty()) {
            Text(
                "解锁 ${change.unlockedAchievements.joinToString("、") { it.title }}",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AvatarPickerPanel(
    profile: PlayerProfile,
    onAvatar: (String) -> Unit,
    onUnlockAvatar: (String) -> Unit
) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("头像商店", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text("未解锁头像会显示开放等级、金币价格和购买状态；消费前会再次确认。", color = Color.White.copy(alpha = 0.68f), fontSize = 12.sp)
        val unlocked = ProfileController.normalizedUnlockedAvatars(profile)
        AvatarCatalog.all.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { avatar ->
                    val selected = profile.avatar.galleryPath == null && profile.avatar.builtInId == avatar.id
                    val owned = avatar.id in unlocked
                    val affordable = profile.coins >= avatar.price
                    val levelEnough = profile.level >= avatar.unlockLevel
                    AvatarShopTile(
                        avatar = avatar,
                        selected = selected,
                        owned = owned,
                        affordable = affordable,
                        levelEnough = levelEnough,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (owned) {
                                onAvatar(avatar.id)
                            } else if (affordable && levelEnough) {
                                onUnlockAvatar(avatar.id)
                            } else {
                                onAvatar(avatar.id)
                            }
                        }
                    )
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CustomAvatarPanel(
    profile: PlayerProfile,
    onPickAvatar: () -> Unit,
    onUnlockCustomAvatar: () -> Unit
) {
    val unlocked = profile.customAvatarUnlocked
    val canUnlock = profile.coins >= ProfileController.CUSTOM_AVATAR_COST
    GamePanel(modifier = Modifier.fillMaxWidth(), selected = profile.avatar.galleryPath != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Avatar(profile.nickname, active = unlocked, size = 64, avatar = profile.avatar)
                if (!unlocked) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.Black.copy(alpha = 0.46f), CircleShape)
                            .border(2.dp, Danger.copy(alpha = 0.86f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("未解锁", color = Cream, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text("${ProfileController.CUSTOM_AVATAR_COST}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("自定义头像", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(
                    if (unlocked) {
                        "已解锁，可从相册选择自己的图片作为头像。"
                    } else {
                        "一次性解锁后可自由更换图片；当前金币 ${profile.coins}。"
                    },
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
        if (!unlocked) {
            LockedFeatureNotice(
                title = if (canUnlock) "需要确认支付金币" else "金币不足，暂时无法解锁",
                body = "自定义头像需要 ${ProfileController.CUSTOM_AVATAR_COST} 金币。解锁前不会打开相册，也不会扣除金币。"
            )
        }
        GoldenButton(
            text = when {
                unlocked -> "更换自定义头像"
                !canUnlock -> "金币不足"
                else -> "解锁自定义头像 ${ProfileController.CUSTOM_AVATAR_COST}"
            },
            enabled = unlocked || canUnlock,
            onClick = {
                if (unlocked) onPickAvatar() else onUnlockCustomAvatar()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LockedFeatureNotice(title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Danger.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .border(1.dp, Danger.copy(alpha = 0.32f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("!", color = Danger, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Cream, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(body, color = Color.White.copy(alpha = 0.76f), fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun AvatarShopTile(
    avatar: BuiltInAvatar,
    selected: Boolean,
    owned: Boolean,
    affordable: Boolean,
    levelEnough: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        selected -> Gold
        owned -> rarityColor(avatar.rarity).copy(alpha = 0.55f)
        else -> Color.White.copy(alpha = 0.1f)
    }
    PressScale(modifier = modifier, enabled = true, onClick = onClick) {
        Column(
            modifier = Modifier
                .height(150.dp)
                .background(if (selected) Gold.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.045f), RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                Avatar(avatar.label, active = selected, size = 44, avatar = AvatarProfile(builtInId = avatar.id))
                if (!owned) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.48f), CircleShape)
                            .border(1.dp, rarityColor(avatar.rarity).copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("未解锁", color = Cream, fontWeight = FontWeight.Black, fontSize = 10.sp)
                            Text(
                                avatarUnavailableLabel(avatar, affordable, levelEnough),
                                color = if (affordable && levelEnough) Gold else Danger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Text(avatar.label, color = if (selected) Gold else Cream, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(avatar.rarity.title, color = rarityColor(avatar.rarity), fontSize = 10.sp, maxLines = 1)
            if (!owned) {
                Text(
                    if (levelEnough) "${avatar.price} 金币" else "Lv.${avatar.unlockLevel} 开放",
                    color = if (levelEnough && affordable) Gold else Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                when {
                    selected -> "使用中"
                    owned -> "使用"
                    !levelEnough -> "等级不足"
                    !affordable -> "金币不足"
                    else -> "可解锁"
                },
                color = if (owned || selected) Gold else Color.White.copy(alpha = 0.68f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

private fun avatarUnavailableLabel(avatar: BuiltInAvatar, affordable: Boolean, levelEnough: Boolean): String {
    return when {
        !levelEnough -> "Lv.${avatar.unlockLevel}"
        !affordable -> "金币不足"
        else -> "${avatar.price}"
    }
}

@Composable
private fun StatsManagementPanel(profile: PlayerProfile, onResetStats: () -> Unit) {
    val canReset = profile.coins >= ProfileController.RESET_STATS_COST && (profile.stats.totalGames > 0 || profile.history.isNotEmpty())
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("战绩管理", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(
            "重置会清空总局、胜率、连胜和历史战绩；等级、经验、金币、头像和成就会保留。",
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        GoldenButton(
            text = when {
                profile.stats.totalGames == 0 && profile.history.isEmpty() -> "暂无战绩"
                profile.coins < ProfileController.RESET_STATS_COST -> "金币不足"
                else -> "重置战绩 ${ProfileController.RESET_STATS_COST}"
            },
            enabled = canReset,
            onClick = onResetStats,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DailyRewardPanel(canClaim: Boolean, onClaimDaily: () -> Unit) {
    GamePanel(modifier = Modifier.fillMaxWidth(), selected = canClaim) {
        Text("每日牌局礼", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(
            if (canClaim) "今天可领取 220 金币和 45 经验" else "今日奖励已领取，明天再来",
            color = Color.White.copy(alpha = 0.76f),
            fontSize = 13.sp
        )
        GoldenButton(
            text = if (canClaim) "领取奖励" else "已领取",
            enabled = canClaim,
            onClick = onClaimDaily,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AchievementPanel(profile: PlayerProfile) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        val unlockedCount = profile.unlockedAchievements.size
        Text("成就墙  $unlockedCount/${AchievementCatalog.all.size}", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        AchievementCatalog.all.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { achievement ->
                    AchievementTile(
                        achievement = achievement,
                        unlocked = achievement.id in profile.unlockedAchievements,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementTile(achievement: AchievementDefinition, unlocked: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(92.dp)
            .background(if (unlocked) Gold.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .border(1.dp, if (unlocked) Gold.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(if (unlocked) "★ ${achievement.title}" else "☆ ${achievement.title}", color = if (unlocked) Gold else Cream, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        Text(achievement.description, color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2)
        Text("+${achievement.coinReward} 金币  +${achievement.xpReward} 经验", color = Color.White.copy(alpha = 0.58f), fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun HistoryPanel(history: List<MatchRecord>) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("历史战绩", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        if (history.isEmpty()) {
            Text("还没有完成对局，打一局后这里会记录排名和奖励。", color = Color.White.copy(alpha = 0.68f), fontSize = 13.sp)
        } else {
            history.take(50).forEachIndexed { index, record ->
                StaggeredEntry(index.coerceAtMost(8)) {
                    MatchRecordRow(record)
                }
            }
        }
    }
}

@Composable
private fun MatchRecordRow(record: MatchRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${record.mode.title}  第 ${record.rank} 名", color = Cream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${record.ruleName} · ${formatRecordTime(record.timestamp)}", color = Color.White.copy(alpha = 0.62f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("金币 ${signed(record.coinsDelta)}", color = if (record.coinsDelta >= 0) Gold else Danger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("经验 ${signed(record.xpDelta)}", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingToggleRow(title: String, enabled: Boolean, onClick: () -> Unit) {
    val knobOffset by animateFloatAsState(targetValue = if (enabled) 1f else 0f, animationSpec = tween(180), label = "toggleKnob")
    val density = LocalDensity.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(18.dp))
            .border(1.dp, if (enabled) Gold.copy(alpha = 0.44f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Cream, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .width(62.dp)
                .height(32.dp)
                .background(if (enabled) Gold.copy(alpha = 0.88f) else Color.Black.copy(alpha = 0.24f), RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                .padding(4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = with(density) { (30f * knobOffset).dp.toPx() }
                    }
                    .size(24.dp)
                    .background(if (enabled) Ink else Cream.copy(alpha = 0.8f), CircleShape)
            )
        }
    }
}

@Composable
private fun TutorialScreen(onBack: () -> Unit) {
    MenuPage(title = "新手教程", subtitle = "快速上手", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RuleExplanationPanel(
                RuleExplanation(
                    title = "快速上手",
                    sections = listOf(
                        RuleSection("目标", "四名玩家轮流出牌，先出完手牌的人获胜。"),
                        RuleSection("首出", "按当前规则，持有关键3的玩家先出，首手必须包含这张牌。"),
                        RuleSection("牌型", "常用牌型为单张、对子、三张、顺子、同花、葫芦、四带一、同花顺。"),
                        RuleSection("五张牌", "五张牌等级为：顺子 < 同花 < 葫芦 < 四带一 < 同花顺。"),
                        RuleSection("南北差异", "南方经典玩法强调同张数压制；北方玩法允许四带一和同花顺压制普通牌型。"),
                        RuleSection("蓝牙", "一台手机创建房间，其他手机加入；房主可添加人机，四个座位都准备后开始。")
                    )
                )
            )
        }
    }
}

@Composable
private fun NearbyScreen(
    ruleSet: RuleSet,
    localProfileName: String,
    restoredTransport: GameTransport?,
    restoredSeats: List<RoomSeat>,
    restoredPlayerId: Int,
    restoredIsHost: Boolean,
    restoredRoomId: String,
    restoredHostEpoch: Int,
    restoredStatus: String,
    onBack: () -> Unit,
    onStartNetworkGame: (GameTransport, Int, RuleSet, List<RoomSeat>, Int, Boolean, String, Int) -> Unit,
    onRoomUpdated: (List<RoomSeat>, RuleSet) -> Unit,
    onNetworkStatus: (String) -> Unit,
    onRoomSessionChanged: (GameTransport, Boolean, List<RoomSeat>, String, Int, String) -> Unit,
    onRoomSessionCleared: (String) -> Unit,
    onNetworkMessage: (GameMessage) -> Unit
) {
    val context = LocalContext.current
    val localClientId = remember(context) { stableBluetoothClientId(context) }
    val restoredRoomActive = restoredTransport != null
    val restoredLocalName = restoredSeats.normalizedSeats()
        .getOrNull(restoredPlayerId)
        ?.name
        ?.takeIf { it.isNotBlank() }
        ?: localProfileName
    var hasPermission by remember { mutableStateOf(hasBluetoothPermissions(context)) }
    var entryMode by remember {
        mutableStateOf(
            when {
                !restoredRoomActive -> BluetoothEntryMode.Choose
                restoredIsHost -> BluetoothEntryMode.HostRoom
                else -> BluetoothEntryMode.JoinRoom
            }
        )
    }
    var status by remember {
        mutableStateOf(restoredStatus.ifBlank { if (restoredRoomActive) "已回到原房间，等待再开一局" else "选择创建房间或加入对局" })
    }
    var roomRuleName by remember { mutableStateOf(ruleSet.name) }
    var roomId by remember { mutableStateOf(restoredRoomId.ifBlank { "room-${System.currentTimeMillis()}" }) }
    var hostEpoch by remember { mutableIntStateOf(restoredHostEpoch) }
    var latestSnapshot by remember { mutableStateOf<GameSnapshot?>(null) }
    var peerSeatKeys by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var devices by remember { mutableStateOf(emptyList<com.example.uml_chudadi.transport.BluetoothDeviceInfo>()) }
    var transport by remember { mutableStateOf(restoredTransport) }
    var hostMode by remember { mutableStateOf(restoredRoomActive && restoredIsHost) }
    var playerName by remember { mutableStateOf(restoredLocalName) }
    var bluetoothDialogMessage by remember { mutableStateOf<String?>(null) }
    var suppressTransportErrorsUntil by remember { mutableStateOf(0L) }
    var boundTransport by remember { mutableStateOf<GameTransport?>(null) }
    var roomSeats by remember {
        mutableStateOf(if (restoredRoomActive) restoredSeats.normalizedSeats() else defaultRoomSeats(localProfileName))
    }
    var discoveryHandle by remember { mutableStateOf<AutoCloseable?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result.values.all { it }
        status = if (hasPermission) "可以创建房间或加入对局" else "需要权限才能发现附近好友"
    }
    val discoverableLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        status = if (result.resultCode == Activity.RESULT_OK) {
            "好友现在可以找到你的房间"
        } else {
            "房间已创建；若好友找不到，请先在系统蓝牙里完成配对"
        }
    }

    val activeDiscoveryHandle = discoveryHandle
    DisposableEffect(activeDiscoveryHandle) {
        onDispose { activeDiscoveryHandle?.close() }
    }

    fun localSeatByIdentity(): RoomSeat? {
        return roomSeats.normalizedSeats().firstOrNull { seat ->
            (seat.clientId.isNotBlank() && seat.clientId == localClientId) ||
                (seat.clientId.isBlank() && seat.name.isNotBlank() && seat.name == playerName)
        }
    }

    fun leaveRoom() {
        val localSeat = localSeatByIdentity()
        if (transport != null && localSeat != null) {
            transport?.send(GameMessageCodec.encode(GameMessage.Leave(localSeat.index)))
        }
        suppressTransportErrorsUntil = System.currentTimeMillis() + 1_800
        discoveryHandle?.close()
        transport?.close()
        boundTransport = null
        onRoomSessionCleared("")
        onBack()
    }

    fun showBluetoothError(rawMessage: String, fallbackStatus: String = "选择创建房间或加入对局") {
        bluetoothDialogMessage = playerFriendlyBluetoothError(rawMessage)
        status = fallbackStatus
    }

    fun handleDiscoveryStatus(message: String) {
        if (message.isBluetoothErrorLike()) {
            showBluetoothError(message, fallbackStatus = "正在搜索附近房间")
        } else {
            status = message
        }
    }

    fun disconnectClientAndReturnToJoin(rawMessage: String) {
        val friendly = playerFriendlyBluetoothError(rawMessage)
        bluetoothDialogMessage = friendly
        onNetworkMessage(GameMessage.Error(friendly))
        suppressTransportErrorsUntil = System.currentTimeMillis() + 1_800
        discoveryHandle?.close()
        discoveryHandle = null
        transport?.close()
        transport = null
        hostMode = false
        roomSeats = emptyRoomSeats()
        latestSnapshot = null
        peerSeatKeys = emptyMap()
        boundTransport = null
        entryMode = BluetoothEntryMode.JoinRoom
        status = "连接已断开，请重新寻找房间"
        onRoomSessionCleared(status)
    }

    fun broadcastRoom(seats: List<RoomSeat> = roomSeats, currentRule: RuleSet = ruleSet) {
        val message = "房间状态已同步"
        onNetworkStatus(message)
        transport?.send(GameMessageCodec.encode(GameMessage.Room(seats.normalizedSeats(), currentRule.name, roomId, hostEpoch)))
    }

    fun localSeatIndex(): Int {
        return roomSeats
            .firstOrNull { it.name == playerName || (it.clientId.isNotBlank() && it.clientId == localClientId) }
            ?.index
            ?: restoredPlayerId
    }

    fun localJoinedSeatIndex(): Int? {
        return roomSeats.normalizedSeats()
            .firstOrNull { seat ->
                seat.occupied &&
                    (
                        (seat.clientId.isNotBlank() && seat.clientId == localClientId) ||
                            (seat.name.isNotBlank() && seat.name == playerName)
                    )
            }
            ?.index
    }

    fun startDiscovery() {
        localSeatByIdentity()?.let { localSeat ->
            transport?.send(GameMessageCodec.encode(GameMessage.Leave(localSeat.index)))
        }
        suppressTransportErrorsUntil = System.currentTimeMillis() + 1_800
        discoveryHandle?.close()
        transport?.close()
        transport = null
        hostMode = false
        playerName = localProfileName
        roomSeats = emptyRoomSeats()
        roomRuleName = ruleSet.name
        roomId = "room-${System.currentTimeMillis()}"
        hostEpoch = 0
        latestSnapshot = null
        peerSeatKeys = emptyMap()
        boundTransport = null
        onRoomSessionCleared("正在搜索附近房间")
        val pairedDevices = runCatching { bondedBluetoothDevices(context) }.getOrDefault(emptyList())
        devices = pairedDevices.distinctBy { it.address }
        status = if (pairedDevices.isEmpty()) {
            "正在搜索附近房间"
        } else {
            "正在搜索，已找到 ${pairedDevices.size} 台可选设备"
        }
        discoveryHandle = discoverBluetoothDevices(
            context = context,
            onDevice = { found ->
                devices = (devices + found).distinctBy { it.address }
                status = "找到 ${devices.size} 台设备，选择房主加入房间"
            },
            onStatus = { message -> handleDiscoveryStatus(message) }
        )
    }

    fun bindTransport(
        newTransport: GameTransport,
        isHost: Boolean,
        localName: String,
        seatsOverride: List<RoomSeat>? = null
    ) {
        if (boundTransport === newTransport) {
            transport = newTransport
            hostMode = isHost
            playerName = localName
            seatsOverride?.let { roomSeats = it.normalizedSeats() }
            onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
            onRoomSessionChanged(newTransport, isHost, roomSeats, roomId, hostEpoch, status)
            return
        }
        if (transport != null && transport !== newTransport) {
            suppressTransportErrorsUntil = System.currentTimeMillis() + 1_800
            transport?.close()
        }
        transport = newTransport
        hostMode = isHost
        playerName = localName
        roomSeats = seatsOverride?.normalizedSeats() ?: if (isHost) {
            defaultRoomSeats(localName).map { seat ->
                if (seat.index == 0) seat.copy(clientId = localClientId) else seat
            }.normalizedSeats()
        } else {
            emptyRoomSeats()
        }
        onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
        onRoomSessionChanged(newTransport, isHost, roomSeats, roomId, hostEpoch, status)
        boundTransport = newTransport
        newTransport.observeEvents { event ->
            when (event) {
                is TransportEvent.PeerDisconnected -> {
                    if (System.currentTimeMillis() < suppressTransportErrorsUntil) return@observeEvents
                    if (!isBluetoothEnabled(context)) {
                        val ownMessage = "自己的蓝牙已关闭，已退出当前蓝牙对局。"
                        if (hostMode) {
                            bluetoothDialogMessage = ownMessage
                            onNetworkMessage(GameMessage.Error(ownMessage))
                            onRoomSessionCleared(ownMessage)
                            transport?.close()
                            transport = null
                            boundTransport = null
                            entryMode = BluetoothEntryMode.Choose
                            status = "选择创建房间或加入对局"
                        } else {
                            disconnectClientAndReturnToJoin(ownMessage)
                        }
                        return@observeEvents
                    }
                    if (hostMode) {
                        val disconnectedSeat = peerSeatKeys[event.peerKey]
                        if (disconnectedSeat != null) {
                            val seatName = roomSeats.getOrNull(disconnectedSeat)?.name ?: "好友"
                            roomSeats = roomSeats.markHumanDisconnected(disconnectedSeat, takeoverByAi = true)
                            onRoomUpdated(roomSeats, ruleSet)
                            status = "$seatName 断线，已由人机托管"
                            onNetworkStatus(status)
                            broadcastRoom()
                            val notice = GameMessage.DisconnectNotice(disconnectedSeat, status)
                            onNetworkMessage(notice)
                            newTransport.send(GameMessageCodec.encode(notice))
                            latestSnapshot?.let {
                                newTransport.send(GameMessageCodec.encode(GameMessage.StateSnapshot(it.copy(message = status))))
                            }
                        } else {
                            status = "有好友连接断开"
                            onNetworkStatus(status)
                        }
                    } else {
                        disconnectClientAndReturnToJoin("房主连接已断开，请重新寻找房间加入。")
                    }
                }
                is TransportEvent.Error -> {
                    if (System.currentTimeMillis() < suppressTransportErrorsUntil) return@observeEvents
                    if (!isBluetoothEnabled(context)) {
                        val ownMessage = "自己的蓝牙已关闭，已退出当前蓝牙对局。"
                        if (hostMode) {
                            bluetoothDialogMessage = ownMessage
                            onNetworkMessage(GameMessage.Error(ownMessage))
                            onRoomSessionCleared(ownMessage)
                            transport?.close()
                            transport = null
                            boundTransport = null
                            entryMode = BluetoothEntryMode.Choose
                            status = "选择创建房间或加入对局"
                        } else {
                            disconnectClientAndReturnToJoin(ownMessage)
                        }
                        return@observeEvents
                    }
                    if (hostMode && event.reason.isBenignBluetoothReturnCode()) {
                        status = "房间已创建，等待好友或添加人机"
                        return@observeEvents
                    }
                    val message = if (entryMode == BluetoothEntryMode.JoinRoom && roomSeats.none { it.occupied }) {
                        "加入失败：${event.reason}。请确认两台手机已配对、房主房间仍在等待，并重新选择房主。"
                    } else {
                        event.reason
                    }
                    if (hostMode) {
                        showBluetoothError(message, fallbackStatus = "房间已创建，等待好友或添加人机")
                    } else {
                        showBluetoothError(message, fallbackStatus = if (entryMode == BluetoothEntryMode.JoinRoom) "正在寻找可加入的房间" else "选择创建房间或加入对局")
                    }
                }
                is TransportEvent.PeerConnected -> {
                    status = if (hostMode) "好友正在加入，等待座位同步" else "已连接房主，正在进入房间"
                    onNetworkStatus(status)
                }
                is TransportEvent.Message -> when (val message = GameMessageCodec.decode(event.raw)) {
                is GameMessage.Hello -> {
                    if (hostMode) {
                        val baseName = message.playerName.ifBlank { "好友" }
                        val resolvedClientId = message.clientId.ifBlank { event.peerKey?.let { "peer-$it" }.orEmpty() }
                        val existingSeat = roomSeats.firstOrNull { it.clientId.isNotBlank() && it.clientId == resolvedClientId }
                        val uniqueName = existingSeat?.name?.ifBlank { baseName } ?: roomSeats.uniqueRoomPlayerName(baseName)
                        val joined = roomSeats.addOrRejoinHuman(
                            name = uniqueName,
                            clientId = resolvedClientId,
                            deviceAddress = event.peerKey.orEmpty(),
                            preferredIndex = message.rejoinSeatIndex
                        )
                        if (joined == null) {
                            status = "房间已满，无法加入更多好友"
                            event.peerKey?.let { newTransport.sendTo(it, GameMessageCodec.encode(GameMessage.Error("房间已满"))) }
                        } else {
                            val (updatedSeats, seatIndex) = joined
                            roomSeats = updatedSeats
                            if (event.peerKey != null) peerSeatKeys = peerSeatKeys + (event.peerKey to seatIndex)
                            onRoomUpdated(updatedSeats, ruleSet)
                            status = if (message.rejoinSeatIndex != null) {
                                "${updatedSeats[seatIndex].name} 已重连，恢复座位 ${seatIndex + 1}"
                            } else {
                                "${updatedSeats[seatIndex].name} 已加入（${updatedSeats.count { it.occupied }}/4）"
                            }
                            onNetworkStatus(status)
                            onRoomSessionChanged(newTransport, hostMode, updatedSeats, roomId, hostEpoch, status)
                            event.peerKey?.let { key ->
                                newTransport.sendTo(key, GameMessageCodec.encode(GameMessage.RejoinAccepted(seatIndex, updatedSeats, latestSnapshot)))
                            }
                            newTransport.send(GameMessageCodec.encode(GameMessage.Room(updatedSeats, ruleSet.name, roomId, hostEpoch)))
                        }
                    }
                }
                is GameMessage.Room -> {
                    roomSeats = message.seats.normalizedSeats()
                    roomRuleName = message.ruleName
                    roomId = message.roomId.ifBlank { roomId }
                    hostEpoch = maxOf(hostEpoch, message.hostEpoch)
                    onRoomUpdated(roomSeats, ruleFromName(message.ruleName))
                    status = "房间人数 ${roomSeats.count { it.occupied }}/4，等待房主开局"
                    onNetworkStatus(status)
                    entryMode = if (hostMode) BluetoothEntryMode.HostRoom else BluetoothEntryMode.JoinRoom
                    onRoomSessionChanged(newTransport, hostMode, roomSeats, roomId, hostEpoch, status)
                }
                is GameMessage.RoomReady -> {
                    if (hostMode) {
                        roomSeats = roomSeats.setReady(message.playerId, message.ready)
                        onRoomUpdated(roomSeats, ruleSet)
                        status = "${roomSeats.getOrNull(message.playerId)?.name ?: "好友"} ${if (message.ready) "已准备" else "取消准备"}"
                        newTransport.send(GameMessageCodec.encode(GameMessage.Room(roomSeats, ruleSet.name, roomId, hostEpoch)))
                    }
                }
                is GameMessage.Leave -> {
                    if (hostMode) {
                        roomSeats = roomSeats.setConnected(message.playerId, false)
                        onRoomUpdated(roomSeats, ruleSet)
                        status = "${roomSeats.getOrNull(message.playerId)?.name ?: "好友"} 已离开"
                        onNetworkStatus(status)
                        onNetworkMessage(message)
                        newTransport.send(GameMessageCodec.encode(GameMessage.Room(roomSeats, ruleSet.name, roomId, hostEpoch)))
                    }
                }
                is GameMessage.Kick -> {
                    val localSeatId = localSeatByIdentity()?.index
                    if (!hostMode && localSeatId == message.playerId) {
                        bluetoothDialogMessage = message.reason
                        status = "已退出房间"
                        entryMode = BluetoothEntryMode.Choose
                        roomSeats = emptyRoomSeats()
                        newTransport.close()
                        boundTransport = null
                        onRoomSessionCleared(status)
                    }
                }
                is GameMessage.Start -> {
                    val seats = message.seats.normalizedSeats()
                    val localId = seats.indexOfFirst { it.clientId.isNotBlank() && it.clientId == localClientId }
                        .takeIf { it >= 0 }
                        ?: seats.indexOfFirst { it.name == playerName }.takeIf { it >= 0 }
                        ?: 0
                    roomId = message.roomId.ifBlank { roomId }
                    hostEpoch = maxOf(hostEpoch, message.hostEpoch)
                    onStartNetworkGame(
                        newTransport,
                        message.seed,
                        ruleFromName(message.ruleName),
                        seats,
                        localId,
                        hostMode,
                        roomId,
                        hostEpoch
                    )
                }
                is GameMessage.Heartbeat -> {
                    onNetworkStatus("蓝牙连接正常")
                    onNetworkMessage(message)
                }
                is GameMessage.DisconnectNotice -> {
                    roomSeats = roomSeats.markHumanDisconnected(message.playerId, takeoverByAi = true)
                    onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
                    status = message.reason
                    onNetworkStatus(message.reason)
                    onNetworkMessage(message)
                }
                is GameMessage.HostMigration -> {
                    roomId = message.roomId.ifBlank { roomId }
                    hostEpoch = maxOf(hostEpoch, message.hostEpoch)
                    roomSeats = message.seats.normalizedSeats()
                    val localSeat = roomSeats.indexOfFirst { seat ->
                        (seat.clientId.isNotBlank() && seat.clientId == localClientId) ||
                            (seat.name.isNotBlank() && seat.name == playerName)
                    }.takeIf { it >= 0 } ?: restoredPlayerId
                    hostMode = message.newHostPlayerId == localSeat
                    onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
                    status = if (hostMode) "你已成为新房主" else "房主迁移完成，继续对局"
                    onNetworkStatus(status)
                    onRoomSessionChanged(newTransport, hostMode, roomSeats, roomId, hostEpoch, status)
                }
                is GameMessage.RejoinAccepted -> {
                    roomSeats = message.seats.normalizedSeats()
                    roomSeats.getOrNull(message.playerId)?.name?.takeIf { it.isNotBlank() }?.let { playerName = it }
                    latestSnapshot = message.snapshot
                    onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
                    status = "已恢复原座位"
                    onNetworkStatus(status)
                    onRoomSessionChanged(newTransport, hostMode, roomSeats, roomId, hostEpoch, status)
                    message.snapshot?.let { onNetworkMessage(GameMessage.StateSnapshot(it)) }
                }
                is GameMessage.StateSnapshot -> {
                    latestSnapshot = message.snapshot
                    onNetworkMessage(message)
                }
                is GameMessage.Play,
                is GameMessage.Pass,
                is GameMessage.MoveRequest,
                is GameMessage.MoveAccepted,
                is GameMessage.SyncRequest -> onNetworkMessage(message)
                is GameMessage.State -> {
                    status = message.summary
                    onNetworkStatus(message.summary)
                }
                is GameMessage.Error -> {
                    showBluetoothError(message.reason, fallbackStatus = status.takeUnless { it.isBluetoothErrorLike() }.orEmpty().ifBlank { "选择创建房间或加入对局" })
                    onNetworkMessage(message)
                }
                else -> Unit
            }
            }
        }
    }

    LaunchedEffect(restoredTransport, restoredRoomActive, restoredIsHost, restoredLocalName) {
        val existingTransport = restoredTransport ?: return@LaunchedEffect
        if (restoredRoomActive && boundTransport !== existingTransport) {
            bindTransport(existingTransport, restoredIsHost, restoredLocalName, restoredSeats)
        }
    }

    fun createRoom() {
        val localName = localBluetoothPlayerName(context)
        roomId = "room-${System.currentTimeMillis()}"
        hostEpoch = 1
        val hostTransport = BluetoothHostTransport(context)
        bindTransport(hostTransport, isHost = true, localName = localName)
        hostTransport.start(TransportRole.Host(localName, localClientId))
        entryMode = BluetoothEntryMode.HostRoom
        status = "房间已创建，等待好友或添加人机"
        onNetworkStatus(status)
        onRoomSessionChanged(hostTransport, true, roomSeats, roomId, hostEpoch, status)
        discoverableLauncher.launch(bluetoothDiscoverableIntent())
    }

    fun joinRoom() {
        entryMode = BluetoothEntryMode.JoinRoom
        startDiscovery()
    }

    fun joinDevice(device: com.example.uml_chudadi.transport.BluetoothDeviceInfo) {
        discoveryHandle?.close()
        discoveryHandle = null
        val localName = localBluetoothPlayerName(context)
        val clientTransport = BluetoothClientTransport(context, device.address)
        bindTransport(clientTransport, isHost = false, localName = localName)
        clientTransport.start(TransportRole.Client(localName, device.address, localClientId, null))
        status = "正在加入 ${device.name}"
        onNetworkStatus(status)
        onRoomSessionChanged(clientTransport, false, roomSeats, roomId, hostEpoch, status)
    }

    val guidePhase = connectionGuidePhase(
        hasPermission = hasPermission,
        status = status,
        hostMode = hostMode,
        transportActive = transport != null,
        discovering = discoveryHandle != null,
        deviceCount = devices.size,
        roomPlayerCount = roomSeats.count { it.occupied }
    )
    val guideSteps = connectionGuideSteps(guidePhase, devices.size, roomSeats.count { it.occupied })

    MenuPage(title = "好友蓝牙对局", subtitle = "附近房间", onBack = ::leaveRoom) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val visibleStatus = status.takeUnless { it.isBluetoothErrorLike() }.orEmpty()
            if (visibleStatus.isNotBlank()) {
                Text(visibleStatus, color = Cream, fontWeight = FontWeight.SemiBold)
            }
            Text("房间规则：$roomRuleName", color = Gold, fontWeight = FontWeight.Bold)
            if (!hasPermission) {
                GoldenButton(text = "开启附近对战", onClick = { launcher.launch(requiredBluetoothPermissions()) }, modifier = Modifier.fillMaxWidth())
            } else if (entryMode == BluetoothEntryMode.Choose) {
                BluetoothModeChooser(onCreate = ::createRoom, onJoin = ::joinRoom)
            }
            if (hasPermission && entryMode == BluetoothEntryMode.JoinRoom && roomSeats.none { it.occupied }) {
                RadarSearchEffect(modifier = Modifier.fillMaxWidth().height(160.dp))
                Text("正在寻找可加入的房间，选择房主设备后进入座位页。", color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
                OutlinedGameButton(
                    text = "重新寻找房间",
                    onClick = { startDiscovery() },
                    modifier = Modifier.fillMaxWidth()
                )
                devices.forEach { device ->
                    SelectCard(selected = false, onClick = { joinDevice(device) }) {
                        Text(device.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(device.address, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                    }
                }
            }
            if (!hasPermission) {
                Unit
            } else {
                when (entryMode) {
                    BluetoothEntryMode.Choose -> Unit
                    BluetoothEntryMode.HostRoom -> {
                        RoomSeatsPanel(
                            seats = roomSeats,
                            isHost = true,
                            localPlayerName = playerName,
                            localClientId = localClientId,
                            onAddAi = { level ->
                                val updated = roomSeats.addAiToFirstEmpty(level)
                                if (updated == null) {
                                    status = "房间已满"
                                } else {
                                    roomSeats = updated
                                    onRoomUpdated(updated, ruleSet)
                                    status = "已添加${level.title}人机（${updated.count { it.occupied }}/4）"
                                    broadcastRoom(updated)
                                }
                            },
                            onToggleAi = { index ->
                                val updated = roomSeats.toggleAiDifficulty(index)
                                roomSeats = updated
                                onRoomUpdated(updated, ruleSet)
                                status = "已切换人机难度"
                                broadcastRoom(updated)
                            },
                            onRemoveAi = { index ->
                                val removedSeat = roomSeats.getOrNull(index)
                                val removedName = removedSeat?.name.orEmpty()
                                if (removedSeat?.kind == RoomSeatKind.Human) {
                                    transport?.send(GameMessageCodec.encode(GameMessage.Kick(index, "房主已移出你的座位")))
                                }
                                val updated = roomSeats.removeAi(index)
                                roomSeats = updated
                                onRoomUpdated(updated, ruleSet)
                                status = if (removedName.isBlank()) "已清空座位" else "已移出 $removedName"
                                broadcastRoom(updated)
                            }
                        )
                        val missing = 4 - roomSeats.count { it.occupied }
                        val waitingReady = roomSeats.count { it.occupied && (!it.ready || !it.connected) }
                        Text(
                            text = when {
                                missing > 0 -> "还差 $missing 个座位，可等待好友或添加人机"
                                waitingReady > 0 -> "还有 $waitingReady 个座位未准备或已断线"
                                else -> "房间已满且全部准备，可以开始"
                            },
                            color = if (missing == 0 && waitingReady == 0) Gold else Color.White.copy(alpha = 0.72f),
                            fontWeight = FontWeight.SemiBold
                        )
                        GoldenButton(
                            text = "开始附近对战",
                            enabled = hostMode && transport != null && roomSeats.canStartRoom(),
                            onClick = {
                                val seed = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                                val seats = roomSeats.normalizedSeats()
                                val start = GameMessage.Start(seed, ruleSet.name, seats, roomId, hostEpoch)
                                transport?.send(GameMessageCodec.encode(start))
                                transport?.let { onStartNetworkGame(it, seed, ruleSet, seats, 0, true, roomId, hostEpoch) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BluetoothEntryMode.JoinRoom -> {
                        if (roomSeats.any { it.occupied }) {
                            RoomSeatsPanel(
                                seats = roomSeats,
                                isHost = false,
                                localPlayerName = playerName,
                                localClientId = localClientId,
                                onReady = { playerId, ready ->
                                    roomSeats = roomSeats.setReady(playerId, ready)
                                    onRoomUpdated(roomSeats, ruleFromName(roomRuleName))
                                    transport?.send(GameMessageCodec.encode(GameMessage.RoomReady(playerId, ready)))
                                    status = if (ready) "已准备，等待房主开始" else "已取消准备"
                                },
                                onAddAi = {},
                                onToggleAi = {},
                                onRemoveAi = {}
                            )
                            OutlinedGameButton(
                                text = "重新寻找房间",
                                onClick = { startDiscovery() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            ConnectionGuidePanel(phase = guidePhase, steps = guideSteps)
            BluetoothTroublePanel()
        }
    }
    bluetoothDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { bluetoothDialogMessage = null },
            title = { Text("蓝牙连接提示") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { bluetoothDialogMessage = null }) {
                    Text("知道了")
                }
            }
        )
    }
}

@Composable
private fun BluetoothModeChooser(onCreate: () -> Unit, onJoin: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        LobbyModeCard("创建房间", "我是房主", "♠", Modifier.weight(1f), onCreate)
        LobbyModeCard("加入对局", "搜索附近", "♥", Modifier.weight(1f), onJoin)
    }
}

@Composable
private fun RadarSearchEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "radar")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "radarSweep"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "radarPulse"
    )
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.16f), RoundedCornerShape(22.dp))
            .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension * 0.38f
            repeat(3) { index ->
                drawCircle(
                    color = Gold.copy(alpha = (0.16f - index * 0.035f) * pulse),
                    radius = radius * (index + 1) / 3f,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
            rotate(sweep, center) {
                drawLine(
                    color = Gold.copy(alpha = 0.75f),
                    start = center,
                    end = Offset(center.x, center.y - radius),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(Gold.copy(alpha = 0.85f), radius = 7f, center = center)
        }
        Text("正在寻找房间", color = Cream, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp))
    }
}

@Composable
private fun RoomSeatsPanel(
    seats: List<RoomSeat>,
    isHost: Boolean,
    localPlayerName: String = "",
    localClientId: String = "",
    onReady: (Int, Boolean) -> Unit = { _, _ -> },
    onAddAi: (Difficulty) -> Unit,
    onToggleAi: (Int) -> Unit,
    onRemoveAi: (Int) -> Unit
) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("房间座位", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        seats.normalizedSeats().forEachIndexed { index, seat ->
            StaggeredEntry(index) {
                RoomSeatRow(
                    seat = seat,
                    isHost = isHost,
                    localPlayerName = localPlayerName,
                    localClientId = localClientId,
                    onReady = { ready -> onReady(seat.index, ready) },
                    onAddAi = onAddAi,
                    onToggleAi = { onToggleAi(seat.index) },
                    onRemoveAi = { onRemoveAi(seat.index) }
                )
            }
        }
    }
}

@Composable
private fun RoomSeatRow(
    seat: RoomSeat,
    isHost: Boolean,
    localPlayerName: String,
    localClientId: String,
    onReady: (Boolean) -> Unit,
    onAddAi: (Difficulty) -> Unit,
    onToggleAi: () -> Unit,
    onRemoveAi: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "seatPulse")
    val emptyPulse by transition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.44f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "emptySeatPulse"
    )
    val isLocalSeat = (seat.clientId.isNotBlank() && seat.clientId == localClientId) ||
        (seat.clientId.isBlank() && seat.name.isNotBlank() && seat.name == localPlayerName)
    val title = when (seat.kind) {
        RoomSeatKind.Empty -> "空位"
        RoomSeatKind.Host -> if (isLocalSeat) "你（房主）" else "${seat.name.ifBlank { "房主" }}（房主）"
        RoomSeatKind.Human -> if (isLocalSeat) "你" else seat.name.ifBlank { "好友" }
        RoomSeatKind.Ai -> "${seat.name.ifBlank { "人机" }} · ${seat.difficulty?.title ?: Difficulty.Easy.title}"
    }
    val subtitle = when (seat.kind) {
        RoomSeatKind.Empty -> if (isHost) "可添加人机或等待好友加入" else "等待房主安排"
        RoomSeatKind.Host -> if (seat.takeoverByAi) "房主断线，人机托管中" else "创建房间"
        RoomSeatKind.Human -> when {
            seat.takeoverByAi -> "玩家断线，人机托管中"
            !seat.connected -> "等待玩家重连"
            seat.ready -> "好友已准备"
            else -> "等待准备"
        }
        RoomSeatKind.Ai -> "房主人机，自动准备"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (seat.kind == RoomSeatKind.Empty) Color.White.copy(alpha = 0.05f) else Panel.copy(alpha = 0.9f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (seat.kind == RoomSeatKind.Empty) Gold.copy(alpha = emptyPulse) else Gold.copy(alpha = if (seat.ready) 0.42f else 0.22f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SeatAvatarFrame(name = if (seat.kind == RoomSeatKind.Empty) "${seat.index + 1}" else title, active = seat.ready || seat.kind == RoomSeatKind.Empty, size = 38)
                Column(modifier = Modifier.weight(1f)) {
                    Text("座位 ${seat.index + 1}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(title, color = Cream, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitle, color = Color.White.copy(alpha = 0.62f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(
                modifier = Modifier.widthIn(min = 44.dp),
                text = when (seat.kind) {
                    RoomSeatKind.Empty -> "待加入"
                    RoomSeatKind.Host -> if (seat.takeoverByAi) "托管" else "房主"
                    RoomSeatKind.Human -> when {
                        seat.takeoverByAi -> "托管"
                        seat.connected -> "好友"
                        else -> "断线"
                    }
                    RoomSeatKind.Ai -> if (seat.ready) "已准备" else "人机"
                },
                color = when {
                    seat.kind == RoomSeatKind.Empty -> Color.White.copy(alpha = 0.55f)
                    !seat.connected -> Danger
                    else -> Gold
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
        if (!isHost && seat.kind == RoomSeatKind.Human && isLocalSeat) {
            OutlinedGameButton(
                text = if (seat.ready) "取消准备" else "我已准备",
                onClick = { onReady(!seat.ready) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isHost && seat.kind == RoomSeatKind.Empty) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedGameButton("添加简单", onClick = { onAddAi(Difficulty.Easy) }, modifier = Modifier.weight(1f))
                OutlinedGameButton("添加普通", onClick = { onAddAi(Difficulty.Normal) }, modifier = Modifier.weight(1f))
                OutlinedGameButton("添加困难", onClick = { onAddAi(Difficulty.Hard) }, modifier = Modifier.weight(1f))
            }
        }
        if (isHost && seat.kind == RoomSeatKind.Ai) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedGameButton("切换难度", onClick = onToggleAi, modifier = Modifier.weight(1f))
                OutlinedGameButton("移除人机", onClick = onRemoveAi, modifier = Modifier.weight(1f))
            }
        }
        if (isHost && seat.kind == RoomSeatKind.Human) {
            OutlinedGameButton("移出座位", onClick = onRemoveAi, modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun connectionGuidePhase(
    hasPermission: Boolean,
    status: String,
    hostMode: Boolean,
    transportActive: Boolean,
    discovering: Boolean,
    deviceCount: Int,
    roomPlayerCount: Int
): ConnectionGuidePhase {
    val statusLooksRecoverable = listOf("失败", "未启动", "拒绝", "找不到").any { status.contains(it) }
    return when {
        !hasPermission -> ConnectionGuidePhase.Permission
        statusLooksRecoverable -> ConnectionGuidePhase.Error
        status.startsWith("正在加入") -> ConnectionGuidePhase.Joining
        status.contains("等待房主") || status.contains("已加入") || roomPlayerCount > 1 -> ConnectionGuidePhase.WaitingStart
        hostMode && transportActive -> ConnectionGuidePhase.Hosting
        discovering || status.contains("搜索") || deviceCount > 0 -> ConnectionGuidePhase.Searching
        else -> ConnectionGuidePhase.Ready
    }
}

private fun connectionGuideSteps(
    phase: ConnectionGuidePhase,
    deviceCount: Int,
    roomPlayerCount: Int
): List<ConnectionGuideStep> {
    fun active(vararg phases: ConnectionGuidePhase): Boolean = phases.any { it == phase }
    val foundText = if (deviceCount > 0) "已发现 $deviceCount 台设备，点选房主手机加入。" else "搜索通常需要十几秒，请让房主手机保持可发现。"
    val roomText = if (roomPlayerCount >= 4) {
        "四个座位都坐满并准备后，房主点击开始附近对战。"
    } else if (roomPlayerCount > 1) {
        "已有 $roomPlayerCount 名玩家在房间，还需要补满四个座位。"
    } else {
        "好友加入后，房主可等待更多好友或添加人机补位。"
    }
    return listOf(
        ConnectionGuideStep(1, "开启附近权限", "允许附近设备和蓝牙权限，旧系统还需要定位权限用于发现设备。", active(ConnectionGuidePhase.Permission)),
        ConnectionGuideStep(2, "房主创建房间", "一台手机点击创建房间，负责等待好友连接。", active(ConnectionGuidePhase.Ready, ConnectionGuidePhase.Hosting)),
        ConnectionGuideStep(3, "允许被发现", "系统弹窗出现时选择允许，好友才能在附近设备里看到房间。", active(ConnectionGuidePhase.Hosting)),
        ConnectionGuideStep(4, "好友寻找设备", foundText, active(ConnectionGuidePhase.Searching)),
        ConnectionGuideStep(5, "选择设备加入", "看到房主手机名称后点选；若弹出配对请求，两台手机都要同意。", active(ConnectionGuidePhase.Joining)),
        ConnectionGuideStep(6, "房主开始对局", roomText, active(ConnectionGuidePhase.WaitingStart))
    )
}

@Composable
private fun ConnectionGuidePanel(phase: ConnectionGuidePhase, steps: List<ConnectionGuideStep>) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Text("连接提示", color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        if (phase == ConnectionGuidePhase.Error) {
            Text(
                "如果连接失败，请两台手机都打开蓝牙，靠近一些，关闭旧房间后重新创建并搜索。",
                color = Danger,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        steps.forEachIndexed { index, step ->
            StaggeredEntry(index) {
                ConnectionGuideStepRow(step)
            }
        }
    }
}

@Composable
private fun ConnectionGuideStepRow(step: ConnectionGuideStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (step.active) Gold.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (step.active) Gold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (step.active) Gold else Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(step.index.toString(), color = if (step.active) Ink else Cream, fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(step.title, color = if (step.active) Gold else Cream, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(step.body, color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun BluetoothTroublePanel() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.78f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("找不到好友时", color = Gold, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("1. 先在系统蓝牙里互相配对，再回到游戏搜索。", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp, lineHeight = 18.sp)
            Text("2. 房主重新点击创建房间，并在系统弹窗里允许被发现。", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp, lineHeight = 18.sp)
            Text("3. 两台设备保持近距离；连接失败后退出房间重新搜索。", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

private fun String.isBluetoothErrorLike(): Boolean {
    if (isBlank()) return false
    val normalized = lowercase(Locale.ROOT)
    return listOf(
        "失败",
        "断开",
        "异常",
        "错误",
        "未启动",
        "拒绝",
        "无法",
        "找不到",
        "block",
        "socket",
        "connection",
        "permission",
        "read failed",
        "bt "
    ).any { key -> normalized.contains(key.lowercase(Locale.ROOT)) }
}

private fun playerFriendlyBluetoothError(rawMessage: String): String {
    val normalized = rawMessage.lowercase(Locale.ROOT)
    return when {
        rawMessage.isBenignBluetoothReturnCode() -> "蓝牙房间已处理完成，请继续等待好友加入。"
        rawMessage.contains("房间已满") -> "房间已满，暂时无法加入。"
        rawMessage.contains("房主已移出") -> rawMessage
        rawMessage.contains("需要权限") || normalized.contains("permission") ->
            "需要允许附近设备/蓝牙权限后才能创建或加入房间。请授权后重新进入好友蓝牙对局。"
        normalized.contains("block") || normalized.contains("bt ") ->
            "蓝牙连接被系统拦截。请确认两台手机蓝牙已开启、已在系统蓝牙中配对，并重新寻找房间。"
        normalized.contains("socket") || normalized.contains("connection") || rawMessage.contains("断开") ->
            "蓝牙连接已断开。请返回好友蓝牙对局，重新寻找房间加入。"
        rawMessage.contains("未启动") ->
            "蓝牙搜索没有启动。请确认蓝牙已开启，并重新寻找房间。"
        rawMessage.contains("找不到") || rawMessage.contains("无法加入") ->
            "暂时没有找到可加入的房间。请让房主重新创建房间，并保持两台手机靠近。"
        else -> rawMessage.ifBlank { "蓝牙连接出现异常，请重新创建或加入房间。" }
    }
}

private fun String.isBenignBluetoothReturnCode(): Boolean {
    val normalized = trim().lowercase(Locale.ROOT)
    return normalized == "-1" ||
        normalized == "error-1" ||
        normalized == "error -1" ||
        normalized == "result -1" ||
        normalized.contains("error-1") ||
        normalized.contains("error -1")
}

private fun ruleFromName(ruleName: String): RuleSet {
    return ruleSetByIdOrName(ruleName)
}

private fun localBluetoothPlayerName(context: Context): String {
    val model = Build.MODEL
        .filter { it.isLetterOrDigit() }
        .take(5)
        .ifBlank { "Android" }
    val androidId = Settings.Secure
        .getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        .orEmpty()
        .takeLast(4)
        .ifBlank { (System.nanoTime() % 10_000).toString().padStart(4, '0') }
    return "牌友$model$androidId"
}

private fun stableBluetoothClientId(context: Context): String {
    val settings = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    val existing = settings.getString(KEY_BLUETOOTH_CLIENT_ID, null)
    if (!existing.isNullOrBlank()) return existing
    val androidId = Settings.Secure
        .getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        .orEmpty()
        .ifBlank { UUID.randomUUID().toString() }
    val generated = "bt-${androidId}-${UUID.randomUUID().toString().take(8)}"
    settings.edit().putString(KEY_BLUETOOTH_CLIENT_ID, generated).apply()
    return generated
}

@Suppress("DEPRECATION")
private fun vibrateOnce(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        context.getSystemService(Vibrator::class.java)
    } ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(70)
    }
}

@Composable
private fun GameScreen(
    state: GameState?,
    localProfile: PlayerProfile,
    difficulty: Difficulty,
    currentTurnDifficulty: Difficulty,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    gameMode: GameMode,
    localPlayerId: Int,
    inputLocked: Boolean,
    networkStatus: String,
    onCanPlay: (List<Card>) -> Boolean,
    onPlay: (List<Card>) -> Unit,
    onPass: () -> Unit,
    onHint: () -> List<Card>,
    onComputerTurn: () -> Unit,
    onTimeout: () -> Unit,
    startPhase: GameStartPhase,
    onStartGame: () -> Unit,
    onDealFinished: () -> Unit,
    onRestart: () -> Unit,
    onLobby: () -> Unit
) {
    if (state == null) return
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 55) }
    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }
    val localHand = state.player(localPlayerId).hand
    val selected = remember(localPlayerId, localHand) { mutableStateListOf<Card>() }
    val playedHandKey = playedHandAnimationKey(state)
    val turnKey = turnTimerKey(state)
    var remainingSeconds by remember(turnKey) { mutableIntStateOf(20) }
    var dealingAnimationPlayed by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var pendingPlayedCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var seatMoveAnimation by remember { mutableStateOf<TableMoveAnimation?>(null) }
    var playAnimationKey by remember { mutableIntStateOf(0) }
    var isPlayAnimating by remember { mutableStateOf(false) }
    val isPlaying = startPhase == GameStartPhase.Playing
    val isPaused = !isPlaying || menuOpen || inputLocked || isPlayAnimating
    LaunchedEffect(startPhase) {
        if (isPlaying) {
            dealingAnimationPlayed = false
            delay(45)
            dealingAnimationPlayed = true
        } else {
            dealingAnimationPlayed = false
        }
    }
    LaunchedEffect(turnKey, state.isFinished, isPaused) {
        if (state.isFinished || isPaused) return@LaunchedEffect
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
        onTimeout()
    }
    LaunchedEffect(turnKey, isPaused) {
        if (!state.isFinished && !isPaused && state.currentPlayer.kind == PlayerKind.LocalAi) {
            delay(
                when (currentTurnDifficulty) {
                    Difficulty.Easy -> 1600
                    Difficulty.Normal -> 1900
                    Difficulty.Hard -> 2300
                }
            )
            onComputerTurn()
        }
    }
    LaunchedEffect(playAnimationKey) {
        if (pendingPlayedCards.isEmpty()) return@LaunchedEffect
        delay(430)
        val cards = pendingPlayedCards
        pendingPlayedCards = emptyList()
        isPlayAnimating = false
        selected.clear()
        onPlay(cards)
    }
    LaunchedEffect(playedHandKey) {
        val last = state.lastPlayedHand
        if (isPlaying && last != null && last.playerId != localPlayerId && last.type.cards.isNotEmpty()) {
            val animation = TableMoveAnimation(playedHandKey, last.playerId, last.type.cards)
            seatMoveAnimation = animation
            delay(520)
            if (seatMoveAnimation?.key == animation.key) {
                seatMoveAnimation = null
            }
        }
    }

    fun requestPlay(cards: List<Card>) {
        if (!isPlaying || isPlayAnimating || cards.isEmpty()) return
        if (!onCanPlay(cards)) return
        if (soundEnabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 140)
        if (vibrationEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            vibrateOnce(context)
        }
        pendingPlayedCards = cards
        isPlayAnimating = true
        playAnimationKey += 1
    }

    GameScaffold(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        animatedBackground = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black.copy(alpha = 0.04f)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TableTopBar(
                state = state,
                difficultyLabel = if (gameMode == GameMode.HumanVsAi) difficulty.title else "好友对局",
                remainingSeconds = remainingSeconds,
                localPlayerId = localPlayerId,
                startPhase = startPhase,
                networkStatus = networkStatus,
                onMenu = { menuOpen = true }
            )
            OpponentArc(state, localPlayerId)
            if (startPhase == GameStartPhase.ReadyToStart) {
                ReadyTableCenter(
                    state = state,
                    difficulty = difficulty,
                    onStartGame = onStartGame
                )
                ReadyPlayerDock(localProfile)
            } else {
                TableCenter(state, playedHandKey, localPlayerId)
                if (startPhase == GameStartPhase.Dealing) {
                    DealingPlayerDock(localProfile)
                } else {
                    PlayerHandArea(
                        state = state,
                        localProfile = localProfile,
                        localPlayerId = localPlayerId,
                        inputLocked = inputLocked,
                        selected = selected,
                        dealingAnimationPlayed = dealingAnimationPlayed,
                        isPlayAnimating = isPlayAnimating,
                        gameStarted = isPlaying,
                        onPlay = { cards -> requestPlay(cards) },
                        onPass = onPass,
                        onHint = onHint
                    )
                }
            }
        }
        if (startPhase == GameStartPhase.Dealing) {
            DealingOverlay(
                animationKey = turnKey,
                localPlayerId = localPlayerId,
                modifier = Modifier.matchParentSize(),
                onFinished = onDealFinished
            )
        }
        if (pendingPlayedCards.isNotEmpty()) {
            PlayedCardsFlight(
                cards = pendingPlayedCards,
                animationKey = playAnimationKey,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        seatMoveAnimation?.let { animation ->
            SeatPlayedCardsFlight(
                cards = animation.cards,
                animationKey = animation.key,
                playerId = animation.playerId,
                localPlayerId = localPlayerId,
                playerCount = state.players.size,
                modifier = Modifier.matchParentSize()
            )
        }
        if (menuOpen) {
            TableSettingsMenu(
                gameMode = gameMode,
                onContinue = { menuOpen = false },
                onRestart = {
                    menuOpen = false
                    onRestart()
                },
                onLobby = {
                    menuOpen = false
                    onLobby()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 12.dp)
            )
        }
    }
}

@Composable
private fun ReadyTableCenter(
    state: GameState,
    difficulty: Difficulty,
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .shadow(14.dp, RoundedCornerShape(32.dp))
            .background(Brush.radialGradient(listOf(Color(0xFF177C5D), Color.Black.copy(alpha = 0.28f))), RoundedCornerShape(32.dp))
            .border(1.dp, Gold.copy(alpha = 0.42f), RoundedCornerShape(32.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        TableCenterGlow()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("牌桌已就绪", color = Cream, fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text(
                "${state.ruleSet.name.removeSuffix("规则")} · ${difficulty.title}",
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text("点击开始后发牌，发牌完成再进入回合", color = Color.White.copy(alpha = 0.74f), fontSize = 12.sp)
            GoldenButton(text = "开始游戏", onClick = onStartGame, modifier = Modifier.width(176.dp))
        }
    }
}

@Composable
private fun ReadyPlayerDock(localProfile: PlayerProfile) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(localProfile.nickname, active = true, size = 52, avatar = localProfile.avatar)
            Column(Modifier.weight(1f)) {
                Text("${localProfile.nickname} 已入座", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("等待开始游戏后发牌", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                repeat(4) { CardBackMini() }
            }
        }
    }
}

@Composable
private fun DealingPlayerDock(localProfile: PlayerProfile) {
    GamePanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(localProfile.nickname, active = true, size = 52, avatar = localProfile.avatar)
            Column(Modifier.weight(1f)) {
                Text("正在发牌", color = Cream, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("发牌完成后自动开始", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("提示", enabled = false, onClick = {})
                ActionButton("重选", enabled = false, onClick = {})
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldenButton(
                text = "出牌",
                enabled = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            OutlinedGameButton(
                text = "不出",
                enabled = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy((-10).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(13) { index ->
                HandCardBack(
                    modifier = Modifier.graphicsLayer {
                        translationY = (index % 3) * -1.6f
                    }
                )
            }
        }
    }
}

@Composable
private fun CardBackMini() {
    Box(
        modifier = Modifier
            .width(38.dp)
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(7.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF204F8F), Color(0xFF102A55))),
                RoundedCornerShape(7.dp)
            )
            .border(1.dp, Gold.copy(alpha = 0.48f), RoundedCornerShape(7.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Gold.copy(alpha = 0.18f), radius = size.minDimension * 0.28f, center = Offset(size.width / 2, size.height / 2))
            drawRoundRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(size.width * 0.2f, size.height * 0.24f),
                size = Size(size.width * 0.6f, size.height * 0.52f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun HandCardBack(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(66.dp)
            .height(104.dp)
            .shadow(5.dp, RoundedCornerShape(9.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF204F8F), Color(0xFF102A55))),
                RoundedCornerShape(9.dp)
            )
            .border(2.dp, Gold.copy(alpha = 0.52f), RoundedCornerShape(9.dp))
            .padding(7.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Gold.copy(alpha = 0.18f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width / 2, size.height / 2)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.13f),
                topLeft = Offset(size.width * 0.16f, size.height * 0.22f),
                size = Size(size.width * 0.68f, size.height * 0.56f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(width = 2.4f)
            )
            drawRoundRect(
                color = Gold.copy(alpha = 0.24f),
                topLeft = Offset(size.width * 0.28f, size.height * 0.34f),
                size = Size(size.width * 0.44f, size.height * 0.32f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun DealingOverlay(
    animationKey: String,
    localPlayerId: Int,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val progress = remember(animationKey) { Animatable(0f) }
    LaunchedEffect(animationKey) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1850))
        onFinished()
    }
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val p = progress.value
            val deckCenter = Offset(size.width * 0.5f, size.height * 0.49f)
            val cardWidth = 66.dp.toPx()
            val cardHeight = 104.dp.toPx()
            val cardRadius = 9.dp.toPx()

            fun targetFor(playerId: Int): Offset {
                val relativeSeat = ((playerId - localPlayerId) % 4 + 4) % 4
                return when (relativeSeat) {
                    1 -> Offset(size.width * 0.20f, size.height * 0.22f)
                    2 -> Offset(size.width * 0.50f, size.height * 0.17f)
                    3 -> Offset(size.width * 0.80f, size.height * 0.22f)
                    else -> Offset(size.width * 0.50f, size.height * 0.84f)
                }
            }

            fun eased(value: Float): Float {
                val clamped = value.coerceIn(0f, 1f)
                return clamped * clamped * (3f - 2f * clamped)
            }

            fun drawBack(center: Offset, angle: Float, alpha: Float = 1f) {
                rotate(angle, center) {
                    drawRoundRect(
                        color = Color(0xFF143A73).copy(alpha = alpha),
                        topLeft = Offset(center.x - cardWidth / 2, center.y - cardHeight / 2),
                        size = Size(cardWidth, cardHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cardRadius, cardRadius)
                    )
                    drawRoundRect(
                        color = Gold.copy(alpha = 0.46f * alpha),
                        topLeft = Offset(center.x - cardWidth / 2 + 7.dp.toPx(), center.y - cardHeight / 2 + 7.dp.toPx()),
                        size = Size(cardWidth - 14.dp.toPx(), cardHeight - 14.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(Gold.copy(alpha = 0.12f * alpha), radius = 13.dp.toPx(), center = center)
                }
            }

            repeat(6) { index ->
                drawBack(deckCenter + Offset(index * 2.2f, -index * 1.8f), -4f + index, alpha = 0.92f)
            }
            repeat(52) { index ->
                val delayRatio = index * 0.016f
                val rawProgress = (p - delayRatio) / 0.12f
                val cardProgress = eased(rawProgress)
                if (rawProgress in 0f..0.98f) {
                    val playerId = index % 4
                    val target = targetFor(playerId)
                    val stackOffset = Offset(((index / 4) - 6) * 2.2.dp.toPx(), ((index / 4) % 3 - 1) * 1.6.dp.toPx())
                    val end = target + stackOffset
                    val arc = kotlin.math.sin(cardProgress * kotlin.math.PI.toFloat()) * -30.dp.toPx()
                    val center = Offset(
                        x = deckCenter.x + (end.x - deckCenter.x) * cardProgress,
                        y = deckCenter.y + (end.y - deckCenter.y) * cardProgress + arc
                    )
                    val angle = when (((playerId - localPlayerId) % 4 + 4) % 4) {
                        1 -> -11f
                        2 -> 0f
                        3 -> 11f
                        else -> 0f
                    } + (1f - cardProgress) * 10f
                    drawBack(center, angle, alpha = 0.98f)
                }
            }
        }
    }
}

@Composable
private fun PlayedCardsFlight(cards: List<Card>, animationKey: Int, modifier: Modifier = Modifier) {
    var launched by remember(animationKey) { mutableStateOf(false) }
    LaunchedEffect(animationKey) { launched = true }
    val density = LocalDensity.current
    val progress by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(430),
        label = "playFlight"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 118.dp)
            .graphicsLayer {
                translationY = with(density) { (-250f * progress).dp.toPx() }
                val flightScale = 1f - progress * 0.14f
                scaleX = flightScale
                scaleY = flightScale
                alpha = 1f - progress * 0.12f
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        cards.forEach { card ->
            FlyingCard(card)
        }
    }
}

@Composable
private fun SeatPlayedCardsFlight(
    cards: List<Card>,
    animationKey: String,
    playerId: Int,
    localPlayerId: Int,
    playerCount: Int,
    modifier: Modifier = Modifier
) {
    var launched by remember(animationKey) { mutableStateOf(false) }
    LaunchedEffect(animationKey) { launched = true }
    val density = LocalDensity.current
    val progress by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(500),
        label = "seatPlayFlight"
    )
    val relativeSeat = ((playerId - localPlayerId) % playerCount + playerCount) % playerCount
    val startAlignment = when (relativeSeat) {
        1 -> Alignment.TopStart
        2 -> Alignment.TopCenter
        3 -> Alignment.TopEnd
        else -> Alignment.BottomCenter
    }
    val horizontalShiftDp = when (relativeSeat) {
        1 -> 92f * progress
        3 -> -92f * progress
        else -> 0f
    }
    val verticalShiftDp = if (relativeSeat == 0) {
        -240f * progress
    } else {
        52f + 148f * progress
    }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .align(startAlignment)
                .padding(horizontal = 18.dp)
                .graphicsLayer {
                    translationX = with(density) { horizontalShiftDp.dp.toPx() }
                    translationY = with(density) { verticalShiftDp.dp.toPx() }
                    val flightScale = 0.9f + progress * 0.1f
                    scaleX = flightScale
                    scaleY = flightScale
                    alpha = 1f - progress * 0.08f
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            cards.forEach { card ->
                FlyingCard(card)
            }
        }
    }
}

@Composable
private fun TableSettingsMenu(
    gameMode: GameMode,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(176.dp),
        colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.98f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuAction("继续游戏", onContinue)
            MenuAction(if (gameMode == GameMode.HumanVsAi) "重新开始" else "重新开房", onRestart)
            MenuAction("返回大厅", onLobby)
        }
    }
}

@Composable
private fun MenuAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Cream,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    )
}

@Composable
private fun FlyingCard(card: Card) {
    val red = card.suit == Suit.Hearts || card.suit == Suit.Diamonds
    Column(
        modifier = Modifier
            .width(54.dp)
            .height(78.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Gold.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(card.rank.label, color = if (red) Danger else Ink, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Text(card.suit.label, color = if (red) Danger else Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TableTopBar(
    state: GameState,
    difficultyLabel: String,
    remainingSeconds: Int,
    localPlayerId: Int,
    startPhase: GameStartPhase,
    networkStatus: String,
    onMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RuleBadge(
                ruleName = state.ruleSet.name.removeSuffix("规则"),
                modeLabel = difficultyLabel
            )
            Text(
                text = "⚙",
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(42.dp)
                    .background(Gold, CircleShape)
                    .clickable(onClick = onMenu)
                    .padding(top = 8.dp)
            )
        }
        val turnText = when (startPhase) {
            GameStartPhase.ReadyToStart -> "等待开局"
            GameStartPhase.Dealing -> "正在发牌"
            GameStartPhase.Playing -> "轮到 ${state.playerDisplayName(state.currentPlayerId, localPlayerId)}"
        }
        TurnPill(
            text = turnText,
            remainingSeconds = if (startPhase == GameStartPhase.Playing) remainingSeconds else 20,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        if (networkStatus.isNotBlank()) {
            NetworkStatusPill(
                text = networkStatus,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun NetworkStatusPill(text: String, modifier: Modifier = Modifier) {
    val warning = listOf("断线", "迁移", "失败", "异常", "托管").any { text.contains(it) }
    Text(
        text = text,
        color = if (warning) Color.White else Gold,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .background(if (warning) Danger.copy(alpha = 0.82f) else Color.Black.copy(alpha = 0.26f), RoundedCornerShape(18.dp))
            .border(1.dp, if (warning) Gold.copy(alpha = 0.5f) else Gold.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun RuleBadge(ruleName: String, modeLabel: String) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .border(1.dp, Gold.copy(alpha = 0.34f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            ruleName,
            color = Gold,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modeLabel,
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OpponentArc(state: GameState, localPlayerId: Int) {
    val opponents = (1 until state.players.size).map { offset ->
        state.player((localPlayerId + offset) % state.players.size)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            opponents.forEach { player ->
                SeatView(player, active = state.currentPlayerId == player.id)
            }
        }
    }
}

@Composable
private fun TableCenter(state: GameState, lastMoveKey: String, localPlayerId: Int) {
    var entered by remember(lastMoveKey) { mutableStateOf(false) }
    LaunchedEffect(lastMoveKey) {
        entered = false
        delay(35)
        entered = true
    }
    val density = LocalDensity.current
    val alpha by animateFloatAsState(targetValue = if (entered) 1f else 0f, animationSpec = tween(420), label = "tableAlpha")
    val slide by animateFloatAsState(targetValue = if (entered) 0f else (-40f), animationSpec = tween(420), label = "tableSlide")
    val scale by animateFloatAsState(targetValue = if (entered) 1f else 0.88f, animationSpec = tween(420), label = "tableScale")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(Brush.radialGradient(listOf(Color(0xFF12694F), Color.Black.copy(alpha = 0.26f))), RoundedCornerShape(32.dp))
            .border(1.dp, Gold.copy(alpha = 0.32f), RoundedCornerShape(32.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        TableCenterGlow()
        val last = state.lastPlayedHand
        if (last == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("等待先手", color = Cream, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(state.localizedMessage(localPlayerId), color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                if (state.currentPlayer.kind == PlayerKind.LocalAi) ThinkingDots()
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${state.playerDisplayName(last.playerId, localPlayerId)} · ${last.type.label}", color = Gold, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .graphicsLayer {
                            this.alpha = alpha
                            translationY = with(density) { slide.dp.toPx() }
                            scaleX = scale
                            scaleY = scale
                        },
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    last.type.cards.forEach { card -> TableCard(card) }
                }
                Text(state.localizedMessage(localPlayerId), color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                if (state.currentPlayer.kind == PlayerKind.LocalAi) ThinkingDots()
            }
        }
    }
}

@Composable
private fun TableCenterGlow() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            color = Gold.copy(alpha = 0.06f),
            radius = size.minDimension * 0.48f,
            center = Offset(size.width / 2, size.height / 2)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.035f),
            radius = size.minDimension * 0.32f,
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun ThinkingDots() {
    val transition = rememberInfiniteTransition(label = "thinkingDots")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 4.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(420, delayMillis = index * 120), RepeatMode.Reverse),
                label = "dot-$index"
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Gold.copy(alpha = alpha), CircleShape)
            )
        }
    }
}

private fun GameState.playerDisplayName(playerId: Int, localPlayerId: Int): String {
    return if (playerId == localPlayerId) "你" else player(playerId).name
}

private fun GameState.localizedMessage(localPlayerId: Int): String {
    val localName = players.firstOrNull { it.id == localPlayerId }?.name.orEmpty()
    return if (localName.isBlank()) message else message.replace(localName, "你")
}

@Composable
private fun PlayerHandArea(
    state: GameState,
    localProfile: PlayerProfile,
    localPlayerId: Int,
    inputLocked: Boolean,
    selected: MutableList<Card>,
    dealingAnimationPlayed: Boolean,
    isPlayAnimating: Boolean,
    gameStarted: Boolean,
    onPlay: (List<Card>) -> Unit,
    onPass: () -> Unit,
    onHint: () -> List<Card>
) {
    val human = state.player(localPlayerId)
    val availability = PlayerActionPolicy.evaluate(
        state = state,
        localPlayerId = localPlayerId,
        gameStarted = gameStarted,
        inputLocked = inputLocked,
        isPlayAnimating = isPlayAnimating
    )
    val canAct = availability.canAct
    val density = LocalDensity.current
    LaunchedEffect(availability.hasLegalPlay, canAct) {
        if (canAct && !availability.hasLegalPlay) {
            selected.clear()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(localProfile.nickname, active = state.currentPlayerId == localPlayerId, size = 52, avatar = localProfile.avatar)
            Column(Modifier.weight(1f)) {
                Text("你的手牌", color = Cream, fontWeight = FontWeight.Bold)
                Text("${human.hand.size} 张", color = Color.White.copy(alpha = 0.68f), fontSize = 12.sp)
            }
            ActionButton("提示", enabled = canAct && availability.hasLegalPlay, onClick = {
                selected.clear()
                selected.addAll(onHint())
            })
            ActionButton("重选", enabled = selected.isNotEmpty(), onClick = { selected.clear() })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GoldenButton(
                text = "出牌",
                enabled = selected.isNotEmpty() && canAct && availability.hasLegalPlay,
                onClick = { onPlay(selected.toList()) },
                modifier = Modifier.weight(1f)
            )
            OutlinedGameButton(
                text = "不出",
                enabled = availability.canPass,
                onClick = onPass,
                modifier = Modifier.weight(1f)
            )
        }
        availability.notice?.let { notice ->
            Text(
                text = notice,
                color = Gold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy((-10).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            human.hand.forEachIndexed { index, card ->
                val visibleShift by animateFloatAsState(
                    targetValue = if (dealingAnimationPlayed) 0f else 80f,
                    animationSpec = tween(durationMillis = 260, delayMillis = index * 24),
                    label = "deal-$index"
                )
                PlayingCard(
                    card = card,
                    selected = card in selected,
                    enabled = canAct && availability.hasLegalPlay,
                    modifier = Modifier.graphicsLayer {
                        translationY = with(density) { visibleShift.dp.toPx() }
                    },
                    onClick = {
                        if (card in selected) selected.remove(card) else selected.add(card)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayingCard(card: Card, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val red = card.suit == Suit.Hearts || card.suit == Suit.Diamonds
    val density = LocalDensity.current
    val lift by animateFloatAsState(targetValue = if (selected) -18f else 0f, animationSpec = tween(150), label = "cardLift")
    val scale by animateFloatAsState(targetValue = if (selected) 1.08f else 1f, animationSpec = tween(150), label = "cardScale")
    Column(
        modifier = modifier
            .graphicsLayer {
                translationY = with(density) { lift.dp.toPx() }
                scaleX = scale
                scaleY = scale
            }
            .width(66.dp)
            .height(104.dp)
            .shadow(if (selected) 14.dp else 5.dp, RoundedCornerShape(9.dp))
            .background(Color.White, RoundedCornerShape(9.dp))
            .border(2.dp, if (selected) Gold else Color(0xFFE4DDD0), RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(7.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(card.rank.label, color = if (red) Danger else Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
        Text(card.suit.label, color = if (red) Danger else Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(card.rank.label, color = if (red) Danger else Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ResultScreen(
    state: GameState?,
    localPlayerId: Int,
    profile: PlayerProfile,
    latestChange: ProfileChange?,
    onAgain: () -> Unit,
    onLobby: () -> Unit
) {
    val winner = state?.winnerId?.let { state.player(it).name } ?: "无人胜出"
    val won = state?.winnerId == localPlayerId
    GameScaffold(contentPadding = PaddingValues(24.dp)) {
        if (won) {
            Celebration()
            CoinBurst()
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = Panel.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, if (won) Gold.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(if (won) "胜利" else "再接再厉", color = if (won) Gold else Cream, fontSize = 34.sp, fontWeight = FontWeight.Black)
                Text("本局赢家：$winner", color = Color.White.copy(alpha = 0.82f), fontSize = 17.sp)
                CoinPill(profile.coins)
                latestChange?.let { ResultRewardPanel(it) }
                state?.players?.sortedBy { it.hand.size }?.forEachIndexed { index, player ->
                    StaggeredEntry(index) {
                        ResultRow(index + 1, player)
                    }
                }
                GoldenButton(text = "再来一局", onClick = onAgain, modifier = Modifier.fillMaxWidth())
                OutlinedGameButton(text = "返回大厅", onClick = onLobby, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ResultRewardPanel(change: ProfileChange) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("本局收获", color = Gold, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(
            "金币 ${signed(change.coinsDelta)}  经验 ${signed(change.xpDelta)}",
            color = Cream,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (change.leveledUp) {
            Text("升级：Lv.${change.oldLevel} → Lv.${change.newLevel}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        if (change.unlockedAchievements.isNotEmpty()) {
            Text(
                "新成就：${change.unlockedAchievements.joinToString("、") { it.title }}",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CoinBurst() {
    val transition = rememberInfiniteTransition(label = "coinBurst")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "coinBurstProgress"
    )
    Canvas(Modifier.fillMaxSize()) {
        repeat(16) { index ->
            val angle = (index * 22.5f + progress * 34f) * kotlin.math.PI.toFloat() / 180f
            val distance = size.minDimension * (0.18f + progress * 0.34f) + index % 3 * 9f
            val center = Offset(
                size.width / 2 + kotlin.math.cos(angle) * distance,
                size.height / 2 + kotlin.math.sin(angle) * distance
            )
            drawCircle(Gold.copy(alpha = 1f - progress * 0.72f), radius = 8f, center = center)
            drawCircle(Cream.copy(alpha = 0.52f), radius = 3f, center = center)
        }
    }
}

@Composable
private fun ResultRow(rank: Int, player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("第 $rank 名  ${player.name}", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text("${player.hand.size} 张", color = Gold)
    }
}

@Composable
private fun MenuPage(title: String, subtitle: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    GameScaffold {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StaggeredEntry(0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(title, color = Cream, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text(subtitle, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    PressScale(
                        modifier = Modifier
                            .height(42.dp)
                            .width(78.dp),
                        onClick = onBack
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                                .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("返回", color = Gold, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun SelectCard(selected: Boolean, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    PressScale(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        GamePanel(modifier = Modifier.fillMaxWidth(), selected = selected, content = content)
    }
}

@Composable
private fun SeatView(player: Player, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SeatAvatarFrame(player.name, active = active, size = 50)
        Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text("${player.hand.size}", color = Gold, fontWeight = FontWeight.Black, fontSize = 16.sp)
    }
}

@Composable
private fun SeatAvatarFrame(name: String, active: Boolean, size: Int) {
    val glow = if (active) {
        val transition = rememberInfiniteTransition(label = "seatFrame")
        val value by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(880), RepeatMode.Reverse),
            label = "seatFrameGlow"
        )
        value
    } else {
        0f
    }
    Box(
        modifier = Modifier
            .size((size + 30).dp)
            .drawBehind {
                if (active) {
                    drawCircle(Gold.copy(alpha = 0.2f * glow), radius = this.size.minDimension / 2 - 2.dp.toPx())
                    drawCircle(
                        Gold.copy(alpha = 0.5f * glow),
                        radius = this.size.minDimension / 2 - 6.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Avatar(name, active = false, size = size)
    }
}

@Composable
private fun Avatar(name: String, active: Boolean, size: Int, avatar: AvatarProfile? = null) {
    val pulse = if (active) {
        val transition = rememberInfiniteTransition(label = "avatarPulse")
        val value by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "pulse"
        )
        value
    } else {
        0f
    }
    val galleryPath = avatar?.galleryPath
    val galleryBitmap = remember(galleryPath) {
        galleryPath
            ?.takeIf { File(it).exists() }
            ?.let { decodeAvatarImage(it) }
    }
    val builtIn = AvatarCatalog.byId(avatar?.builtInId ?: AvatarCatalog.all.first().id)
    val colors = avatarColors(builtIn.id)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size((size + 28).dp)) {
        if (active) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Gold.copy(alpha = 0.18f + 0.14f * pulse),
                    radius = this.size.minDimension / 2 - 2.dp.toPx()
                )
                drawCircle(
                    color = Gold.copy(alpha = 0.38f * pulse),
                    radius = this.size.minDimension / 2 - 7.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(colors), CircleShape)
                .border(2.dp, Cream.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f),
                    radius = this.size.minDimension * 0.34f,
                    center = Offset(this.size.width * 0.34f, this.size.height * 0.26f)
                )
                drawCircle(
                    color = rarityColor(builtIn.rarity).copy(alpha = 0.28f),
                    radius = this.size.minDimension * 0.44f,
                    center = Offset(this.size.width * 0.72f, this.size.height * 0.78f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(this.size.width * 0.24f, this.size.height * 0.18f),
                    size = Size(this.size.width * 0.52f, this.size.height * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
            if (galleryBitmap != null) {
                Image(
                    bitmap = galleryBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = avatar?.let { builtIn.mark } ?: name.take(1),
                    color = if (builtIn.id == "heart_star" || builtIn.id == "diamond_flash") Color.White else Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = (size / 2).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun avatarColors(id: String): List<Color> = when (id) {
    "heart_star" -> listOf(Color(0xFFFF6C6C), Color(0xFFC93B31))
    "club_master" -> listOf(Color(0xFF3FD48B), Color(0xFF0D714E))
    "diamond_flash" -> listOf(Color(0xFFFF8B4A), Color(0xFFB93D22))
    "moon" -> listOf(Color(0xFFBFD7FF), Color(0xFF3D5D94))
    "sun" -> listOf(Color(0xFFFFE28A), Color(0xFFCC7A18))
    "dragon" -> listOf(Color(0xFF66E0C2), Color(0xFF096854))
    "crown" -> listOf(Color(0xFFFFD86B), Color(0xFF7C4A0F))
    else -> listOf(Gold, GoldDeep)
}

private fun rarityColor(rarity: AvatarRarity): Color = when (rarity) {
    AvatarRarity.Free -> Gold
    AvatarRarity.Common -> Color(0xFF8BE9A8)
    AvatarRarity.Rare -> Color(0xFF78B7FF)
    AvatarRarity.Legendary -> Color(0xFFFF8FDB)
}

private fun decodeAvatarImage(path: String) = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > 512 || bounds.outHeight / sampleSize > 512) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize.coerceAtLeast(1) }
    BitmapFactory.decodeFile(path, options)?.asImageBitmap()
}.getOrNull()

@Composable
private fun TableCard(card: Card) {
    val red = card.suit == Suit.Hearts || card.suit == Suit.Diamonds
    Column(
        modifier = Modifier
            .width(44.dp)
            .height(64.dp)
            .background(Color.White, RoundedCornerShape(7.dp))
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(card.rank.label, color = if (red) Danger else Ink, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(card.suit.label, color = if (red) Danger else Ink, fontSize = 18.sp)
    }
}

@Composable
private fun TurnPill(text: String, remainingSeconds: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = (remainingSeconds.coerceIn(0, 20) / 20f),
        animationSpec = tween(250),
        label = "countdownProgress"
    )
    Row(
        modifier = modifier
            .background(Gold, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            color = Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (remainingSeconds <= 5) Danger else Cream, CircleShape)
                .drawBehind {
                    drawArc(
                        color = if (remainingSeconds <= 5) Color.White else Felt,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = remainingSeconds.toString(),
                color = if (remainingSeconds <= 5) Color.White else Ink,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CoinPill(coins: Int) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .border(1.dp, Gold.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("●", color = Gold, fontSize = 13.sp)
        Text(coins.toString(), color = Cream, fontWeight = FontWeight.Bold)
    }
}

private fun signed(value: Int): String = if (value > 0) "+$value" else value.toString()

private fun formatRecordTime(timestamp: Long): String {
    return SimpleDateFormat("MM/dd HH:mm", Locale.CHINA).format(Date(timestamp))
}

private fun currentDayKey(): String {
    return SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(Date())
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun copyAvatarToPrivateStorage(context: Context, uri: Uri): String? {
    return runCatching {
        val target = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    }.getOrNull()
}

@Composable
private fun GoldenButton(text: String, enabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Ink,
            disabledContainerColor = Color(0xFF75684A),
            disabledContentColor = Color.White.copy(alpha = 0.55f)
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}

@Composable
private fun OutlinedGameButton(text: String, enabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cream, disabledContentColor = Color.White.copy(alpha = 0.42f)),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (enabled) Cream else Color.White.copy(alpha = 0.36f),
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(Color.Black.copy(alpha = if (enabled) 0.22f else 0.1f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
    )
}

@Composable
private fun CardFan(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy((-20).dp)) {
        listOf("A" to "♠", "K" to "♥", "Q" to "♦").forEachIndexed { index, (rank, suit) ->
            val red = suit == "♥" || suit == "♦"
            Column(
                modifier = Modifier
                    .offset(y = (index * 10).dp)
                    .width(58.dp)
                    .height(86.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(7.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(rank, color = if (red) Danger else Ink, fontWeight = FontWeight.Black)
                Text(suit, color = if (red) Danger else Ink, fontSize = 25.sp)
            }
        }
    }
}

@Composable
private fun FeltPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.035f)
        val step = 64f
        var x = -size.height
        while (x < size.width) {
            drawLine(lineColor, Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 2f, cap = StrokeCap.Round)
            x += step
        }
    }
}

@Composable
private fun Celebration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(18) { index ->
            val x = size.width * ((index * 37 % 100) / 100f)
            val y = size.height * ((index * 53 % 100) / 100f)
            drawCircle(Gold.copy(alpha = 0.34f), radius = 8f + (index % 5) * 3f, center = Offset(x, y))
        }
    }
}
