package ru.glebik.treetestapp.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.glebik.treetestapp.MainViewModel
import ru.glebik.treetestapp.data.NodeSerializer

val mainModule = module {
    viewModel { MainViewModel(get(), get()) }
    single<NodeSerializer> { NodeSerializer() }
}