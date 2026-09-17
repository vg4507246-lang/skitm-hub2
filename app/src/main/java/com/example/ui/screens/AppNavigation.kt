package com.example.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(300)) }
    ) {
        composable<LoginRoute> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { navController.navigate(DashboardRoute) { popUpTo(LoginRoute) { inclusive = true } } }
            )
        }
        composable<DashboardRoute> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable<AcademicsRoute> {
            AcademicsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<PlacementsRoute> {
            PlacementsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<ResumeBuilderRoute> {
            ResumeBuilderScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<ATSRoute> {
            ATSScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<AISearchRoute> {
            AISearchScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<AboutRoute> {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable<ExamCalendarRoute> {
            ExamCalendarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<CampusMapRoute> {
            CampusMapScreen(onBack = { navController.popBackStack() })
        }
        composable<DigitalIDRoute> {
            DigitalIDScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<AttendanceRoute> {
            AttendanceScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable<TimetableRoute> {
            TimetableScreen(onBack = { navController.popBackStack() })
        }
        composable<NotificationsRoute> {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable<ResultsRoute> {
            ResultsScreen(onBack = { navController.popBackStack() })
        }
        composable<EventsRoute> {
            EventsScreen(onBack = { navController.popBackStack() })
        }
        composable<FacultyRoute> {
            FacultyScreen(onBack = { navController.popBackStack() })
        }
        composable<PermissionsRoute> {
            PermissionsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
