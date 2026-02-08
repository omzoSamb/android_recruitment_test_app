package fr.leboncoin.androidrecruitmenttestapp.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Convertit un Flow<T> en Flow<UiState<T>> avec gestion d'erreur
 */
fun <T> Flow<T>.toResultFlow(context: Context): Flow<UiState<T>> {
    return flow {
        val isInternetConnected = hasInternetConnection(context)
        if (!isInternetConnected) {
            emit(UiState.Error("No Internet Connection"))
            return@flow
        }

        emit(UiState.Loading)
        this@toResultFlow
            .catch { e ->
                emit(UiState.Error(e.message ?: "Error occurred"))
            }
            .collect { data ->
                emit(UiState.Success(data))
            }
    }.flowOn(Dispatchers.IO)
}

/**
 * Convertit un suspend function qui retourne T en Flow<UiState<T>>
 */
inline fun <T> toResultFlow(
    context: Context,
    crossinline call: suspend () -> T
): Flow<UiState<T>> {
    return flow {
        val isInternetConnected = hasInternetConnection(context)
        if (!isInternetConnected) {
            emit(UiState.Error("No Internet Connection"))
            return@flow
        }

        emit(UiState.Loading)
        try {
            val result = call()
            emit(UiState.Success(result))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error occurred"))
        }
    }.flowOn(Dispatchers.IO)
}