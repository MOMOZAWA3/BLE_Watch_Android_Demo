package com.starmax.sdkdemo.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.starmax.sdkdemo.MainActivity
import org.koin.androidx.compose.defaultExtras
import org.koin.androidx.viewmodel.lazyResolveViewModel
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope

@OptIn(KoinInternalApi::class)
@Composable
inline fun <reified T : ViewModel> lazyKoinViewModel(
    qualifier: Qualifier? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    extras: CreationExtras = defaultExtras(viewModelStoreOwner),
    scope: Scope = LocalKoinScope.current,
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> {
    val context = LocalContext.current
    return lazyResolveViewModel(
        T::class, { if(context is MainActivity) context.viewModelStore else viewModelStoreOwner.viewModelStore }, key,
        { extras }, qualifier, scope, parameters
    )
}