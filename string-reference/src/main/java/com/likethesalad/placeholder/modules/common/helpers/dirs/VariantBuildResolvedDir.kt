package com.likethesalad.placeholder.modules.common.helpers.dirs

import com.likethesalad.placeholder.providers.BuildDirProvider
import com.likethesalad.tools.android.plugin.data.AndroidVariantData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.io.File

class VariantBuildResolvedDir @AssistedInject constructor(
    buildDirProvider: BuildDirProvider,
    sourceSetsHandler: SourceSetsHandler,
    @Assisted androidVariantData: AndroidVariantData
) {
    @AssistedFactory
    interface Factory {
        fun create(androidVariantData: AndroidVariantData): VariantBuildResolvedDir
    }

    companion object {
        const val RESOLVED_DIR_BUILD_RELATIVE_PATH = "generated/resolved"
    }

    private val variantName by lazy { androidVariantData.getVariantName() }

    val resolvedDir: File by lazy {
        val dir = File(buildDirProvider.getBuildDir(), "$RESOLVED_DIR_BUILD_RELATIVE_PATH/$variantName")
        sourceSetsHandler.addToSourceSets(dir, variantName)
        dir
    }
}