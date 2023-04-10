package net.arwix.placeselectorfeature.parts.edit.location.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class InputTextFieldState(
    val value: String,
    val onValueChange: (String) -> Unit,
    val isError: Boolean = false,
    val textError: String)