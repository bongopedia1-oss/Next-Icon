package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.*
import com.example.ui.*
import com.example.ui.theme.NextBlack
import com.example.ui.theme.NextIconTheme
import com.example.viewmodel.NextIconViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextIconTheme {
                NextIconApp()
            }
        }
    }
}

@Composable
fun NextIconApp(
    viewModel: NextIconViewModel = viewModel()
) {
    val language by viewModel.repository.currentLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.repository.currentUser.collectAsStateWithLifecycle()
    val liveStreams by viewModel.repository.liveStreams.collectAsStateWithLifecycle()
    val socialPosts by viewModel.repository.socialPosts.collectAsStateWithLifecycle()
    val conversations by viewModel.repository.conversations.collectAsStateWithLifecycle()
    val virtualGifts by viewModel.repository.virtualGifts.collectAsStateWithLifecycle()
    val coinPackages by viewModel.repository.coinPackages.collectAsStateWithLifecycle()
    val withdrawals by viewModel.repository.withdrawals.collectAsStateWithLifecycle()
    val notices by viewModel.repository.notices.collectAsStateWithLifecycle()
    val agencyDetails by viewModel.repository.agencyDetails.collectAsStateWithLifecycle()
    val rankings by viewModel.repository.rankingCreators.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeLiveRoom by viewModel.activeLiveRoom.collectAsStateWithLifecycle()
    val activeChat by viewModel.activeChatConversation.collectAsStateWithLifecycle()
    val liveRoomMessages by viewModel.liveRoomMessages.collectAsStateWithLifecycle()
    val activeGiftAnim by viewModel.activeGiftAnimation.collectAsStateWithLifecycle()

    val showRecharge by viewModel.showRechargeDialog.collectAsStateWithLifecycle()
    val showWithdraw by viewModel.showWithdrawDialog.collectAsStateWithLifecycle()
    val showGoLive by viewModel.showGoLiveModal.collectAsStateWithLifecycle()
    val showAgency by viewModel.showAgencyScreen.collectAsStateWithLifecycle()
    val showGames by viewModel.showGamesScreen.collectAsStateWithLifecycle()
    val showVip by viewModel.showVipScreen.collectAsStateWithLifecycle()
    val showAdmin by viewModel.showAdminPanel.collectAsStateWithLifecycle()

    val isBeautyFilterOn by viewModel.isBeautyFilterEnabled.collectAsStateWithLifecycle()
    val isMicOn by viewModel.isMicEnabled.collectAsStateWithLifecycle()
    val snackMsg by viewModel.snackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        if (snackMsg != null) {
            snackbarHostState.showSnackbar(snackMsg!!)
        }
    }

    // Predictive / system back handling
    BackHandler(enabled = activeLiveRoom != null || activeChat != null || selectedTab != 0) {
        when {
            activeLiveRoom != null -> viewModel.closeLiveRoom()
            activeChat != null -> viewModel.closeChat()
            selectedTab != 0 -> viewModel.selectTab(0)
        }
    }

    // If a live room is currently open, show the full-screen immersive live streaming room!
    if (activeLiveRoom != null) {
        LiveRoomScreen(
            stream = activeLiveRoom!!,
            messages = liveRoomMessages,
            gifts = virtualGifts,
            activeGiftAnim = activeGiftAnim,
            language = language,
            isBeautyFilterOn = isBeautyFilterOn,
            isMicOn = isMicOn,
            onSendMessage = { text -> viewModel.sendLiveRoomMessage(text) },
            onSendGift = { gift -> viewModel.sendGift(gift) },
            onToggleBeauty = { viewModel.toggleBeautyFilter() },
            onToggleMic = { viewModel.toggleMic() },
            onFlipCamera = { viewModel.flipCamera() },
            onCloseRoom = { viewModel.closeLiveRoom() }
        )
    } else {
        Scaffold(
            containerColor = NextBlack,
            topBar = {
                if (activeChat == null) {
                    NextIconTopBar(
                        user = currentUser,
                        language = language,
                        onToggleLanguage = { viewModel.repository.toggleLanguage() },
                        onOpenRecharge = { viewModel.setShowRecharge(true) },
                        onOpenWithdraw = { viewModel.setShowWithdraw(true) }
                    )
                }
            },
            bottomBar = {
                if (activeChat == null) {
                    NextIconBottomNav(
                        selectedTab = selectedTab,
                        language = language,
                        onTabSelected = { tab -> viewModel.selectTab(tab) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(NextBlack)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        liveStreams = liveStreams,
                        rankings = rankings,
                        notices = notices,
                        selectedCategory = selectedCategory,
                        language = language,
                        onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                        onStreamClick = { stream -> viewModel.openLiveRoom(stream) },
                        onRankingClick = { rankCreator ->
                            val s = liveStreams.firstOrNull { it.hostName.contains(rankCreator.name.take(4)) }
                                ?: liveStreams.first()
                            viewModel.openLiveRoom(s)
                        },
                        onNoticeClick = { viewModel.setShowAdmin(true) }
                    )

                    1 -> SocialScreen(
                        posts = socialPosts,
                        language = language,
                        onLikePost = { id -> viewModel.repository.togglePostLike(id) },
                        onAddComment = { id, text -> viewModel.repository.addPostComment(id, text) },
                        onCreatePost = { text, type ->
                            viewModel.repository.createPost(text, type)
                            viewModel.showSnackbar("Post published to community feed!")
                        },
                        onSharePost = {
                            viewModel.showSnackbar("Post link copied to clipboard!")
                        }
                    )

                    3 -> ChatScreen(
                        conversations = conversations,
                        activeChat = activeChat,
                        language = language,
                        onSelectChat = { conv -> viewModel.openChat(conv) },
                        onSendMessage = { chatId, text -> viewModel.repository.sendChatMessage(chatId, text) },
                        onCloseChat = { viewModel.closeChat() }
                    )

                    4 -> ProfileScreen(
                        user = currentUser,
                        language = language,
                        onOpenRecharge = { viewModel.setShowRecharge(true) },
                        onOpenWithdraw = { viewModel.setShowWithdraw(true) },
                        onOpenAgency = { viewModel.setShowAgency(true) },
                        onOpenGames = { viewModel.setShowGames(true) },
                        onOpenVip = { viewModel.setShowVip(true) },
                        onOpenAdmin = { viewModel.setShowAdmin(true) },
                        onToggleLanguage = { viewModel.repository.toggleLanguage() }
                    )
                }
            }
        }
    }

    // Modal Dialog Sheets
    if (showRecharge) {
        RechargeCoinsDialog(
            packages = coinPackages,
            language = language,
            onSelectPackage = { pkg ->
                viewModel.repository.rechargeCoins(pkg)
                viewModel.showSnackbar("Recharged ${pkg.coins + pkg.bonusCoins} Coins successfully!")
            },
            onDismiss = { viewModel.setShowRecharge(false) }
        )
    }

    if (showWithdraw) {
        CreatorWithdrawalDialog(
            user = currentUser,
            withdrawals = withdrawals,
            language = language,
            onSubmitWithdrawal = { diamonds, method, account ->
                val ok = viewModel.repository.submitWithdrawal(diamonds, method, account)
                if (ok) {
                    viewModel.showSnackbar("Cashout request for $diamonds Diamonds submitted!")
                }
                ok
            },
            onDismiss = { viewModel.setShowWithdraw(false) }
        )
    }

    if (showGoLive) {
        GoLiveModalSheet(
            language = language,
            isBeautyFilterOn = isBeautyFilterOn,
            isMicOn = isMicOn,
            onToggleBeauty = { viewModel.toggleBeautyFilter() },
            onToggleMic = { viewModel.toggleMic() },
            onFlipCamera = { viewModel.flipCamera() },
            onStartLive = { title, category, type ->
                viewModel.startCustomLive(title, category, type)
            },
            onDismiss = { viewModel.setShowGoLive(false) }
        )
    }

    if (showAgency) {
        AgencyModalSheet(
            agency = agencyDetails,
            language = language,
            onDismiss = { viewModel.setShowAgency(false) }
        )
    }

    if (showGames) {
        GamesModalSheet(
            user = currentUser,
            language = language,
            onSpinWheel = { cost ->
                val result = viewModel.repository.spinWheelPrize(cost)
                result
            },
            onDismiss = { viewModel.setShowGames(false) }
        )
    }

    if (showVip) {
        VipModalSheet(
            user = currentUser,
            language = language,
            onDismiss = { viewModel.setShowVip(false) }
        )
    }

    if (showAdmin) {
        AdminPanelModalSheet(
            withdrawals = withdrawals,
            language = language,
            onApproveWithdrawal = { id ->
                viewModel.repository.updateWithdrawalStatus(id, WithdrawalStatus.APPROVED)
                viewModel.showSnackbar("Withdrawal $id approved and paid!")
            },
            onRejectWithdrawal = { id ->
                viewModel.repository.updateWithdrawalStatus(id, WithdrawalStatus.REJECTED)
                viewModel.showSnackbar("Withdrawal $id rejected.")
            },
            onDismiss = { viewModel.setShowAdmin(false) }
        )
    }
}
