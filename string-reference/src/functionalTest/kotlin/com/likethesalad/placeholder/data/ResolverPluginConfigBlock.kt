package com.likethesalad.placeholder.data

import com.likethesalad.tools.functional.testing.layout.items.GradleBlockItem

class ResolverPluginConfigBlock(private val resolverPluginConfig: ResolverPluginConfig) : GradleBlockItem {

    override fun getItemText(): String {
        return """
            stringXmlReference {
                resolveOnBuild = ${resolverPluginConfig.resolveOnBuild}
                useDependenciesRes = ${resolverPluginConfig.useDependenciesRes}
            }
        """.trimIndent()
    }
}