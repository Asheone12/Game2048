package com.muen.game2048.rxbus

import androidx.lifecycle.LifecycleOwner
import io.reactivex.functions.Consumer

inline fun <reified T> LifecycleOwner.rxBus(action: Consumer<T>) {
    RxBus.get().toIOSubscribe(T::class.java, action).autoClear(this)
}