package com.likethesalad.android.templates.provider.di

import com.likethesalad.android.templates.provider.task.TemplatesServiceGeneratorTask
import dagger.Component
import javax.inject.Singleton

@Component(modules = [TemplatesProviderModule::class])
@Singleton
interface TemplatesProviderComponent {
    fun templatesServiceGeneratorArgs(): TemplatesServiceGeneratorTask.Args
}